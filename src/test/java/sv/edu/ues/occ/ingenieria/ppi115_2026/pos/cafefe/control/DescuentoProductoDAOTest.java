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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class DescuentoProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<DescuentoProducto> typedQuery;

    @InjectMocks
    private DescuentoProductoDAO descuentoProductoDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(descuentoProductoDAO.getEntityManager());
        assertEquals(em, descuentoProductoDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByProducto retorna lista vacía si el producto o su ID son nulos")
    void testFindByProductoNull() {
        // Caso 1: Objeto Producto nulo
        List<DescuentoProducto> res1 = descuentoProductoDAO.findByProducto(null);
        assertNotNull(res1);
        assertTrue(res1.isEmpty());

        // Caso 2: Objeto Producto con idProducto nulo
        Producto pSinId = new Producto();
        List<DescuentoProducto> res2 = descuentoProductoDAO.findByProducto(pSinId);
        assertNotNull(res2);
        assertTrue(res2.isEmpty());

        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("findByProducto ejecuta la consulta JPQL correctamente si el producto es válido")
    void testFindByProductoExitoso() {
        Producto producto = new Producto();
        producto.setIdProducto(UUID.randomUUID());
        List<DescuentoProducto> esperados = List.of(new DescuentoProducto());
        String expectedQuery = "SELECT dp FROM DescuentoProducto dp WHERE dp.idProducto = :producto";

        when(em.createQuery(expectedQuery, DescuentoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("producto", producto)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<DescuentoProducto> resultado = descuentoProductoDAO.findByProducto(producto);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createQuery(expectedQuery, DescuentoProducto.class);
        verify(typedQuery).setParameter("producto", producto);
    }

    @Test
    @DisplayName("findByFechaDesde ejecuta NamedQuery y devuelve la lista correspondiente")
    void testFindByFechaDesde() {
        Date fecha = new Date();
        List<DescuentoProducto> esperados = Collections.singletonList(new DescuentoProducto());

        when(em.createNamedQuery("DescuentoProducto.findByFechaDesde", DescuentoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("fechaDesde", fecha)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<DescuentoProducto> resultado = descuentoProductoDAO.findByFechaDesde(fecha);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("DescuentoProducto.findByFechaDesde", DescuentoProducto.class);
        verify(typedQuery).setParameter("fechaDesde", fecha);
    }

    @Test
    @DisplayName("findByFechaHasta ejecuta NamedQuery y devuelve la lista correspondiente")
    void testFindByFechaHasta() {
        Date fecha = new Date();
        List<DescuentoProducto> esperados = Collections.singletonList(new DescuentoProducto());

        when(em.createNamedQuery("DescuentoProducto.findByFechaHasta", DescuentoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("fechaHasta", fecha)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<DescuentoProducto> resultado = descuentoProductoDAO.findByFechaHasta(fecha);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("DescuentoProducto.findByFechaHasta", DescuentoProducto.class);
        verify(typedQuery).setParameter("fechaHasta", fecha);
    }

    @Test
    @DisplayName("findByValor ejecuta NamedQuery y devuelve la lista correspondiente")
    void testFindByValor() {
        Integer valor = 10;
        List<DescuentoProducto> esperados = Collections.singletonList(new DescuentoProducto());

        when(em.createNamedQuery("DescuentoProducto.findByValor", DescuentoProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("valor", valor)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<DescuentoProducto> resultado = descuentoProductoDAO.findByValor(valor);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("DescuentoProducto.findByValor", DescuentoProducto.class);
        verify(typedQuery).setParameter("valor", valor);
    }
}