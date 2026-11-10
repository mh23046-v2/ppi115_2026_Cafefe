package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import static org.junit.jupiter.api.Assertions.*;

public class ReglasFacturaTest {

    private static EmpleadoRol asignacion(String rol, boolean asignacionActiva, boolean empleadoActivo, boolean rolActivo) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(rol);
        r.setActivo(rolActivo);
        Empleado e = new Empleado(UUID.randomUUID());
        e.setActivo(empleadoActivo);
        EmpleadoRol er = new EmpleadoRol(UUID.randomUUID());
        er.setIdRol(r);
        er.setIdEmpleado(e);
        er.setActivo(asignacionActiva);
        return er;
    }

    @Test
    @DisplayName("Un cajero activo puede facturar, sin importar mayúsculas ni espacios")
    void testCajero() {
        assertTrue(ReglasFactura.puedeFacturar(asignacion("  CAJERO ", true, true, true)));
    }

    @Test
    @DisplayName("Camarero, barista o rol nulo no pueden facturar")
    void testOtrosRoles() {
        assertFalse(ReglasFactura.puedeFacturar(asignacion("Camarero", true, true, true)));
        assertFalse(ReglasFactura.puedeFacturar(asignacion("Barista", true, true, true)));
        assertFalse(ReglasFactura.puedeFacturar(null));
    }

    @Test
    @DisplayName("Un cajero con la asignación, el empleado o el rol inactivos no puede facturar")
    void testInactivos() {
        assertFalse(ReglasFactura.puedeFacturar(asignacion("Cajero", false, true, true)));
        assertFalse(ReglasFactura.puedeFacturar(asignacion("Cajero", true, false, true)));
        assertFalse(ReglasFactura.puedeFacturar(asignacion("Cajero", true, true, false)));
    }

    @ParameterizedTest(name = "{0} → {1}: {2}")
    @CsvSource(nullValues = "null", value = {
        "null, PENDIENTE, true",       // nueva factura: nace pendiente
        "null, ANULADA, false",        // no se crea anulada
        "null, PAGADA, false",         // pagada solo la pone el cobro
        "PENDIENTE, PENDIENTE, true",
        "PENDIENTE, ANULADA, true",    // anular una pendiente
        "PENDIENTE, PAGADA, false",    // no se marca pagada a mano
        "ANULADA, PENDIENTE, false",   // no se reabre: podría facturar doble
        "PAGADA, ANULADA, false",
        "PAGADA, PAGADA, true",
        "PENDIENTE, null, false"
    })
    @DisplayName("Transiciones de estado permitidas")
    void testCambioDeEstado(String guardado, String nuevo, boolean esperado) {
        assertEquals(esperado, ReglasFactura.cambioDeEstadoPermitido(guardado, nuevo));
    }

    @Test
    @DisplayName("Solo una factura pendiente (o sin estado aún) es editable")
    void testEditable() {
        Factura f = new Factura(UUID.randomUUID());
        assertTrue(ReglasFactura.esEditable(f));
        f.setEstado("PENDIENTE");
        assertTrue(ReglasFactura.esEditable(f));
        f.setEstado("ANULADA");
        assertFalse(ReglasFactura.esEditable(f));
        f.setEstado("PAGADA");
        assertFalse(ReglasFactura.esEditable(f));
        assertFalse(ReglasFactura.esEditable(null));
    }

    @Test
    @DisplayName("Caja disponible solo si existe y está activa")
    void testCaja() {
        Caja c = new Caja(UUID.randomUUID());
        assertFalse(ReglasFactura.cajaDisponible(c));
        c.setActivo(true);
        assertTrue(ReglasFactura.cajaDisponible(c));
        assertFalse(ReglasFactura.cajaDisponible(null));
    }

    @Test
    @DisplayName("Cliente vacío queda como 'Consumidor final'")
    void testCliente() {
        assertEquals("Consumidor final", ReglasFactura.normalizarCliente(null));
        assertEquals("Consumidor final", ReglasFactura.normalizarCliente("   "));
        assertEquals("Ana López", ReglasFactura.normalizarCliente("  Ana López "));
    }
}
