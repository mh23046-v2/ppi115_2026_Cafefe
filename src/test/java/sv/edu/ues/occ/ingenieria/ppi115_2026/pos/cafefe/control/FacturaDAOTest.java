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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Factura> queryFactura;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private FacturaDAO dao;

    @Test
    @DisplayName("findByEstado: Lanza IllegalArgumentException si el estado es inválido o el rango es erróneo")
    void testFindByEstadoArgumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado("   ", 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado("PENDIENTE", -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByEstado("PENDIENTE", 0, 0));
    }

    @Test
    @DisplayName("findByEstado: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByEstadoParametrosValidos() {
        when(em.createNamedQuery("Factura.findByEstado", Factura.class)).thenReturn(queryFactura);
        when(queryFactura.setParameter("estado", "PENDIENTE")).thenReturn(queryFactura);
        when(queryFactura.setFirstResult(0)).thenReturn(queryFactura);
        when(queryFactura.setMaxResults(10)).thenReturn(queryFactura);
        when(queryFactura.getResultList()).thenReturn(List.of(new Factura()));

        List<Factura> resultado = dao.findByEstado("PENDIENTE", 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryFactura).setParameter("estado", "PENDIENTE");
        verify(queryFactura).setFirstResult(0);
        verify(queryFactura).setMaxResults(10);
    }

    @Test
    @DisplayName("findByEstado: Propaga AccesoDatosException al ocurrir PersistenceException")
    void testFindByEstadoLanzaExcepcionBD() {
        when(em.createNamedQuery("Factura.findByEstado", Factura.class)).thenReturn(queryFactura);
        when(queryFactura.setParameter("estado", "PENDIENTE")).thenReturn(queryFactura);
        when(queryFactura.setFirstResult(0)).thenReturn(queryFactura);
        when(queryFactura.setMaxResults(10)).thenReturn(queryFactura);

        PersistenceException causa = new PersistenceException("Error consulta BD");
        when(queryFactura.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () ->
            dao.findByEstado("PENDIENTE", 0, 10)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("countByEstado: Lanza IllegalArgumentException si el estado es nulo o vacío")
    void testCountByEstadoArgumentoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> dao.countByEstado(null));
        assertThrows(IllegalArgumentException.class, () -> dao.countByEstado(""));
    }

    @Test
    @DisplayName("countByEstado: Retorna total si la consulta es exitosa")
    void testCountByEstadoExitoso() {
        when(em.createNamedQuery("Factura.countByEstado", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("estado", "PAGADA")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(10L);

        int total = dao.countByEstado("PAGADA");

        assertEquals(10, total);
        verify(queryCount).setParameter("estado", "PAGADA");
    }

    @Test
    @DisplayName("findByCaja: Lanza IllegalArgumentException si la caja es nula o no tiene ID")
    void testFindByCajaArgumentosInvalidos() {
        Caja cajaSinId = mock(Caja.class);
        when(cajaSinId.getIdCaja()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.findByCaja(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByCaja(cajaSinId, 0, 10));
    }

    @Test
    @DisplayName("findByCaja: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByCajaParametrosValidos() {
        Caja cajaValida = mock(Caja.class);
        when(cajaValida.getIdCaja()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Factura.findByCaja", Factura.class)).thenReturn(queryFactura);
        when(queryFactura.setParameter("caja", cajaValida)).thenReturn(queryFactura);
        when(queryFactura.setFirstResult(0)).thenReturn(queryFactura);
        when(queryFactura.setMaxResults(10)).thenReturn(queryFactura);
        when(queryFactura.getResultList()).thenReturn(List.of(new Factura()));

        List<Factura> resultado = dao.findByCaja(cajaValida, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryFactura).setParameter("caja", cajaValida);
    }

    @Test
    @DisplayName("countByCaja: Retorna total si la consulta es exitosa")
    void testCountByCajaExitoso() {
        Caja cajaValida = mock(Caja.class);
        when(cajaValida.getIdCaja()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("Factura.countByCaja", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("caja", cajaValida)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(4L);

        int total = dao.countByCaja(cajaValida);

        assertEquals(4, total);
    }

    @Test
    @DisplayName("findByCliente: Lanza IllegalArgumentException si el nombre del cliente es inválido")
    void testFindByClienteArgumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByCliente(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByCliente("  ", 0, 10));
    }

    @Test
    @DisplayName("findByCliente: Limpia y formatea con patronLike el nombre del cliente")
    void testFindByClienteParametrosValidos() {
        when(em.createNamedQuery("Factura.findByCliente", Factura.class)).thenReturn(queryFactura);
        when(queryFactura.setParameter("cliente", "%juan%")).thenReturn(queryFactura);
        when(queryFactura.setFirstResult(0)).thenReturn(queryFactura);
        when(queryFactura.setMaxResults(10)).thenReturn(queryFactura);
        when(queryFactura.getResultList()).thenReturn(List.of(new Factura()));

        List<Factura> resultado = dao.findByCliente("  Juan ", 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryFactura).setParameter("cliente", "%juan%");
    }

    @Test
    @DisplayName("countByCliente: Retorna total de facturas por cliente")
    void testCountByClienteExitoso() {
        when(em.createNamedQuery("Factura.countByCliente", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("cliente", "%maria%")).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(2L);

        int total = dao.countByCliente("Maria");

        assertEquals(2, total);
        verify(queryCount).setParameter("cliente", "%maria%");
    }
}