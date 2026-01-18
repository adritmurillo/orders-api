package com.dinet.orders_api.application.service;

import com.dinet.orders_api.application.dto.PedidoCsvDto;
import com.dinet.orders_api.application.dto.ResumenCargaDto;
import com.dinet.orders_api.application.ports.input.CargarPedidosUseCase;
import com.dinet.orders_api.domain.exception.ValidationException;
import com.dinet.orders_api.domain.model.*;
import com.dinet.orders_api.domain.ports.out.CargaIdempotenciaPort;
import com.dinet.orders_api.domain.ports.out.ClienteRepositoryPort;
import com.dinet.orders_api.domain.ports.out.PedidoRepositoryPort;
import com.dinet.orders_api.domain.ports.out.ZonaRepositoryPort;
import com.dinet.orders_api.domain.service.PedidoValidationService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private final HashService hashService;

    @Value("${app.carga.batch-size:500}")
    private int batchSize;

    @Override
    @Transactional
    public ResumenCargaDto ejecutar(MultipartFile archivo, String idempotencyKey, String correlationId) {
        log.info("Iniciando carga de pedidos. CorrelationId: {}, IdempotencyKey: {}", correlationId, idempotencyKey);
        ResumenCargaDto resumen = ResumenCargaDto.builder()
                .correlationId(correlationId)
                .detalleErrores(new ArrayList<>())
                .erroresAgrupados(new HashMap<>())
                .build();
        try {
            String archivoHash;
            try(InputStream inputStream = archivo.getInputStream()){
                archivoHash = hashService.calcularSHA256(inputStream);
            }

            log.debug("Hash SHA-256 calculado: {}", archivoHash);

            Optional<CargaIdempotencia> cargaExistente = cargaRepo.findByIdempotencyKeyAndArchivoHash(
                    idempotencyKey,
                    archivoHash
            );
            if (cargaExistente.isPresent()){
                log.warn("Esta carga ya ha sido procesado anteriormente. IdempotencyKey: {}, Hash: {}",
                        idempotencyKey, archivoHash);
                resumen.agregarError(0, "Carga ya procesada anteriormente", TipoError.DUPLICADO);
                return resumen;
            }

            CargaIdempotencia carga = new CargaIdempotencia();
            carga.setId(UUID.randomUUID());
            carga.setIdempotencyKey(idempotencyKey);
            carga.setArchivoHash(archivoHash);
            carga.setCreatedAt(LocalDateTime.now());
            cargaRepo.save(carga);
            log.info("Carga registrada para idempotencia");

            Map<String, Cliente> clientesMap = cargarClientes();
            Map<String, Zona> zonaMap = cargarZonas();
            log.info("Catalogos cargados: {} clientes, {} zonas", clientesMap.size(), zonaMap.size());

            try (InputStream inputStream = archivo.getInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 CSVReader csvReader = new CSVReader(reader)) {

                List<String[]> lineas = csvReader.readAll();

                if (lineas.isEmpty()){
                    resumen.agregarError(0, "El archivo CSV esta vacio", TipoError.FORMATO_INVALIDO);
                    return resumen;
                }

                List<String[]> lineasDatos = lineas.subList(1, lineas.size());
                procesarEnLotes(lineasDatos, clientesMap, zonaMap, resumen);
            } catch (CsvException e){
                log.error("Error leyendo el archivo CSV", e);
                resumen.agregarError(0, "Error leyendo el archivo CSV: " + e.getMessage(),
                        TipoError.FORMATO_INVALIDO);
            }
            log.info("Carga completada. Total {}, Guardados {}, Con error {}",
                    resumen.getTotalProcesados(),
                    resumen.getGuardados(),
                    resumen.getConError());
        } catch (IOException e){
            log.error("Error procesando el archivo", e);
            resumen.agregarError(0, "Error leyendo el archivo: "+ e.getMessage(),
                    TipoError.FORMATO_INVALIDO);
        } catch (Exception e){
            log.error("Error inesperado durante la carga", e);
            resumen.agregarError(0, "Error inesperado: " + e.getMessage(),
                    TipoError.FORMATO_INVALIDO);
        }
        return resumen;
    }


    private void procesarEnLotes(
            List<String[]> lineas,
            Map<String, Cliente> clientesMap,
            Map<String, Zona> zonasMap,
            ResumenCargaDto resumen
    ){
        int totalLineas = lineas.size();
        int numeroLote = 0;

        for (int i = 0 ; i < totalLineas ; i += batchSize) {
            numeroLote++;
            int finLote = Math.min(i + batchSize, totalLineas);
            List<String[]> lote = lineas.subList(i, finLote);

            log.debug("Procesando lote {} ({} lineas)", numeroLote, lote.size());
            procesarLote(lote, i + 2, clientesMap, zonasMap, resumen);
        }
    }

    private void procesarLote(
            List<String[]> lote,
            int numeroLineaInicial,
            Map<String, Cliente> clientesMap,
            Map<String, Zona> zonasMap,
            ResumenCargaDto resumen
    ){
        List<Pedido> pedidosValidos = new ArrayList<>();
        Set<String> numerosPedidoLote = lote.stream()
                .filter(linea -> linea.length > 0)
                .map(linea -> linea[0].trim())
                .collect(Collectors.toSet());

        Set<String> numerosPedidoExistentes = pedidoRepo.existsByNumeroPedidoIn(numerosPedidoLote);

        for(int i = 0; i < lote.size(); i++){
            int numeroLinea = numeroLineaInicial + i;
            String[] linea = lote.get(i);
            resumen.incrementarProcesados();

            try{
                PedidoCsvDto dto = parsearLinea(linea, numeroLinea);
                Pedido pedido = validacion.validarYConvertir(dto, clientesMap, zonasMap, numerosPedidoExistentes);
                pedidosValidos.add(pedido);

                numerosPedidoExistentes.add(pedido.getNumeroPedido());
            } catch (ValidationException e){
                log.debug("Error de validacion en lina {}: {}", numeroLinea, e.getMessage());
                resumen.agregarError(numeroLinea, e.getMessage(), e.getTipoError());
            } catch (Exception e){
                log.warn("Error inesperado en linea {}: {}", numeroLinea, e.getMessage());
                resumen.agregarError(numeroLinea, e.getMessage(), TipoError.FORMATO_INVALIDO);
            }
        }

        if (!pedidosValidos.isEmpty()){
            try{
                pedidoRepo.saveAll(pedidosValidos);
                pedidosValidos.forEach(p -> resumen.agregarExito());
                log.debug("Guardados {} pedidos de llote ", pedidosValidos.size());
            } catch (Exception e){
                log.error("Error guardando pedidos", e);
                pedidosValidos.forEach(p -> resumen.agregarError(
                        0,
                        "Error guardando pedido: " + p.getNumeroPedido(),
                        TipoError.FORMATO_INVALIDO)
                );
            }
        }

    }

    private PedidoCsvDto parsearLinea(String[] datos, int numeroLinea) throws Exception{
        if (datos.length < 6){
            throw new Exception("Fomato incorrecto, se necesitan 6 columnas, se encontraron " + datos.length);
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

    private Map<String, Cliente> cargarClientes(){
        List<Cliente> clientes = clienteRepo.findAllActivos();
        return clientes.stream().collect(Collectors.toMap(Cliente :: getId, c -> c));
    }

    private Map<String, Zona> cargarZonas(){
        List<Zona> zonas = zonaRepo.findAll();
        return zonas.stream().collect(Collectors.toMap(Zona :: getId, z -> z));
    }
}
