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
public class TipoProductoTest {
    
    @Test
    @DisplayName("El ID es obligatorio")
    void testIdObligatorio() throws NoSuchFieldException {
        Field field = TipoProducto.class.getDeclaredField("idTipoProducto");
 
        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idTipoProducto' debe tener @NotNull");
    }
 
    @Test
    @DisplayName("El nombre es obligatorio (@NotBlank) y tiene máximo 155 caracteres (@Size)")
    void testValidacionesNombre() throws NoSuchFieldException {
        Field field = TipoProducto.class.getDeclaredField("nombre");
 
        assertNotNull(field.getAnnotation(NotBlank.class), "El campo 'nombre' debe tener @NotBlank");
        Size size = field.getAnnotation(Size.class);
        assertNotNull(size, "El campo 'nombre' debe tener @Size");
        assertEquals(155, size.max(), "El máximo debe coincidir con la columna de la BD (155)");
    }
 
    @Test
    @DisplayName("Dos objetos con el mismo ID son iguales aunque cambien los demás datos")
    void testEqualsMismoId() {
        UUID id = UUID.randomUUID();
        TipoProducto a = new TipoProducto(id);
        TipoProducto b = new TipoProducto(id);
        a.setNombre("Bebidas");
        b.setNombre("Bebidas (editado)");
 
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode(), "Si son iguales, su hashCode debe coincidir");
    }
 
    @Test
    @DisplayName("Objetos con distinto ID, null u otra clase no son iguales")
    void testEqualsDistintos() {
        TipoProducto a = new TipoProducto(UUID.randomUUID());
 
        assertNotEquals(a, new TipoProducto(UUID.randomUUID()));
        assertNotEquals(null, a);
        assertNotEquals("no soy un TipoProducto", a);
    }
 
    @Test
    @DisplayName("El constructor con ID y los setters guardan los datos")
    void testConstructorYSetters() {
        UUID id = UUID.randomUUID();
        TipoProducto tipo = new TipoProducto(id);
        tipo.setNombre("Postres");
        tipo.setActivo(false);
        tipo.setObservaciones("Solo fines de semana");
 
        assertEquals(id, tipo.getIdTipoProducto());
        assertEquals("Postres", tipo.getNombre());
        assertFalse(tipo.getActivo());
        assertEquals("Solo fines de semana", tipo.getObservaciones());
    }
}
