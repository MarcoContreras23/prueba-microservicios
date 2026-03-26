package prueba.microservicios.cuenta_movimientos_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.CuentaResponseDTO;
import prueba.microservicios.cuenta_movimientos_service.service.CuentaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
@Slf4j
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crear(@Valid @RequestBody CuentaDTO dto) {
        log.info("POST /cuentas - Creando nueva cuenta");
        CuentaResponseDTO response = cuentaService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CuentaResponseDTO>> listarTodas() {
        log.info("GET /cuentas - Listando todas las cuentas");
        return ResponseEntity.ok(cuentaService.listarTodas());
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> obtenerPorNumeroCuenta(@PathVariable String numeroCuenta) {
        log.info("GET /cuentas/{} - Obteniendo cuenta", numeroCuenta);
        return ResponseEntity.ok(cuentaService.obtenerPorNumeroCuenta(numeroCuenta));
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> actualizar(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CuentaDTO dto) {
        log.info("PUT /cuentas/{} - Actualizando cuenta", numeroCuenta);
        return ResponseEntity.ok(cuentaService.actualizar(numeroCuenta, dto));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroCuenta) {
        log.info("DELETE /cuentas/{} - Eliminando cuenta", numeroCuenta);
        cuentaService.eliminar(numeroCuenta);
        return ResponseEntity.noContent().build();
    }
}
