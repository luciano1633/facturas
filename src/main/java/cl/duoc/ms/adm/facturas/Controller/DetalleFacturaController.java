package cl.duoc.ms.adm.facturas.Controller;

import cl.duoc.ms.adm.facturas.dto.DetalleFacturaDTO;
import cl.duoc.ms.adm.facturas.model.DetalleFactura;
import cl.duoc.ms.adm.facturas.service.DetalleFacturaService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/detalles-factura")
public class DetalleFacturaController {
    private final DetalleFacturaService detalleFacturaService;

    public DetalleFacturaController(DetalleFacturaService detalleFacturaService) {
        this.detalleFacturaService = detalleFacturaService;
    }

    @GetMapping
    public List<DetalleFacturaDTO> listar() {
        return detalleFacturaService.listarDetalles().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public DetalleFacturaDTO agregar(@RequestBody DetalleFacturaDTO detalleDTO) {
        DetalleFactura detalle = toEntity(detalleDTO);
        DetalleFactura guardado = detalleFacturaService.guardarDetalle(detalle);
        return toDTO(guardado);
    }

    private DetalleFacturaDTO toDTO(DetalleFactura detalle) {
        DetalleFacturaDTO dto = new DetalleFacturaDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProductoId());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setCantidad(detalle.getCantidad());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }

    private DetalleFactura toEntity(DetalleFacturaDTO dto) {
        DetalleFactura detalle = new DetalleFactura();
        detalle.setId(dto.getId());
        detalle.setProductoId(dto.getProductoId());
        detalle.setPrecioUnitario(dto.getPrecioUnitario());
        detalle.setCantidad(dto.getCantidad());
        detalle.setSubtotal(dto.getSubtotal());
        return detalle;
    }
}