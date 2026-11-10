package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.json.bind.annotation.JsonbDateFormat;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;

@Path("descuento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DescuentoResource implements Serializable {

    @Inject
    private DescuentoDAO descuentoDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {

        int maxEfectivo = Math.min(Math.max(1, max), 50);
        List<Descuento> registros = descuentoDAO.findRange(first, maxEfectivo);
        long total = descuentoDAO.count();

        return Response.ok(registros)
                .header("Total-registros", total)
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        Descuento encontrado = descuentoDAO.findById(id);
        if (encontrado != null) {
            return Response.ok(encontrado).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id)
                .build();
    }

    @POST
    public Response crear(Descuento registro, @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdDescuento(UUID.randomUUID());
        descuentoDAO.create(registro);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(registro.getIdDescuento().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, Descuento registro) {
        if (registro == null || id == null) {
            return Response.status(422).header("Wrong-Param", "id/registro").build();
        }
        Descuento existente = descuentoDAO.findById(id);
        if (existente == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Entity-Not-Found", id).build();
        }
        registro.setIdDescuento(id);
        Descuento modificado = descuentoDAO.modify(registro);
        return Response.ok(modificado).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        Descuento existente = descuentoDAO.findById(id);
        if (existente == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Entity-Not-Found", id).build();
        }
        descuentoDAO.delete(existente);
        return Response.noContent().build();
    }
}
