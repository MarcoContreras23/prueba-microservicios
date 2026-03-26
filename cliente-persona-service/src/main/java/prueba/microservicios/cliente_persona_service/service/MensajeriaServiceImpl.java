package prueba.microservicios.cliente_persona_service.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cliente_persona_service.config.RabbitMQConfig;
import prueba.microservicios.cliente_persona_service.entity.Cliente;

@Service
@RequiredArgsConstructor
@Slf4j
public class MensajeriaServiceImpl implements MensajeriaService {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishEvent(String routingKey, Cliente cliente) {
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
