package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.TipoDescuentoModel;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@ExtendWith(MockitoExtension.class)
class TipoDescuentoModelTest {

    @Mock
    private TipoDescuentoDAO tipoDescuentoDAO;

    @InjectMocks
    private TipoDescuentoModel tipoDescuentoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertNotNull(tipoDescuentoModel.getDAO());
        assertEquals(tipoDescuentoDAO, tipoDescuentoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        TipoDescuento entidad = new TipoDescuento(expectedUuid);

        assertEquals(expectedUuid, tipoDescuentoModel.getId(entidad));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar la entidad con ID no nulo, activo en true y descuento máximo en 50")
    void testNuevoRegistro() {
        TipoDescuento resultado = tipoDescuentoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoDescuento());
        assertTrue(resultado.getActivo());
        assertEquals(50, resultado.getDescuentoMaximo());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Tipo de Descuento", tipoDescuentoModel.nombreBean());
    }

    @Test
    @DisplayName("validarRegistro rechaza nombres nulos, vacíos o solo con espacios")
    void testValidarRegistroNombreInvalido() {
        TipoDescuento td = new TipoDescuento(UUID.randomUUID());
        td.setDescuentoMaximo(20);

        // 1. Nombre nulo
        td.setNombre(null);
        String err1 = tipoDescuentoModel.validarRegistro(td);
        assertNotNull(err1);
        assertTrue(err1.contains("obligatorio"));

        // 2. Nombre en blanco
        td.setNombre("   ");
        String err2 = tipoDescuentoModel.validarRegistro(td);
        assertNotNull(err2);
        assertTrue(err2.contains("obligatorio"));
    }

    @Test
    @DisplayName("validarRegistro rechaza porcentajes de descuento fuera del rango de 1 a 100")
    void testValidarRegistroDescuentoMaximoInvalido() {
        TipoDescuento td = new TipoDescuento(UUID.randomUUID());
        td.setNombre("Descuento Temporada");

        // 1. Descuento máximo igual a 0
        td.setDescuentoMaximo(0);
        String err1 = tipoDescuentoModel.validarRegistro(td);
        assertNotNull(err1);
        assertTrue(err1.contains("entre 1 y 100"));

        // 2. Descuento máximo negativo
        td.setDescuentoMaximo(-10);
        String err2 = tipoDescuentoModel.validarRegistro(td);
        assertNotNull(err2);

        // 3. Descuento máximo mayor a 100
        td.setDescuentoMaximo(101);
        String err3 = tipoDescuentoModel.validarRegistro(td);
        assertNotNull(err3);
    }

    @Test
    @DisplayName("validarRegistro retorna null cuando el tipo de descuento es válido")
    void testValidarRegistroExitoso() {
        TipoDescuento td = new TipoDescuento(UUID.randomUUID());
        td.setNombre("Descuento Navideño");

        // Frontera inferior (1%)
        td.setDescuentoMaximo(1);
        assertNull(tipoDescuentoModel.validarRegistro(td));

        // Valor medio (50%)
        td.setDescuentoMaximo(50);
        assertNull(tipoDescuentoModel.validarRegistro(td));

        // Frontera superior (100%)
        td.setDescuentoMaximo(100);
        assertNull(tipoDescuentoModel.validarRegistro(td));
    }
}