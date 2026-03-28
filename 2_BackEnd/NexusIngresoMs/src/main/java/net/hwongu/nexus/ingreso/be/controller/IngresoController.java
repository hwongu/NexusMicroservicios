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
 * Controlador REST del recurso ingresos.
 *
 * <p>Este controlador traslada la logica HTTP del antiguo modulo de ingresos a
 * Spring MVC, manteniendo la capa web simple y delegando la integracion con
 * otros microservicios al servicio.</p>
 *
 * @author Henry Wong
 */
@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    /**
     * Lista todos los ingresos registrados.
     *
     * @return lista de ingresos.
     */
    @GetMapping
    public ResponseEntity<List<IngresoDTO>> listarIngresos() {
        return ResponseEntity.ok(ingresoService.listarIngresos());
    }

    /**
     * Lista los detalles de un ingreso especifico.
     *
     * @param id identificador del ingreso.
     * @return detalles asociados.
     */
    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<DetalleIngresoDTO>> buscarDetallesPorIngreso(@PathVariable Integer id) {
        return ResponseEntity.ok(ingresoService.buscarDetallesPorIngreso(id));
    }

    /**
     * Registra un ingreso completo con cabecera y detalle.
     *
     * @param requestDTO datos del ingreso.
     * @return ingreso creado con HTTP 201.
     */
    @PostMapping
    public ResponseEntity<IngresoDTO> registrarIngreso(@Valid @RequestBody RegistrarIngresoRequestDTO requestDTO) {
        IngresoDTO ingresoCreado = ingresoService.registrarIngresoCompleto(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(ingresoCreado);
    }

    /**
     * Anula un ingreso ya registrado.
     *
     * @param id identificador del ingreso.
     * @return mensaje de confirmacion.
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<Map<String, String>> anularIngreso(@PathVariable Integer id) {
        ingresoService.anularIngreso(id);
        return ResponseEntity.ok(Map.of("message", "Ingreso anulado exitosamente"));
    }

    /**
     * Actualiza manualmente el estado de un ingreso.
     *
     * @param id identificador del ingreso.
     * @param requestDTO nuevo estado solicitado.
     * @return mensaje de confirmacion.
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, String>> actualizarEstadoIngreso(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarEstadoIngresoDTO requestDTO
    ) {
        ingresoService.actualizarEstadoIngreso(id, requestDTO.getEstado());
        return ResponseEntity.ok(Map.of("message", "Estado del ingreso actualizado correctamente"));
    }
}
