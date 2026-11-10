package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CajaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.EmpleadoRolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.FacturaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.RolDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FacturaModelTest {

    @Mock
    private FacturaDAO facturaDAO;
    @Mock
    private CajaDAO cajaDAO;
    @Mock
    private EmpleadoRolDAO empleadoRolDAO;
    @Mock
    private RolDAO rolDAO;
    @Mock
    private FacturaOrdenProductoModel facturaOrdenProductoModel;
    @Mock
    private Etiquetas etiquetas;
    @Mock
    private FacesContext facesContext;

    @InjectMocks
    private FacturaModel model;

    @Test
    @DisplayName("nuevoRegistro: factura con ID, fecha de ahora y estado PENDIENTE")
    void testNuevoRegistro() {
        Factura f = model.nuevoRegistro();
        assertNotNull(f.getIdFactura());
        assertNotNull(f.getFechaFacturacion());
        assertEquals("PENDIENTE", f.getEstado());
        assertEquals("Factura", model.nombreBean());
        assertSame(facturaDAO, model.getDAO());
    }

    @Test
    @DisplayName("Crear con cajero y caja activa: guarda, pone 'Consumidor final' y salta a la pestaña Productos")
    void testCrearValida() {
        conFaces(() -> {
            model.btnNuevoHandler();
            model.getRegistro().setIdEmpleadoRol(asignacion("Cajero"));
            model.getRegistro().setIdCaja(caja(true));

            model.btnCrearHandler();

            verify(facturaDAO).create(any(Factura.class));
            assertEquals("Consumidor final", model.getRegistro().getCliente());
            assertEquals(ESTADO_CRUD.MODIFICAR, model.getEstado());
            assertEquals(1, model.getTabActiva());
            assertEquals(FacesMessage.SEVERITY_INFO, severidad());
        });
    }

    @Test
    @DisplayName("Crear con un camarero: NO guarda (solo cajeros facturan)")
    void testCrearNoCajero() {
        conFaces(() -> {
            model.btnNuevoHandler();
            EmpleadoRol camarero = asignacion("Camarero");
            model.getRegistro().setIdEmpleadoRol(camarero);
            model.getRegistro().setIdCaja(caja(true));
            when(etiquetas.empleadoRol(camarero)).thenReturn("Ana (Camarero)");

            model.btnCrearHandler();

            verify(facturaDAO, never()).create(any());
            assertEquals(ESTADO_CRUD.CREAR, model.getEstado());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidad());
        });
    }

    @Test
    @DisplayName("Crear con caja inactiva o sin caja: NO guarda")
    void testCrearCajaInactiva() {
        conFaces(() -> {
            model.btnNuevoHandler();
            model.getRegistro().setIdEmpleadoRol(asignacion("Cajero"));
            model.getRegistro().setIdCaja(caja(false));

            model.btnCrearHandler();
            model.getRegistro().setIdCaja(null);
            model.btnCrearHandler();

            verify(facturaDAO, never()).create(any());
        });
    }

    @Test
    @DisplayName("Modificar: anular una PENDIENTE se permite; reabrir una ANULADA no")
    void testCambiosDeEstado() {
        conFaces(() -> {
            Factura f = facturaGuardada("PENDIENTE");
            model.onRowSelect(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class),
                    mock(jakarta.faces.component.behavior.Behavior.class), f));
            f.setEstado("ANULADA");
            model.btnModificarHandler();
            verify(facturaDAO).modify(f);

            Factura anulada = facturaGuardada("ANULADA");
            model.onRowSelect(new org.primefaces.event.SelectEvent<>(mock(jakarta.faces.component.UIComponent.class),
                    mock(jakarta.faces.component.behavior.Behavior.class), anulada));
            anulada.setEstado("PENDIENTE");
            model.btnModificarHandler();
            verify(facturaDAO, never()).modify(anulada);
        });
    }

    @Test
    @DisplayName("Eliminar: solo una factura PENDIENTE; una PAGADA no se toca")
    void testEliminar() {
        conFaces(() -> {
            Factura pagada = facturaGuardada("PAGADA");
            model.setRegistro(pagada);
            model.btnEliminarHandler();
            verify(facturaDAO, never()).delete(any());

            Factura pendiente = facturaGuardada("PENDIENTE");
            model.setRegistro(pendiente);
            model.btnEliminarHandler();
            verify(facturaDAO).delete(pendiente);
        });
    }

    @Test
    @DisplayName("completarEmpleadoRol: solo pide a la base los roles de cajero")
    void testCompletarCajero() {
        Rol cajero = rol("Cajero");
        Rol camarero = rol("Camarero");
        when(rolDAO.findActivos()).thenReturn(List.of(cajero, camarero));

        model.completarEmpleadoRol("ana");

        verify(empleadoRolDAO).findActivosPorRolesYNombre(List.of(cajero), "ana", 10);
    }

    @Test
    @DisplayName("completarCaja: busca solo activas; si falla devuelve vacío y avisa")
    void testCompletarCaja() {
        conFaces(() -> {
            when(cajaDAO.buscarActivasPorNombre("web", 10)).thenThrow(new IllegalStateException("caída"));
            assertTrue(model.completarCaja("web").isEmpty());
            assertEquals(FacesMessage.SEVERITY_ERROR, severidad());
        });
    }

    @Test
    @DisplayName("Estados: al crear no se elige; una PAGADA solo muestra PAGADA")
    void testEstados() {
        model.btnNuevoHandler();
        assertFalse(model.isEstadoEditable());
        assertEquals(List.of("PENDIENTE", "ANULADA"), model.getEstados());
        model.setRegistro(facturaGuardadaSinStub("PAGADA"));
        assertEquals(List.of("PAGADA"), model.getEstados());
    }

    // ---------------- utilidades ----------------
    private void conFaces(Runnable prueba) {
        try (MockedStatic<FacesContext> faces = mockStatic(FacesContext.class)) {
            faces.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
            prueba.run();
        }
    }

    private FacesMessage.Severity severidad() {
        ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);
        verify(facesContext, atLeastOnce()).addMessage(isNull(), captor.capture());
        return captor.getValue().getSeverity();
    }

    private Factura facturaGuardada(String estado) {
        Factura f = facturaGuardadaSinStub(estado);
        Factura enBase = new Factura(f.getIdFactura());
        enBase.setEstado(estado);
        when(facturaDAO.findById(f.getIdFactura())).thenReturn(enBase);
        return f;
    }

    private Factura facturaGuardadaSinStub(String estado) {
        Factura f = new Factura(UUID.randomUUID());
        f.setEstado(estado);
        f.setFechaFacturacion(new java.util.Date());
        f.setIdEmpleadoRol(asignacion("Cajero"));
        f.setIdCaja(caja(true));
        return f;
    }

    private static Rol rol(String nombre) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombre);
        r.setActivo(true);
        return r;
    }

    private static EmpleadoRol asignacion(String rol) {
        Empleado e = new Empleado(UUID.randomUUID());
        e.setActivo(true);
        EmpleadoRol er = new EmpleadoRol(UUID.randomUUID());
        er.setIdEmpleado(e);
        er.setIdRol(rol(rol));
        er.setActivo(true);
        return er;
    }

    private static Caja caja(boolean activa) {
        Caja c = new Caja(UUID.randomUUID());
        c.setNombre("Caja 1");
        c.setActivo(activa);
        return c;
    }
}
