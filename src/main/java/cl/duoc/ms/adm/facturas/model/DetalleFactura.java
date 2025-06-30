package cl.duoc.ms.adm.facturas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class DetalleFactura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productoId;
    private Double precioUnitario;
    private Integer cantidad;
    private Double subtotal;
}