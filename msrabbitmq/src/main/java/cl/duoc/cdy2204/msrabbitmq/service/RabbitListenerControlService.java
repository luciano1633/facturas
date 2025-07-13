package cl.duoc.cdy2204.msrabbitmq.service;

public interface RabbitListenerControlService {

	void pausarListener(String id);

	void reanudarListener(String id);

	boolean isListenerRunning(String id);
}
