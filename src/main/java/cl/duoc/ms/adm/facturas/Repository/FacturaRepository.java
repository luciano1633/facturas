package cl.duoc.ms.adm.facturas.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.adm.facturas.model.Factura;

import java.util.List;

public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByClienteId(Long clienteId);
}