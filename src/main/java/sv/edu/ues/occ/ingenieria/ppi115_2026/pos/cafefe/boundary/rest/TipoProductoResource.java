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
import java.net.URI;
import java.util.List;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

@Path("tipo_producto")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoProductoResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    private TipoProductoDAO tipoProductoDAO;

    @Inject
    private ProductoDAO productoDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max,
            @QueryParam("soloActivos") @DefaultValue("false") boolean soloActivos) {
        if (first < 0 || max <= 0) {
            return paginacionInvalida(first);
        }
        int tope = Math.min(max, MAX_REGISTROS);
        List<TipoProducto> registros;
        long total;

        if (soloActivos) {
            registros = tipoProductoDAO.findActivos(first, tope);
            total = tipoProductoDAO.countActivos();
        } else {
            registros = tipoProductoDAO.findRange(first, tope);
            total = tipoProductoDAO.count();
        }

        return Response.ok(registros)
                .header("Total-registros", total)
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoProducto encontrado = tipoProductoDAO.findById(id);
        if (encontrado == null) {
            return noEncontrado(id);
        }
        return Response.ok(encontrado).build();
    }

    @GET
    @Path("{id}/producto")
    public Response productosActivos(
            @PathParam("id") UUID id,
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return paginacionInvalida(first);
        }
        TipoProducto tipo = tipoProductoDAO.findById(id);
        if (tipo == null) {
            return noEncontrado(id);
        }
        
        List<Producto> registros = productoDAO.findActivosByTipo(tipo, first, Math.min(max, MAX_REGISTROS));
        return Response.ok(registros)
                .header("Total-registros", productoDAO.countActivosByTipo(tipo))
                .build();
    }

    @POST
    public Response crear(TipoProducto entity, @Context UriInfo uriInfo) {
        if (entity == null || entity.getNombre() == null || entity.getNombre().trim().isEmpty()) {
            return Response.status(422).header("Wrong-Param", "entity").build();
        }
        
        // 1. Asignar ID antes de persistir
        entity.setIdTipoProducto(UUID.randomUUID());
        
        // 2. Default para activo si viene nulo
        if (entity.getActivo() == null) {
            entity.setActivo(Boolean.TRUE);
        }
        
        tipoProductoDAO.create(entity);
        
        URI location = uriInfo.getAbsolutePathBuilder().path(entity.getIdTipoProducto().toString()).build();
        return Response.created(location).entity(entity).build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, TipoProducto entity) {
        if (id == null || entity == null || entity.getIdTipoProducto() == null || !id.equals(entity.getIdTipoProducto())) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoProducto existente = tipoProductoDAO.findById(id);
        if (existente == null) {
            return noEncontrado(id);
        }
        
        TipoProducto modificado = tipoProductoDAO.modify(entity);
        return Response.ok(modificado != null ? modificado : entity).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        TipoProducto existente = tipoProductoDAO.findById(id);
        if (existente == null) {
            return noEncontrado(id);
        }
        tipoProductoDAO.delete(existente);
        return Response.noContent().build();
    }

    private static Response paginacionInvalida(int first) {
        return Response.status(422)
                .header("Wrong-Param", first < 0 ? "first" : "max")
                .build();
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id != null ? id.toString() : "null")
                .build();
    }
}