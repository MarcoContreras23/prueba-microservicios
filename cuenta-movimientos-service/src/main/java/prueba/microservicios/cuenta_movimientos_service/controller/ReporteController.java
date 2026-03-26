package prueba.microservicios.cuenta_movimientos_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.dto.ReporteDTO;
import prueba.microservicios.cuenta_movimientos_service.service.ReporteService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public ResponseEntity<ReporteDTO> generarReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam String cliente) {
        log.info("GET /reportes - Generando reporte para cliente: {}, desde: {} hasta: {}",
                cliente, fechaInicio, fechaFin);
        ReporteDTO reporte = reporteService.generarEstadoCuenta(cliente, fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
}
