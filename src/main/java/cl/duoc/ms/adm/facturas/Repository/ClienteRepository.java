package cl.duoc.ms.adm.facturas.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.duoc.ms.adm.facturas.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {}