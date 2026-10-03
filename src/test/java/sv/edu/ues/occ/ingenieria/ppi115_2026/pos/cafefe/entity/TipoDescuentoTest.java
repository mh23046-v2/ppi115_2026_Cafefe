package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;

/**
 *
 * @author johnyv
 */
public class TipoDescuentoTest {

    @Test
    @DisplayName("Verifica la declaración de limites de descuento (0 a 100) en el atributo descuentoMaximo")
    void testDeclaracionLimitesDescuentoMaximo() throws NoSuchFieldException {
        Field field = TipoDescuento.class.getDeclaredField("descuentoMaximo");

        Min minAnnotation = field.getAnnotation(Min.class);
        assertNotNull(minAnnotation, "El campo 'descuentoMaximo' debe tener la anotación @Min");
        assertEquals(0, minAnnotation.value(), "El valor mínimo declarado en @Min debe ser 0");

        Max maxAnnotation = field.getAnnotation(Max.class);
        assertNotNull(maxAnnotation, "El campo 'descuentoMaximo' debe tener la anotación @Max");
        assertEquals(100, maxAnnotation.value(), "El valor máximo declarado en @Max debe ser 100");
    }
}
