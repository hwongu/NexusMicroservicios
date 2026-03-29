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
 * Coordina la logica de negocio de categorias.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCategorias() {
        return categoriaRepository.findAllByOrderByIdCategoriaAsc()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO buscarCategoriaPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada."));
        return convertirADTO(categoria);
    }

    @Transactional
    public CategoriaDTO registrarCategoria(CategoriaDTO categoriaDTO) {
        Categoria categoria = convertirAEntidad(categoriaDTO);
        categoria.setIdCategoria(null);
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return convertirADTO(categoriaGuardada);
    }

    @Transactional
    public void actualizarCategoria(Integer id, CategoriaDTO categoriaDTO) {
        Categoria categoriaExistente = categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoria no encontrada."));

        categoriaExistente.setNombre(categoriaDTO.getNombre());
        categoriaExistente.setDescripcion(categoriaDTO.getDescripcion());

        categoriaRepository.save(categoriaExistente);
    }

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

    private CategoriaDTO convertirADTO(Categoria categoria) {
        return CategoriaDTO.builder()
                .idCategoria(categoria.getIdCategoria())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }

    private Categoria convertirAEntidad(CategoriaDTO categoriaDTO) {
        return Categoria.builder()
                .idCategoria(categoriaDTO.getIdCategoria())
                .nombre(categoriaDTO.getNombre())
                .descripcion(categoriaDTO.getDescripcion())
                .build();
    }
}
