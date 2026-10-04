package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author hernandez
 */
public class EmpleadoTest {

    @Test
    @DisplayName("El ID es obligatorio (@NotNull)")
    void testIdObligatorio() throws NoSuchFieldException {
        Field field = Empleado.class.getDeclaredField("idEmpleado");

        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idEmpleado' debe tener @NotNull");
    }

    @Test
    @DisplayName("Nombre y apellido son obligatorios (@NotBlank) con máximo 255 caracteres (@Size)")
    void testValidacionesNombreYApellido() throws NoSuchFieldException {
        for (String campo : new String[]{"nombre", "apellido"}) {
            Field field = Empleado.class.getDeclaredField(campo);

            assertNotNull(field.getAnnotation(NotBlank.class), "El campo '" + campo + "' debe tener @NotBlank");
            Size size = field.getAnnotation(Size.class);
            assertNotNull(size, "El campo '" + campo + "' debe tener @Size");
            assertEquals(255, size.max(), "El máximo de '" + campo + "' debe coincidir con la BD (255)");
        }
    }

    @Test
    @DisplayName("Dos empleados con el mismo ID son iguales aunque cambien los demás datos")
    void testEqualsMismoId() {
        UUID id = UUID.randomUUID();
        Empleado a = new Empleado(id);
        Empleado b = new Empleado(id);
        a.setNombre("Ana");
        b.setNombre("Ana María");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode(), "Si son iguales, su hashCode debe coincidir");
    }

    @Test
    @DisplayName("Empleados con distinto ID, null u otra clase no son iguales")
    void testEqualsDistintos() {
        Empleado a = new Empleado(UUID.randomUUID());

        assertNotEquals(a, new Empleado(UUID.randomUUID()));
        assertNotEquals(null, a);
        assertNotEquals("no soy un Empleado", a);
    }

    @Test
    @DisplayName("El constructor con ID y los setters guardan los datos")
    void testConstructorYSetters() {
        UUID id = UUID.randomUUID();
        Empleado empleado = new Empleado(id);
        empleado.setNombre("Carlos");
        empleado.setApellido("Pérez");
        empleado.setActivo(true);
        empleado.setComentarios("Nuevo ingreso");

        assertEquals(id, empleado.getIdEmpleado());
        assertEquals("Carlos", empleado.getNombre());
        assertEquals("Pérez", empleado.getApellido());
        assertTrue(empleado.getActivo());
        assertEquals("Nuevo ingreso", empleado.getComentarios());
    }
}
