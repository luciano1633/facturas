package cl.duoc.ms.adm.facturas.config;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	public static final String FACTURAS_QUEUE = "facturas-queue";
	public static final String FACTURAS_EXCHANGE = "facturas-exchange";
	public static final String FACTURAS_ROUTING_KEY = "facturas.process";
	public static final String DLX_QUEUE = "facturas-dlq";
	public static final String DLX_EXCHANGE = "facturas-dlx-exchange";
	public static final String DLX_ROUTING_KEY = "facturas.dlq";

	@Bean
	Jackson2JsonMessageConverter messageConverter() {

		return new Jackson2JsonMessageConverter();
	}

	@Bean
	CachingConnectionFactory connectionFactory() {

		CachingConnectionFactory factory = new CachingConnectionFactory();

		return factory;
	}

	@Bean
	Queue facturasQueue() {

		return new Queue(FACTURAS_QUEUE, true, false, false,
				Map.of("x-dead-letter-exchange", DLX_EXCHANGE, "x-dead-letter-routing-key", DLX_ROUTING_KEY));
	}

	@Bean
	Queue dlxQueue() {

		return new Queue(DLX_QUEUE);
	}

	@Bean
	DirectExchange facturasExchange() {

		return new DirectExchange(FACTURAS_EXCHANGE);
	}

	@Bean
	DirectExchange dlxExchange() {

		return new DirectExchange(DLX_EXCHANGE);
	}

	@Bean
	Binding facturasBinding(Queue facturasQueue, DirectExchange facturasExchange) {

		return BindingBuilder.bind(facturasQueue).to(facturasExchange).with(FACTURAS_ROUTING_KEY);
	}

	@Bean
	Binding dlxBinding() {

		return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(DLX_ROUTING_KEY);
	}
}
