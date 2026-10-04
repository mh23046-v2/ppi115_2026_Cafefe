package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author hernandez
 */
public class ReglasOrdenTest {

    private static final long DIA = 24L * 60 * 60 * 1000;
    private final Date hoy = new Date();
    private final Date ayer = new Date(hoy.getTime() - DIA);
    private final Date manana = new Date(hoy.getTime() + DIA);

    // ---------------- Roles ----------------

    @ParameterizedTest
    @ValueSource(strings = {"Camarero", "BARISTA", "Atención al cliente", "  atencion   al Cliente "})
    @DisplayName("Los roles admitidos se reconocen sin importar tildes, mayúsculas ni espacios")
    void testRolAdmitido(String nombre) {
        assertTrue(ReglasOrden.rolAdmitido(rol(nombre, true)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Cajero", "Administrador", "Cocinero", ""})
    @DisplayName("Otros roles no pueden tomar órdenes")
    void testRolNoAdmitido(String nombre) {
        assertFalse(ReglasOrden.rolAdmitido(rol(nombre, true)));
        assertFalse(ReglasOrden.rolAdmitido(null));
    }

    @Test
    @DisplayName("Puede tomar órdenes: asignación, empleado y rol activos con rol admitido")
    void testPuedeTomarOrdenes() {
        assertTrue(ReglasOrden.puedeTomarOrdenes(empleadoRol(true, true, true, "Barista")));
    }

    @Test
    @DisplayName("No puede tomar órdenes si algo está inactivo o el rol no es admitido")
    void testNoPuedeTomarOrdenes() {
        assertFalse(ReglasOrden.puedeTomarOrdenes(empleadoRol(false, true, true, "Barista")), "asignación inactiva");
        assertFalse(ReglasOrden.puedeTomarOrdenes(empleadoRol(true, false, true, "Barista")), "empleado inactivo");
        assertFalse(ReglasOrden.puedeTomarOrdenes(empleadoRol(true, true, false, "Barista")), "rol inactivo");
        assertFalse(ReglasOrden.puedeTomarOrdenes(empleadoRol(true, true, true, "Cajero")), "rol no admitido");
        assertFalse(ReglasOrden.puedeTomarOrdenes(null));
    }

    // ---------------- Vigencia ----------------

    @Test
    @DisplayName("Vigencia: dentro del rango y en los extremos sí; antes o después no")
    void testDentroDeVigencia() {
        assertTrue(ReglasOrden.dentroDeVigencia(ayer, manana, hoy));
        assertTrue(ReglasOrden.dentroDeVigencia(hoy, hoy, hoy), "los extremos cuentan");
        assertFalse(ReglasOrden.dentroDeVigencia(hoy, manana, ayer), "antes de empezar");
        assertFalse(ReglasOrden.dentroDeVigencia(ayer, hoy, manana), "ya terminó");
        assertFalse(ReglasOrden.dentroDeVigencia(ayer, manana, null), "sin fecha de orden");
        assertTrue(ReglasOrden.dentroDeVigencia(null, null, hoy), "sin límites");
    }

    @Test
    @DisplayName("Descuento aplicable: descuento y asignación vigentes, tipo activo")
    void testDescuentoAplicable() {
        assertTrue(ReglasOrden.descuentoAplicable(descuentoProducto(ayer, manana, 20, true, 50), hoy));
    }

    @Test
    @DisplayName("Una orden fuera de la vigencia del descuento NO recibe el descuento")
    void testDescuentoFueraDeVigencia() {
        DescuentoProducto dp = descuentoProducto(ayer, hoy, 20, true, 50);

        assertFalse(ReglasOrden.descuentoAplicable(dp, manana));
    }

    @Test
    @DisplayName("Tampoco aplica si la asignación al producto venció o el tipo está inactivo")
    void testDescuentoAsignacionVencidaOTipoInactivo() {
        DescuentoProducto vencido = descuentoProducto(ayer, manana, 20, true, 50);
        vencido.setFechaHasta(ayer);
        assertFalse(ReglasOrden.descuentoAplicable(vencido, hoy));

        assertFalse(ReglasOrden.descuentoAplicable(descuentoProducto(ayer, manana, 20, false, 50), hoy));
    }

    @Test
    @DisplayName("Un descuento con rango abierto (sin fecha de inicio o de fin) sí se aplica")
    void testDescuentoRangoAbierto() {
        assertTrue(ReglasOrden.descuentoAplicable(descuentoProducto(ayer, null, 20, true, 50), hoy));
        assertTrue(ReglasOrden.descuentoAplicable(descuentoProducto(null, manana, 20, true, 50), hoy));
        assertTrue(ReglasOrden.descuentoAplicable(descuentoProducto(null, null, 20, true, 50), hoy));
    }

    @Test
    @DisplayName("Un descuento de rango abierto no se aplica antes de su fecha de inicio")
    void testDescuentoAbiertoAntesDelInicio() {
        assertFalse(ReglasOrden.descuentoAplicable(descuentoProducto(manana, null, 20, true, 50), hoy));
    }

    // ---------------- Precio ----------------

    @Test
    @DisplayName("El porcentaje no pasa del máximo del tipo de descuento")
    void testPorcentajeEfectivo() {
        assertEquals(20, ReglasOrden.porcentajeEfectivo(descuentoProducto(ayer, manana, 20, true, 50)));
        assertEquals(15, ReglasOrden.porcentajeEfectivo(descuentoProducto(ayer, manana, 40, true, 15)));
    }

    @Test
    @DisplayName("Precio con descuento: 5.00 con 20% = 4.00, redondeado a 2 decimales")
    void testPrecioConDescuento() {
        assertEquals(new BigDecimal("4.00"), ReglasOrden.precioConDescuento(new BigDecimal("5.00"), 20));
        assertEquals(new BigDecimal("2.08"), ReglasOrden.precioConDescuento(new BigDecimal("2.77"), 25));
        assertEquals(new BigDecimal("3.50"), ReglasOrden.precioConDescuento(new BigDecimal("3.5"), 0));
        assertNull(ReglasOrden.precioConDescuento(null, 10));
    }

    // ---------------- utilidades ----------------

    private Rol rol(String nombre, boolean activo) {
        Rol r = new Rol(UUID.randomUUID());
        r.setNombre(nombre);
        r.setActivo(activo);
        return r;
    }

    private EmpleadoRol empleadoRol(boolean asignacionActiva, boolean empleadoActivo, boolean rolActivo, String rol) {
        Empleado e = new Empleado(UUID.randomUUID());
        e.setActivo(empleadoActivo);
        EmpleadoRol er = new EmpleadoRol(UUID.randomUUID());
        er.setActivo(asignacionActiva);
        er.setIdEmpleado(e);
        er.setIdRol(rol(rol, rolActivo));
        return er;
    }

    static DescuentoProducto descuentoProducto(Date desde, Date hasta, int valor, boolean tipoActivo, int maximo) {
        TipoDescuento tipo = new TipoDescuento(UUID.randomUUID());
        tipo.setActivo(tipoActivo);
        tipo.setDescuentoMaximo(maximo);
        Descuento d = new Descuento(UUID.randomUUID());
        d.setNombre("Cliente Platinum");
        d.setFechaDesde(desde);
        d.setFechaHasta(hasta);
        d.setIdTipoDescuento(tipo);
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());
        dp.setIdDescuento(d);
        dp.setValor(valor);
        return dp;
    }
}