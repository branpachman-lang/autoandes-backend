package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenResponseDTO;
import pe.edu.upeu.autoandes.enums.EstadoOrden;
import pe.edu.upeu.autoandes.service.service.OrdenService;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {

    private final OrdenService ordenes;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrdenResponseDTO> registrar(@Valid @RequestBody OrdenRequestDTO r, Authentication usuario) {
        var respuesta = ordenes.registrar(r, usuario.getName());
        return ResponseEntity.created(URI.create("/api/v1/ordenes/" + respuesta.getId())).body(respuesta);
    }
    @GetMapping
    public List<OrdenResponseDTO> listar(@RequestParam(required = false) String placa,
    @RequestParam(required = false) EstadoOrden estado,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ordenes.listar(placa, estado, desde, hasta);
    }
    @GetMapping("/{id}")
    public OrdenResponseDTO buscar(@PathVariable @Positive Long id) { return ordenes.buscar(id); }
    @PatchMapping("/{id}/anular")
    public OrdenResponseDTO anular(@PathVariable @Positive Long id, @Valid @RequestBody AnularOrdenRequestDTO r) {
        return ordenes.anular(id, r);
    }

}
