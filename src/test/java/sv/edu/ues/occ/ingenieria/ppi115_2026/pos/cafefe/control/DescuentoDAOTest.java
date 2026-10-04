package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class DescuentoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Descuento> typedQuery;

    @InjectMocks
    private DescuentoDAO descuentoDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(descuentoDAO.getEntityManager());
        assertEquals(em, descuentoDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo debe retornar la lista correspondiente al filtro")
    void testFindByActivo() {
        Boolean activo = true;
        List<Descuento> esperados = Collections.singletonList(new Descuento());

        when(em.createNamedQuery("Descuento.findByActivo", Descuento.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", activo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Descuento> resultado = descuentoDAO.findByActivo(activo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("Descuento.findByActivo", Descuento.class);
        verify(typedQuery).setParameter("activo", activo);
    }

    @Test
    @DisplayName("findByNombre consulta con el parámetro nombre y devuelve el resultado")
    void testFindByNombre() {
        String nombre = "Descuento Navideño";
        List<Descuento> esperados = Collections.singletonList(new Descuento());

        when(em.createNamedQuery("Descuento.findByNombre", Descuento.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("nombre", nombre)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Descuento> resultado = descuentoDAO.findByNombre(nombre);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("Descuento.findByNombre", Descuento.class);
        verify(typedQuery).setParameter("nombre", nombre);
    }

    @Test
    @DisplayName("findByIdTipoDescuento retorna lista vacía si el parametro es nulo")
    void testFindByIdTipoDescuentoNull() {
        List<Descuento> resultado = descuentoDAO.findByIdTipoDescuento(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByIdTipoDescuento ejecuta query y retorna la lista correspondiente")
    void testFindByIdTipoDescuentoExitoso() {
        TipoDescuento tipo = new TipoDescuento(UUID.randomUUID());
        List<Descuento> esperados = List.of(new Descuento());
        String queryExpected = "SELECT d FROM Descuento d WHERE d.idTipoDescuento = :idTipoDescuento";

        when(em.createQuery(queryExpected, Descuento.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("idTipoDescuento", tipo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Descuento> resultado = descuentoDAO.findByIdTipoDescuento(tipo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createQuery(queryExpected, Descuento.class);
        verify(typedQuery).setParameter("idTipoDescuento", tipo);
    }
}