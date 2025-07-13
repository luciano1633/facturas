package cl.duoc.ms.adm.facturas.service;

import cl.duoc.ms.adm.facturas.model.Factura;
import cl.duoc.ms.adm.facturas.Repository.FacturaRepository;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

import cl.duoc.ms.adm.facturas.service.ProducirMensajeService;

@Service
public class FacturaService {
    private final FacturaRepository facturaRepository;
    private final AwsS3Service awsS3Service;
    private final ProducirMensajeService producirMensajeService;
    private final String BUCKET_NAME = "bucketduocpruebas3";

    public FacturaService(FacturaRepository facturaRepository, AwsS3Service awsS3Service, ProducirMensajeService producirMensajeService) {
        this.facturaRepository = facturaRepository;
        this.awsS3Service = awsS3Service;
        this.producirMensajeService = producirMensajeService;
    }

    public List<Factura> listarFacturas() {
        return facturaRepository.findAll();
    }

    public Optional<Factura> obtenerFacturaPorId(Long id) {
        return facturaRepository.findById(id);
    }

    public void guardarFactura(Factura factura) {
        // Criterio 3: Enviar la solicitud de creación de factura a una cola de RabbitMQ
        // para su procesamiento asíncrono.
        producirMensajeService.enviarObjeto(factura);
    }

    public Factura actualizarFacturaS3(Long id, MultipartFile file) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con el id: " + id));

        try {
            String key = factura.getArchivoPath();
            if (key == null || key.isEmpty()) {
                key = generarKeyS3(factura);
            }
            awsS3Service.upload(BUCKET_NAME, key, file.getBytes());
            
            factura.setArchivoPath(key);
            factura.setFecha(LocalDateTime.now()); // Actualizar fecha de modificación
            return facturaRepository.save(factura);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar el archivo en S3", e);
        }
    }

    public void eliminarFactura(Long id) {
        facturaRepository.findById(id).ifPresent(factura -> {
            if (factura.getArchivoPath() != null && !factura.getArchivoPath().isEmpty()) {
                awsS3Service.deleteObject(BUCKET_NAME, factura.getArchivoPath());
            }
            facturaRepository.deleteById(id);
        });
    }

    public List<Factura> obtenerFacturasPorCliente(Long clienteId) {
        return facturaRepository.findByClienteId(clienteId);
    }

    public byte[] descargarFactura(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con el id: " + id));

        if (factura.getArchivoPath() == null || factura.getArchivoPath().isEmpty()) {
            throw new RuntimeException("La factura no tiene un archivo asociado.");
        }

        return awsS3Service.downloadAsBytes(BUCKET_NAME, factura.getArchivoPath());
    }

    private String generarKeyS3(Factura factura) {
        LocalDate localDate = factura.getFecha().toLocalDate();
        String carpeta = factura.getClienteId() + "/" + localDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return carpeta + "/" + factura.getId() + ".pdf";
    }
}
