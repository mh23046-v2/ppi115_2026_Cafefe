package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.model.SelectItem;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SesionUsuarioTest {

    @Mock
    private FacesContext mockFacesContext;

    @Mock
    private UIViewRoot mockViewRoot;

    @Mock
    private AjaxBehaviorEvent mockEvent;

    private SesionUsuario sesion;

    @BeforeEach
    public void setUp() {
        sesion = new SesionUsuario();
        sesion.inicializar();
    }

    @Test
    @DisplayName("El idioma por defecto debe ser español 'es'")
    public void testIdiomaPorDefecto() {
        assertEquals("es", sesion.getIdiomaSeleccionado());
    }

    @Test
    @DisplayName("inicializar() genera las 3 opciones de idioma esperadas")
    public void testOfreceTresIdiomas() {
        List<SelectItem> opciones = sesion.getOpcionesIdioma();

        assertNotNull(opciones);
        assertEquals(3, opciones.size());

        assertEquals("es", opciones.get(0).getValue());
        assertEquals("Español", opciones.get(0).getLabel());

        assertEquals("en_US", opciones.get(1).getValue());
        assertEquals("English", opciones.get(1).getLabel());

        assertEquals("de_DE", opciones.get(2).getValue());
        assertEquals("Deutsch", opciones.get(2).getLabel());
    }

    @Test
    @DisplayName("cambiarIdioma() aplica el Locale correspondiente en el UIViewRoot de JSF")
    public void testCambiarIdiomaAplicaLocale() {
        sesion.setIdiomaSeleccionado("en_US");
        when(mockFacesContext.getViewRoot()).thenReturn(mockViewRoot);

        try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {
            mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockFacesContext);

            sesion.cambiarIdioma(mockEvent);

            verify(mockViewRoot, times(1)).setLocale(Locale.of("en", "US"));
        }
    }

    @Test
    @DisplayName("cambiarIdioma() no modifica el Locale si el idioma seleccionado no está en la lista soportada")
    public void testCambiarIdiomaIgnoraDesconocidos() {
        sesion.setIdiomaSeleccionado("fr_FR");

        try (MockedStatic<FacesContext> mockedFacesContext = mockStatic(FacesContext.class)) {
            mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockFacesContext);

            sesion.cambiarIdioma(mockEvent);

            verify(mockViewRoot, never()).setLocale(any());
        }
    }
}
