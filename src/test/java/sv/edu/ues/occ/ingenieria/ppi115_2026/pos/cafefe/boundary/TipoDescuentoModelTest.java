package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class TipoDescuentoModelTest {
    
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
    @DisplayName("nuevoRegistro debe inicializar la entidad con ID no nulo y valores por defecto")
    void testNuevoRegistro() {
        TipoDescuento resultado = tipoDescuentoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdTipoDescuento());
        assertTrue(resultado.getActivo());
        assertTrue(resultado.getDescuentoMaximo() >= 0 && resultado.getDescuentoMaximo() <= 100);
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Tipo de Descuento", tipoDescuentoModel.nombreBean());
    }
    
}
