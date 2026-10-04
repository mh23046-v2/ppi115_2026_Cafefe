package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author hernandez
 */
public class EmpleadoRolTest {

    @Test
    @DisplayName("El ID es obligatorio (@NotNull)")
    void testIdObligatorio() throws NoSuchFieldException {
        Field field = EmpleadoRol.class.getDeclaredField("idEmpleadoRol");

        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idEmpleadoRol' debe tener @NotNull");
    }

    @Test
    @DisplayName("El rol es obligatorio: no se puede asignar un rol vacío (@NotNull)")
    void testRolObligatorio() throws NoSuchFieldException {
        Field field = EmpleadoRol.class.getDeclaredField("idRol");

        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idRol' debe tener @NotNull");
    }

    @Test
    @DisplayName("Guarda el empleado y el rol que relaciona")
    void testRelaciones() {
        Empleado empleado = new Empleado(UUID.randomUUID());
        Rol rol = new Rol(UUID.randomUUID());
        EmpleadoRol asignacion = new EmpleadoRol(UUID.randomUUID());

        asignacion.setIdEmpleado(empleado);
        asignacion.setIdRol(rol);
        asignacion.setActivo(true);

        assertSame(empleado, asignacion.getIdEmpleado());
        assertSame(rol, asignacion.getIdRol());
        assertTrue(asignacion.getActivo());
    }

    @Test
    @DisplayName("Dos asignaciones con el mismo ID son iguales")
    void testEqualsMismoId() {
        UUID id = UUID.randomUUID();
        EmpleadoRol a = new EmpleadoRol(id);
        EmpleadoRol b = new EmpleadoRol(id);
        b.setObservaciones("Otra observación");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Asignaciones con distinto ID, null u otra clase no son iguales")
    void testEqualsDistintos() {
        EmpleadoRol a = new EmpleadoRol(UUID.randomUUID());

        assertNotEquals(a, new EmpleadoRol(UUID.randomUUID()));
        assertNotEquals(null, a);
        assertNotEquals("no soy un EmpleadoRol", a);
    }
}
