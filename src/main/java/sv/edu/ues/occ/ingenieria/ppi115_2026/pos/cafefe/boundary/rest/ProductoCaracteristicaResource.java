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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;

@Path("producto/{idProducto}/caracteristica")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductoCaracteristicaResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Inject
    private CaracteristicaDAO caracteristicaDAO;

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
        List<ProductoCaracteristica> registros = productoCaracteristicaDAO.findByProducto(producto, first, Math.min(max, MAX_REGISTROS));
        return Response.ok(registros)
                .header("Total-registros", productoCaracteristicaDAO.countByProducto(producto))
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(
            @PathParam("idProducto") UUID idProducto,
            @PathParam("id") UUID id) {
        ProductoCaracteristica encontrado = productoCaracteristicaDAO.findById(id);
        if (!pertenece(encontrado, idProducto)) {
            return noEncontrado(id);
        }
        return Response.ok(encontrado).build();
    }

    @POST
    public Response crear(
            @PathParam("idProducto") UUID idProducto,
            @Valid ProductoCaracteristica registro,
            @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdProductoCaracteristica(UUID.randomUUID());
        registro.setIdProducto(new Producto(idProducto));

        Response validacion = validarReglas(registro);
        if (validacion != null) {
            return validacion;
        }

        productoCaracteristicaDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdProductoCaracteristica().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(
            @PathParam("idProducto") UUID idProducto,
            @PathParam("id") UUID id,
            @Valid ProductoCaracteristica registro) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        if (!pertenece(productoCaracteristicaDAO.findById(id), idProducto)) {
            return noEncontrado(id);
        }
        registro.setIdProductoCaracteristica(id);
        registro.setIdProducto(new Producto(idProducto));

        Response validacion = validarReglas(registro);
        if (validacion != null) {
            return validacion;
        }

        return Response.ok(productoCaracteristicaDAO.modify(registro)).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(
            @PathParam("idProducto") UUID idProducto,
            @PathParam("id") UUID id) {
        ProductoCaracteristica existente = productoCaracteristicaDAO.findById(id);
        if (!pertenece(existente, idProducto)) {
            return noEncontrado(id);
        }
        productoCaracteristicaDAO.delete(existente);
        return Response.noContent().build();
    }

    private Response validarReglas(ProductoCaracteristica registro) {
        if (registro.getIdCaracteristica() != null && registro.getIdCaracteristica().getIdCaracteristica() != null) {
            Caracteristica c = caracteristicaDAO.findById(registro.getIdCaracteristica().getIdCaracteristica());
            registro.setIdCaracteristica(c);
        }
        String error = ReglasProductoCaracteristica.validar(registro);
        if (error != null) {
            return Response.status(422).header("Wrong-Param", error).build();
        }
        return null;
    }

    private static boolean pertenece(ProductoCaracteristica registro, UUID idProducto) {
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