package prueba.microservicios.cliente_persona_service.controller.integration;

import static org.mockito.Mockito.mock;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import prueba.microservicios.cliente_persona_service.service.MensajeriaService;

@TestConfiguration
public class TestRabbitMQConfig {

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public MensajeriaService mensajeriaService() {
        return mock(MensajeriaService.class);
    }
}
