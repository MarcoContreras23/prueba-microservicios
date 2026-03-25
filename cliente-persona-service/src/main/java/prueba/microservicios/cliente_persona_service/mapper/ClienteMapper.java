package prueba.microservicios.cliente_persona_service.mapper;

import org.springframework.stereotype.Component;

import prueba.microservicios.cliente_persona_service.dto.ClienteDTO;
import prueba.microservicios.cliente_persona_service.dto.ClienteResponseDTO;
import prueba.microservicios.cliente_persona_service.entity.Cliente;

@Component
public class ClienteMapper {

    public void toEntity(ClienteDTO dto, Cliente cliente) {
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setContrasena(dto.getContrasena());
    }

    public Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        toEntity(dto, cliente);
        return cliente;
    }

    public ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .personaId(cliente.getPersonaId())
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .identificacion(cliente.getIdentificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .build();
    }
}
