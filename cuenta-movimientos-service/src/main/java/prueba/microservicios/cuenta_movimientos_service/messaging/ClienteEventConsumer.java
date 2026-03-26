package prueba.microservicios.cuenta_movimientos_service.messaging;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import prueba.microservicios.cuenta_movimientos_service.config.RabbitMQConfig;
import prueba.microservicios.cuenta_movimientos_service.entity.ClienteLocal;
import prueba.microservicios.cuenta_movimientos_service.repository.ClienteLocalRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final ClienteLocalRepository clienteLocalRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleClienteEvent(Map<String, Object> event) {
        try {
            String clienteId = (String) event.get("clienteId");
            String nombre = (String) event.get("nombre");
            Boolean estado = (Boolean) event.get("estado");
            String action = (String) event.get("action");

            log.info("Evento recibido: {} para clienteId: {}", action, clienteId);

            ClienteLocal clienteLocal = clienteLocalRepository.findByClienteId(clienteId)
                    .orElse(new ClienteLocal());

            clienteLocal.setClienteId(clienteId);
            clienteLocal.setNombre(nombre);
            clienteLocal.setEstado(estado != null ? estado : true);

            clienteLocalRepository.save(clienteLocal);
            log.info("Cliente local actualizado: clienteId={}, nombre={}", clienteId, nombre);

        } catch (Exception e) {
            log.error("Error al procesar evento de cliente: {}", e.getMessage(), e);
        }
    }
}