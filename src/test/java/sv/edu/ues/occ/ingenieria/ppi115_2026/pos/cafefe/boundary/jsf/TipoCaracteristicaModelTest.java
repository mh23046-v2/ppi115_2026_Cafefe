package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.TipoCaracteristicaModel;
import static org.junit.jupiter.api.Assertions.*;
import jakarta.faces.validator.ValidatorException;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@ExtendWith(MockitoExtension.class)
class TipoCaracteristicaModelTest {

    @Mock
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @InjectMocks
    private TipoCaracteristicaModel tipoCaracteristicaModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertNotNull(tipoCaracteristicaModel.getDAO());
        assertEquals(tipoCaracteristicaDAO, tipoCaracteristicaModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        TipoCaracteristica entidad = new TipoCaracteristica(expectedUuid);

        assertEquals(expectedUuid, tipoCaracteristicaModel.getId(entidad));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar entidad con ID no nulo, activo en true y regex '.*'")
    void testNuevoRegistro() {
        TipoCaracteristica resultado = tipoCaracteristicaModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoCaracteristica());
        assertTrue(resultado.getActivo());
        assertEquals(".*", resultado.getExpresionRegular());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Tipo de Característica", tipoCaracteristicaModel.nombreBean());
    }

    @Test
    @DisplayName("validarRegistro rechaza nombres nulos, vacíos o con espacios en blanco")
    void testValidarRegistroNombreInvalido() {
        TipoCaracteristica tc = new TipoCaracteristica(UUID.randomUUID());

        // 1. Nombre nulo
        tc.setNombre(null);
        assertNotNull(tipoCaracteristicaModel.validarRegistro(tc));

        // 2. Nombre en blanco
        tc.setNombre("   ");
        assertNotNull(tipoCaracteristicaModel.validarRegistro(tc));
    }

    @Test
    @DisplayName("validarRegistro rechaza expresiones regulares con sintaxis inválida")
    void testValidarRegistroRegexInvalida() {
        TipoCaracteristica tc = new TipoCaracteristica(UUID.randomUUID());
        tc.setNombre("Tueste");
        tc.setExpresionRegular("[a-z"); // Expresión mal formada (falta corchete de cierre)

        String error = tipoCaracteristicaModel.validarRegistro(tc);
        assertNotNull(error);
        assertTrue(error.contains("expresión regular no es válida"));
    }

    @Test
    @DisplayName("validarRegistro retorna null cuando la entidad cumple todas las reglas")
    void testValidarRegistroExitoso() {
        TipoCaracteristica tc = new TipoCaracteristica(UUID.randomUUID());
        tc.setNombre("Tueste");
        tc.setExpresionRegular("^[0-9]+$");

        assertNull(tipoCaracteristicaModel.validarRegistro(tc));
    }

    @Test
    @DisplayName("validarExpresionRegular lanza ValidatorException si el valor no es una Regex válida")
    void testValidarExpresionRegularUIInvalida() {
        assertThrows(ValidatorException.class, () -> 
            tipoCaracteristicaModel.validarExpresionRegular(null, null, "[0-9")
        );
    }

    @Test
    @DisplayName("validarExpresionRegular no lanza excepción si la Regex es válida o nula/vacía")
    void testValidarExpresionRegularUIExitoso() {
        assertDoesNotThrow(() -> tipoCaracteristicaModel.validarExpresionRegular(null, null, null));
        assertDoesNotThrow(() -> tipoCaracteristicaModel.validarExpresionRegular(null, null, ""));
        assertDoesNotThrow(() -> tipoCaracteristicaModel.validarExpresionRegular(null, null, ".*"));
    }
}