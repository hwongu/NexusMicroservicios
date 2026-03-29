package net.hwongu.nexus.ingreso.be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.ingreso.be.dto.ActualizarEstadoIngresoDTO;
import net.hwongu.nexus.ingreso.be.dto.DetalleIngresoDTO;
import net.hwongu.nexus.ingreso.be.dto.IngresoDTO;
import net.hwongu.nexus.ingreso.be.dto.RegistrarIngresoRequestDTO;
import net.hwongu.nexus.ingreso.be.service.IngresoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Expone endpoints REST para ingresos.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    @GetMapping
    public ResponseEntity<List<IngresoDTO>> listarIngresos() {
        return ResponseEntity.ok(ingresoService.listarIngresos());
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<DetalleIngresoDTO>> buscarDetallesPorIngreso(@PathVariable Integer id) {
        return ResponseEntity.ok(ingresoService.buscarDetallesPorIngreso(id));
    }

    @PostMapping
    public ResponseEntity<IngresoDTO> registrarIngreso(@Valid @RequestBody RegistrarIngresoRequestDTO requestDTO) {
        IngresoDTO ingresoCreado = ingresoService.registrarIngresoCompleto(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ingresoCreado);
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Map<String, String>> anularIngreso(@PathVariable Integer id) {
        ingresoService.anularIngreso(id);
        return ResponseEntity.ok(Map.of("message", "Ingreso anulado exitosamente"));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, String>> actualizarEstadoIngreso(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarEstadoIngresoDTO requestDTO
    ) {
        ingresoService.actualizarEstadoIngreso(id, requestDTO.getEstado());
        return ResponseEntity.ok(Map.of("message", "Estado del ingreso actualizado correctamente"));
    }
}
