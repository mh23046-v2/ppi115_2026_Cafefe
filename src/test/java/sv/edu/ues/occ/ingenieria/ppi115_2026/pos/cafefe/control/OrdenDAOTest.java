package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class OrdenDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Orden> typedQuery;

    @Mock
    private TypedQuery<EmpleadoRol> queryEmpleadoRol;

    @Mock
    private Query queryBorrado;

    @InjectMocks
    private OrdenDAO ordenDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertEquals(em, ordenDAO.getEntityManager());
    }

    @Test
    @DisplayName("findRangeOrdenado usa la NamedQuery ordenada por fecha y pagina")
    void testFindRangeOrdenado() {
        List<Orden> esperadas = Collections.singletonList(new Orden(UUID.randomUUID()));
        when(em.createNamedQuery("Orden.findAllOrdenado", Orden.class)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(10)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperadas);

        assertEquals(esperadas, ordenDAO.findRangeOrdenado(10, 5));
    }

    @Test
    @DisplayName("findEmpleadoRolActivos filtra asignación, empleado y rol activos")
    void testFindEmpleadoRolActivos() {
        List<EmpleadoRol> esperados = Collections.singletonList(new EmpleadoRol(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(EmpleadoRol.class))).thenReturn(queryEmpleadoRol);
        when(queryEmpleadoRol.getResultList()).thenReturn(esperados);

        assertEquals(esperados, ordenDAO.findEmpleadoRolActivos());
        verify(em).createQuery(contains("er.activo = true AND e.activo = true AND r.activo = true"), eq(EmpleadoRol.class));
    }

    @Test
    @DisplayName("delete borra primero los productos de la orden y luego la orden")
    void testDeleteBorraProductosPrimero() {
        Orden orden = new Orden(UUID.randomUUID());
        when(em.createQuery(contains("DELETE FROM OrdenProducto"))).thenReturn(queryBorrado);
        when(queryBorrado.setParameter("idOrden", orden.getIdOrden())).thenReturn(queryBorrado);
        when(em.contains(orden)).thenReturn(true);

        ordenDAO.delete(orden);

        InOrder orden1 = inOrder(queryBorrado, em);
        orden1.verify(queryBorrado).executeUpdate();
        orden1.verify(em).remove(orden);
    }

    @Test
    @DisplayName("delete con null lanza error y no toca la base de datos")
    void testDeleteNulo() {
        assertThrows(IllegalArgumentException.class, () -> ordenDAO.delete(null));

        verifyNoInteractions(em);
    }
}
