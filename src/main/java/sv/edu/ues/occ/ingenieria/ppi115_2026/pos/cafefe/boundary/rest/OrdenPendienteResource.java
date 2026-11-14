package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.NoEncontradoException;

/**
 * HU3 (Cocina) y HU4 (Caja): órdenes que tienen productos sin cobrar.
 *
 * GET orden/pendiente?first=0&max=20 → lista + header Total-registros
 * GET orden/pendiente/{id}           → una orden con sus productos pendientes
 *
 * Una orden deja de estar pendiente cuando la caja cobra todos sus productos.
 * Convive con OrdenResource (@Path("orden")): JAX-RS elige la ruta más
 * específica, así que POST /orden sigue llegando al recurso de Johnny.
 */
@Path("orden/pendiente")
@Produces(MediaType.APPLICATION_JSON)
public class OrdenPendienteResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    CobroService cobroService;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("20") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        return Response.ok(cobroService.pendientes(first, Math.min(max, MAX_REGISTROS)))
                .header("Total-registros", cobroService.contarPendientes())
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        try {
            return Response.ok(cobroService.pendiente(id)).build();
        } catch (NoEncontradoException ex) {
            return Respuestas.noEncontrado(ex.getId());
        }
    }
}
