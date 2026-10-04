package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class OrdenProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<OrdenProducto> typedQuery;

    @Mock
    private TypedQuery<Long> queryConteo;

    @Mock
    private TypedQuery<BigDecimal> querySuma;

    @Mock
    private TypedQuery<Producto> queryProducto;

    @Mock
    private TypedQuery<DescuentoProducto> queryDescuento;

    @InjectMocks
    private OrdenProductoDAO ordenProductoDAO;

    private final UUID idOrden = UUID.randomUUID();

    @Test
    @DisplayName("findByIdOrden usa la NamedQuery con el id de la orden y pagina")
    void testFindByIdOrden() {
        List<OrdenProducto> esperados = Collections.singletonList(new OrdenProducto(UUID.randomUUID()));
        when(em.createNamedQuery("OrdenProducto.findByIdOrden", OrdenProducto.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("idOrden", idOrden)).thenReturn(typedQuery);
        when(typedQuery.setFirstResult(0)).thenReturn(typedQuery);
        when(typedQuery.setMaxResults(5)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        assertEquals(esperados, ordenProductoDAO.findByIdOrden(idOrden, 0, 5));
    }

    @Test
    @DisplayName("findByIdOrden con id nulo lanza error y no consulta")
    void testFindByIdOrdenNulo() {
        assertThrows(IllegalArgumentException.class, () -> ordenProductoDAO.findByIdOrden(null, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> ordenProductoDAO.countByIdOrden(null));
        assertThrows(IllegalArgumentException.class, () -> ordenProductoDAO.totalByIdOrden(null));
        assertThrows(IllegalArgumentException.class, () -> ordenProductoDAO.findDescuentosDeProductosEnOrden(null));

        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("countByIdOrden devuelve el conteo de la NamedQuery")
    void testCountByIdOrden() {
        when(em.createNamedQuery("OrdenProducto.countByIdOrden", Long.class)).thenReturn(queryConteo);
        when(queryConteo.setParameter("idOrden", idOrden)).thenReturn(queryConteo);
        when(queryConteo.getSingleResult()).thenReturn(3L);

        assertEquals(3L, ordenProductoDAO.countByIdOrden(idOrden));
    }

    @Test
    @DisplayName("totalByIdOrden devuelve 0 cuando la orden no tiene productos (SUM = null)")
    void testTotalSinProductos() {
        when(em.createNamedQuery("OrdenProducto.sumPrecioByIdOrden", BigDecimal.class)).thenReturn(querySuma);
        when(querySuma.setParameter("idOrden", idOrden)).thenReturn(querySuma);
        when(querySuma.getSingleResult()).thenReturn(null);

        assertEquals(BigDecimal.ZERO, ordenProductoDAO.totalByIdOrden(idOrden));
    }

    @Test
    @DisplayName("findProductosActivos solo trae productos activos")
    void testFindProductosActivos() {
        List<Producto> esperados = Collections.singletonList(new Producto(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(Producto.class))).thenReturn(queryProducto);
        when(queryProducto.getResultList()).thenReturn(esperados);

        assertEquals(esperados, ordenProductoDAO.findProductosActivos());
        verify(em).createQuery(contains("p.activo = true"), eq(Producto.class));
    }

    @Test
    @DisplayName("findDescuentosDeProductosEnOrden busca descuentos de los productos de la orden")
    void testFindDescuentosDeProductosEnOrden() {
        List<DescuentoProducto> esperados = Collections.singletonList(new DescuentoProducto(UUID.randomUUID()));
        when(em.createQuery(anyString(), eq(DescuentoProducto.class))).thenReturn(queryDescuento);
        when(queryDescuento.setParameter("idOrden", idOrden)).thenReturn(queryDescuento);
        when(queryDescuento.getResultList()).thenReturn(esperados);

        assertEquals(esperados, ordenProductoDAO.findDescuentosDeProductosEnOrden(idOrden));
    }
}
