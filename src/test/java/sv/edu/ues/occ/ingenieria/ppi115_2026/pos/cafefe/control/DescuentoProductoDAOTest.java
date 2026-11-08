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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DescuentoProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<DescuentoProducto> queryList;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private DescuentoProductoDAO dao;

    @Test
    @DisplayName("findByProducto: Lanza IllegalArgumentException si producto o parámetros de paginación son inválidos")
    void testFindByProductoArgumentosInvalidos() {
        Producto productoValido = new Producto(UUID.randomUUID());
        Producto productoSinId = new Producto();

        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(productoSinId, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(productoValido, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByProducto(productoValido, 0, 0));
    }

    @Test
    @DisplayName("findByProducto: Ejecuta la consulta paginada correctamente")
    void testFindByProductoExitoso() {
        Producto producto = new Producto(UUID.randomUUID());

        when(em.createNamedQuery("DescuentoProducto.findByProducto", DescuentoProducto.class)).thenReturn(queryList);
        when(queryList.setParameter("producto", producto)).thenReturn(queryList);
        when(queryList.setFirstResult(0)).thenReturn(queryList);
        when(queryList.setMaxResults(10)).thenReturn(queryList);
        when(queryList.getResultList()).thenReturn(List.of(new DescuentoProducto()));

        List<DescuentoProducto> resultado = dao.findByProducto(producto, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryList).setParameter("producto", producto);
        verify(queryList).setFirstResult(0);
        verify(queryList).setMaxResults(10);
    }

    @Test
    @DisplayName("countByProducto: Retorna el total de registros por producto")
    void testCountByProductoExitoso() {
        Producto producto = new Producto(UUID.randomUUID());

        when(em.createNamedQuery("DescuentoProducto.countByProducto", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("producto", producto)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(5L);

        int total = dao.countByProducto(producto);

        assertEquals(5, total);
        verify(queryCount).setParameter("producto", producto);
    }

    @Test
    @DisplayName("findByProducto: Propaga AccesoDatosException envolviendo PersistenceException")
    void testFindByProductoLanzaExcepcionBD() {
        Producto producto = new Producto(UUID.randomUUID());

        when(em.createNamedQuery("DescuentoProducto.findByProducto", DescuentoProducto.class)).thenReturn(queryList);
        when(queryList.setParameter("producto", producto)).thenReturn(queryList);
        when(queryList.setFirstResult(anyInt())).thenReturn(queryList);
        when(queryList.setMaxResults(anyInt())).thenReturn(queryList);

        PersistenceException causa = new PersistenceException("Error al consultar la BD");
        when(queryList.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () -> 
            dao.findByProducto(producto, 0, 10)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("ordenar: Verifica la lista de criterios de ordenamiento de DescuentoProducto")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void testOrdenarRetornaListaCriterios() {
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Root<DescuentoProducto> root = mock(Root.class);

        List<Order> resultado = dao.ordenar(cb, root);

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
    }
}