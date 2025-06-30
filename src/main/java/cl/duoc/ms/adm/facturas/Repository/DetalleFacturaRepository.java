package cl.duoc.ms.adm.facturas.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.adm.facturas.model.DetalleFactura;

public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Long> {}