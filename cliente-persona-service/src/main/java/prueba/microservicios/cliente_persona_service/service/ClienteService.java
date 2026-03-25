package prueba.microservicios.cliente_persona_service.service;

import java.util.List;

import prueba.microservicios.cliente_persona_service.dto.ClienteDTO;
import prueba.microservicios.cliente_persona_service.dto.ClienteResponseDTO;

public interface ClienteService {

    ClienteResponseDTO crear(ClienteDTO dto);

    List<ClienteResponseDTO> listarTodos();

    ClienteResponseDTO obtenerPorClienteId(String clienteId);

    ClienteResponseDTO actualizar(String clienteId, ClienteDTO dto);

    void eliminar(String clienteId);
}