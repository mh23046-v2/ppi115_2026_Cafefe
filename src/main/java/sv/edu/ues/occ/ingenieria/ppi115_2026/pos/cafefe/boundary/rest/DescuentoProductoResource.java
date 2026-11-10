package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@Path("descuento_producto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DescuentoProductoResource implements Serializable {

    @Inject
    private DescuentoProductoDAO descuentoProductoDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {

        int maxEfectivo = Math.min(Math.max(1, max), 50);
        List<DescuentoProducto> registros = descuentoProductoDAO.findRange(first, maxEfectivo);
        long total = descuentoProductoDAO.count();

        return Response.ok(registros)
                .header("Total-registros", total)
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        DescuentoProducto encontrado = descuentoProductoDAO.findById(id);
        if (encontrado != null) {
            return Response.ok(encontrado).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id)
                .build();
    }

    // Endpoint especializado apoyado en la named query del DAO
    @GET
    @Path("producto/{idProducto}")
    public Response findByProducto(
            @PathParam("idProducto") UUID idProducto,
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {

        if (idProducto == null) {
            return Response.status(422).header("Wrong-Param", "idProducto").build();
        }

        int maxEfectivo = Math.min(Math.max(1, max), 50);
        Producto p = new Producto(idProducto);

        List<DescuentoProducto> registros = descuentoProductoDAO.findByProducto(p, first, maxEfectivo);
        int total = descuentoProductoDAO.countByProducto(p);

        return Response.ok(registros)
                .header("Total-registros", total)
                .build();
    }

    @POST
    public Response crear(DescuentoProducto registro, @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdDescuentoProducto(UUID.randomUUID());
        descuentoProductoDAO.create(registro);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(registro.getIdDescuentoProducto().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, DescuentoProducto registro) {
        if (registro == null || id == null) {
            return Response.status(422).header("Wrong-Param", "id/registro").build();
        }
        DescuentoProducto existente = descuentoProductoDAO.findById(id);
        if (existente == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Entity-Not-Found", id).build();
        }
        registro.setIdDescuentoProducto(id);
        DescuentoProducto modificado = descuentoProductoDAO.modify(registro);
        return Response.ok(modificado).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        DescuentoProducto existente = descuentoProductoDAO.findById(id);
        if (existente == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Entity-Not-Found", id).build();
        }
        descuentoProductoDAO.delete(existente);
        return Response.noContent().build();
    }
}