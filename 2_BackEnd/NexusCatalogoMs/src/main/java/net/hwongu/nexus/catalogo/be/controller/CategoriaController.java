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
 * Controlador REST para exponer operaciones HTTP sobre categorias.
 *
 * <p>{@link RestController} combina {@code @Controller} y
 * {@code @ResponseBody}, por lo que cada metodo devuelve directamente JSON.
 * {@link RequestMapping} define la ruta base del recurso siguiendo una
 * convencion REST clara y coherente para el microservicio.</p>
 *
 * @author Henry Wong
 */
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Devuelve todas las categorias.
     *
     * @return lista de categorias en formato JSON.
     */
    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarCategorias() {
        return ResponseEntity.ok(categoriaService.listarCategorias());
    }

    /**
     * Busca una categoria por ID.
     *
     * @param id identificador solicitado en la URL.
     * @return categoria encontrada.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarCategoriaPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(categoriaService.buscarCategoriaPorId(id));
    }

    /**
     * Registra una nueva categoria.
     *
     * <p>{@link Valid} obliga a ejecutar Jakarta Validation sobre el DTO antes
     * de entrar a la logica del servicio.</p>
     *
     * @param categoriaDTO datos enviados por el cliente.
     * @return categoria creada con codigo HTTP 201.
     */
    @PostMapping
    public ResponseEntity<CategoriaDTO> registrarCategoria(@Valid @RequestBody CategoriaDTO categoriaDTO) {
        CategoriaDTO categoriaCreada = categoriaService.registrarCategoria(categoriaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaCreada);
    }

    /**
     * Actualiza una categoria existente.
     *
     * @param id identificador del recurso.
     * @param categoriaDTO nuevos datos de la categoria.
     * @return mensaje de confirmacion.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaDTO categoriaDTO
    ) {
        categoriaService.actualizarCategoria(id, categoriaDTO);
        return ResponseEntity.ok(Map.of("message", "Categoria actualizada exitosamente"));
    }

    /**
     * Elimina una categoria por su ID.
     *
     * @param id identificador de la categoria.
     * @return respuesta vacia con 204.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }
}
