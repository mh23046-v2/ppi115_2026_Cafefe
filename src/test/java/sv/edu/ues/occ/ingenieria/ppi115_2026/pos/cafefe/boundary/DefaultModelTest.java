package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.AbstractDataAccess;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DefaultModelTest {

    // Clase ficticia para simular la entidad con UUID
    public static class EntidadPrueba {

        private final UUID id;

        public EntidadPrueba(UUID id) {
            this.id = id;
        }

        public UUID getId() {
            return id;
        }
    }

    // Subclase concreta de prueba implementando todos los métodos abstractos reales
    public static class TestDefaultModel extends DefaultModel<EntidadPrueba> {

        private final AbstractDataAccess<EntidadPrueba> dao;
        public boolean cargarOpcionesLlamado = false;
        public int cambiosDeRegistro = 0;

        public TestDefaultModel(AbstractDataAccess<EntidadPrueba> dao) {
            this.dao = dao;
        }

        @Override
        protected AbstractDataAccess<EntidadPrueba> getDAO() {
            return dao;
        }

        @Override
        protected UUID getId(EntidadPrueba entidad) {
            return entidad != null ? entidad.getId() : null;
        }

        @Override
        protected EntidadPrueba nuevoRegistro() {
            return new EntidadPrueba(UUID.randomUUID());
        }

        @Override
        public String nombreBean() {
            return "TestBean";
        }

        @Override
        public void cargarOpciones() {
            this.cargarOpcionesLlamado = true;
        }

        @Override
        protected void registroCambio() {
            cambiosDeRegistro++;
        }
    }

    @Mock
    private AbstractDataAccess<EntidadPrueba> mockDAO;

    @Mock
    private FacesContext mockFacesContext;

    private TestDefaultModel model;
    private EntidadPrueba registroPrueba;
    private UUID idPrueba;

    @BeforeEach
    public void setUp() {
        model = new TestDefaultModel(mockDAO);
        idPrueba = UUID.randomUUID();
        registroPrueba = new EntidadPrueba(idPrueba);
        model.inicializar(); // Instancia el LazyDataModel y llama a cargarOpciones()
    }

    @Nested
    @DisplayName("Pruebas de Inicialización y Estado de la Vista")
    class InicializacionTests {

        @Test
        @DisplayName("inicializar() debe cargar opciones y establecer el estado en NINGUNO")
        public void testInicializar() {
            assertTrue(model.cargarOpcionesLlamado);
            assertEquals(ESTADO_CRUD.NINGUNO, model.getEstado());
        }

        @Test
        @DisplayName("btnNuevoHandler() establece el estado en CREAR y genera una nueva instancia")
        public void testBtnNuevoHandler() {
            model.btnNuevoHandler();

            assertEquals(ESTADO_CRUD.CREAR, model.getEstado());
            assertNotNull(model.getRegistro());
        }

        @Test
        @DisplayName("btnCancelarHandler() reinicia el estado a NINGUNO y limpia el registro")
        public void testBtnCancelarHandler() {
            model.setRegistro(registroPrueba);
            model.btnNuevoHandler();

            model.btnCancelarHandler();

            assertEquals(ESTADO_CRUD.NINGUNO, model.getEstado());
            assertNull(model.getRegistro());
        }

        @Test
        @DisplayName("setRegistro y btnCancelarHandler avisan el cambio mediante registroCambio()")
        public void testElGanchoSeInvocaEnCadaCambio() {
            model.cambiosDeRegistro = 0; // Reiniciar contador tras setUp

            model.setRegistro(registroPrueba);
            assertEquals(1, model.cambiosDeRegistro);

            model.btnCancelarHandler();
            assertEquals(2, model.cambiosDeRegistro);
        }
    }

    @Nested
    @DisplayName("Pruebas de Acciones CRUD y Manejo de Eventos")
    class AccionesCrudTests {

        @Test
        @DisplayName("btnCrearHandler() exitoso persiste la entidad, reinicia el estado y muestra mensaje")
        public void testBtnCrearHandlerExito() {
            model.setRegistro(registroPrueba);

            try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {
                mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockFacesContext);

                model.btnCrearHandler();

                verify(mockDAO, times(1)).create(registroPrueba);
                assertEquals(ESTADO_CRUD.NINGUNO, model.getEstado());
                assertNull(model.getRegistro());
                verify(mockFacesContext, times(1)).addMessage(eq(null), any(FacesMessage.class));
            }
        }

        @Test
        @DisplayName("btnModificarHandler() exitoso actualiza la entidad, reinicia el estado y muestra mensaje")
        public void testBtnModificarHandlerExito() {
            model.setRegistro(registroPrueba);

            try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {
                mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockFacesContext);

                model.btnModificarHandler();

                verify(mockDAO, times(1)).modify(registroPrueba);
                assertEquals(ESTADO_CRUD.NINGUNO, model.getEstado());
                assertNull(model.getRegistro());
                verify(mockFacesContext, times(1)).addMessage(eq(null), any(FacesMessage.class));
            }
        }

        @Test
        @DisplayName("btnEliminarHandler() exitoso invoca DAO.delete() y recarga las opciones")
        public void testEliminarExitoso() {
            model.setRegistro(registroPrueba);
            model.cargarOpcionesLlamado = false;

            try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {
                mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockFacesContext);

                model.btnEliminarHandler();

                verify(mockDAO, times(1)).delete(registroPrueba);
                assertTrue(model.cargarOpcionesLlamado);
                assertEquals(ESTADO_CRUD.NINGUNO, model.getEstado());
                assertNull(model.getRegistro());
                verify(mockFacesContext, times(1)).addMessage(eq(null), any(FacesMessage.class));
            }
        }

        @Test
        @DisplayName("Manejo de excepciones durante la ejecución muestra un mensaje de ERROR")
        public void testEjecutarAccionConExcepcion() {
            model.setRegistro(registroPrueba);
            doThrow(new RuntimeException("Error de base de datos")).when(mockDAO).delete(registroPrueba);

            try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {
                mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockFacesContext);

                model.btnEliminarHandler();

                verify(mockFacesContext, times(1)).addMessage(eq(null), argThat(msg
                        -> msg.getSeverity().equals(FacesMessage.SEVERITY_ERROR)
                ));
            }
        }
    }

    @Nested
    @DisplayName("Pruebas del LazyDataModel para Paginación")
    class LazyDataModelTests {

        @Test
        @DisplayName("load() y count() llaman a findRange y count del DAO correctamente")
        public void testLazyDataModelLoadAndCount() {
            List<EntidadPrueba> listaResultado = List.of(registroPrueba);
            when(mockDAO.findRange(0, 10)).thenReturn(listaResultado);
            when(mockDAO.count()).thenReturn(1L);

            List<EntidadPrueba> resultado = model.getLazyModel().load(0, 10, null, null);
            int total = model.getLazyModel().count(null);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(1, total);
            verify(mockDAO, times(1)).findRange(0, 10);
            verify(mockDAO, times(1)).count();
        }

        @Test
        @DisplayName("getRowKey y getRowData interactúan correctamente con el DAO")
        public void testLazyDataModelRowKeyAndData() {
            when(mockDAO.findById(idPrueba)).thenReturn(registroPrueba);

            String key = model.getLazyModel().getRowKey(registroPrueba);
            EntidadPrueba element = model.getLazyModel().getRowData(key);

            assertEquals(idPrueba.toString(), key);
            assertEquals(registroPrueba, element);
            verify(mockDAO, times(1)).findById(idPrueba);
        }
    }
}
