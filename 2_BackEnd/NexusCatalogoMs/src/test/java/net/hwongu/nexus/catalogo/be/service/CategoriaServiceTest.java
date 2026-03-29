package net.hwongu.nexus.catalogo.be.service;

import net.hwongu.nexus.catalogo.be.dto.CategoriaDTO;
import net.hwongu.nexus.catalogo.be.entity.Categoria;
import net.hwongu.nexus.catalogo.be.exception.RecursoNoEncontradoException;
import net.hwongu.nexus.catalogo.be.repository.CategoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el servicio de categorias.
 *
 * @author Henry Wong
 * GitHub @hwongu
 * https://github.com/hwongu
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    void listarCategorias_debeRetornarCategoriasConvertidasADto() {
        Categoria categoria1 = crearCategoria(1, "Laptops", "Equipos portatiles");
        Categoria categoria2 = crearCategoria(2, "Monitores", "Pantallas");
        when(categoriaRepository.findAllByOrderByIdCategoriaAsc()).thenReturn(List.of(categoria1, categoria2));

        List<CategoriaDTO> resultado = categoriaService.listarCategorias();

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getIdCategoria());
        assertEquals("Laptops", resultado.get(0).getNombre());
        assertEquals(2, resultado.get(1).getIdCategoria());
        assertEquals("Monitores", resultado.get(1).getNombre());
        verify(categoriaRepository).findAllByOrderByIdCategoriaAsc();
    }

    @Test
    void listarCategorias_debeRetornarListaVaciaCuandoNoHayRegistros() {
        when(categoriaRepository.findAllByOrderByIdCategoriaAsc()).thenReturn(List.of());

        List<CategoriaDTO> resultado = categoriaService.listarCategorias();

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
        verify(categoriaRepository).findAllByOrderByIdCategoriaAsc();
    }

    @Test
    void buscarCategoriaPorId_debeRetornarCategoriaCuandoExiste() {
        Categoria categoria = crearCategoria(1, "Laptops", "Equipos portatiles");
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(categoria));

        CategoriaDTO resultado = categoriaService.buscarCategoriaPorId(1);

        assertEquals(1, resultado.getIdCategoria());
        assertEquals("Laptops", resultado.getNombre());
        assertEquals("Equipos portatiles", resultado.getDescripcion());
        verify(categoriaRepository).findById(1);
    }

    @Test
    void buscarCategoriaPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.buscarCategoriaPorId(99)
        );

        assertEquals("Categoria no encontrada.", exception.getMessage());
        verify(categoriaRepository).findById(99);
    }

    @Test
    void registrarCategoria_debeGuardarCategoriaConIdNuloYRetornarDto() {
        CategoriaDTO request = CategoriaDTO.builder()
                .idCategoria(88)
                .nombre("Perifericos")
                .descripcion("Mouse y teclados")
                .build();
        Categoria categoriaGuardada = crearCategoria(5, "Perifericos", "Mouse y teclados");
        when(categoriaRepository.save(org.mockito.ArgumentMatchers.any(Categoria.class))).thenReturn(categoriaGuardada);

        CategoriaDTO resultado = categoriaService.registrarCategoria(request);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        Categoria categoriaEnviadaAGuardar = captor.getValue();

        assertNull(categoriaEnviadaAGuardar.getIdCategoria());
        assertEquals("Perifericos", categoriaEnviadaAGuardar.getNombre());
        assertEquals("Mouse y teclados", categoriaEnviadaAGuardar.getDescripcion());
        assertEquals(5, resultado.getIdCategoria());
        assertEquals("Perifericos", resultado.getNombre());
    }

    @Test
    void actualizarCategoria_debeActualizarDatosCuandoExiste() {
        Categoria categoriaExistente = crearCategoria(3, "Monitores", "Pantallas");
        CategoriaDTO request = CategoriaDTO.builder()
                .nombre("Monitores Gamer")
                .descripcion("Pantallas de alta frecuencia")
                .build();
        when(categoriaRepository.findById(3)).thenReturn(Optional.of(categoriaExistente));

        categoriaService.actualizarCategoria(3, request);

        verify(categoriaRepository).findById(3);
        verify(categoriaRepository).save(categoriaExistente);
        assertEquals("Monitores Gamer", categoriaExistente.getNombre());
        assertEquals("Pantallas de alta frecuencia", categoriaExistente.getDescripcion());
    }

    @Test
    void actualizarCategoria_debeLanzarExcepcionCuandoNoExiste() {
        CategoriaDTO request = CategoriaDTO.builder()
                .nombre("Monitores Gamer")
                .descripcion("Pantallas de alta frecuencia")
                .build();
        when(categoriaRepository.findById(7)).thenReturn(Optional.empty());

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.actualizarCategoria(7, request)
        );

        assertEquals("Categoria no encontrada.", exception.getMessage());
        verify(categoriaRepository).findById(7);
        verify(categoriaRepository, never()).save(org.mockito.ArgumentMatchers.any(Categoria.class));
    }

    @Test
    void eliminarCategoria_debeEliminarYHacerFlushCuandoExiste() {
        when(categoriaRepository.existsById(4)).thenReturn(true);

        categoriaService.eliminarCategoria(4);

        verify(categoriaRepository).existsById(4);
        verify(categoriaRepository).deleteById(4);
        verify(categoriaRepository).flush();
    }

    @Test
    void eliminarCategoria_debeLanzarExcepcionCuandoNoExiste() {
        when(categoriaRepository.existsById(10)).thenReturn(false);

        RecursoNoEncontradoException exception = assertThrows(
                RecursoNoEncontradoException.class,
                () -> categoriaService.eliminarCategoria(10)
        );

        assertEquals("Categoria no encontrada.", exception.getMessage());
        verify(categoriaRepository).existsById(10);
        verify(categoriaRepository, never()).deleteById(10);
        verify(categoriaRepository, never()).flush();
    }

    @Test
    void eliminarCategoria_debeTraducirErrorDeIntegridadReferencial() {
        when(categoriaRepository.existsById(4)).thenReturn(true);
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk"))
                .when(categoriaRepository).flush();

        DataIntegrityViolationException exception = assertThrows(
                DataIntegrityViolationException.class,
                () -> categoriaService.eliminarCategoria(4)
        );

        assertEquals(
                "No se puede eliminar la categoria porque tiene productos asociados.",
                exception.getMessage()
        );
        verify(categoriaRepository).deleteById(4);
        verify(categoriaRepository).flush();
    }

    private Categoria crearCategoria(Integer id, String nombre, String descripcion) {
        return Categoria.builder()
                .idCategoria(id)
                .nombre(nombre)
                .descripcion(descripcion)
                .build();
    }
}
