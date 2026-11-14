package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Consultas nuevas de OrdenDAO y OrdenProductoDAO para cocina y caja. */
class OrdenPendienteDAOTest {

    @ExtendWith(MockitoExtension.class)
    @org.junit.jupiter.api.Nested
    class DeOrden {

        @Mock
        EntityManager em;
        @Mock
        TypedQuery<Orden> lista;
        @Mock
        TypedQuery<Long> conteo;
        @InjectMocks
        OrdenDAO dao;

        @Test
        @DisplayName("findPendientesDeCobro pagina y excluye facturas anuladas")
        void pagina() {
            when(em.createQuery(OrdenDAO.JPQL_PENDIENTES, Orden.class)).thenReturn(lista);
            when(lista.setParameter("anulada", ReglasFactura.ANULADA)).thenReturn(lista);
            when(lista.setFirstResult(20)).thenReturn(lista);
            when(lista.setMaxResults(10)).thenReturn(lista);
            when(lista.getResultList()).thenReturn(List.of(new Orden()));

            assertEquals(1, dao.findPendientesDeCobro(20, 10).size());
            verify(lista).setFirstResult(20);
        }

        @Test
        @DisplayName("findPendientesDeCobro rechaza paginación inválida")
        void rangoInvalido() {
            assertThrows(IllegalArgumentException.class, () -> dao.findPendientesDeCobro(-1, 10));
            assertThrows(IllegalArgumentException.class, () -> dao.findPendientesDeCobro(0, 0));
        }

        @Test
        @DisplayName("countPendientesDeCobro devuelve el total (0 si viene null)")
        void cuenta() {
            when(em.createQuery(OrdenDAO.JPQL_COUNT_PENDIENTES, Long.class)).thenReturn(conteo);
            when(conteo.setParameter("anulada", ReglasFactura.ANULADA)).thenReturn(conteo);
            when(conteo.getSingleResult()).thenReturn(3L, (Long) null);

            assertEquals(3, dao.countPendientesDeCobro());
            assertEquals(0, dao.countPendientesDeCobro());
        }

        @Test
        @DisplayName("Las órdenes salen de la más antigua a la más reciente")
        void ordenAscendente() {
            assertTrue(OrdenDAO.JPQL_PENDIENTES.contains("ORDER BY o.fechaCreacion ASC"));
        }
    }

    @ExtendWith(MockitoExtension.class)
    @org.junit.jupiter.api.Nested
    class DeOrdenProducto {

        @Mock
        EntityManager em;
        @Mock
        TypedQuery<OrdenProducto> lista;
        @InjectMocks
        OrdenProductoDAO dao;

        @Test
        @DisplayName("findPendientesDeOrdenes no consulta la base si no hay órdenes")
        void listaVacia() {
            assertTrue(dao.findPendientesDeOrdenes(List.of()).isEmpty());
            assertTrue(dao.findPendientesDeOrdenes(null).isEmpty());
            verifyNoInteractions(em);
        }

        @Test
        @DisplayName("findPendientesDeOrdenes trae las líneas de todas las órdenes en una consulta")
        void variasOrdenes() {
            List<Orden> ordenes = List.of(new Orden(UUID.randomUUID()), new Orden(UUID.randomUUID()));
            when(em.createQuery(OrdenProductoDAO.JPQL_PENDIENTES_DE_ORDENES, OrdenProducto.class)).thenReturn(lista);
            when(lista.setParameter("ordenes", ordenes)).thenReturn(lista);
            when(lista.setParameter("anulada", ReglasFactura.ANULADA)).thenReturn(lista);
            when(lista.getResultList()).thenReturn(List.of(new OrdenProducto(), new OrdenProducto()));

            assertEquals(2, dao.findPendientesDeOrdenes(ordenes).size());
        }

        @Test
        @DisplayName("findPendientesByOrden usa la named query existente")
        void unaOrden() {
            Orden orden = new Orden(UUID.randomUUID());
            when(em.createNamedQuery("OrdenProducto.findPendientesByOrden", OrdenProducto.class)).thenReturn(lista);
            when(lista.setParameter("orden", orden)).thenReturn(lista);
            when(lista.setParameter("anulada", ReglasFactura.ANULADA)).thenReturn(lista);
            when(lista.getResultList()).thenReturn(List.of(mock(OrdenProducto.class)));

            assertEquals(1, dao.findPendientesByOrden(orden).size());
            assertThrows(IllegalArgumentException.class, () -> dao.findPendientesByOrden(null));
            assertThrows(IllegalArgumentException.class, () -> dao.findPendientesByOrden(new Orden()));
        }
    }
}
