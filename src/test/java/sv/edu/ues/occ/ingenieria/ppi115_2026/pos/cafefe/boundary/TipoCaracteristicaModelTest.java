package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import static org.junit.jupiter.api.Assertions.*;

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
}