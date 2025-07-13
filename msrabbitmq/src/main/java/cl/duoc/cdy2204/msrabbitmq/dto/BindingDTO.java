package cl.duoc.cdy2204.msrabbitmq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BindingDTO {

	private String nombreCola;
	private String nombreExchange;
	private String routingKey;
}
