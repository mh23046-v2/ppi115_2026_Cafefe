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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Consultas y operaciones nuevas de Cajas y Pagos (completar, anular, eliminar). */
@ExtendWith(MockitoExtension.class)
class CobroDAOTest {

    @Mock
    EntityManager em;
    @Mock
    TypedQuery<Object> query;
    @Mock
    Query bulk;

    private PagoDAO pagoDAO;
    private Factura factura;
    private Pago pago;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() throws ReflectiveOperationException {
        lenient().when(em.createNamedQuery(anyString(), any(Class.class))).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        lenient().when(query.setFirstResult(anyInt())).thenReturn(query);
        lenient().when(query.setMaxResults(anyInt())).thenReturn(query);
        pagoDAO = conEm(new PagoDAO());
        factura = new Factura(UUID.randomUUID());
        factura.setEstado("PENDIENTE");
        pago = new Pago(UUID.randomUUID());
        pago.setEstado("PENDIENTE");
        pago.setIdFactura(factura);
    }

    // ---------------- completar ----------------
    @Test
    @DisplayName("completar: pago COMPLETADO, factura PAGADA y devuelve el cambio")
    void completarPagoCubierto() {
        when(em.find(Pago.class, pago.getIdPago())).thenReturn(pago);
        when(query.getSingleResult()).thenReturn(new BigDecimal("14.00"));
        when(query.getResultList()).thenReturn(List.of(detalle("EFECTIVO", "5.00"), detalle("TARJETA", "10.00")));

        BigDecimal cambio = pagoDAO.completar(pago);

        assertEquals(new BigDecimal("1.00"), cambio);
        assertEquals("COMPLETADO", pago.getEstado());
        assertEquals("PAGADA", factura.getEstado());
        verify(em).flush();
    }

    @Test
    @DisplayName("completar: si no cubre el total lanza ReglaNegocioException y no cambia estados")
    void completarNoCubre() {
        when(em.find(Pago.class, pago.getIdPago())).thenReturn(pago);
        when(query.getSingleResult()).thenReturn(new BigDecimal("14.00"));
        when(query.getResultList()).thenReturn(List.of(detalle("EFECTIVO", "4.00")));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> pagoDAO.completar(pago));

        assertTrue(ex.getMessage().contains("no cubre"));
        assertEquals("PENDIENTE", pago.getEstado());
        assertEquals("PENDIENTE", factura.getEstado());
        verify(em, never()).flush();
    }

    @Test
    @DisplayName("completar: pago ya completado, factura no pendiente o pago sin guardar no se completan")
    void completarEstadosInvalidos() {
        Pago completado = new Pago(UUID.randomUUID());
        completado.setEstado("COMPLETADO");
        when(em.find(Pago.class, completado.getIdPago())).thenReturn(completado);
        assertThrows(ReglaNegocioException.class, () -> pagoDAO.completar(completado));

        factura.setEstado("ANULADA");
        when(em.find(Pago.class, pago.getIdPago())).thenReturn(pago);
        assertThrows(ReglaNegocioException.class, () -> pagoDAO.completar(pago));

        Pago sinGuardar = new Pago(UUID.randomUUID());
        assertThrows(ReglaNegocioException.class, () -> pagoDAO.completar(sinGuardar));
        assertThrows(IllegalArgumentException.class, () -> pagoDAO.completar(null));
    }

    @Test
    @DisplayName("completar: un error de la base se envuelve en AccesoDatosException")
    void completarErrorDeBase() {
        when(em.find(Pago.class, pago.getIdPago())).thenThrow(new PersistenceException("caída"));
        assertThrows(AccesoDatosException.class, () -> pagoDAO.completar(pago));
    }

    // ---------------- anular ----------------
    @Test
    @DisplayName("anular un pago COMPLETADO devuelve la factura a PENDIENTE")
    void anularCompletado() {
        pago.setEstado("COMPLETADO");
        factura.setEstado("PAGADA");
        when(em.find(Pago.class, pago.getIdPago())).thenReturn(pago);

        pagoDAO.anular(pago);

        assertEquals("ANULADO", pago.getEstado());
        assertEquals("PENDIENTE", factura.getEstado());
    }

    @Test
    @DisplayName("anular un pago PENDIENTE no toca la factura; uno ANULADO no se anula de nuevo")
    void anularPendienteYAnulado() {
        when(em.find(Pago.class, pago.getIdPago())).thenReturn(pago);
        pagoDAO.anular(pago);
        assertEquals("ANULADO", pago.getEstado());
        assertEquals("PENDIENTE", factura.getEstado());

        assertThrows(ReglaNegocioException.class, () -> pagoDAO.anular(pago));
        assertThrows(IllegalArgumentException.class, () -> pagoDAO.anular(null));
    }

    // ---------------- eliminar y consultas ----------------
    @Test
    @DisplayName("delete: borra primero los detalles y luego el pago")
    void eliminarPagoConDetalles() {
        when(em.createNamedQuery("PagoDetalle.deleteByPago")).thenReturn(bulk);
        when(bulk.setParameter(anyString(), any())).thenReturn(bulk);
        when(em.contains(pago)).thenReturn(true);

        pagoDAO.delete(pago);

        var orden = inOrder(bulk, em);
        orden.verify(bulk).executeUpdate();
        orden.verify(em).remove(pago);
    }

    @Test
    @DisplayName("countVigentesByFactura: excluye anulados y el propio pago")
    void countVigentes() {
        when(query.getSingleResult()).thenReturn(1L);

        assertEquals(1, pagoDAO.countVigentesByFactura(factura, pago.getIdPago()));
        verify(query).setParameter("anulado", "ANULADO");
        verify(query).setParameter("excluir", pago.getIdPago());
        assertEquals(0, pagoDAO.countVigentesByFactura(null, null));
    }

    @Test
    @DisplayName("findRange usa la consulta de recientes con paginación")
    void findRangeRecientes() {
        when(query.getResultList()).thenReturn(List.of(pago));

        assertEquals(List.of(pago), pagoDAO.findRange(10, 5));
        verify(em).createNamedQuery("Pago.findRecientes", Pago.class);
        verify(query).setFirstResult(10);
        verify(query).setMaxResults(5);
    }

    @Test
    @DisplayName("PagoDetalleDAO: total del pago y detalles ordenados")
    void pagoDetalle() throws ReflectiveOperationException {
        PagoDetalleDAO dao = conEm(new PagoDetalleDAO());
        when(query.getSingleResult()).thenReturn(new BigDecimal("7.50"));
        when(query.getResultList()).thenReturn(List.of());

        assertEquals(new BigDecimal("7.50"), dao.totalByPago(pago));
        assertEquals(BigDecimal.ZERO, dao.totalByPago(null));
        assertTrue(dao.findByPagoOrdenado(pago, 0, 5).isEmpty());
        verify(em).createNamedQuery("PagoDetalle.findByPagoOrdenado", PagoDetalle.class);
        assertThrows(IllegalArgumentException.class, () -> dao.findByPagoOrdenado(null, 0, 5));
    }

    @Test
    @DisplayName("CajaDAO.existeNombre compara sin mayúsculas ni espacios y excluye la propia caja")
    void cajaExisteNombre() throws ReflectiveOperationException {
        CajaDAO dao = conEm(new CajaDAO());
        UUID propia = UUID.randomUUID();
        when(query.getSingleResult()).thenReturn(1L);

        assertTrue(dao.existeNombre("  Caja 1 ", propia));
        verify(query).setParameter("nombre", "caja 1");
        verify(query).setParameter("excluir", propia);
        assertFalse(dao.existeNombre(" ", null));
    }

    @Test
    @DisplayName("FacturaDAO.findPorCobrar filtra pendientes sin pago vigente; delete rechaza facturas con pagos")
    void facturaPorCobrarYDelete() throws ReflectiveOperationException {
        FacturaDAO dao = conEm(new FacturaDAO());
        when(query.getResultList()).thenReturn(List.of(factura));
        assertEquals(List.of(factura), dao.findPorCobrar("ana", 10));
        verify(query).setParameter("pendiente", "PENDIENTE");
        verify(query).setParameter("anulado", "ANULADO");
        verify(query).setParameter("texto", "%ana%");
        assertThrows(IllegalArgumentException.class, () -> dao.findPorCobrar("x", 0));

        when(query.getSingleResult()).thenReturn(2L);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> dao.delete(factura));
        assertTrue(ex.getMessage().contains("pagos"));
        verify(em, never()).remove(any());
    }

    @Test
    @DisplayName("EmpleadoRolDAO.existeAsignacion consulta por empleado y rol")
    void empleadoRolExiste() throws ReflectiveOperationException {
        EmpleadoRolDAO dao = conEm(new EmpleadoRolDAO());
        Empleado e = new Empleado(UUID.randomUUID());
        Rol r = new Rol(UUID.randomUUID());
        when(query.getSingleResult()).thenReturn(0L);

        assertFalse(dao.existeAsignacion(e, r, null));
        verify(em).createNamedQuery("EmpleadoRol.countByEmpleadoYRol", Long.class);
        assertFalse(dao.existeAsignacion(null, r, null));
        assertFalse(dao.existeAsignacion(e, null, null));
    }

    // ---------------- utilidades ----------------
    private <D> D conEm(D dao) throws ReflectiveOperationException {
        Field f = dao.getClass().getDeclaredField("em");
        f.setAccessible(true);
        f.set(dao, em);
        return dao;
    }

    private static PagoDetalle detalle(String tipo, String monto) {
        PagoDetalle d = new PagoDetalle(UUID.randomUUID());
        d.setTipoPago(tipo);
        d.setMonto(new BigDecimal(monto));
        d.setReferenciaExterna("EFECTIVO".equals(tipo) ? null : "REF-1");
        return d;
    }
}
