package prueba.microservicios.cliente_persona_service.service;

import prueba.microservicios.cliente_persona_service.entity.Cliente;

public interface MensajeriaService{

    void publishEvent(String routingKey, Cliente cliente);   
}
