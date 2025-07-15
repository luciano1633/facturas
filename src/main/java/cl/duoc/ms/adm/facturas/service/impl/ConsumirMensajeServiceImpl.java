package cl.duoc.ms.adm.facturas.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import cl.duoc.ms.adm.facturas.Repository.FacturaRepository;
import cl.duoc.ms.adm.facturas.config.RabbitMQConfig;
import cl.duoc.ms.adm.facturas.model.Factura;
import cl.duoc.ms.adm.facturas.service.AwsS3Service;
import cl.duoc.ms.adm.facturas.service.ConsumirMensajeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsumirMensajeServiceImpl implements ConsumirMensajeService {

    private final FacturaRepository facturaRepository;
    private final AwsS3Service awsS3Service;
    private final ObjectMapper objectMapper;
    private final String BUCKET_NAME = "bucketduocpruebas3";

	@Override
	public String obtenerUltimoMensaje() {
		// Este método sigue siendo manual y es principalmente para depuración.
		String mensaje = null;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("rabbitmq");
		factory.setUsername("guest");
		factory.setPassword("guest");

		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			GetResponse response = channel.basicGet(RabbitMQConfig.FACTURAS_QUEUE, true);
			if (response != null) {
				mensaje = new String(response.getBody(), "UTF-8");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mensaje;
	}

	@Override
	public void recibirMensaje(Object objeto) {
		// Este listener no se usa, el que tiene ACK manual es el principal.
		System.out.println("Mensaje recibido en facturas-queue: " + objeto);
	}

	@RabbitListener(id = "listener-facturas-queue", queues = RabbitMQConfig.FACTURAS_QUEUE, ackMode = "MANUAL")
	@Override
	public void recibirMensajeConAckManual(Message mensaje, Channel canal) throws IOException {
		Factura factura = null;
		try {
			// 1. Deserializar el mensaje a un objeto Factura
			factura = objectMapper.readValue(mensaje.getBody(), Factura.class);
			System.out.println("Procesando solicitud de factura para cliente ID: " + factura.getClienteId());

            // Simulación de error para probar la DLQ
            if (factura.getClienteId() != null && factura.getClienteId() == 0) {
                throw new RuntimeException("Error forzado para cliente ID 0, probando DLQ.");
            }

			// 2. Guardar la factura inicialmente para obtener un ID
			Factura facturaGuardada = facturaRepository.save(factura);

			// 3. Generar el PDF
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Document document = new Document();
			PdfWriter.getInstance(document, baos);
			document.open();
			document.add(new Paragraph("Factura generada automáticamente"));
			document.add(new Paragraph("ID Factura: " + facturaGuardada.getId()));
			document.add(new Paragraph("Cliente ID: " + facturaGuardada.getClienteId()));
			document.add(new Paragraph("Fecha: " + facturaGuardada.getFecha()));
			document.add(new Paragraph("Total: " + facturaGuardada.getTotal()));
			document.close();

			// 4. Subir el PDF a S3
			String key = generarKeyS3(facturaGuardada);
			// awsS3Service.upload(BUCKET_NAME, key, baos.toByteArray()); // Comentado para simulación por falta de permisos IAM
			System.out.println("SIMULACIÓN: El archivo se hubiera subido a S3 con la clave: " + key);
			facturaGuardada.setArchivoPath(key);

			// 5. Actualizar la factura con la ruta del archivo S3
			facturaRepository.save(facturaGuardada);

			// 6. Si todo fue exitoso, enviar el ACK
			canal.basicAck(mensaje.getMessageProperties().getDeliveryTag(), false);
			System.out.println("Factura ID " + facturaGuardada.getId() + " procesada y guardada en S3. ACK enviado.");

		} catch (Exception e) {
			System.err.println("Error al procesar factura. Enviando a DLQ. Causa: " + e.getMessage());
            if (factura != null && factura.getId() != null) {
                // Si la factura se guardó pero algo más falló, se revierte la creación.
                facturaRepository.deleteById(factura.getId());
                System.err.println("Se ha revertido la creación de la factura ID: " + factura.getId());
            }
			// Enviar NACK para que el mensaje vaya a la DLQ
			canal.basicNack(mensaje.getMessageProperties().getDeliveryTag(), false, false);
		}
	}

    private String generarKeyS3(Factura factura) {
        LocalDate localDate = factura.getFecha().toLocalDate();
        String carpeta = factura.getClienteId() + "/" + localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return carpeta + "/" + factura.getId() + ".pdf";
    }
}
