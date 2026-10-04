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
public class RolTest {

    @Test
    @DisplayName("El ID es obligatorio (@NotNull)")
    void testIdObligatorio() throws NoSuchFieldException {
        Field field = Rol.class.getDeclaredField("idRol");

        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idRol' debe tener @NotNull");
    }

    @Test
    @DisplayName("El nombre es obligatorio (@NotBlank) y tiene máximo 155 caracteres (@Size)")
    void testValidacionesNombre() throws NoSuchFieldException {
        Field field = Rol.class.getDeclaredField("nombre");

        assertNotNull(field.getAnnotation(NotBlank.class), "El campo 'nombre' debe tener @NotBlank");
        Size size = field.getAnnotation(Size.class);
        assertNotNull(size, "El campo 'nombre' debe tener @Size");
        assertEquals(155, size.max(), "El máximo debe coincidir con la columna de la BD (155)");
    }

    @Test
    @DisplayName("Dos roles con el mismo ID son iguales aunque cambien los demás datos")
    void testEqualsMismoId() {
        UUID id = UUID.randomUUID();
        Rol a = new Rol(id);
        Rol b = new Rol(id);
        a.setNombre("Camarero");
        b.setNombre("Mesero");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode(), "Si son iguales, su hashCode debe coincidir");
    }

    @Test
    @DisplayName("Roles con distinto ID, null u otra clase no son iguales")
    void testEqualsDistintos() {
        Rol a = new Rol(UUID.randomUUID());

        assertNotEquals(a, new Rol(UUID.randomUUID()));
        assertNotEquals(null, a);
        assertNotEquals("no soy un Rol", a);
    }

    @Test
    @DisplayName("El constructor con ID y los setters guardan los datos")
    void testConstructorYSetters() {
        UUID id = UUID.randomUUID();
        Rol rol = new Rol(id);
        rol.setNombre("Cajero");
        rol.setActivo(false);
        rol.setObservaciones("Turno de la tarde");

        assertEquals(id, rol.getIdRol());
        assertEquals("Cajero", rol.getNombre());
        assertFalse(rol.getActivo());
        assertEquals("Turno de la tarde", rol.getObservaciones());
    }
}
