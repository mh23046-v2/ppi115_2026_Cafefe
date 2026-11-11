package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.jsf;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CajaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CajaModelTest {

    @Mock
    private CajaDAO cajaDAO;

    @InjectMocks
    private CajaModel model;

    @Test
    @DisplayName("nuevoRegistro: caja con ID y activa")
    void nuevoRegistro() {
        Caja c = model.nuevoRegistro();
        assertNotNull(c.getIdCaja());
        assertTrue(c.getActivo());
        assertEquals("Caja", model.nombreBean());
        assertSame(cajaDAO, model.getDAO());
        assertEquals(c.getIdCaja(), model.getId(c));
    }

    @Test
    @DisplayName("validarRegistro: nombre obligatorio, sin espacios sobrantes y sin repetir")
    void validarRegistro() {
        Caja c = new Caja(UUID.randomUUID());
        assertNotNull(model.validarRegistro(null));
        assertTrue(model.validarRegistro(c).contains("obligatorio"));
        c.setNombre("   ");
        assertTrue(model.validarRegistro(c).contains("obligatorio"));

        c.setNombre("  Caja 1 ");
        when(cajaDAO.existeNombre("Caja 1", c.getIdCaja())).thenReturn(true);
        assertTrue(model.validarRegistro(c).contains("Ya existe"));
        assertEquals("Caja 1", c.getNombre());

        when(cajaDAO.existeNombre("Caja 1", c.getIdCaja())).thenReturn(false);
        assertNull(model.validarRegistro(c));
    }

    @Test
    @DisplayName("cargarDatos y contarDatos paginan en la base")
    void cargarYContar() {
        when(cajaDAO.findRange(0, 10)).thenReturn(List.of(new Caja(UUID.randomUUID())));
        when(cajaDAO.count()).thenReturn(1L);

        assertEquals(1, model.cargarDatos(0, 10).size());
        assertEquals(1, model.contarDatos());
    }
}
