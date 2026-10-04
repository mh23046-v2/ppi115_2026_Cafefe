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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class ProductoCaracteristicaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<ProductoCaracteristica> typedQuery;

    @InjectMocks
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(productoCaracteristicaDAO.getEntityManager());
        assertEquals(em, productoCaracteristicaDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByProducto retorna lista vacía si el producto o su ID son nulos")
    void testFindByProductoNull() {
        // Caso 1: Objeto Producto nulo
        List<ProductoCaracteristica> res1 = productoCaracteristicaDAO.findByProducto(null);
        assertNotNull(res1);
        assertTrue(res1.isEmpty());

        // Caso 2: Objeto Producto con idProducto nulo
        Producto pSinId = new Producto();
        List<ProductoCaracteristica> res2 = productoCaracteristicaDAO.findByProducto(pSinId);
        assertNotNull(res2);
        assertTrue(res2.isEmpty());

        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByProducto ejecuta la consulta exitosamente cuando el producto es válido")
    void testFindByProductoExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        List<ProductoCaracteristica> esperados = List.of(new ProductoCaracteristica());
        String expectedQuery = "SELECT pc FROM ProductoCaracteristica pc WHERE pc.idProducto = :producto";

        when(em.createQuery(expectedQuery, ProductoCaracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("producto", producto)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<ProductoCaracteristica> resultado = productoCaracteristicaDAO.findByProducto(producto);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createQuery(expectedQuery, ProductoCaracteristica.class);
        verify(typedQuery).setParameter("producto", producto);
    }

    @Test
    @DisplayName("findByValor ejecuta NamedQuery y devuelve la lista correspondiente")
    void testFindByValor() {
        String valor = "Grande 16oz";
        List<ProductoCaracteristica> esperados = Collections.singletonList(new ProductoCaracteristica());

        when(em.createNamedQuery("ProductoCaracteristica.findByValor", ProductoCaracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("valor", valor)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<ProductoCaracteristica> resultado = productoCaracteristicaDAO.findByValor(valor);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("ProductoCaracteristica.findByValor", ProductoCaracteristica.class);
        verify(typedQuery).setParameter("valor", valor);
    }
}