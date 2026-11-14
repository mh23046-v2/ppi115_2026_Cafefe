package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.SolicitudCobro;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.SolicitudCobroInvalidaException;

/**
 * HU4: cobrar una orden.
 *
 * POST orden/{id}/cobro
 * {"idCaja": "...", "idEmpleadoRol": "...", "cliente": "Ana",
 *  "pagos": [{"tipoPago": "EFECTIVO", "monto": 5.00},
 *            {"tipoPago": "TARJETA", "monto": 4.50, "referenciaExterna": "AUT-1"}]}
 *
 * 201 + Location: .../factura/{idFactura}/ticket
 *     + {idFactura, idOrden, cliente, total, pagado, cambio}
 * 404 Entity-Not-Found si la orden no existe; 422 Wrong-Param si algo no cumple.
 */
@Path("orden/{id}/cobro")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CobroResource implements Serializable {

    @Inject
    CobroService cobroService;

    @POST
    public Response cobrar(@PathParam("id") UUID id, SolicitudCobro solicitud, @Context UriInfo uriInfo) {
        try {
            Map<String, Object> resultado = cobroService.cobrar(id, solicitud);
            URI location = uriInfo.getBaseUriBuilder()
                    .path("factura").path(String.valueOf(resultado.get("idFactura"))).path("ticket")
                    .build();
            return Response.created(location).entity(resultado).build();
        } catch (NoEncontradoException ex) {
            return Respuestas.noEncontrado(ex.getId());
        } catch (SolicitudCobroInvalidaException ex) {
            return Respuestas.invalido(ex.getWrongParam(), ex.getMessage());
        }
    }
}
