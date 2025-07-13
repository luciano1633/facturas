package cl.duoc.ms.adm.facturas.service;

public interface RabbitListenerControlService {
    
    void pausarListener(String id);

	void reanudarListener(String id);

	boolean isListenerRunning(String id);

}
