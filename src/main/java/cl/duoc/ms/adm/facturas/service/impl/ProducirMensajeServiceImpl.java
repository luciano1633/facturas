package cl.duoc.ms.adm.facturas.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.ms.adm.facturas.config.RabbitMQConfig;
import cl.duoc.ms.adm.facturas.service.ProducirMensajeService;

@Service
public class ProducirMensajeServiceImpl implements ProducirMensajeService {

	private final RabbitTemplate rabbitTemplate;

	public ProducirMensajeServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void enviarMensaje(String mensaje) {

		rabbitTemplate.convertAndSend(RabbitMQConfig.FACTURAS_EXCHANGE, RabbitMQConfig.FACTURAS_ROUTING_KEY, mensaje);
	}

	@Override
	public void enviarObjeto(Object objeto) {

		rabbitTemplate.convertAndSend(RabbitMQConfig.FACTURAS_EXCHANGE, RabbitMQConfig.FACTURAS_ROUTING_KEY, objeto);
	}
}
