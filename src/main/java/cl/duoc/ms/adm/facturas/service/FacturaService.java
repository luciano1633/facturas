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

@Service
public class FacturaService {
    private final FacturaRepository facturaRepository;
    private final AwsS3Service awsS3Service;
    private final String BUCKET_NAME = "bucketduocpruebas3";

    public FacturaService(FacturaRepository facturaRepository, AwsS3Service awsS3Service) {
        this.facturaRepository = facturaRepository;
        this.awsS3Service = awsS3Service;
    }

    public List<Factura> listarFacturas() {
        return facturaRepository.findAll();
    }

    public Optional<Factura> obtenerFacturaPorId(Long id) {
        return facturaRepository.findById(id);
    }

    public Factura guardarFactura(Factura factura) {
        // Guardar la factura para obtener el ID
        Factura facturaGuardada = facturaRepository.save(factura);

        try {
            // Generar PDF
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

            // Subir a S3
            String key = generarKeyS3(facturaGuardada);
            awsS3Service.upload(BUCKET_NAME, key, baos.toByteArray());
            facturaGuardada.setArchivoPath(key);

            // Actualizar la factura con la ruta del archivo S3
            return facturaRepository.save(facturaGuardada);
        } catch (Exception e) {
            // En caso de error al generar o subir el PDF, se elimina la factura creada.
            facturaRepository.deleteById(facturaGuardada.getId());
            throw new RuntimeException("Error al generar o subir el PDF de la factura. La factura ha sido revertida.", e);
        }
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
