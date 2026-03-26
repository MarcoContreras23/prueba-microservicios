package prueba.microservicios.cuenta_movimientos_service.service;

import java.time.LocalDate;

import prueba.microservicios.cuenta_movimientos_service.dto.ReporteDTO;

public interface ReporteService {

    ReporteDTO generarEstadoCuenta(String clienteId, LocalDate fechaInicio, LocalDate fechaFin);
}
