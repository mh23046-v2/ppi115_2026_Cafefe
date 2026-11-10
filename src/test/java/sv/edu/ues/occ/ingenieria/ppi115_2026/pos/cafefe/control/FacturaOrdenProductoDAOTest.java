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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.FacturaOrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaOrdenProductoDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<FacturaOrdenProducto> queryFop;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private FacturaOrdenProductoDAO dao;

    @Test
    @DisplayName("findByFactura: Lanza IllegalArgumentException si la factura es nula o no tiene ID")
    void testFindByFacturaArgumentosInvalidos() {
        Factura facturaSinId = mock(Factura.class);
        when(facturaSinId.getIdFactura()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(facturaSinId, 0, 10));

        Factura facturaValida = mock(Factura.class);
        when(facturaValida.getIdFactura()).thenReturn(UUID.randomUUID());
        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(facturaValida, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByFactura(facturaValida, 0, 0));
    }

    @Test
    @DisplayName("findByFactura: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByFacturaParametrosValidos() {
        Factura facturaValida = mock(Factura.class);
        when(facturaValida.getIdFactura()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("FacturaOrdenProducto.findByFactura", FacturaOrdenProducto.class)).thenReturn(queryFop);
        when(queryFop.setParameter("factura", facturaValida)).thenReturn(queryFop);
        when(queryFop.setFirstResult(0)).thenReturn(queryFop);
        when(queryFop.setMaxResults(10)).thenReturn(queryFop);
        when(queryFop.getResultList()).thenReturn(List.of(new FacturaOrdenProducto()));

        List<FacturaOrdenProducto> resultado = dao.findByFactura(facturaValida, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryFop).setParameter("factura", facturaValida);
        verify(queryFop).setFirstResult(0);
        verify(queryFop).setMaxResults(10);
    }

    @Test
    @DisplayName("findByFactura: Propaga AccesoDatosException al ocurrir PersistenceException")
    void testFindByFacturaLanzaExcepcionBD() {
        Factura facturaValida = mock(Factura.class);
        when(facturaValida.getIdFactura()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("FacturaOrdenProducto.findByFactura", FacturaOrdenProducto.class)).thenReturn(queryFop);
        when(queryFop.setParameter("factura", facturaValida)).thenReturn(queryFop);
        when(queryFop.setFirstResult(0)).thenReturn(queryFop);
        when(queryFop.setMaxResults(10)).thenReturn(queryFop);

        PersistenceException causa = new PersistenceException("Error conexion BD");
        when(queryFop.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () ->
            dao.findByFactura(facturaValida, 0, 10)
        );
        assertSame(causa, ex.getCause());
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

        when(em.createNamedQuery("FacturaOrdenProducto.countByFactura", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("factura", facturaValida)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(3L);

        int total = dao.countByFactura(facturaValida);

        assertEquals(3, total);
        verify(queryCount).setParameter("factura", facturaValida);
    }

    @Test
    @DisplayName("countByFactura: Retorna 0 si getSingleResult devuelve null")
    void testCountByFacturaResultNull() {
        Factura facturaValida = mock(Factura.class);
        when(facturaValida.getIdFactura()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("FacturaOrdenProducto.countByFactura", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("factura", facturaValida)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(null);

        int total = dao.countByFactura(facturaValida);

        assertEquals(0, total);
    }

    @Test
    @DisplayName("findByOrdenProducto: Lanza IllegalArgumentException si OrdenProducto es nulo o sin ID")
    void testFindByOrdenProductoArgumentosInvalidos() {
        OrdenProducto opSinId = mock(OrdenProducto.class);
        when(opSinId.getIdOrdenProducto()).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> dao.findByOrdenProducto(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByOrdenProducto(opSinId, 0, 10));
    }

    @Test
    @DisplayName("findByOrdenProducto: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByOrdenProductoParametrosValidos() {
        OrdenProducto opValida = mock(OrdenProducto.class);
        when(opValida.getIdOrdenProducto()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("FacturaOrdenProducto.findByOrdenProducto", FacturaOrdenProducto.class)).thenReturn(queryFop);
        when(queryFop.setParameter("ordenProducto", opValida)).thenReturn(queryFop);
        when(queryFop.setFirstResult(0)).thenReturn(queryFop);
        when(queryFop.setMaxResults(10)).thenReturn(queryFop);
        when(queryFop.getResultList()).thenReturn(List.of(new FacturaOrdenProducto()));

        List<FacturaOrdenProducto> resultado = dao.findByOrdenProducto(opValida, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryFop).setParameter("ordenProducto", opValida);
    }

    @Test
    @DisplayName("countByOrdenProducto: Retorna total si la consulta es exitosa")
    void testCountByOrdenProductoExitoso() {
        OrdenProducto opValida = mock(OrdenProducto.class);
        when(opValida.getIdOrdenProducto()).thenReturn(UUID.randomUUID());

        when(em.createNamedQuery("FacturaOrdenProducto.countByOrdenProducto", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("ordenProducto", opValida)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(1L);

        int total = dao.countByOrdenProducto(opValida);

        assertEquals(1, total);
        verify(queryCount).setParameter("ordenProducto", opValida);
    }
}