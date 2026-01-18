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
        ResumenCargaDto resumen = ResumenCargaDto.builder()
                .correlationId(correlationId)
                .detalleErrores(new ArrayList<>())
                .erroresAgrupados(new HashMap<>())
                .build();

        try {
            byte[] archivoBytes;
            try (InputStream inputStream = archivo.getInputStream()) {
                archivoBytes = inputStream.readAllBytes();
            }

            String archivoHash = hash.calcularSHA256(new ByteArrayInputStream(archivoBytes));
            log.debug("Hash SHA-256 calculado: {}", archivoHash);

            Optional<CargaIdempotencia> cargaExistente =
                    cargaRepo.findByIdempotencyKeyAndArchivoHash(
                            idempotencyKey,
                            archivoHash
                    );

            if (cargaExistente.isPresent()) {
                log.warn("Archivo ya procesado. IdempotencyKey: {}, Hash: {}",
                        idempotencyKey, archivoHash);
                resumen.agregarError(
                        0,
                        "Este archivo ya fue procesado anteriormente con esta clave de idempotencia",
                        TipoError.DUPLICADO
                );
                return resumen;
            }

            registrarCargaIdempotencia(idempotencyKey, archivoHash);
            log.info("Carga registrada para idempotencia");

            Map<String, Cliente> clientesMap = cargarClientes();
            Map<String, Zona> zonasMap = cargarZonas();
            log.info("Catálogos cargados: {} clientes, {} zonas",
                    clientesMap.size(), zonasMap.size());

            try (InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(archivoBytes), StandardCharsets.UTF_8);
                 CSVReader csvReader = new CSVReader(reader)) {

                List<String[]> lineas = csvReader.readAll();

                if (lineas.isEmpty()) {
                    resumen.agregarError(0, "El archivo CSV está vacío", TipoError.FORMATO_INVALIDO);
                    return resumen;
                }

                List<String[]> lineasDatos = lineas.subList(1, lineas.size());
                procesarEnLotes(lineasDatos, clientesMap, zonasMap, resumen);

            } catch (CsvException e) {
                log.error("Error parseando CSV", e);
                resumen.agregarError(0, "Error al leer el archivo CSV: " + e.getMessage(),
                        TipoError.FORMATO_INVALIDO);
            }

            log.info("Carga completada. Total: {}, Guardados: {}, Errores: {}",
                    resumen.getTotalProcesados(), resumen.getGuardados(), resumen.getConError());

        } catch (IOException e) {
            log.error("Error procesando archivo", e);
            resumen.agregarError(0, "Error leyendo el archivo: " + e.getMessage(),
                    TipoError.FORMATO_INVALIDO);
        } catch (Exception e) {
            log.error("Error inesperado durante la carga", e);
            resumen.agregarError(0, "Error inesperado: " + e.getMessage(),
                    TipoError.FORMATO_INVALIDO);
        }

        return resumen;
    }

    @Transactional
    protected void registrarCargaIdempotencia(String idempotencyKey, String archivoHash) {
        CargaIdempotencia carga = new CargaIdempotencia();
        carga.setId(UUID.randomUUID());
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
        Set<String> numerosPedidoLote = lote.stream()
                .filter(linea -> linea.length > 0)
                .map(linea -> linea[0].trim())
                .collect(Collectors.toSet());

        Set<String> numerosPedidoExistentes = pedidoRepo.existsByNumeroPedidoIn(numerosPedidoLote);

        for (int i = 0; i < lote.size(); i++) {
            int numeroLinea = numeroLineaInicial + i;
            String[] linea = lote.get(i);

            resumen.incrementarProcesados();

            try {
                PedidoCsvDto dto = parsearLinea(linea, numeroLinea);

                Pedido pedido = validacion.validarYConvertir(
                        dto, clientesMap, zonasMap, numerosPedidoExistentes);

                pedidosValidos.add(pedido);

                numerosPedidoExistentes.add(pedido.getNumeroPedido());

            } catch (ValidationException e) {
                log.debug("Error validación en línea {}: {}", numeroLinea, e.getMessage());
                resumen.agregarError(numeroLinea, e.getMessage(), e.getTipoError());
            } catch (Exception e) {
                log.warn("Error inesperado en línea {}: {}", numeroLinea, e.getMessage());
                resumen.agregarError(numeroLinea, e.getMessage(), TipoError.FORMATO_INVALIDO);
            }
        }

        if (!pedidosValidos.isEmpty()) {
            try {
                pedidoRepo.saveAll(pedidosValidos);
                pedidosValidos.forEach(p -> resumen.agregarExito());
                log.debug("Guardados {} pedidos del lote", pedidosValidos.size());
            } catch (Exception e) {
                log.error("Error guardando lote de pedidos", e);
                pedidosValidos.forEach(p ->
                        resumen.agregarError(0, "Error guardando pedido: " + p.getNumeroPedido(),
                                TipoError.FORMATO_INVALIDO));
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
        int numeroLote = 0;

        for (int i = 0; i < totalLineas; i += batchSize) {
            numeroLote++;
            int finLote = Math.min(i + batchSize, totalLineas);
            List<String[]> lote = lineas.subList(i, finLote);

            log.debug("Procesando lote {} ({} líneas)", numeroLote, lote.size());

            try {
                procesarLote(lote, i + 2, clientesMap, zonasMap, resumen);
            } catch (Exception e) {
                log.error("Error procesando lote {}", numeroLote, e);
            }
        }
    }

    private PedidoCsvDto parsearLinea(String[] datos, int numeroLinea) throws Exception {
        if (datos.length < 6) {
            throw new Exception("Formato incorrecto: se esperaban 6 columnas, se encontraron " + datos.length);
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
        return clientes.stream()
                .collect(Collectors.toMap(Cliente::getId, c -> c));
    }

    private Map<String, Zona> cargarZonas() {
        List<Zona> zonas = zonaRepo.findAll();
        return zonas.stream()
                .collect(Collectors.toMap(Zona::getId, z -> z));
    }
}