package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class DescuentoModelTest {

    @Mock
    private DescuentoDAO descuentoDAO;

    @Mock
    private TipoDescuentoDAO tipoDescuentoDAO;

    @Mock
    private Etiquetas etiquetas;

    @InjectMocks
    private DescuentoModel descuentoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertNotNull(descuentoModel.getDAO());
        assertEquals(descuentoDAO, descuentoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad Descuento")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        Descuento entidad = new Descuento(expectedUuid);

        assertEquals(expectedUuid, descuentoModel.getId(entidad));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar una nueva entidad Descuento con ID no nulo")
    void testNuevoRegistro() {
        Descuento resultado = descuentoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdDescuento());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo del bean")
    void testNombreBean() {
        assertEquals("Descuento", descuentoModel.nombreBean());
    }

    @Test
    @DisplayName("cargarOpciones debe poblar la lista con los SelectItem deshabilitando los inactivos")
    void testCargarOpciones() {
        TipoDescuento tipoActivo = new TipoDescuento(UUID.randomUUID());
        tipoActivo.setActivo(true);

        TipoDescuento tipoInactivo = new TipoDescuento(UUID.randomUUID());
        tipoInactivo.setActivo(false);

        when(tipoDescuentoDAO.findAll()).thenReturn(List.of(tipoActivo, tipoInactivo));
        when(etiquetas.tipoDescuento(tipoActivo)).thenReturn("Temporada");
        when(etiquetas.tipoDescuento(tipoInactivo)).thenReturn("Especial");

        descuentoModel.cargarOpciones();

        assertNotNull(descuentoModel.getOpcionesIdTipoDescuento());
        assertEquals(2, descuentoModel.getOpcionesIdTipoDescuento().size());
        
        // Verifica que la opción inactiva quede deshabilitada (isDisabled == true)
        assertFalse(descuentoModel.getOpcionesIdTipoDescuento().get(0).isDisabled());
        assertTrue(descuentoModel.getOpcionesIdTipoDescuento().get(1).isDisabled());

        verify(tipoDescuentoDAO).findAll();
    }

    @Test
    @DisplayName("cargarOpciones maneja una lista vacía de TipoDescuento")
    void testCargarOpcionesVacio() {
        when(tipoDescuentoDAO.findAll()).thenReturn(Collections.emptyList());

        descuentoModel.cargarOpciones();

        assertNotNull(descuentoModel.getOpcionesIdTipoDescuento());
        assertTrue(descuentoModel.getOpcionesIdTipoDescuento().isEmpty());
    }
}