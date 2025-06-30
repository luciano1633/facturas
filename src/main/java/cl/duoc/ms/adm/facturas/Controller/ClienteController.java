package cl.duoc.ms.adm.facturas.Controller;

import cl.duoc.ms.adm.facturas.dto.ClienteDTO;
import cl.duoc.ms.adm.facturas.model.Cliente;
import cl.duoc.ms.adm.facturas.service.ClienteService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }


    @GetMapping
    public List<ClienteDTO> listar() {
        return clienteService.listarClientes().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ClienteDTO agregar(@RequestBody ClienteDTO clienteDTO) {
        Cliente cliente = toEntity(clienteDTO);
        Cliente guardado = clienteService.guardarCliente(cliente);
        return toDTO(guardado);
    }

    private ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setEmail(cliente.getEmail());
        return dto;
    }

    private Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(dto.getId());
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        return cliente;
    }
}