package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoCaracteristicaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductoCaracteristica> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private ProductoCaracteristicaDAO dao;

    @Test
    @DisplayName("findByProducto: Lanza IllegalArgumentException con argumentos inválidos")
    void testFindByProductoArgumentosInvalidos() {
        Producto productoValido = new Producto(UUID.randomUUID());
        Producto productoSinId = new Producto();

        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(productoSinId, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(productoValido, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(productoValido, 0, 0));
    }

    @Test
    @DisplayName("findByProducto: Ejecuta la consulta paginada exitosamente")
    void testFindByProductoExitoso() {
        Producto producto = new Producto(UUID.randomUUID());

        when(em.createNamedQuery("ProductoCaracteristica.findByProducto", ProductoCaracteristica.class)).thenReturn(queryList);
        when(queryList.setParameter("producto", producto)).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new ProductoCaracteristica()));

        List<ProductoCaracteristica> resultado = dao.findByProducto(producto, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setParameter("producto", producto);
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countByProducto: Retorna el conteo total por producto")
    void testCountByProductoExitoso() {
        Producto producto = new Producto(UUID.randomUUID());

        when(em.createNamedQuery("ProductoCaracteristica.countByProducto", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("producto", producto)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(3L);

        int total = dao.countByProducto(producto);

        assertEquals(3, total);
        verify(queryCount).setParameter("producto", producto);
    }

    @Test
    @DisplayName("findByProducto: Propaga AccesoDatosException envolviendo error de BD")
    void testFindByProductoLanzaExcepcionBD() {
        Producto producto = new Producto(UUID.randomUUID());

        when(em.createNamedQuery("ProductoCaracteristica.findByProducto", ProductoCaracteristica.class)).thenReturn(queryList);
        when(queryList.setParameter("producto", producto)).thenReturn(queryList);
        when(queryList.setFirstResult(anyInt())).thenReturn(queryList);
        when(queryList.setMaxResults(anyInt())).thenReturn(queryList);

        PersistenceException causa = new PersistenceException("Error en base de datos");
        when(queryList.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, ()
                -> dao.findByProducto(producto, 0, 10)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("ordenar: Retorna criterio de orden por ID")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenarRetornaListaCriterios() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<ProductoCaracteristica> root = mock(Root.class);
        Path pathId = mock(Path.class);
        Order orderId = mock(Order.class);

        when(root.get("idProductoCaracteristica")).thenReturn(pathId);
        when(cb.asc(pathId)).thenReturn(orderId);

        List<Order> resultado = dao.ordenar(cb, root);

        assertEquals(1, resultado.size());
        assertEquals(orderId, resultado.get(0));
    }
}
