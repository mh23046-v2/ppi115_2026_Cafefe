package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenService.SolicitudOrdenInvalidaException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OrdenResourceTest {

    private OrdenService ordenService;
    private UriInfo uriInfo;
    private OrdenResource resource;

    @BeforeEach
    void setUp() {
        ordenService = Mockito.mock(OrdenService.class);
        uriInfo = Mockito.mock(UriInfo.class);

        resource = new OrdenResource();
        resource.ordenService = ordenService;
    }

    @Test
    void crearDevuelve201ConLocationYDto() {
        UUID idOrden = UUID.fromString("ce6d03ea-7005-4c53-b22d-556cdcdd2419");
        Map<String, Object> dto = Map.of(
                "idOrden", idOrden.toString(),
                "cliente", "Ana",
                "total", new BigDecimal("10.25"),
                "lineas", List.of()
        );

        when(ordenService.crearOrden(any())).thenReturn(dto);
        when(uriInfo.getAbsolutePathBuilder())
                .thenReturn(UriBuilder.fromUri("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/orden"));

        Response resp = resource.crear(Map.of("cliente", "Ana"), uriInfo);

        assertEquals(201, resp.getStatus());
        assertEquals(
                "http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/orden/" + idOrden,
                resp.getHeaderString("Location")
        );
        assertEquals(dto, resp.getEntity());
    }

    @Test
    void crearDevuelve422ConWrongParamCuandoServicioRechazaSolicitud() {
        when(ordenService.crearOrden(any()))
                .thenThrow(new SolicitudOrdenInvalidaException("items[0].idProducto", "El producto seleccionado no existe o está inactivo"));

        Response resp = resource.crear(Map.of("cliente", "Ana"), uriInfo);

        assertEquals(422, resp.getStatus());
        assertEquals("items[0].idProducto", resp.getHeaderString("Wrong-Param"));
    }
}