package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Pago> queryPago;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private PagoDAO dao;

    @Test
    @DisplayName("findByEstado: Lanza IllegalArgumentException si el estado es inválido o rango erróneo")
    void testFindByEstadoArgumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado("   ", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado("PROCESADO", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado("PROCESADO", 0, 0));
    }

    @Test
    @DisplayName("findByEstado: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByEstadoParametrosValidos() {
        when(em.createNamedQuery("Pago.findByEstado", Pago.class)).thenReturn(queryPago);
        when(queryPago.setParameter("estado", "PROCESADO")).thenReturn(queryPago);
        when(queryPago.setFirstResult(0)).thenReturn(queryPago);
        when(queryPago.setMaxResults(10)).thenReturn(queryPago);
        when(queryPago.getResultList()).thenReturn(List.of(new Pago()));

        List<Pago> resultado = dao.findByEstado("PROCESADO", 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryPago).setParameter("estado", "PROCESADO");
        verify(queryPago).setFirstResult(0);
        verify(queryPago).setMaxResults(10);
    }

    @Test
    @DisplayName("findByEstado: Propaga AccesoDatosException al ocurrir PersistenceException")
    void testFindByEstadoLanzaExcepcionBD() {
        when(em.createNamedQuery("Pago.findByEstado", Pago.class)).thenReturn(queryPago);
        when(queryPago.setParameter("estado", "PROCESADO")).thenReturn(queryPago);
        when(queryPago.setFirstResult(0)).thenReturn(queryPago);
        when(queryPago.setMaxResults(10)).thenReturn(queryPago);

        PersistenceException causa = new PersistenceException("Error consulta BD");
        when(queryPago.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () ->
            dao.findByEstado("PROCESADO", 0, 10)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("countByEstado: Lanza IllegalArgumentException si estado es nulo o vacío")
    void testCountByEstadoArgumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.countByEstado(null));
        assertThrows(IllegalArgumentException.class, () -> dao.countByEstado(""));
    }

    @Test
    @DisplayName("countByEstado: Retorna total si la consulta es exitosa")
    void testCountByEstadoExitoso() {
        when(em.createNamedQuery("Pago.countByEstado", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("estado", "PROCESADO")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(3L);

        int total = dao.countByEstado("PROCESADO");

        assertEquals(3, total);
        verify(queryCount).setParameter("estado", "PROCESADO");
    }

    @Test
    @DisplayName("countByEstado: Retorna 0 si getSingleResult devuelve null")
    void testCountByEstadoResultNull() {
        when(em.createNamedQuery("Pago.countByEstado", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("estado", "ANULADO")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(null);

        int total = dao.countByEstado("ANULADO");

        assertEquals(0, total);
    }

    @Test
    @DisplayName("findByFactura: Lanza IllegalArgumentException si la factura es nula o sin ID")
    void testFindByFacturaArgumentosInvalidos() {
        Factura facturaSinId = mock(Factura.class);
        when(facturaSinId.getIdFactura()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(facturaSinId, 0, 10));
    }

    @Test
    @DisplayName("findByFactura: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByFacturaParametrosValidos() {
        Factura facturaValida = mock(Factura.class);
        when(facturaValida.getIdFactura()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Pago.findByFactura", Pago.class)).thenReturn(queryPago);
        when(queryPago.setParameter("factura", facturaValida)).thenReturn(queryPago);
        when(queryPago.setFirstResult(0)).thenReturn(queryPago);
        when(queryPago.setMaxResults(10)).thenReturn(queryPago);
        when(queryPago.getResultList()).thenReturn(List.of(new Pago()));

        List<Pago> resultado = dao.findByFactura(facturaValida, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryPago).setParameter("factura", facturaValida);
    }

    @Test
    @DisplayName("countByFactura: Lanza IllegalArgumentException si la factura es nula o sin ID")
    void testCountByFacturaArgumentosInvalidos() {
        Factura facturaSinId = mock(Factura.class);
        when(facturaSinId.getIdFactura()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.countByFactura(null));
        assertThrows(IllegalArgumentException.class, () -> dao.countByFactura(facturaSinId));
    }

    @Test
    @DisplayName("countByFactura: Retorna total si la consulta es exitosa")
    void testCountByFacturaExitoso() {
        Factura facturaValida = mock(Factura.class);
        when(facturaValida.getIdFactura()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Pago.countByFactura", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("factura", facturaValida)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(2L);

        int total = dao.countByFactura(facturaValida);

        assertEquals(2, total);
        verify(queryCount).setParameter("factura", facturaValida);
    }
}