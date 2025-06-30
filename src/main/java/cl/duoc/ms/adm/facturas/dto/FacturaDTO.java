package cl.duoc.ms.adm.facturas.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FacturaDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fecha;
    private Double total;
    private List<DetalleFacturaDTO> detalles;
    private String archivoPath;
}