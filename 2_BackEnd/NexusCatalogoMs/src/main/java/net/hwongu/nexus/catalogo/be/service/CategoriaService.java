package net.hwongu.nexus.catalogo.be.service;

import lombok.RequiredArgsConstructor;
import net.hwongu.nexus.catalogo.be.dto.CategoriaDTO;
import net.hwongu.nexus.catalogo.be.entity.Categoria;
import net.hwongu.nexus.catalogo.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.catalogo.be.repository.CategoriaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Capa de servicio para la gestion de categorias.
 *
 * <p>La anotacion {@link Service} marca esta clase como un componente de la
 * capa de negocio. Aqui se centralizan las validaciones simples, las
 * conversiones entre DTO y entidad, y la coordinacion de operaciones
 * transaccionales.</p>
 *
 * @author Henry Wong
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Lista todas las categorias en el orden del monolito original.
     *
     * @return categorias convertidas a DTO.
     */
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAllByOrderByIdCategoriaAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    /**
     * Busca una categoria por ID.
     *
     * @param id identificador a buscar.
     * @return DTO de la categoria encontrada.
     */
    @Transactional(readOnly = true)
    public CategoriaDTO buscarCategoriaPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada."));
        return convertirADTO(categoria);
    }

    /**
     * Registra una nueva categoria.
     *
     * @param categoriaDTO datos recibidos desde la API.
     * @return categoria creada con su ID generado.
     */
    @Transactional
    public CategoriaDTO registrarCategoria(CategoriaDTO categoriaDTO) {
        Categoria categoria = convertirAEntidad(categoriaDTO);
        categoria.setIdCategoria(null);
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return convertirADTO(categoriaGuardada);
    }

    /**
     * Actualiza una categoria existente.
     *
     * <p>Antes de guardar, se verifica que el registro exista para no responder
     * exitosamente sobre un recurso inexistente.</p>
     *
     * @param id identificador de la categoria a modificar.
     * @param categoriaDTO nuevos datos de la categoria.
     */
    @Transactional
    public void actualizarCategoria(Integer id, CategoriaDTO categoriaDTO) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada."));

        categoriaExistente.setNombre(categoriaDTO.getNombre());
        categoriaExistente.setDescripcion(categoriaDTO.getDescripcion());

        categoriaRepository.save(categoriaExistente);
    }

    /**
     * Elimina una categoria si existe y si no tiene dependencias que violen la
     * integridad referencial.
     *
     * @param id identificador de la categoria.
     */
    @Transactional
    public void eliminarCategoria(Integer id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Categoria no encontrada.");
        }

        try {
            categoriaRepository.deleteById(id);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException(
                    "No se puede eliminar la categoria porque tiene productos asociados.",
                    e
            );
        }
    }

    /**
     * Convierte una entidad a DTO.
     *
     * @param categoria entidad JPA.
     * @return DTO listo para respuesta.
     */
    private CategoriaDTO convertirADTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }

    /**
     * Convierte un DTO a entidad.
     *
     * @param categoriaDTO datos recibidos desde cliente o capas superiores.
     * @return entidad lista para persistir.
     */
    private Categoria convertirAEntidad(CategoriaDTO categoriaDTO) {
        return Categoria.builder()
                .idCategoria(categoriaDTO.getIdCategoria())
                .nombre(categoriaDTO.getNombre())
                .descripcion(categoriaDTO.getDescripcion())
                .build();
    }
}
