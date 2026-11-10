package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Consultas y operaciones nuevas que necesita la pantalla de Facturas. */
@ExtendWith(MockitoExtension.class)
class FacturacionDAOTest {

    @Mock
    EntityManager em;
    @Mock
    TypedQuery<Object> query;
    @Mock
    Query bulk;

    private final Factura factura = new Factura(UUID.randomUUID());
    private final Orden orden = new Orden(UUID.randomUUID());

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() {
        lenient().when(em.createNamedQuery(anyString(), any(Class.class))).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        lenient().when(query.setFirstResult(anyInt())).thenReturn(query);
        lenient().when(query.setMaxResults(anyInt())).thenReturn(query);
    }

    private static <D> D conEm(D dao, EntityManager em) throws ReflectiveOperationException {
        Field f = dao.getClass().getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
        return dao;
    }

    @Test
    @DisplayName("FacturaDAO.delete: borra primero los productos de la factura y luego la factura, en la misma transacción")
    void testEliminarFacturaConDetalle() throws Exception {
        FacturaDAO dao = conEm(new FacturaDAO(), em);
        when(em.createNamedQuery("FacturaOrdenProducto.deleteByFactura")).thenReturn(bulk);
        when(bulk.setParameter("idFactura", factura.getIdFactura())).thenReturn(bulk);
        when(em.contains(factura)).thenReturn(true);

        dao.delete(factura);

        InOrder orden = inOrder(bulk, em);
        orden.verify(bulk).executeUpdate();
        orden.verify(em).remove(factura);
        assertThrows(IllegalArgumentException.class, () -> dao.delete(new Factura()));
    }

    @Test
    @DisplayName("FacturaDAO.findRange: usa la consulta ordenada por fecha (paginación estable)")
    void testFindRangeOrdenado() throws Exception {
        FacturaDAO dao = conEm(new FacturaDAO(), em);
        when(query.getResultList()).thenReturn(List.of());

        dao.findRange(10, 5);

        verify(em).createNamedQuery("Factura.findRecientes", Factura.class);
        verify(query).setFirstResult(10);
        verify(query).setMaxResults(5);
        assertThrows(IllegalArgumentException.class, () -> dao.findRange(-1, 5));
    }

    @Test
    @DisplayName("totalByFactura: suma en la base; sin productos devuelve 0")
    void testTotal() throws Exception {
        FacturaOrdenProductoDAO dao = conEm(new FacturaOrdenProductoDAO(), em);
        when(query.getSingleResult()).thenReturn(new BigDecimal("9.75"), (Object) null);

        assertEquals(new BigDecimal("9.75"), dao.totalByFactura(factura));
        assertEquals(BigDecimal.ZERO, dao.totalByFactura(factura));
        assertEquals(BigDecimal.ZERO, dao.totalByFactura(null));
    }

    @Test
    @DisplayName("countVigentesByOrdenProducto: ignora las facturas ANULADAS")
    void testVigentesIgnoraAnuladas() throws Exception {
        FacturaOrdenProductoDAO dao = conEm(new FacturaOrdenProductoDAO(), em);
        OrdenProducto op = new OrdenProducto(UUID.randomUUID());
        when(query.getSingleResult()).thenReturn(1L);

        assertEquals(1, dao.countVigentesByOrdenProducto(op));
        verify(query).setParameter("anulada", "ANULADA");
        assertThrows(IllegalArgumentException.class, () -> dao.countVigentesByOrdenProducto(null));
    }

    @Test
    @DisplayName("agregarPendientesDeOrden: crea un detalle por cada producto pendiente, copiando precio y observaciones")
    void testAgregarOrden() throws Exception {
        FacturaOrdenProductoDAO dao = conEm(new FacturaOrdenProductoDAO(), em);
        OrdenProducto cafe = new OrdenProducto(UUID.randomUUID());
        cafe.setPrecio(new BigDecimal("2.50"));
        cafe.setObservaciones("sin azúcar");
        OrdenProducto pie = new OrdenProducto(UUID.randomUUID());
        pie.setPrecio(new BigDecimal("4.00"));
        when(query.getResultList()).thenReturn(List.of(cafe, pie));
        when(em.merge(factura)).thenReturn(factura);

        int agregados = dao.agregarPendientesDeOrden(factura, orden);

        assertEquals(2, agregados);
        verify(em).createNamedQuery("OrdenProducto.findPendientesByOrden", OrdenProducto.class);
        ArgumentCaptor<Object> creados = ArgumentCaptor.forClass(Object.class);
        verify(em, times(2)).persist(creados.capture());
        FacturaOrdenProducto primero = (FacturaOrdenProducto) creados.getAllValues().get(0);
        assertSame(factura, primero.getIdFactura());
        assertSame(cafe, primero.getIdOrdenProducto());
        assertEquals(new BigDecimal("2.50"), primero.getPrecio());
        assertEquals("sin azúcar", primero.getObservaciones());
        verify(em).flush();
    }

    @Test
    @DisplayName("agregarPendientesDeOrden: sin factura guardada u orden se rechaza; un fallo de la base se envuelve")
    void testAgregarOrdenErrores() throws Exception {
        FacturaOrdenProductoDAO dao = conEm(new FacturaOrdenProductoDAO(), em);
        assertThrows(IllegalArgumentException.class, () -> dao.agregarPendientesDeOrden(new Factura(), orden));
        assertThrows(IllegalArgumentException.class, () -> dao.agregarPendientesDeOrden(factura, null));

        when(query.getResultList()).thenThrow(new PersistenceException("caída"));
        assertThrows(AccesoDatosException.class, () -> dao.agregarPendientesDeOrden(factura, orden));
        verify(em, never()).persist(any());
    }

    @Test
    @DisplayName("Consultas de apoyo: pendientes de facturar, órdenes con pendientes, cajas activas y cajeros")
    void testConsultasDeApoyo() throws Exception {
        when(query.getResultList()).thenReturn(List.of());

        conEm(new OrdenProductoDAO(), em).findPendientesDeFacturar("lat", 10);
        verify(em).createNamedQuery("OrdenProducto.findPendientesDeFacturar", OrdenProducto.class);
        verify(query).setParameter("texto", "%lat%");

        conEm(new OrdenDAO(), em).findConPendientesDeFacturar(50);
        verify(em).createNamedQuery("Orden.findConPendientesDeFacturar", Orden.class);

        conEm(new CajaDAO(), em).buscarActivasPorNombre(null, 10);
        verify(em).createNamedQuery("Caja.buscarActivasPorNombre", Caja.class);

        conEm(new RolDAO(), em).findActivos();
        verify(em).createNamedQuery("Rol.findActivos", Rol.class);

        Rol cajero = new Rol(UUID.randomUUID());
        conEm(new EmpleadoRolDAO(), em).findActivosPorRolesYNombre(List.of(cajero), "ana", 10);
        verify(em).createNamedQuery("EmpleadoRol.findActivosPorRolesYNombre", EmpleadoRol.class);
        verify(query).setParameter("roles", List.of(cajero));

        // Sin roles admitidos no se consulta la base
        assertTrue(conEm(new EmpleadoRolDAO(), em).findActivosPorRolesYNombre(List.of(), "ana", 10).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> conEm(new OrdenProductoDAO(), em).findPendientesDeFacturar("x", 0));
    }
}
