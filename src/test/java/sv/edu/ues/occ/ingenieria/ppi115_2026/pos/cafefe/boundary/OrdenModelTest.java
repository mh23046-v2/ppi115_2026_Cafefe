package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class OrdenModelTest {

    @Mock
    private OrdenDAO ordenDAO;

    @Mock
    private OrdenProductoModel ordenProductoModel;

    @Mock
    private Etiquetas etiquetas;

    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private OrdenModel ordenModel;

    @Test
    @DisplayName("getDAO, getId y nombreBean devuelven lo esperado")
    void testBasicos() {
        UUID id = UUID.randomUUID();

        assertEquals(ordenDAO, ordenModel.getDAO());
        assertEquals(id, ordenModel.getId(new Orden(id)));
        assertEquals("Orden", ordenModel.nombreBean());
    }

    @Test
    @DisplayName("nuevoRegistro crea la orden con ID y la fecha de ahora")
    void testNuevoRegistro() {
        Orden nueva = ordenModel.nuevoRegistro();

        assertNotNull(nueva.getIdOrden());
        assertNotNull(nueva.getFechaCreacion());
    }

    @Test
    @DisplayName("El combo de empleados solo muestra roles admitidos (camarero, barista...)")
    void testCargarOpcionesSoloAdmitidos() {
        when(ordenDAO.findEmpleadoRolActivos()).thenReturn(List.of(empleadoRol("Barista"), empleadoRol("Cajero")));
        when(etiquetas.empleadoRol(any())).thenReturn("Empleado");

        ordenModel.cargarOpciones();

        assertEquals(1, ordenModel.getOpcionesEmpleado().size());
        assertEquals("Barista", ((EmpleadoRol) ordenModel.getOpcionesEmpleado().get(0).getValue()).getIdRol().getNombre());
    }

    @Test
    @DisplayName("Crear con un empleado no admitido: NO guarda y muestra error")
    void testCrearEmpleadoNoAdmitido() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            ordenModel.btnNuevoHandler();
            ordenModel.getRegistro().setIdEmpleadoRol(empleadoRol("Cajero"));

            ordenModel.btnCrearHandler();

            verify(ordenDAO, never()).create(any());
            assertEquals(ESTADO_CRUD.CREAR, ordenModel.getEstado());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Crear sin empleado: NO guarda y muestra error")
    void testCrearSinEmpleado() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            ordenModel.btnNuevoHandler();

            ordenModel.btnCrearHandler();

            verify(ordenDAO, never()).create(any());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Crear (caso feliz): guarda y se queda en la orden, en la pestaña Productos")
    void testCrearExitoso() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            ordenModel.btnNuevoHandler();
            Orden nueva = ordenModel.getRegistro();
            nueva.setIdEmpleadoRol(empleadoRol("Camarero"));

            ordenModel.btnCrearHandler();

            verify(ordenDAO).create(nueva);
            assertSame(nueva, ordenModel.getRegistro());
            assertEquals(ESTADO_CRUD.MODIFICAR, ordenModel.getEstado());
            assertEquals(1, ordenModel.getTabActiva());
            verify(ordenProductoModel, atLeastOnce()).setOrden(nueva);
            assertEquals(FacesMessage.SEVERITY_INFO, severidadDelMensaje());
        }
    }

    @Test
    @DisplayName("Modificar sin fecha: NO guarda y muestra error")
    void testModificarSinFecha() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            Orden orden = new Orden(UUID.randomUUID());
            orden.setIdEmpleadoRol(empleadoRol("Barista"));
            ordenModel.setRegistro(orden);

            ordenModel.btnModificarHandler();

            verify(ordenDAO, never()).modify(any());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidadDelMensaje());
        }
    }

    private EmpleadoRol empleadoRol(String nombreRol) {
        Empleado e = new Empleado(UUID.randomUUID());
        e.setActivo(true);
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombreRol);
        r.setActivo(true);
        EmpleadoRol er = new EmpleadoRol(UUID.randomUUID());
        er.setActivo(true);
        er.setIdEmpleado(e);
        er.setIdRol(r);
        return er;
    }

    /** Captura el mensaje que el Model envió a la pantalla y devuelve su severidad. */
    private FacesMessage.Severity severidadDelMensaje() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext).addMessage(isNull(), captor.capture());
        return captor.getValue().getSeverity();
    }
}
