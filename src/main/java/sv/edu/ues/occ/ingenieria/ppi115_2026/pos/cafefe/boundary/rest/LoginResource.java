package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ReglasOrden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;

@Path("login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String MENSAJE_INVALIDO = "Credenciales inválidas";

    @Inject
    EmpleadoDAO empleadoDAO;

    @Inject
    EmpleadoRolDAO empleadoRolDAO;

    public static class LoginRequest {

        public String nombre;
        public String uid;
    }

    @POST
    public Response autenticar(LoginRequest request) {
        if (request == null || request.nombre == null || request.nombre.isBlank()
                || request.uid == null || request.uid.isBlank()) {
            return invalido();
        }

        UUID idEmpleado;
        try {
            idEmpleado = UUID.fromString(request.uid.trim());
        } catch (IllegalArgumentException e) {
            return invalido();
        }

        Empleado empleado = empleadoDAO.findById(idEmpleado);
        if (empleado == null || !Boolean.TRUE.equals(empleado.getActivo())
                || !coincideNombre(request.nombre, empleado)) {
            return invalido();
        }

        List<EmpleadoRol> rolesActivos = empleadoRolDAO.findActivosByEmpleadoId(idEmpleado).stream()
                .filter(er -> !ReglasOrden.esRolEnLinea(er.getIdRol()))
                .toList();
        if (rolesActivos.isEmpty()) {
            return invalido();
        }

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("empleado", empleadoDto(empleado));
        respuesta.put("roles", rolesActivos.stream().map(LoginResource::rolDto).toList());
        return Response.ok(respuesta).build();
    }

    private static Map<String, Object> empleadoDto(Empleado empleado) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idEmpleado", empleado.getIdEmpleado().toString());
        dto.put("nombre", empleado.getNombre());
        dto.put("apellido", empleado.getApellido());
        return dto;
    }

    private static Map<String, Object> rolDto(EmpleadoRol er) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("idEmpleadoRol", er.getIdEmpleadoRol().toString());
        dto.put("idRol", er.getIdRol().getIdRol().toString());
        dto.put("rol", er.getIdRol().getNombre());
        return dto;
    }

    private static Response invalido() {
        return ErrorMapper.construir(401, MENSAJE_INVALIDO);
    }

    static boolean coincideNombre(String entrada, Empleado empleado) {
        String buscado = ReglasOrden.normalizar(entrada);
        String soloNombre = ReglasOrden.normalizar(empleado.getNombre());
        String nombreCompleto = ReglasOrden.normalizar(empleado.getNombre() + " " + empleado.getApellido());
        return buscado.equals(soloNombre) || buscado.equals(nombreCompleto);
    }
}