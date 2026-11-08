package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Orden> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @Mock
    private Query queryDelete;

    @InjectMocks
    private OrdenDAO dao;

    @Test
    @DisplayName("findRange: Lanza excepción ante paginación inválida")
    void testFindRangePaginacionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(0, 0));
    }

    @Test
    @DisplayName("findRange: Retorna la lista de órdenes paginada")
    void testFindRangeExitoso() {
        when(em.createNamedQuery("Orden.findAll", Orden.class)).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new Orden()));

        List<Orden> resultado = dao.findRange(0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countAll: Retorna el total de órdenes")
    void testCountAll() {
        when(em.createNamedQuery("Orden.countAll", Long.class)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(12L);

        assertEquals(12, dao.countAll());
    }

    @Test
    @DisplayName("delete: Elimina el detalle OrdenProducto y luego remueve la orden")
    void testDelete() {
        Orden orden = new Orden();
        orden.setIdOrden(UUID.randomUUID());

        when(em.createQuery("DELETE FROM OrdenProducto op WHERE op.idOrden.idOrden = :idOrden")).thenReturn(queryDelete);
        when(queryDelete.setParameter("idOrden", orden.getIdOrden())).thenReturn(queryDelete);
        when(queryDelete.executeUpdate()).thenReturn(1);
        when(em.merge(orden)).thenReturn(orden);

        dao.delete(orden);

        verify(queryDelete).executeUpdate();
        verify(em).remove(orden);
    }

    @Test
    @DisplayName("ordenar: Configura el criterio por fechaCreacion DESC")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenar() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<Orden> root = mock(Root.class);
        Path pathFecha = mock(Path.class);
        Order orderFecha = mock(Order.class);

        when(root.get("fechaCreacion")).thenReturn(pathFecha);
        when(cb.desc(pathFecha)).thenReturn(orderFecha);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(1, resultado.size());
        assertEquals(orderFecha, resultado.get(0));
    }
}