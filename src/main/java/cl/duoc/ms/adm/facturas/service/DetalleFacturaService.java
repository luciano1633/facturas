package cl.duoc.ms.adm.facturas.service;

import cl.duoc.ms.adm.facturas.model.DetalleFactura;
import cl.duoc.ms.adm.facturas.Repository.DetalleFacturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DetalleFacturaService {
    private final DetalleFacturaRepository detalleFacturaRepository;

    public DetalleFacturaService(DetalleFacturaRepository detalleFacturaRepository) {
        this.detalleFacturaRepository = detalleFacturaRepository;
    }

    public List<DetalleFactura> listarDetalles() {
        return detalleFacturaRepository.findAll();
    }

    public Optional<DetalleFactura> obtenerDetallePorId(Long id) {
        return detalleFacturaRepository.findById(id);
    }

    public DetalleFactura guardarDetalle(DetalleFactura detalle) {
        return detalleFacturaRepository.save(detalle);
    }

    public void eliminarDetalle(Long id) {
        detalleFacturaRepository.deleteById(id);
    }
}
