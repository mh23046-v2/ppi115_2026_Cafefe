package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FacturaOrdenProductoTest {

    @Test
    @DisplayName("El precio es obligatorio, no negativo y con 2 decimales (igual que OrdenProducto)")
    void testPrecio() throws NoSuchFieldException {
        Field precio = FacturaOrdenProducto.class.getDeclaredField("precio");
        assertNotNull(precio.getAnnotation(NotNull.class));
        assertEquals("0.00", precio.getAnnotation(DecimalMin.class).value());
        assertEquals(2, precio.getAnnotation(Digits.class).fraction());
    }

    @Test
    @DisplayName("Debe pertenecer a una factura y a un producto de orden")
    void testRelacionesObligatorias() throws NoSuchFieldException {
        assertNotNull(FacturaOrdenProducto.class.getDeclaredField("idFactura").getAnnotation(NotNull.class));
        assertNotNull(FacturaOrdenProducto.class.getDeclaredField("idOrdenProducto").getAnnotation(NotNull.class));
    }

    @Test
    @DisplayName("Guarda la factura, el producto de la orden y el precio")
    void testSetters() {
        Factura f = new Factura(UUID.randomUUID());
        OrdenProducto op = new OrdenProducto(UUID.randomUUID());
        FacturaOrdenProducto fop = new FacturaOrdenProducto(UUID.randomUUID());
        fop.setIdFactura(f);
        fop.setIdOrdenProducto(op);
        fop.setPrecio(new BigDecimal("3.50"));

        assertSame(f, fop.getIdFactura());
        assertSame(op, fop.getIdOrdenProducto());
        assertEquals(new BigDecimal("3.50"), fop.getPrecio());
    }
}
