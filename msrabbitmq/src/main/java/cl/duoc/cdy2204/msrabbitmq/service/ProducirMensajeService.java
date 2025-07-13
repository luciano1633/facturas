package cl.duoc.cdy2204.msrabbitmq.service;

public interface ProducirMensajeService {

	void enviarMensaje(String mensaje);

	public void enviarObjeto(Object objeto);
}
