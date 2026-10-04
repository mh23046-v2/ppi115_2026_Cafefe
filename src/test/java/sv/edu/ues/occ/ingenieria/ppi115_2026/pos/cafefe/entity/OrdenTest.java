package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author hernandez
 */
public class OrdenTest {

    @Test
    @DisplayName("El ID es obligatorio (@NotNull)")
    void testIdObligatorio() throws NoSuchFieldException {
        Field field = Orden.class.getDeclaredField("idOrden");

        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idOrden' debe tener @NotNull");
    }

    @Test
    @DisplayName("Guarda la fecha de creación y el empleado (con su rol) que tomó la orden")
    void testSetters() {
        UUID id = UUID.randomUUID();
        Date ahora = new Date();
        EmpleadoRol camarero = new EmpleadoRol(UUID.randomUUID());
        Orden orden = new Orden(id);

        orden.setFechaCreacion(ahora);
        orden.setIdEmpleadoRol(camarero);

        assertEquals(id, orden.getIdOrden());
        assertEquals(ahora, orden.getFechaCreacion());
        assertSame(camarero, orden.getIdEmpleadoRol());
    }

    @Test
    @DisplayName("Dos órdenes con el mismo ID son iguales")
    void testEqualsMismoId() {
        UUID id = UUID.randomUUID();
        Orden a = new Orden(id);
        Orden b = new Orden(id);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Órdenes con distinto ID, null u otra clase no son iguales")
    void testEqualsDistintos() {
        Orden a = new Orden(UUID.randomUUID());

        assertNotEquals(a, new Orden(UUID.randomUUID()));
        assertNotEquals(null, a);
        assertNotEquals("no soy una Orden", a);
    }
}
