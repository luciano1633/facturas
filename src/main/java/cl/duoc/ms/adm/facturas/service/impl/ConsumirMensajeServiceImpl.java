package cl.duoc.ms.adm.facturas.service.impl;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import cl.duoc.ms.adm.facturas.config.RabbitMQConfig;
import cl.duoc.ms.adm.facturas.service.ConsumirMensajeService;

@Service
public class ConsumirMensajeServiceImpl implements ConsumirMensajeService {

	@Override
	public String obtenerUltimoMensaje() {

		String mensaje = null;

		ConnectionFactory factory = new ConnectionFactory();

		factory.setHost("host.docker.internal");
		factory.setUsername("guest");
		factory.setPassword("guest");

		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

			GetResponse response = channel.basicGet("myQueue", true);

			if (response != null) {
				mensaje = new String(response.getBody(), "UTF-8");
				System.out.println("Mensaje recibido: " + mensaje);
			} else {
				System.out.println("No hay mensajes en la cola");
			}

		} catch (Exception e) {
			System.out.println("Error al consumir mensaje de RabbitMQ");
			e.printStackTrace();
		}
		return mensaje;
	}

//	@RabbitListener(id = "listener-myQueue", queues = RabbitMQConfig.MAIN_QUEUE)
	@Override
	public void recibirMensaje(Object objeto) {

		System.out.println("Mensaje recibido en myQueue: " + objeto);
	}

	@RabbitListener(id = "listener-myQueue", queues = RabbitMQConfig.MAIN_QUEUE, ackMode = "MANUAL")
	@Override
	public void recibirMensajeConAckManual(Message mensaje, Channel canal) throws IOException {

		try {

			String body = new String(mensaje.getBody());
			System.out.println("Mensaje recibido: " + body);

			if (body.contains("error")) {
				throw new RuntimeException("Error forzado para probar la DLQ");
			}

			Thread.sleep(5000);

			canal.basicAck(mensaje.getMessageProperties().getDeliveryTag(), false);
			System.out.println("Acknowledge OK enviado");
		} catch (Exception e) {
			canal.basicNack(mensaje.getMessageProperties().getDeliveryTag(), false, false);
			System.out.println("Acknowledge NO OK enviado");
		}
	}
}

