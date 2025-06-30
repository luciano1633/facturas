package cl.duoc.ms.adm.facturas.dto;

import lombok.Data;

@Data
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String email;
}