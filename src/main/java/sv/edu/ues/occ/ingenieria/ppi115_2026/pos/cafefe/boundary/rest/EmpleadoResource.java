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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;

@Path("empleado")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmpleadoResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    EmpleadoDAO empleadoDAO;

    @GET
    public Response findRange(
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max,
            @QueryParam("nombre") String nombre) {
        if (first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", first < 0 ? "first" : "max")
                    .build();
        }
        int tope = Math.min(max, MAX_REGISTROS);

        List<Empleado> registros = empleadoDAO.findByNombre(nombre, first, tope);
        Long total = empleadoDAO.countByNombre(nombre);

        return Response.ok(registros)
                .header("Total-registros", total != null ? total : 0L)
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        Empleado encontrado = empleadoDAO.findById(id);
        if (encontrado == null) {
            return noEncontrado(id);
        }
        return Response.ok(encontrado).build();
    }

    @POST
    public Response crear(Empleado registro, @Context UriInfo uriInfo) {
        if (registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        registro.setIdEmpleado(UUID.randomUUID());
        if (registro.getActivo() == null) {
            registro.setActivo(Boolean.TRUE);
        }
        empleadoDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdEmpleado().toString()).build())
                .entity(registro)
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(@PathParam("id") UUID id, Empleado registro) {
        if (id == null || registro == null) {
            return Response.status(422).header("Wrong-Param", id == null ? "id" : "registro").build();
        }
        Empleado existente = empleadoDAO.findById(id);
        if (existente == null) {
            return noEncontrado(id);
        }

        existente.setNombre(registro.getNombre());
        existente.setApellido(registro.getApellido());
        existente.setComentarios(registro.getComentarios());
        if (registro.getActivo() != null) {
            existente.setActivo(registro.getActivo());
        }

        Empleado modificado = empleadoDAO.modify(existente);
        return Response.ok(modificado != null ? modificado : existente).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        if (id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        Empleado existente = empleadoDAO.findById(id);
        if (existente == null) {
            return noEncontrado(id);
        }
        empleadoDAO.delete(existente);
        return Response.noContent().build();
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id != null ? id.toString() : "null")
                .build();
    }
}