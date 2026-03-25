package prueba.microservicios.cliente_persona_service.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cliente_persona_service.config.RabbitMQConfig;
import prueba.microservicios.cliente_persona_service.dto.ClienteDTO;
import prueba.microservicios.cliente_persona_service.dto.ClienteResponseDTO;
import prueba.microservicios.cliente_persona_service.entity.Cliente;
import prueba.microservicios.cliente_persona_service.exception.ResourceNotFoundException;
import prueba.microservicios.cliente_persona_service.mapper.ClienteMapper;
import prueba.microservicios.cliente_persona_service.repository.ClienteRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteDTO dto) {
        if (clienteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un cliente con la identificación: " + dto.getIdentificacion());
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        cliente.setClienteId(UUID.randomUUID().toString().substring(0, 8));
        cliente.setEstado(dto.getEstado() != null ? dto.getEstado() : true);

        Cliente saved = clienteRepository.save(cliente);
        log.info("Cliente creado con clienteId: {}", saved.getClienteId());

        publishEvent("cliente.event.created", saved);

        return clienteMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteResponseDTO> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO obtenerPorClienteId(String clienteId) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con clienteId: " + clienteId));
        return clienteMapper.toResponseDTO(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(String clienteId, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con clienteId: " + clienteId));

        clienteMapper.toEntity(dto, cliente);
        if (dto.getEstado() != null) {
            cliente.setEstado(dto.getEstado());
        }

        Cliente updated = clienteRepository.save(cliente);
        log.info("Cliente actualizado con clienteId: {}", updated.getClienteId());

        publishEvent("cliente.event.updated", updated);

        return clienteMapper.toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void eliminar(String clienteId) {
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con clienteId: " + clienteId));

        cliente.setEstado(false);
        clienteRepository.save(cliente);
        log.info("Cliente eliminado (soft delete) con clienteId: {}", clienteId);

        publishEvent("cliente.event.deleted", cliente);
    }

    private void publishEvent(String routingKey, Cliente cliente) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("clienteId", cliente.getClienteId());
            event.put("nombre", cliente.getNombre());
            event.put("estado", cliente.getEstado());
            event.put("action", routingKey);

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, event);
            log.info("Evento publicado: {} para clienteId: {}", routingKey, cliente.getClienteId());
        } catch (Exception e) {
            log.error("Error al publicar evento para clienteId: {}. Error: {}", cliente.getClienteId(), e.getMessage());
        }
    }

}
