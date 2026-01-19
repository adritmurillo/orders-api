package com.dinet.orders_api.application.service;

import com.dinet.orders_api.application.dto.PedidoCsvDto;
import com.dinet.orders_api.application.dto.ResumenCargaDto;
import com.dinet.orders_api.application.ports.input.CargarPedidosUseCase;
import com.dinet.orders_api.domain.exception.ValidationException;
import com.dinet.orders_api.domain.model.*;
import com.dinet.orders_api.domain.ports.out.*;
import com.dinet.orders_api.domain.service.PedidoValidationService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CargarPedidosService implements CargarPedidosUseCase {

    private final PedidoRepositoryPort pedidoRepo;
    private final ClienteRepositoryPort clienteRepo;
    private final ZonaRepositoryPort zonaRepo;
    private final CargaIdempotenciaPort cargaRepo;
    private final PedidoValidationService validacion;
    private final HashService hash;

    @Value("${app.batch.size:500}")
    private int batchSize;

    @Override
    public ResumenCargaDto ejecutar(
            MultipartFile archivo,
            String idempotencyKey,
            String correlationId
    ) {
        log.info("Iniciando carga de pedidos");

        ResumenCargaDto resumen = ResumenCargaDto.builder()
                .correlationId(correlationId)
                .detalleErrores(new ArrayList<>())
                .erroresAgrupados(new HashMap<>())
                .build();

        try {
            // Leer archivo completo
            byte[] archivoBytes;
            try (InputStream inputStream = archivo.getInputStream()) {
                archivoBytes = inputStream.readAllBytes();
            }

            // Calcular hash
            String archivoHash = hash.calcularSHA256(new ByteArrayInputStream(archivoBytes));

            // Verificar si ya fue procesado
            Optional<CargaIdempotencia> cargaExistente =
                    cargaRepo.findByIdempotencyKeyAndArchivoHash(idempotencyKey, archivoHash);

            if (cargaExistente.isPresent()) {
                resumen.agregarError(0, "Archivo ya procesado anteriormente", TipoError.DUPLICADO);
                return resumen;
            }

            // Registrar carga
            registrarCargaIdempotencia(idempotencyKey, archivoHash);

            // Cargar catálogos
            Map<String, Cliente> clientesMap = cargarClientes();
            Map<String, Zona> zonasMap = cargarZonas();

            // Procesar CSV
            try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(archivoBytes), StandardCharsets.UTF_8);
                 CSVReader csvReader = new CSVReader(reader)) {

                List<String[]> lineas = csvReader.readAll();

                if (lineas.isEmpty()) {
                    resumen.agregarError(0, "Archivo vacío", TipoError.FORMATO_INVALIDO);
                    return resumen;
                }

                List<String[]> lineasDatos = lineas.subList(1, lineas.size());
                procesarEnLotes(lineasDatos, clientesMap, zonasMap, resumen);

            } catch (CsvException e) {
                resumen.agregarError(0, "Error leyendo CSV: " + e.getMessage(), TipoError.FORMATO_INVALIDO);
            }

        } catch (IOException e) {
            resumen.agregarError(0, "Error con el archivo: " + e.getMessage(), TipoError.FORMATO_INVALIDO);
        } catch (Exception e) {
            resumen.agregarError(0, "Error inesperado: " + e.getMessage(), TipoError.FORMATO_INVALIDO);
        }

        return resumen;
    }

    @Transactional
    protected void registrarCargaIdempotencia(String idempotencyKey, String archivoHash) {
        CargaIdempotencia carga = new CargaIdempotencia();
//        carga.setId(UUID.randomUUID());
        carga.setIdempotencyKey(idempotencyKey);
        carga.setArchivoHash(archivoHash);
        carga.setCreatedAt(LocalDateTime.now());
        cargaRepo.save(carga);
    }

    @Transactional
    protected void procesarLote(
            List<String[]> lote,
            int numeroLineaInicial,
            Map<String, Cliente> clientesMap,
            Map<String, Zona> zonasMap,
            ResumenCargaDto resumen
    ) {
        List<Pedido> pedidosValidos = new ArrayList<>();

        // Obtener números de pedido del lote
        Set<String> numerosPedidoLote = lote.stream()
                .filter(linea -> linea.length > 0)
                .map(linea -> linea[0].trim())
                .collect(Collectors.toSet());

        Set<String> numerosPedidoExistentes = pedidoRepo.existsByNumeroPedidoIn(numerosPedidoLote);

        // Procesar cada línea
        for (int i = 0; i < lote.size(); i++) {
            int numeroLinea = numeroLineaInicial + i;
            String[] linea = lote.get(i);

            resumen.incrementarProcesados();

            try {
                PedidoCsvDto dto = parsearLinea(linea, numeroLinea);
                Pedido pedido = validacion.validarYConvertir(dto, clientesMap, zonasMap, numerosPedidoExistentes);
                pedidosValidos.add(pedido);
                numerosPedidoExistentes.add(pedido.getNumeroPedido());

            } catch (ValidationException e) {
                resumen.agregarError(numeroLinea, e.getMessage(), e.getTipoError());
            } catch (Exception e) {
                resumen.agregarError(numeroLinea, e.getMessage(), TipoError.FORMATO_INVALIDO);
            }
        }

        // Guardar pedidos válidos
        if (!pedidosValidos.isEmpty()) {
            try {
                pedidoRepo.saveAll(pedidosValidos);
                pedidosValidos.forEach(p -> resumen.agregarExito());
            } catch (Exception e) {
                log.error("Error guardando lote", e);
                throw e;
            }
        }
    }

    private void procesarEnLotes(
            List<String[]> lineas,
            Map<String, Cliente> clientesMap,
            Map<String, Zona> zonasMap,
            ResumenCargaDto resumen
    ) {
        int totalLineas = lineas.size();

        for (int i = 0; i < totalLineas; i += batchSize) {
            int finLote = Math.min(i + batchSize, totalLineas);
            List<String[]> lote = lineas.subList(i, finLote);

            try {
                procesarLote(lote, i + 2, clientesMap, zonasMap, resumen);
            } catch (Exception e) {
                log.error("Error procesando lote", e);

            }
        }
    }

    private PedidoCsvDto parsearLinea(String[] datos, int numeroLinea) throws Exception {
        if (datos.length < 6) {
            throw new Exception("Formato incorrecto: faltan columnas");
        }

        return PedidoCsvDto.builder()
                .numeroPedido(datos[0])
                .clienteId(datos[1])
                .fechaEntrega(datos[2])
                .estado(datos[3])
                .zonaEntrega(datos[4])
                .requiereRefrigeracion(datos[5])
                .numeroLinea(numeroLinea)
                .build();
    }

    private Map<String, Cliente> cargarClientes() {
        List<Cliente> clientes = clienteRepo.findAllActivos();
        return clientes.stream().collect(Collectors.toMap(Cliente::getId, c -> c));
    }

    private Map<String, Zona> cargarZonas() {
        List<Zona> zonas = zonaRepo.findAll();
        return zonas.stream().collect(Collectors.toMap(Zona::getId, z -> z));
    }
}