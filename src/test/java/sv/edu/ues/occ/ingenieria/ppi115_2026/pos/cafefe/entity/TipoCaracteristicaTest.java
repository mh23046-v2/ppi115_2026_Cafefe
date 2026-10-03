package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.lang.reflect.Method;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author johnyv
 */
public class TipoCaracteristicaTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("Una expresión regular vacía o en blanco se reemplaza por .*")
    void asignarExpresionPorDefectoCuandoVacia(String regex) {
        TipoCaracteristica tipo = new TipoCaracteristica(UUID.randomUUID());
        tipo.setActivo(true);
        tipo.setExpresionRegular(regex);
        tipo.aplicarExpresionPorDefecto();
        assertEquals(".*", tipo.getExpresionRegular());
    }

    @Test
    @DisplayName("Una expresión regular indicada por el usuario se conserva")
    void conservarExpresionIndicada() {
        TipoCaracteristica tipo = new TipoCaracteristica(UUID.randomUUID());
        tipo.setExpresionRegular("^\\d+$");
        tipo.aplicarExpresionPorDefecto();
        assertEquals("^\\d+$", tipo.getExpresionRegular());
    }

    @Test
    @DisplayName("La regla de expresión por defecto se ejecuta antes de insertar y de actualizar")
    void testExpresionRegularAntesDeInsertarActualizar() throws NoSuchMethodException {
        Method metodo = TipoCaracteristica.class.getDeclaredMethod("aplicarExpresionPorDefecto");
        assertTrue(metodo.isAnnotationPresent(PrePersist.class));
        assertTrue(metodo.isAnnotationPresent(PreUpdate.class));
    }

    @Test
    @DisplayName("Prueba de igualdades con equals y hashCode")
    void probarEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        TipoCaracteristica tipo1 = new TipoCaracteristica(id);
        TipoCaracteristica tipo2 = new TipoCaracteristica(id);

        assertEquals(tipo1, tipo2);
        assertEquals(tipo1.hashCode(), tipo2.hashCode());
    }
}
