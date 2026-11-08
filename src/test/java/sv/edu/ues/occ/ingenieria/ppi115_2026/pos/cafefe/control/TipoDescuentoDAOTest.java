package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoDescuentoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoDescuento> query;

    @InjectMocks
    private TipoDescuentoDAO dao;

    @Test
    @DisplayName("buscarPorNombre: Lanza IllegalArgumentException si el límite máximo es inválido")
    void testBuscarPorNombreLanzaExcepcionMaxInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombre("texto", 0));
        assertThrows(IllegalArgumentException.class, () -> dao.buscarPorNombre("texto", -5));
    }

    @Test
    @DisplayName("buscarPorNombre: Limpia el texto ingresado y ejecuta la consulta correctamente")
    void testBuscarPorNombreTextoValidoEjecutaQuery() {
        when(em.createNamedQuery("TipoDescuento.buscarPorNombre", TipoDescuento.class)).thenReturn(query);
        when(query.setParameter("texto", "%promocion%")).thenReturn(query);
        when(query.setMaxResults(10)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new TipoDescuento()));

        List<TipoDescuento> resultado = dao.buscarPorNombre("  ProMoCiOn ", 10);

        assertFalse(resultado.isEmpty(), "La lista de resultados no debe estar vacía");
        verify(query).setParameter("texto", "%promocion%");
        verify(query).setMaxResults(10);
    }

    @Test
    @DisplayName("buscarPorNombre: Construye el patrón vacío '%%' cuando el texto es null")
    void testBuscarPorNombreTextoNullEjecutaQueryConPatronVacio() {
        when(em.createNamedQuery("TipoDescuento.buscarPorNombre", TipoDescuento.class)).thenReturn(query);
        when(query.setParameter("texto", "%%")).thenReturn(query);
        when(query.setMaxResults(5)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        List<TipoDescuento> resultado = dao.buscarPorNombre(null, 5);

        assertTrue(resultado.isEmpty(), "La lista de resultados debe estar vacía por defecto");
        verify(query).setParameter("texto", "%%");
    }

    @Test
    @DisplayName("buscarPorNombre: Propaga AccesoDatosException conservando la causa de persistencia original")
    void testBuscarPorNombreLanzaAccesoDatosException() {
        when(em.createNamedQuery("TipoDescuento.buscarPorNombre", TipoDescuento.class)).thenReturn(query);
        when(query.setParameter(anyString(), anyString())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);

        PersistenceException causaOriginal = new PersistenceException("Fallo en BD");
        when(query.getResultList()).thenThrow(causaOriginal);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () -> dao.buscarPorNombre("test", 5));
        assertSame(causaOriginal, ex.getCause(), "Debe conservar la causa original de persistencia");
    }

    @Test
    @DisplayName("ordenar: Retorna la lista de criterios de ordenamiento por nombre e ID")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenarRetornaListaCriterios() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<TipoDescuento> root = mock(Root.class);

        Path pathNombre = mock(Path.class);
        Path pathId = mock(Path.class);
        Order orderNombre = mock(Order.class);
        Order orderId = mock(Order.class);

        when(root.get("nombre")).thenReturn(pathNombre);
        when(root.get("idTipoDescuento")).thenReturn(pathId);
        when(cb.asc(pathNombre)).thenReturn(orderNombre);
        when(cb.asc(pathId)).thenReturn(orderId);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(2, resultado.size());
        assertEquals(orderNombre, resultado.get(0));
        assertEquals(orderId, resultado.get(1));
    }
}
