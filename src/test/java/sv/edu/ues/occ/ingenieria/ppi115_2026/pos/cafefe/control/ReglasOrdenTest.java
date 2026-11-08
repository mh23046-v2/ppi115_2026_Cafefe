package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

import static org.junit.jupiter.api.Assertions.*;

class ReglasOrdenTest {

    @Test
    @DisplayName("normalizar: Elimina tildes, mayúsculas y espacios extra")
    void testNormalizar() {
        assertEquals("", ReglasOrden.normalizar(null));
        assertEquals("atencion al cliente", ReglasOrden.normalizar("  Atención   al  Cliente  "));
        assertEquals("camarero", ReglasOrden.normalizar("CAMARERO"));
    }

    @Test
    @DisplayName("rolAdmitido: Valida roles pertenecientes al conjunto ROLES_ADMITIDOS")
    void testRolAdmitido() {
        assertFalse(ReglasOrden.rolAdmitido(null));

        Rol r1 = new Rol(UUID.randomUUID());
        r1.setNombre("Administrador");
        assertFalse(ReglasOrden.rolAdmitido(r1));

        r1.setNombre("Camarero");
        assertTrue(ReglasOrden.rolAdmitido(r1));

        r1.setNombre("Barista");
        assertTrue(ReglasOrden.rolAdmitido(r1));

        r1.setNombre("Atención al Cliente");
        assertTrue(ReglasOrden.rolAdmitido(r1));
    }

    @Test
    @DisplayName("puedeTomarOrdenes: Exige que asignación, empleado y rol estén activos y admitidos")
    void testPuedeTomarOrdenes() {
        assertFalse(ReglasOrden.puedeTomarOrdenes(null));

        Empleado e = new Empleado(UUID.randomUUID());
        e.setActivo(true);

        Rol r = new Rol(UUID.randomUUID());
        r.setNombre("Camarero");
        r.setActivo(true);

        EmpleadoRol er = new EmpleadoRol(UUID.randomUUID());
        er.setActivo(true);
        er.setIdEmpleado(e);
        er.setIdRol(r);

        assertTrue(ReglasOrden.puedeTomarOrdenes(er));

        // Desactivando empleado
        e.setActivo(false);
        assertFalse(ReglasOrden.puedeTomarOrdenes(er));
        e.setActivo(true);

        // Desactivando rol
        r.setActivo(false);
        assertFalse(ReglasOrden.puedeTomarOrdenes(er));
        r.setActivo(true);

        // Desactivando asignación
        er.setActivo(false);
        assertFalse(ReglasOrden.puedeTomarOrdenes(er));
    }

    @Test
    @DisplayName("dentroDeVigencia: Verifica rangos de fechas con extremos opcionales")
    void testDentroDeVigencia() {
        Date hoy = new Date();
        assertFalse(ReglasOrden.dentroDeVigencia(null, null, null));

        Calendar cal = Calendar.getInstance();
        cal.setTime(hoy);
        cal.add(Calendar.DAY_OF_MONTH, -5);
        Date hace5Dias = cal.getTime();

        cal.setTime(hoy);
        cal.add(Calendar.DAY_OF_MONTH, 5);
        Date en5Dias = cal.getTime();

        assertTrue(ReglasOrden.dentroDeVigencia(hace5Dias, en5Dias, hoy));
        assertTrue(ReglasOrden.dentroDeVigencia(null, en5Dias, hoy));
        assertTrue(ReglasOrden.dentroDeVigencia(hace5Dias, null, hoy));

        cal.setTime(hoy);
        cal.add(Calendar.DAY_OF_MONTH, 10);
        Date en10Dias = cal.getTime();
        assertFalse(ReglasOrden.dentroDeVigencia(hace5Dias, en5Dias, en10Dias));
    }

    @Test
    @DisplayName("porcentajeEfectivo y precioConDescuento: Calcula adecuadamente los valores finales")
    void testCalculosPreciosYDescuentos() {
        DescuentoProducto dp = new DescuentoProducto();
        dp.setValor(25);

        TipoDescuento td = new TipoDescuento();
        td.setDescuentoMaximo(20);

        Descuento d = new Descuento();
        d.setIdTipoDescuento(td);
        dp.setIdDescuento(d);

        // Acotado por el máximo de 20%
        assertEquals(20, ReglasOrden.porcentajeEfectivo(dp));

        // Cálculo de precio con 20% de descuento sobre $10.00 -> $8.00
        BigDecimal precioBase = new BigDecimal("10.00");
        BigDecimal precioEsperado = new BigDecimal("8.00");
        assertEquals(precioEsperado, ReglasOrden.precioConDescuento(precioBase, 20));
    }
}