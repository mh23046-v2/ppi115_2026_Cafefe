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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

@Path("empleado/{idEmpleado}/rol")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmpleadoRolResource implements Serializable {

    static final int MAX_REGISTROS = 50;

    @Inject
    EmpleadoRolDAO empleadoRolDAO;

    @Inject
    EmpleadoDAO empleadoDAO;

    @Inject
    RolDAO rolDAO;

    @GET
    public Response findByEmpleado(
            @PathParam("idEmpleado") UUID idEmpleado,
            @QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (idEmpleado == null || first < 0 || max <= 0) {
            return Response.status(422)
                    .header("Wrong-Param", idEmpleado == null ? "idEmpleado" : (first < 0 ? "first" : "max"))
                    .build();
        }
        Empleado empleado = empleadoDAO.findById(idEmpleado);
        if (empleado == null) {
            return noEncontrado(idEmpleado);
        }

        List<EmpleadoRol> registros = empleadoRolDAO.findByEmpleado(empleado, first, Math.min(max, MAX_REGISTROS));
        int total = empleadoRolDAO.countByEmpleado(empleado);

        // Lista de mapas DTO para evitar proxies lazy de JPA al serializar
        List<Map<String, Object>> salida = registros.stream()
                .map(this::toDto)
                .toList();

        return Response.ok(salida)
                .header("Total-registros", total)
                .build();
    }

    @GET
    @Path("{id}")
    public Response findById(
            @PathParam("idEmpleado") UUID idEmpleado,
            @PathParam("id") UUID id) {
        if (idEmpleado == null || id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        EmpleadoRol encontrado = empleadoRolDAO.findById(id);
        if (!pertenece(encontrado, idEmpleado)) {
            return noEncontrado(id);
        }
        return Response.ok(toDto(encontrado)).build();
    }

    @POST
    public Response crear(
            @PathParam("idEmpleado") UUID idEmpleado,
            EmpleadoRol registro,
            @Context UriInfo uriInfo) {
        if (idEmpleado == null || registro == null || registro.getIdRol() == null || registro.getIdRol().getIdRol() == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        Empleado empleado = empleadoDAO.findById(idEmpleado);
        if (empleado == null) {
            return noEncontrado(idEmpleado);
        }
        UUID idRol = registro.getIdRol().getIdRol();
        Rol rol = rolDAO.findById(idRol);
        if (rol == null) {
            return Response.status(422).header("Wrong-Param", "idRol").build();
        }
        if (empleadoRolDAO.existeAsignacion(empleado, rol, null)) {
            return Response.status(422)
                    .header("Wrong-Param", "idRol")
                    .header("App-Error", "El empleado ya tiene asignado ese rol")
                    .build();
        }

        registro.setIdEmpleadoRol(UUID.randomUUID());
        registro.setIdEmpleado(empleado);
        registro.setIdRol(rol);
        if (registro.getActivo() == null) {
            registro.setActivo(Boolean.TRUE);
        }

        empleadoRolDAO.create(registro);
        return Response.created(uriInfo.getAbsolutePathBuilder()
                .path(registro.getIdEmpleadoRol().toString()).build())
                .entity(toDto(registro))
                .build();
    }

    @PUT
    @Path("{id}")
    public Response modificar(
            @PathParam("idEmpleado") UUID idEmpleado,
            @PathParam("id") UUID id,
            EmpleadoRol registro) {
        if (idEmpleado == null || id == null || registro == null) {
            return Response.status(422).header("Wrong-Param", "registro").build();
        }
        EmpleadoRol existente = empleadoRolDAO.findById(id);
        if (!pertenece(existente, idEmpleado)) {
            return noEncontrado(id);
        }

        if (registro.getIdRol() != null && registro.getIdRol().getIdRol() != null) {
            UUID nuevoIdRol = registro.getIdRol().getIdRol();
            Rol rol = rolDAO.findById(nuevoIdRol);
            if (rol == null) {
                return Response.status(422).header("Wrong-Param", "idRol").build();
            }
            if (empleadoRolDAO.existeAsignacion(existente.getIdEmpleado(), rol, id)) {
                return Response.status(422)
                        .header("Wrong-Param", "idRol")
                        .header("App-Error", "El empleado ya tiene asignado ese rol")
                        .build();
            }
            existente.setIdRol(rol);
        }

        existente.setObservaciones(registro.getObservaciones());
        if (registro.getActivo() != null) {
            existente.setActivo(registro.getActivo());
        }

        EmpleadoRol modificado = empleadoRolDAO.modify(existente);
        return Response.ok(toDto(modificado != null ? modificado : existente)).build();
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(
            @PathParam("idEmpleado") UUID idEmpleado,
            @PathParam("id") UUID id) {
        if (idEmpleado == null || id == null) {
            return Response.status(422).header("Wrong-Param", "id").build();
        }
        EmpleadoRol existente = empleadoRolDAO.findById(id);
        if (!pertenece(existente, idEmpleado)) {
            return noEncontrado(id);
        }
        empleadoRolDAO.delete(existente);
        return Response.noContent().build();
    }

    private Map<String, Object> toDto(EmpleadoRol er) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idEmpleadoRol", er.getIdEmpleadoRol());
        dto.put("activo", er.getActivo());
        dto.put("observaciones", er.getObservaciones());
        dto.put("idEmpleado", er.getIdEmpleado() != null ? er.getIdEmpleado().getIdEmpleado() : null);
        if (er.getIdRol() != null) {
            dto.put("idRol", er.getIdRol().getIdRol());
            dto.put("rol", er.getIdRol().getNombre());
        }
        return dto;
    }

    private static boolean pertenece(EmpleadoRol registro, UUID idEmpleado) {
        return registro != null
                && registro.getIdEmpleado() != null
                && idEmpleado.equals(registro.getIdEmpleado().getIdEmpleado());
    }

    private static Response noEncontrado(UUID id) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Entity-Not-Found", id != null ? id.toString() : "null")
                .build();
    }
}