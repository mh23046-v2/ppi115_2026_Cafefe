package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@Path("producto/{idProducto}/descuento")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DescuentoProductoResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    private DescuentoProductoDAO descuentoProductoDAO;

    @GET
    public Response findByProducto(
            @PathParam("idProducto") UUID idProducto,
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        Producto producto = new Producto(idProducto);
        List<DescuentoProducto> registros
                = descuentoProductoDAO.findByProducto(producto, first, Math.min(max, MAX_REGISTROS));
        return Response.ok(registros)
                .header("Total-registros", descuentoProductoDAO.countByProducto(producto))
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(
            @PathParam("idProducto") UUID idProducto,
            @PathParam("id") UUID id) {
        DescuentoProducto encontrado = descuentoProductoDAO.findById(id);
        if (!pertenece(encontrado, idProducto)) {
            return noEncontrado(id);
        }
        return Response.ok(encontrado).build();
    }

    @POST
    public Response crear(
            @PathParam("idProducto") UUID idProducto,
            @Valid DescuentoProducto registro,
            @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdDescuentoProducto(UUID.randomUUID());
        registro.setIdProducto(new Producto(idProducto));
        descuentoProductoDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdDescuentoProducto().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(
            @PathParam("idProducto") UUID idProducto,
            @PathParam("id") UUID id,
            @Valid DescuentoProducto registro) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        if (!pertenece(descuentoProductoDAO.findById(id), idProducto)) {
            return noEncontrado(id);
        }
        registro.setIdDescuentoProducto(id);
        registro.setIdProducto(new Producto(idProducto));
        return Response.ok(descuentoProductoDAO.modify(registro)).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(
            @PathParam("idProducto") UUID idProducto,
            @PathParam("id") UUID id) {
        DescuentoProducto existente = descuentoProductoDAO.findById(id);
        if (!pertenece(existente, idProducto)) {
            return noEncontrado(id);
        }
        descuentoProductoDAO.delete(existente);
        return Response.noContent().build();
    }

    private static boolean pertenece(DescuentoProducto registro, UUID idProducto) {
        return registro != null
                && registro.getIdProducto() != null
                && idProducto.equals(registro.getIdProducto().getIdProducto());
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id.toString())
                .build();
    }
}