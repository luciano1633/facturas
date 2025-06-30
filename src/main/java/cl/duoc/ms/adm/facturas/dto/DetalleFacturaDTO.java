package cl.duoc.ms.adm.facturas.dto;

import lombok.Data;

@Data
public class DetalleFacturaDTO {
    private Long id;
    private Long productoId;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}