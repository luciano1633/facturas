package cl.duoc.ms.adm.facturas.Controller;

import cl.duoc.ms.adm.facturas.dto.FacturaDTO;
import cl.duoc.ms.adm.facturas.dto.DetalleFacturaDTO;
import cl.duoc.ms.adm.facturas.model.Factura;
import cl.duoc.ms.adm.facturas.service.FacturaService;
import cl.duoc.ms.adm.facturas.model.DetalleFactura;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/facturas")
public class FacturaController {
    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public List<FacturaDTO> listar() {
        return facturaService.listarFacturas().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/cliente/{clienteId}")
    public List<FacturaDTO> historialPorCliente(@PathVariable Long clienteId) {
        return facturaService.obtenerFacturasPorCliente(clienteId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<String> agregar(@RequestBody FacturaDTO facturaDTO) {
        Factura factura = toEntity(facturaDTO);
        if (factura.getFecha() == null) {
            factura.setFecha(LocalDateTime.now());
        }
        facturaService.guardarFactura(factura);
        String mensaje = "Solicitud de factura recibida. Se está procesando de forma asíncrona.";
        return ResponseEntity.accepted().body(mensaje);
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<FacturaDTO> actualizar(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Factura facturaActualizada = facturaService.actualizarFacturaS3(id, file);
        return ResponseEntity.ok(toDTO(facturaActualizada));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        facturaService.eliminarFactura(id);
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        byte[] data = facturaService.descargarFactura(id);
        String nombreArchivo = "factura_" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    private FacturaDTO toDTO(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        dto.setClienteId(factura.getClienteId());
        dto.setFecha(factura.getFecha());
        dto.setTotal(factura.getTotal());
        dto.setArchivoPath(factura.getArchivoPath());
        if (factura.getDetalles() != null) {
            dto.setDetalles(factura.getDetalles().stream()
                    .map(this::toDetalleDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private Factura toEntity(FacturaDTO dto) {
        Factura factura = new Factura();
        factura.setId(dto.getId());
        factura.setClienteId(dto.getClienteId());
        factura.setFecha(dto.getFecha());
        factura.setTotal(dto.getTotal());
        factura.setArchivoPath(dto.getArchivoPath());
        if (dto.getDetalles() != null) {
            factura.setDetalles(dto.getDetalles().stream()
                    .map(this::toDetalleEntity)
                    .collect(Collectors.toList()));
        }
        return factura;
    }

    private DetalleFacturaDTO toDetalleDTO(DetalleFactura detalle) {
        DetalleFacturaDTO dto = new DetalleFacturaDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProductoId());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setCantidad(detalle.getCantidad());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }

    private DetalleFactura toDetalleEntity(DetalleFacturaDTO dto) {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(dto.getId());
        detalle.setProductoId(dto.getProductoId());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());
        detalle.setCantidad(dto.getCantidad());
        detalle.setSubtotal(dto.getSubtotal());
        return detalle;
    }
}
