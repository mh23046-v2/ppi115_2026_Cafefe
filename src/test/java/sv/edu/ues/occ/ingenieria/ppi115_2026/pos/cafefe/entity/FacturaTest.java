package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FacturaTest {

    @Test
    @DisplayName("Fecha, estado, caja y cajero son obligatorios (@NotNull con mensaje propio)")
    void testObligatorios() throws NoSuchFieldException {
        for (String campo : new String[]{"idFactura", "fechaFacturacion", "estado", "idCaja", "idEmpleadoRol"}) {
            Field f = Factura.class.getDeclaredField(campo);
            assertNotNull(f.getAnnotation(NotNull.class), "El campo '" + campo + "' debe tener @NotNull");
        }
        assertFalse(Factura.class.getDeclaredField("idCaja").getAnnotation(NotNull.class).message().startsWith("{"),
                "La caja debe tener un mensaje legible, no el de por defecto");
    }

    @Test
    @DisplayName("Cliente hasta 155 caracteres y estado hasta 20, como en la base")
    void testLargos() throws NoSuchFieldException {
        assertEquals(155, Factura.class.getDeclaredField("cliente").getAnnotation(Size.class).max());
        assertEquals(20, Factura.class.getDeclaredField("estado").getAnnotation(Size.class).max());
    }

    @Test
    @DisplayName("Dos facturas con el mismo ID son iguales; con distinto ID no")
    void testEquals() {
        UUID id = UUID.randomUUID();
        assertEquals(new Factura(id), new Factura(id));
        assertEquals(new Factura(id).hashCode(), new Factura(id).hashCode());
        assertNotEquals(new Factura(id), new Factura(UUID.randomUUID()));
        assertNotEquals(null, new Factura(id));
    }
}
