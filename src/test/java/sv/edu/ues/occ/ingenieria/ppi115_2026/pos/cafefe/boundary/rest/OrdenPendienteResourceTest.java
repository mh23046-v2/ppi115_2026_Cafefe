package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RegistroDespachos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class OrdenPendienteResourceTest {

    private CobroService cobroService;
    private OrdenPendienteResource resource;

    @BeforeEach
    void setUp() {
        cobroService = Mockito.mock(CobroService.class);
        resource = new OrdenPendienteResource();
        resource.cobroService = cobroService;
        resource.despachos = new RegistroDespachos();
    }

    @Test
    void listaConTotalRegistros() {
        List<Map<String, Object>> datos = List.of(Map.of("idOrden", "x"));
        when(cobroService.pendientes(0, 20)).thenReturn(datos);
        when(cobroService.contarPendientes()).thenReturn(7);

        Response r = resource.findRange(0, 20);

        assertEquals(200, r.getStatus());
        assertEquals(List.of(Map.of("idOrden", "x", "despachada", false)), r.getEntity());
        assertEquals(7, r.getHeaders().getFirst("Total-registros"));
    }

    @Test
    void maxSeLimitaA50() {
        resource.findRange(10, 500);
        verify(cobroService).pendientes(10, 50);
    }

    @Test
    void paginacionInvalida() {
        assertEquals("first", resource.findRange(-1, 10).getHeaderString("Wrong-Param"));
        assertEquals("max", resource.findRange(0, 0).getHeaderString("Wrong-Param"));
        assertEquals(422, resource.findRange(0, -5).getStatus());
        verifyNoInteractions(cobroService);
    }

    @Test
    void unaOrden() {
        UUID id = UUID.randomUUID();
        when(cobroService.pendiente(id)).thenReturn(Map.of("idOrden", id.toString()));
        assertEquals(200, resource.findById(id).getStatus());
    }

    @Test
    void unaOrdenQueNoExiste() {
        UUID id = UUID.randomUUID();
        when(cobroService.pendiente(id)).thenThrow(new NoEncontradoException(id, "La orden no existe"));
        Response r = resource.findById(id);
        assertEquals(404, r.getStatus());
        assertEquals(id.toString(), r.getHeaderString("Entity-Not-Found"));
    }

    // ------------------------------------------------------------ despacho (en memoria)

    @Test
    @SuppressWarnings("unchecked")
    void despacharLaMarcaEnLaListaYEnUnaOrden() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        when(cobroService.pendiente(a)).thenReturn(Map.of("idOrden", a.toString()));
        when(cobroService.pendientes(0, 20)).thenReturn(List.of(
                Map.of("idOrden", a.toString()), Map.of("idOrden", b.toString())));

        assertEquals(204, resource.despachar(a).getStatus());

        List<Map<String, Object>> lista = (List<Map<String, Object>>) resource.findRange(0, 20).getEntity();
        assertEquals(true, lista.get(0).get("despachada"));
        assertEquals(false, lista.get(1).get("despachada"));
        Map<String, Object> una = (Map<String, Object>) resource.findById(a).getEntity();
        assertEquals(true, una.get("despachada"));
    }

    @Test
    void deshacerDespachoLaRegresaACocina() {
        UUID a = UUID.randomUUID();
        when(cobroService.pendiente(a)).thenReturn(Map.of("idOrden", a.toString()));
        resource.despachar(a);

        assertEquals(204, resource.deshacerDespacho(a).getStatus());
        assertFalse(resource.despachos.estaDespachada(a));
        // deshacer una que no estaba despachada tampoco falla
        assertEquals(204, resource.deshacerDespacho(UUID.randomUUID()).getStatus());
    }

    @Test
    void despacharUnaOrdenQueNoExisteDa404YNoLaGuarda() {
        UUID id = UUID.randomUUID();
        when(cobroService.pendiente(id)).thenThrow(new NoEncontradoException(id, "La orden no existe"));

        Response r = resource.despachar(id);

        assertEquals(404, r.getStatus());
        assertEquals(id.toString(), r.getHeaderString("Entity-Not-Found"));
        assertFalse(resource.despachos.estaDespachada(id));
    }

    @Test
    void elDespachoSeCompartePorqueElRegistroEsUnoSolo() {
        // Dos peticiones distintas (Cocina y Caja) usan el mismo RegistroDespachos (@ApplicationScoped)
        UUID a = UUID.randomUUID();
        when(cobroService.pendiente(a)).thenReturn(Map.of("idOrden", a.toString()));
        OrdenPendienteResource deCaja = new OrdenPendienteResource();
        deCaja.cobroService = cobroService;
        deCaja.despachos = resource.despachos;

        resource.despachar(a);

        assertTrue(deCaja.despachos.estaDespachada(a));
    }

    // ------------------------------------------------------------ pagadas por adelantado (Cocina)

    @Test
    @SuppressWarnings("unchecked")
    void pagadasSinDespacharTraeLaOrdenCompletaMarcadaComoPagada() {
        UUID a = UUID.randomUUID();
        when(cobroService.ordenCompleta(a)).thenReturn(Map.of("idOrden", a.toString(), "total", 5));
        resource.despachos.registrarCobro(a);

        Response r = resource.pagadasSinDespachar();

        assertEquals(200, r.getStatus());
        List<Map<String, Object>> lista = (List<Map<String, Object>>) r.getEntity();
        assertEquals(1, lista.size());
        assertEquals(true, lista.get(0).get("pagada"));
        assertEquals(false, lista.get(0).get("despachada"));
        assertEquals(5, lista.get(0).get("total"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void unaPagadaQueYaNoExisteSeOlvida() {
        UUID a = UUID.randomUUID();
        when(cobroService.ordenCompleta(a)).thenThrow(new NoEncontradoException(a, "La orden no existe"));
        resource.despachos.registrarCobro(a);

        List<Map<String, Object>> lista = (List<Map<String, Object>>) resource.pagadasSinDespachar().getEntity();

        assertTrue(lista.isEmpty());
        assertTrue(resource.despachos.pagadasSinDespachar().isEmpty());
    }
}
