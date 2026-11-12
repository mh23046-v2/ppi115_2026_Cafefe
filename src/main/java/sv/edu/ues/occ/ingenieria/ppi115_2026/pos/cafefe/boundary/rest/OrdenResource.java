package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenService.SolicitudOrdenInvalidaException;

@Path("orden")
@RequestScoped
public class OrdenResource {

    @Inject
    OrdenService ordenService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(Map<String, Object> solicitud, @Context UriInfo uriInfo) {
        try {
            Map<String, Object> resultado = ordenService.crearOrden(solicitud);
            String idOrden = String.valueOf(resultado.get("idOrden"));
            URI location = uriInfo.getAbsolutePathBuilder().path(idOrden).build();
            return Response.status(Response.Status.CREATED)
                    .location(location)
                    .entity(resultado)
                    .build();
        } catch (SolicitudOrdenInvalidaException ex) {
            Response base = ErrorMapper.construir(422, ex.getMessage());
            if (ex.getWrongParam() != null && !ex.getWrongParam().isBlank()) {
                return Response.fromResponse(base)
                        .header("Wrong-Param", ErrorMapper.limpiarCabecera(ex.getWrongParam()))
                        .build();
            }
            return base;
        }
    }
}