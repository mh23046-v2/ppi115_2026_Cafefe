package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class CaracteristicaModelTest {

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Mock
    private Etiquetas etiquetas;

    @InjectMocks
    private CaracteristicaModel caracteristicaModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertNotNull(caracteristicaModel.getDAO());
        assertEquals(caracteristicaDAO, caracteristicaModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        Caracteristica entidad = new Caracteristica(expectedUuid);

        assertEquals(expectedUuid, caracteristicaModel.getId(entidad));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar entidad con ID no nulo y activo en true")
    void testNuevoRegistro() {
        Caracteristica resultado = caracteristicaModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdCaracteristica());
        assertTrue(resultado.getActivo());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo para la vista")
    void testNombreBean() {
        assertEquals("Característica", caracteristicaModel.nombreBean());
    }

    @Test
    @DisplayName("cargarOpciones debe poblar la lista de opciones de TipoCaracteristica")
    void testCargarOpciones() {
        TipoCaracteristica tipo = new TipoCaracteristica(UUID.randomUUID());
        tipo.setNombre("Volumen");

        when(tipoCaracteristicaDAO.findAll()).thenReturn(Collections.singletonList(tipo));
        when(etiquetas.tipoCaracteristica(tipo)).thenReturn("Volumen");

        caracteristicaModel.cargarOpciones();

        assertNotNull(caracteristicaModel.getOpcionesIdTipoCaracteristica());
        assertEquals(1, caracteristicaModel.getOpcionesIdTipoCaracteristica().size());
        assertEquals("Volumen", caracteristicaModel.getOpcionesIdTipoCaracteristica().get(0).getLabel());
        verify(tipoCaracteristicaDAO).findAll();
    }

    @Test
    @DisplayName("cargarOpciones maneja una lista vacía de TipoCaracteristica")
    void testCargarOpcionesVacio() {
        when(tipoCaracteristicaDAO.findAll()).thenReturn(Collections.emptyList());

        caracteristicaModel.cargarOpciones();

        assertNotNull(caracteristicaModel.getOpcionesIdTipoCaracteristica());
        assertTrue(caracteristicaModel.getOpcionesIdTipoCaracteristica().isEmpty());
    }
}
