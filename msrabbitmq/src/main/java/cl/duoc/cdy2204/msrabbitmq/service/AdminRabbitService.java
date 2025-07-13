package cl.duoc.cdy2204.msrabbitmq.service;

import cl.duoc.cdy2204.msrabbitmq.dto.BindingDTO;

public interface AdminRabbitService {

	public void crearCola(String nombreCola);

	public void crearExchange(String nombreExchange);

	public void crearBinding(BindingDTO request);

	public void eliminarCola(String nombreCola);

	public void eliminarExchange(String nombreExchange);
}
