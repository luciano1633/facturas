package cl.duoc.cdy2204.msrabbitmq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.cdy2204.msrabbitmq.dto.ProductoDTO;
import cl.duoc.cdy2204.msrabbitmq.dto.UsuarioDTO;
import cl.duoc.cdy2204.msrabbitmq.service.ConsumirMensajeService;
import cl.duoc.cdy2204.msrabbitmq.service.ProducirMensajeService;

@RestController
@RequestMapping("/api")
public class MensajeController {

	private final ProducirMensajeService producirMensajeService;
	private final ConsumirMensajeService consumirMensajeService;

	public MensajeController(ProducirMensajeService mensajeService, ConsumirMensajeService consumirMensajeService) {

		this.producirMensajeService = mensajeService;
		this.consumirMensajeService = consumirMensajeService;
	}

	@PostMapping("/mensajes")
	public ResponseEntity<String> enviar(@RequestBody String mensaje) {

		producirMensajeService.enviarMensaje(mensaje);
		return ResponseEntity.ok("Mensaje enviado: " + mensaje);
	}

	@PostMapping("/usuarios")
	public ResponseEntity<String> enviarObjetoUsuario(@RequestBody UsuarioDTO usuario) {

		producirMensajeService.enviarObjeto(usuario);
		return ResponseEntity.ok("Mensaje enviado: " + usuario.toString());
	}

	@PostMapping("/prodductos")
	public ResponseEntity<String> enviarObjetoProducto(@RequestBody ProductoDTO producto) {

		producirMensajeService.enviarObjeto(producto);
		return ResponseEntity.ok("Mensaje enviado: " + producto.toString());
	}

	@GetMapping("/mensaje/ultimo")
	public ResponseEntity<String> obtenerUltimoMensaje() {

		String message = consumirMensajeService.obtenerUltimoMensaje();

		if (null == message) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(message);
		}
	}
}
