package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoTipoProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductoTipoProducto> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private ProductoTipoProductoDAO dao;

    @Test
    @DisplayName("findByProducto: Retorna lista vacía si el producto o su ID son nulos")
    void testFindByProductoNulo() {
        assertTrue(dao.findByProducto(null, 0, 10).isEmpty());

        Producto pSinId = new Producto();
        assertTrue(dao.findByProducto(pSinId, 0, 10).isEmpty());
    }

    @Test
    @DisplayName("findByProducto: Lanza IllegalArgumentException ante rangos de paginación inválidos")
    void testFindByProductoPaginacionInvalida() {
        Producto p = new Producto();
        p.setIdProducto(UUID.randomUUID());

        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(p, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(p, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(p, 0, -5));
    }

    @Test
    @DisplayName("findByProducto: Ejecuta la NamedQuery paginada correctamente")
    void testFindByProductoExitoso() {
        Producto p = new Producto();
        p.setIdProducto(UUID.randomUUID());

        when(em.createNamedQuery("ProductoTipoProducto.findByProducto", ProductoTipoProducto.class)).thenReturn(queryList);
        when(queryList.setParameter("producto", p)).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new ProductoTipoProducto()));

        List<ProductoTipoProducto> res = dao.findByProducto(p, 0, 10);

        assertFalse(res.isEmpty());
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countByProducto: Retorna 0L si el producto o su ID son nulos")
    void testCountByProductoNulo() {
        assertEquals(0L, dao.countByProducto(null));

        Producto pSinId = new Producto();
        assertEquals(0L, dao.countByProducto(pSinId));
    }

    @Test
    @DisplayName("countByProducto: Ejecuta el conteo correctamente")
    void testCountByProductoExitoso() {
        Producto p = new Producto();
        p.setIdProducto(UUID.randomUUID());

        when(em.createNamedQuery("ProductoTipoProducto.countByProducto", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("producto", p)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(5L);

        assertEquals(5L, dao.countByProducto(p));
    }

    @Test
    @DisplayName("ordenar: Criterios por fechaCreacion descendente e idProductoTipoProducto ascendente")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenar() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<ProductoTipoProducto> root = mock(Root.class);
        Path pathFecha = mock(Path.class);
        Path pathId = mock(Path.class);
        Order orderFecha = mock(Order.class);
        Order orderId = mock(Order.class);

        when(root.get("fechaCreacion")).thenReturn(pathFecha);
        when(root.get("idProductoTipoProducto")).thenReturn(pathId);
        when(cb.desc(pathFecha)).thenReturn(orderFecha);
        when(cb.asc(pathId)).thenReturn(orderId);

        List<Order> res = dao.ordenar(cb, root);

        assertEquals(2, res.size());
        assertEquals(orderFecha, res.get(0));
        assertEquals(orderId, res.get(1));
    }
}