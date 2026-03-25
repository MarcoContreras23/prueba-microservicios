package prueba.microservicios.cliente_persona_service.controller;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import prueba.microservicios.cliente_persona_service.dto.ClienteDTO;
import prueba.microservicios.cliente_persona_service.dto.ClienteResponseDTO;
import prueba.microservicios.cliente_persona_service.service.ClienteService;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteDTO dto) {
        log.info("POST /clientes - Creando nuevo cliente");
        ClienteResponseDTO response = clienteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        log.info("GET /clientes - Listando todos los clientes");
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable String clienteId) {
        log.info("GET /clientes/{} - Obteniendo cliente", clienteId);
        return ResponseEntity.ok(clienteService.obtenerPorClienteId(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable String clienteId,
            @Valid @RequestBody ClienteDTO dto) {
        log.info("PUT /clientes/{} - Actualizando cliente", clienteId);
        return ResponseEntity.ok(clienteService.actualizar(clienteId, dto));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
        log.info("DELETE /clientes/{} - Eliminando cliente", clienteId);
        clienteService.eliminar(clienteId);
        return ResponseEntity.noContent().build();
    }
}