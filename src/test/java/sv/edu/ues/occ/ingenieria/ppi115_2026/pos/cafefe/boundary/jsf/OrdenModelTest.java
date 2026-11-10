package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.ESTADO_CRUD;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.Etiquetas;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.OrdenModel;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf.OrdenProductoModel;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.Date;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrdenModelTest {

    @Mock
    private OrdenDAO ordenDAO;

    @Mock
    private EmpleadoRolDAO empleadoRolDAO;

    @Mock
    private RolDAO rolDAO;

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
    @DisplayName("completarEmpleadoRol: pide a la base solo los roles que pueden tomar órdenes, con límite de 10")
    void testCompletarEmpleadoRolSoloAdmitidos() {
        EmpleadoRol erBarista = empleadoRol("Barista");
        Rol barista = erBarista.getIdRol();
        Rol atencion = empleadoRol("Atención al Cliente").getIdRol();
        Rol cajero = empleadoRol("Cajero").getIdRol();

        when(rolDAO.findActivos()).thenReturn(List.of(barista, cajero, atencion));
        when(empleadoRolDAO.findActivosPorRolesYNombre(List.of(barista, atencion), "juan", 10))
                .thenReturn(List.of(erBarista));

        List<EmpleadoRol> resultado = ordenModel.completarEmpleadoRol("juan");

        assertEquals(List.of(erBarista), resultado);
        verify(empleadoRolDAO, never()).findByEmpleadoNombre(anyString(), anyInt());
    }

    @Test
    @DisplayName("completarEmpleadoRol: si la base falla devuelve lista vacía y avisa")
    void testCompletarEmpleadoRolError() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            when(rolDAO.findActivos()).thenThrow(new IllegalStateException("caída"));

            assertTrue(ordenModel.completarEmpleadoRol("juan").isEmpty());
            verify(facesContext).addMessage(isNull(), any(FacesMessage.class));
        }
    }

    @Test
    @DisplayName("Crear con un empleado no admitido: NO guarda y muestra error")
    void testCrearEmpleadoNoAdmitido() {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            ordenModel.btnNuevoHandler();
            EmpleadoRol cajero = empleadoRol("Cajero");
            ordenModel.getRegistro().setIdEmpleadoRol(cajero);
            when(etiquetas.empleadoRol(cajero)).thenReturn("Cajero Juan");

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
            orden.setFechaCreacion(null);
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