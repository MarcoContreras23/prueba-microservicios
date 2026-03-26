package prueba.microservicios.cuenta_movimientos_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoDTO;
import prueba.microservicios.cuenta_movimientos_service.dto.MovimientoResponseDTO;
import prueba.microservicios.cuenta_movimientos_service.service.MovimientoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@Slf4j
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponseDTO> registrar(@Valid @RequestBody MovimientoDTO dto) {
        log.info("POST /movimientos - Registrando movimiento para cuenta: {}", dto.getNumeroCuenta());
        MovimientoResponseDTO response = movimientoService.registrarMovimiento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MovimientoResponseDTO>> listarTodos() {
        log.info("GET /movimientos - Listando todos los movimientos");
        return ResponseEntity.ok(movimientoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("GET /movimientos/{} - Obteniendo movimiento", id);
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("DELETE /movimientos/{} - Eliminando movimiento", id);
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
