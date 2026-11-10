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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.PagoDetalle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoDetalleDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<PagoDetalle> queryPagoDetalle;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private PagoDetalleDAO dao;

    @Test
    @DisplayName("findByPago: Lanza IllegalArgumentException si el pago es nulo o no tiene ID")
    void testFindByPagoArgumentosInvalidos() {
        Pago pagoSinId = mock(Pago.class);
        when(pagoSinId.getIdPago()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.findByPago(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByPago(pagoSinId, 0, 10));
        
        Pago pagoValido = mock(Pago.class);
        when(pagoValido.getIdPago()).thenReturn(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> dao.findByPago(pagoValido, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByPago(pagoValido, 0, 0));
    }

    @Test
    @DisplayName("findByPago: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByPagoParametrosValidos() {
        Pago pagoValido = mock(Pago.class);
        when(pagoValido.getIdPago()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("PagoDetalle.findByPago", PagoDetalle.class)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setParameter("pago", pagoValido)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setFirstResult(0)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setMaxResults(10)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.getResultList()).thenReturn(List.of(new PagoDetalle()));

        List<PagoDetalle> resultado = dao.findByPago(pagoValido, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryPagoDetalle).setParameter("pago", pagoValido);
        verify(queryPagoDetalle).setFirstResult(0);
        verify(queryPagoDetalle).setMaxResults(10);
    }

    @Test
    @DisplayName("findByPago: Propaga AccesoDatosException al ocurrir PersistenceException")
    void testFindByPagoLanzaExcepcionBD() {
        Pago pagoValido = mock(Pago.class);
        when(pagoValido.getIdPago()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("PagoDetalle.findByPago", PagoDetalle.class)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setParameter("pago", pagoValido)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setFirstResult(0)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setMaxResults(10)).thenReturn(queryPagoDetalle);

        PersistenceException causa = new PersistenceException("Error conexion BD");
        when(queryPagoDetalle.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () ->
            dao.findByPago(pagoValido, 0, 10)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("countByPago: Lanza IllegalArgumentException si el pago es nulo o no tiene ID")
    void testCountByPagoArgumentosInvalidos() {
        Pago pagoSinId = mock(Pago.class);
        when(pagoSinId.getIdPago()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.countByPago(null));
        assertThrows(IllegalArgumentException.class, () -> dao.countByPago(pagoSinId));
    }

    @Test
    @DisplayName("countByPago: Retorna total si la consulta es exitosa")
    void testCountByPagoExitoso() {
        Pago pagoValido = mock(Pago.class);
        when(pagoValido.getIdPago()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("PagoDetalle.countByPago", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("pago", pagoValido)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(3L);

        int total = dao.countByPago(pagoValido);

        assertEquals(3, total);
        verify(queryCount).setParameter("pago", pagoValido);
    }

    @Test
    @DisplayName("countByPago: Retorna 0 si getSingleResult devuelve null")
    void testCountByPagoResultNull() {
        Pago pagoValido = mock(Pago.class);
        when(pagoValido.getIdPago()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("PagoDetalle.countByPago", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("pago", pagoValido)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(null);

        int total = dao.countByPago(pagoValido);

        assertEquals(0, total);
    }

    @Test
    @DisplayName("findByTipoPago: Lanza IllegalArgumentException si el tipoPago es nulo o vacío")
    void testFindByTipoPagoArgumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByTipoPago(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByTipoPago("   ", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByTipoPago("EFECTIVO", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByTipoPago("EFECTIVO", 0, 0));
    }

    @Test
    @DisplayName("findByTipoPago: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByTipoPagoParametrosValidos() {
        when(em.createNamedQuery("PagoDetalle.findByTipoPago", PagoDetalle.class)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setParameter("tipoPago", "EFECTIVO")).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setFirstResult(0)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.setMaxResults(10)).thenReturn(queryPagoDetalle);
        when(queryPagoDetalle.getResultList()).thenReturn(List.of(new PagoDetalle()));

        List<PagoDetalle> resultado = dao.findByTipoPago("EFECTIVO", 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryPagoDetalle).setParameter("tipoPago", "EFECTIVO");
    }

    @Test
    @DisplayName("countByTipoPago: Lanza IllegalArgumentException si tipoPago es nulo o vacío")
    void testCountByTipoPagoArgumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.countByTipoPago(null));
        assertThrows(IllegalArgumentException.class, () -> dao.countByTipoPago(""));
    }

    @Test
    @DisplayName("countByTipoPago: Retorna total si la consulta es exitosa")
    void testCountByTipoPagoExitoso() {
        when(em.createNamedQuery("PagoDetalle.countByTipoPago", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("tipoPago", "TARJETA")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(4L);

        int total = dao.countByTipoPago("TARJETA");

        assertEquals(4, total);
        verify(queryCount).setParameter("tipoPago", "TARJETA");
    }
}