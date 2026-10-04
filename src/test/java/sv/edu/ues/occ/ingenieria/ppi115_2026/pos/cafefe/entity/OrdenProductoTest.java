package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author hernandez
 */
public class OrdenProductoTest {

    @Test
    @DisplayName("El ID es obligatorio (@NotNull)")
    void testIdObligatorio() throws NoSuchFieldException {
        Field field = OrdenProducto.class.getDeclaredField("idOrdenProducto");

        assertNotNull(field.getAnnotation(NotNull.class), "El campo 'idOrdenProducto' debe tener @NotNull");
    }

    @Test
    @DisplayName("Guarda la orden, el producto y el precio cobrado")
    void testSetters() {
        Orden orden = new Orden(UUID.randomUUID());
        Producto producto = new Producto(UUID.randomUUID());
        OrdenProducto detalle = new OrdenProducto(UUID.randomUUID());

        detalle.setIdOrden(orden);
        detalle.setIdProducto(producto);
        detalle.setPrecio(new BigDecimal("2.50"));
        detalle.setObservaciones("Sin azúcar");

        assertSame(orden, detalle.getIdOrden());
        assertSame(producto, detalle.getIdProducto());
        assertEquals(0, new BigDecimal("2.50").compareTo(detalle.getPrecio()));
        assertEquals("Sin azúcar", detalle.getObservaciones());
    }

    @Test
    @DisplayName("Dos detalles con el mismo ID son iguales")
    void testEqualsMismoId() {
        UUID id = UUID.randomUUID();
        OrdenProducto a = new OrdenProducto(id);
        OrdenProducto b = new OrdenProducto(id);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("Detalles con distinto ID, null u otra clase no son iguales")
    void testEqualsDistintos() {
        OrdenProducto a = new OrdenProducto(UUID.randomUUID());

        assertNotEquals(a, new OrdenProducto(UUID.randomUUID()));
        assertNotEquals(null, a);
        assertNotEquals("no soy un OrdenProducto", a);
    }
}
