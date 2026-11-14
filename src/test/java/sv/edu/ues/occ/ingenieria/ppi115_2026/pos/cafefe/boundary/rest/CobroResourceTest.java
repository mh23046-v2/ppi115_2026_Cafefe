package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.SolicitudCobro;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.SolicitudCobroInvalidaException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

class CobroResourceTest {

    private CobroService cobroService;
    private UriInfo uriInfo;
    private CobroResource resource;
    private final UUID idOrden = UUID.randomUUID();
    private final SolicitudCobro solicitud = new SolicitudCobro();

    @BeforeEach
    void setUp() {
        cobroService = Mockito.mock(CobroService.class);
        uriInfo = Mockito.mock(UriInfo.class);
        resource = new CobroResource();
        resource.cobroService = cobroService;
        // UriBuilder real de Jersey (jersey-common está en el pom con scope test)
        when(uriInfo.getBaseUriBuilder())
                .thenAnswer(i -> UriBuilder.fromUri("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/"));
    }

    @Test
    void cobroCorrectoDa201ConLocationDelTicket() {
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("idFactura", "fa000009");
        resultado.put("cambio", new BigDecimal("1.00"));
        when(cobroService.cobrar(idOrden, solicitud)).thenReturn(resultado);

        Response r = resource.cobrar(idOrden, solicitud, uriInfo);

        assertEquals(201, r.getStatus());
        assertEquals(resultado, r.getEntity());
        assertEquals("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/factura/fa000009/ticket",
                r.getLocation().toString());
    }

    @Test
    void ordenQueNoExisteDa404() {
        when(cobroService.cobrar(idOrden, solicitud)).thenThrow(new NoEncontradoException(idOrden, "La orden no existe"));
        Response r = resource.cobrar(idOrden, solicitud, uriInfo);
        assertEquals(404, r.getStatus());
        assertEquals(idOrden.toString(), r.getHeaderString("Entity-Not-Found"));
    }

    @Test
    void reglaIncumplidaDa422ConWrongParamYAppError() {
        when(cobroService.cobrar(idOrden, solicitud))
                .thenThrow(new SolicitudCobroInvalidaException("pagos[0]", "El monto debe ser mayor que cero"));
        Response r = resource.cobrar(idOrden, solicitud, uriInfo);
        assertEquals(422, r.getStatus());
        assertEquals("pagos[0]", r.getHeaderString("Wrong-Param"));
        assertEquals("El monto debe ser mayor que cero", r.getHeaderString("App-Error"));
    }

    @Test
    void sinWrongParamSoloVaAppError() {
        when(cobroService.cobrar(idOrden, solicitud)).thenThrow(new SolicitudCobroInvalidaException(" ", "Error"));
        Response r = resource.cobrar(idOrden, solicitud, uriInfo);
        assertEquals(422, r.getStatus());
        assertNull(r.getHeaderString("Wrong-Param"));
    }
}
