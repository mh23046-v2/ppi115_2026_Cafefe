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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    }

    @Test
    void listaConTotalRegistros() {
        List<Map<String, Object>> datos = List.of(Map.of("idOrden", "x"));
        when(cobroService.pendientes(0, 20)).thenReturn(datos);
        when(cobroService.contarPendientes()).thenReturn(7);

        Response r = resource.findRange(0, 20);

        assertEquals(200, r.getStatus());
        assertEquals(datos, r.getEntity());
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
}
