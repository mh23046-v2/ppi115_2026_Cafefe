package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.CaracteristicaModel;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@ExtendWith(MockitoExtension.class)
class CaracteristicaModelTest {

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Spy
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
    @DisplayName("completarTipoCaracteristica debe invocar buscarPorNombre en el DAO con límite de 10")
    void testCompletarTipoCaracteristicaExitoso() {
        TipoCaracteristica tipo = new TipoCaracteristica(UUID.randomUUID());
        tipo.setNombre("Origen");
        when(tipoCaracteristicaDAO.buscarPorNombre("Origen", 10)).thenReturn(List.of(tipo));

        List<TipoCaracteristica> resultado = caracteristicaModel.completarTipoCaracteristica("Origen");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(tipoCaracteristicaDAO).buscarPorNombre("Origen", 10);
    }

    @Test
    @DisplayName("completarTipoCaracteristica retorna lista vacía ante excepciones del DAO")
    void testCompletarTipoCaracteristicaConExcepcion() {
        when(tipoCaracteristicaDAO.buscarPorNombre(anyString(), eq(10)))
                .thenThrow(new RuntimeException("Error simulado de base de datos"));

        // Evita que la notificación intente acceder a FacesContext (Mojarra)
        doNothing().when(caracteristicaModel).notificarError(anyString(), any(Exception.class));

        List<TipoCaracteristica> resultado = caracteristicaModel.completarTipoCaracteristica("Origen");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(caracteristicaModel).notificarError(eq("No se pudieron cargar los tipos de característica"), any(Exception.class));
    }

    @Test
    @DisplayName("validarRegistro evalúa todas las restricciones de negocio para la característica")
    void testValidarRegistro() {
        Caracteristica c = new Caracteristica(UUID.randomUUID());

        // 1. Nombre nulo
        c.setNombre(null);
        String err1 = caracteristicaModel.validarRegistro(c);
        assertNotNull(err1);
        assertTrue(err1.contains("obligatorio"));

        // 2. Nombre vacío / espacios
        c.setNombre("   ");
        String err2 = caracteristicaModel.validarRegistro(c);
        assertNotNull(err2);

        // 3. Sin TipoCaracteristica
        c.setNombre("Pacamara");
        c.setIdTipoCaracteristica(null);
        String err3 = caracteristicaModel.validarRegistro(c);
        assertNotNull(err3);
        assertTrue(err3.contains("válido"));

        // 4. TipoCaracteristica sin ID
        TipoCaracteristica tipoSinId = new TipoCaracteristica();
        c.setIdTipoCaracteristica(tipoSinId);
        String err4 = caracteristicaModel.validarRegistro(c);
        assertNotNull(err4);

        // 5. TipoCaracteristica inactivo
        TipoCaracteristica tipoInactivo = new TipoCaracteristica(UUID.randomUUID());
        tipoInactivo.setActivo(false);
        c.setIdTipoCaracteristica(tipoInactivo);
        String err5 = caracteristicaModel.validarRegistro(c);
        assertNotNull(err5);
        assertTrue(err5.contains("inactivo"));

        // 6. Registro válido
        TipoCaracteristica tipoActivo = new TipoCaracteristica(UUID.randomUUID());
        tipoActivo.setActivo(true);
        c.setIdTipoCaracteristica(tipoActivo);
        assertNull(caracteristicaModel.validarRegistro(c));
    }
}