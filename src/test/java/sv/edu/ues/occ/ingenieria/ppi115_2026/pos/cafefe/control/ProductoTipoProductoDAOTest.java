package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.Date;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoTipoProducto;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class ProductoTipoProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductoTipoProducto> typedQuery;

    @InjectMocks
    private ProductoTipoProductoDAO productoTipoProductoDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(productoTipoProductoDAO.getEntityManager());
        assertEquals(em, productoTipoProductoDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByProducto retorna lista vacía si el producto o su ID son nulos")
    void testFindByProductoNull() {
        // Caso 1: Objeto Producto nulo
        List<ProductoTipoProducto> res1 = productoTipoProductoDAO.findByProducto(null);
        assertNotNull(res1);
        assertTrue(res1.isEmpty());

        // Caso 2: Objeto Producto con idProducto nulo
        Producto pSinId = new Producto();
        List<ProductoTipoProducto> res2 = productoTipoProductoDAO.findByProducto(pSinId);
        assertNotNull(res2);
        assertTrue(res2.isEmpty());

        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByProducto ejecuta la consulta exitosamente cuando el producto es válido")
    void testFindByProductoExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        List<ProductoTipoProducto> esperados = List.of(new ProductoTipoProducto());
        String expectedQuery = "SELECT ptp FROM ProductoTipoProducto ptp WHERE ptp.producto = :producto";

        when(em.createQuery(expectedQuery, ProductoTipoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("producto", producto)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<ProductoTipoProducto> resultado = productoTipoProductoDAO.findByProducto(producto);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createQuery(expectedQuery, ProductoTipoProducto.class);
        verify(typedQuery).setParameter("producto", producto);
    }

    @Test
    @DisplayName("findByFechaCreacion ejecuta NamedQuery y devuelve la lista")
    void testFindByFechaCreacion() {
        Date fecha = new Date();
        List<ProductoTipoProducto> esperados = Collections.singletonList(new ProductoTipoProducto());

        when(em.createNamedQuery("ProductoTipoProducto.findByFechaCreacion", ProductoTipoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("fechaCreacion", fecha)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<ProductoTipoProducto> resultado = productoTipoProductoDAO.findByFechaCreacion(fecha);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("ProductoTipoProducto.findByFechaCreacion", ProductoTipoProducto.class);
        verify(typedQuery).setParameter("fechaCreacion", fecha);
    }

    @Test
    @DisplayName("findByIdProducto retorna lista vacía si idProducto es nulo")
    void testFindByIdProductoNull() {
        List<ProductoTipoProducto> resultado = productoTipoProductoDAO.findByIdProducto(null);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByIdProducto ejecuta la consulta exitosamente si idProducto no es nulo")
    void testFindByIdProductoExitoso() {
        UUID idProducto = UUID.randomUUID();
        List<ProductoTipoProducto> esperados = List.of(new ProductoTipoProducto());
        String expectedQuery = "SELECT ptp FROM ProductoTipoProducto ptp WHERE ptp.producto.idProducto = :idProducto";

        when(em.createQuery(expectedQuery, ProductoTipoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("idProducto", idProducto)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<ProductoTipoProducto> resultado = productoTipoProductoDAO.findByIdProducto(idProducto);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createQuery(expectedQuery, ProductoTipoProducto.class);
        verify(typedQuery).setParameter("idProducto", idProducto);
    }
}