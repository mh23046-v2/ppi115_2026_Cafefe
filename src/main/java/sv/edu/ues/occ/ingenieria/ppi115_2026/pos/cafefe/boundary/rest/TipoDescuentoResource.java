package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@Path("tipo_descuento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoDescuentoResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    private TipoDescuentoDAO tdDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        List<TipoDescuento> registros = tdDAO.findRange(first, Math.min(max, MAX_REGISTROS));
        return Response.ok(registros)
                .header("Total-registros", tdDAO.count())
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoDescuento encontrado = tdDAO.findById(id);
        if (encontrado == null) {
            return noEncontrado(id);
        }
        return Response.ok(encontrado).build();
    }

    @POST
    public Response crear(TipoDescuento registro, @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdTipoDescuento(UUID.randomUUID());
        tdDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdTipoDescuento().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, TipoDescuento registro) {
        if (id == null || registro == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        if (tdDAO.findById(id) == null) {
            return noEncontrado(id);
        }
        registro.setIdTipoDescuento(id);
        TipoDescuento modificado = tdDAO.modify(registro);
        return Response.ok(modificado != null ? modificado : registro).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoDescuento existente = tdDAO.findById(id);
        if (existente == null) {
            return noEncontrado(id);
        }
        tdDAO.delete(existente);
        return Response.noContent().build();
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id != null ? id.toString() : "null")
                .build();
    }
}