package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CajaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CajaResourceTest {

    private CajaDAO cajaDAO;
    private CajaResource resource;

    @BeforeEach
    void setUp() {
        cajaDAO = Mockito.mock(CajaDAO.class);
        resource = new CajaResource();
        resource.cajaDAO = cajaDAO;
    }

    @Test
    void soloCajasActivasConTotal() {
        List<Caja> cajas = List.of(new Caja(UUID.randomUUID()));
        when(cajaDAO.findByActivo(true, 0, 50)).thenReturn(cajas);
        when(cajaDAO.countByActivo(true)).thenReturn(2);

        Response r = resource.activas(0, 50);

        assertEquals(200, r.getStatus());
        assertEquals(cajas, r.getEntity());
        assertEquals(2, r.getHeaders().getFirst("Total-registros"));
    }

    @Test
    void maxSeLimita() {
        resource.activas(0, 1000);
        verify(cajaDAO).findByActivo(true, 0, 50);
    }

    @Test
    void paginacionInvalida() {
        assertEquals("first", resource.activas(-1, 5).getHeaderString("Wrong-Param"));
        assertEquals("max", resource.activas(0, 0).getHeaderString("Wrong-Param"));
        verifyNoInteractions(cajaDAO);
    }
}
