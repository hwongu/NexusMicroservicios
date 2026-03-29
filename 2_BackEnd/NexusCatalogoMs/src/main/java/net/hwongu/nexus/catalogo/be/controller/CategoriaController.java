package net.hwongu.nexus.catalogo.be.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.catalogo.be.dto.CategoriaDTO;
import net.hwongu.nexus.catalogo.be.service.CategoriaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * Expone endpoints REST para categorias.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarCategoriaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.buscarCategoriaPorId(id));
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> registrarCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoriaCreada = categoriaService.registrarCategoria(categoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDTO categoriaDTO
    ) {
        categoriaService.actualizarCategoria(id, categoriaDTO);
        return ResponseEntity.ok(Map.of("message", "Categoria actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
