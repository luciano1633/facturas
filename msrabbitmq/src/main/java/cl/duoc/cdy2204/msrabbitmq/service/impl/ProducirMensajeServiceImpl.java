package cl.duoc.cdy2204.msrabbitmq.service.impl;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import cl.duoc.cdy2204.msrabbitmq.config.RabbitMQConfig;
import cl.duoc.cdy2204.msrabbitmq.service.ProducirMensajeService;

@Service
public class ProducirMensajeServiceImpl implements ProducirMensajeService {

	private final RabbitTemplate rabbitTemplate;

	public ProducirMensajeServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void enviarMensaje(String mensaje) {

		rabbitTemplate.convertAndSend(RabbitMQConfig.MAIN_QUEUE, mensaje);
	}

	@Override
	public void enviarObjeto(Object objeto) {

		rabbitTemplate.convertAndSend(RabbitMQConfig.MAIN_QUEUE, objeto);
	}
}
