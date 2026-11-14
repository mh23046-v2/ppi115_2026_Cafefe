package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReglasCobroTest {

    private static OrdenProducto linea(String observaciones) {
        OrdenProducto op = new OrdenProducto(UUID.randomUUID());
        op.setObservaciones(observaciones);
        return op;
    }

    @Test
    void clienteDesdeObservacionesConYSinNota() {
        assertEquals("Ana", ReglasCobro.clienteDesdeObservaciones("Cliente: Ana | sin azúcar"));
        assertEquals("Carlos", ReglasCobro.clienteDesdeObservaciones("Cliente: Carlos"));
    }

    @Test
    void clienteDesdeObservacionesSinPrefijoDevuelveNull() {
        assertNull(ReglasCobro.clienteDesdeObservaciones("Mesa 4"));
        assertNull(ReglasCobro.clienteDesdeObservaciones(null));
        assertNull(ReglasCobro.clienteDesdeObservaciones("Cliente:  "));
    }

    @Test
    void clienteDeLineasTomaLaPrimeraQueLoTenga() {
        List<OrdenProducto> lineas = Arrays.asList(null, linea("Mesa 4"), linea("Cliente: Sofía"));
        assertEquals("Sofía", ReglasCobro.clienteDeLineas(lineas));
        assertNull(ReglasCobro.clienteDeLineas(List.of(linea("Mesa 4"))));
        assertNull(ReglasCobro.clienteDeLineas(null));
    }

    @Test
    void notaDeLineaQuitaElPrefijoDelCliente() {
        assertEquals("sin azúcar", ReglasCobro.notaDeLinea("Cliente: Ana | sin azúcar"));
        assertNull(ReglasCobro.notaDeLinea("Cliente: Ana"));
        assertNull(ReglasCobro.notaDeLinea("Cliente: Ana |  "));
        assertEquals("Mesa 4", ReglasCobro.notaDeLinea(" Mesa 4 "));
        assertNull(ReglasCobro.notaDeLinea(""));
        assertNull(ReglasCobro.notaDeLinea(null));
    }

    @Test
    void clienteParaFacturaPrefiereElDelCajero() {
        assertEquals("Luisa", ReglasCobro.clienteParaFactura(" Luisa ", "Ana"));
        assertEquals("Ana", ReglasCobro.clienteParaFactura("  ", "Ana"));
        assertEquals("Consumidor final", ReglasCobro.clienteParaFactura(null, null));
    }

    @Test
    void uuidONull() {
        UUID id = UUID.randomUUID();
        assertEquals(id, ReglasCobro.uuidONull(" " + id + " "));
        assertNull(ReglasCobro.uuidONull("no-es-uuid"));
        assertNull(ReglasCobro.uuidONull(""));
        assertNull(ReglasCobro.uuidONull(null));
    }
}
