package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@Path("tipo_descuento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoDescuentoResource implements Serializable {

    @Inject
    private TipoDescuentoDAO tdDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        
        int maxEfectivo = Math.min(Math.max(1, max), 50);
        List<TipoDescuento> registros = tdDAO.findRange(first, maxEfectivo);
        long total = tdDAO.count();

        return Response.ok(registros)
                .header("Total-registros", total)
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        TipoDescuento encontrado = tdDAO.findById(id);
        if (encontrado != null) {
            return Response.ok(encontrado).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id)
                .build();
    }

    @POST
    public Response crear(TipoDescuento registro, @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdTipoDescuento(UUID.randomUUID());
        tdDAO.create(registro);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(registro.getIdTipoDescuento().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, TipoDescuento registro) {
        if (registro == null || id == null) {
            return Response.status(422).header("Wrong-Param", "id/registro").build();
        }
        TipoDescuento existente = tdDAO.findById(id);
        if (existente == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Entity-Not-Found", id).build();
        }
        registro.setIdTipoDescuento(id);
        TipoDescuento modificado = tdDAO.modify(registro);
        return Response.ok(modificado).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        TipoDescuento existente = tdDAO.findById(id);
        if (existente == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Entity-Not-Found", id).build();
        }
        tdDAO.delete(existente);
        return Response.noContent().build();
    }
}