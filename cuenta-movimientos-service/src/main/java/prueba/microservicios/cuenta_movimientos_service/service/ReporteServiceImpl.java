package prueba.microservicios.cuenta_movimientos_service.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.dto.EstadoCuentaDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoReporteDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.ReporteDTO;
import prueba.microservicios.cuenta_movimientos_service.entity.ClienteLocal;
import prueba.microservicios.cuenta_movimientos_service.entity.Cuenta;
import prueba.microservicios.cuenta_movimientos_service.entity.Movimiento;
import prueba.microservicios.cuenta_movimientos_service.exception.ResourceNotFoundException;
import prueba.microservicios.cuenta_movimientos_service.repository.ClienteLocalRepository;
import prueba.microservicios.cuenta_movimientos_service.repository.CuentaRepository;
import prueba.microservicios.cuenta_movimientos_service.repository.MovimientoRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReporteServiceImpl implements ReporteService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClienteLocalRepository clienteLocalRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @Transactional(readOnly = true)
    public ReporteDTO generarEstadoCuenta(String clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte para clienteId: {}, desde: {} hasta: {}", clienteId, fechaInicio, fechaFin);

        ClienteLocal clienteLocal = clienteLocalRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente no encontrado en registros locales: " + clienteId));

        List<Cuenta> cuentas = cuentaRepository.findByClienteId(clienteId);

        if (cuentas.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron cuentas para el cliente: " + clienteId);
        }

        LocalDateTime fechaInicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime fechaFinDateTime = fechaFin.atTime(LocalTime.MAX);

        List<EstadoCuentaDTO> estadosCuenta = cuentas.stream()
                .map(cuenta -> buildEstadoCuenta(cuenta, fechaInicioDateTime, fechaFinDateTime))
                .collect(Collectors.toList());

        return ReporteDTO.builder()
                .cliente(clienteLocal.getNombre())
                .cuentas(estadosCuenta)
                .build();
    }

    private EstadoCuentaDTO buildEstadoCuenta(Cuenta cuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        List<Movimiento> movimientos = movimientoRepository.findByCuentaAndFechaBetween(
                cuenta.getNumeroCuenta(), fechaInicio, fechaFin);

        List<MovimientoReporteDTO> movimientosDTO = movimientos.stream()
                .map(mov -> MovimientoReporteDTO.builder()
                        .fecha(mov.getFecha().format(DATE_FORMATTER))
                        .tipo(mov.getTipoMovimiento())
                        .valor(mov.getValor())
                        .saldo(mov.getSaldo())
                        .build())
                .collect(Collectors.toList());

        return EstadoCuentaDTO.builder()
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipo(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .estado(cuenta.getEstado())
                .movimientos(movimientosDTO)
                .build();
    }
}
