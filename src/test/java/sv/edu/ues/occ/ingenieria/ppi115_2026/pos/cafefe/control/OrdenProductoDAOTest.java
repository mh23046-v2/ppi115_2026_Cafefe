package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdenProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<OrdenProducto> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @Mock
    private TypedQuery<BigDecimal> querySum;

    @InjectMocks
    private OrdenProductoDAO dao;

    @Test
    @DisplayName("findByOrden: Lanza excepción si la orden es nula o carece de ID")
    void testFindByOrdenValidaciones() {
        Orden oSinId = new Orden();
        Orden oValida = new Orden(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> dao.findByOrden(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByOrden(oSinId, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByOrden(oValida, -1, 10));
    }

    @Test
    @DisplayName("findByOrden: Retorna la lista paginada de detalles")
    void testFindByOrdenExitoso() {
        Orden orden = new Orden(UUID.randomUUID());

        when(em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)).thenReturn(queryList);
        when(queryList.setParameter("orden", orden)).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new OrdenProducto()));

        List<OrdenProducto> resultado = dao.findByOrden(orden, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countByOrden: Retorna 0 con orden inválida o devuelve el conteo real")
    void testCountByOrden() {
        assertEquals(0, dao.countByOrden(null));

        Orden orden = new Orden(UUID.randomUUID());
        when(em.createNamedQuery("OrdenProducto.countByOrden", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("orden", orden)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(3L);

        assertEquals(3, dao.countByOrden(orden));
    }

    @Test
    @DisplayName("totalByOrden: Retorna BigDecimal.ZERO si no hay suma o la orden es nula")
    void testTotalByOrden() {
        assertEquals(BigDecimal.ZERO, dao.totalByOrden(null));

        Orden orden = new Orden(UUID.randomUUID());
        when(em.createNamedQuery("OrdenProducto.sumPrecioByOrden", BigDecimal.class)).thenReturn(querySum);
        when(querySum.setParameter("orden", orden)).thenReturn(querySum);
        when(querySum.getSingleResult()).thenReturn(new BigDecimal("15.50"));

        assertEquals(new BigDecimal("15.50"), dao.totalByOrden(orden));
    }
}