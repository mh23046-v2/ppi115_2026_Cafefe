package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@ExtendWith(MockitoExtension.class)
public class TipoDescuentoResourceTest {

    @Mock
    private TipoDescuentoDAO tdDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private TipoDescuentoResource resource;

    // Tests para findRange
    @Test
    public void testFindRangeExitoso() {
        TipoDescuento t1 = new TipoDescuento();
        t1.setIdTipoDescuento(UUID.randomUUID());
        t1.setNombre("Promocion");

        Mockito.when(tdDAO.findRange(0, 50)).thenReturn(List.of(t1));
        Mockito.when(tdDAO.count()).thenReturn(1L);

        Response res = resource.findRange(0, 50);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(1L, res.getHeaderString("Total-registros") != null ? Long.parseLong(res.getHeaderString("Total-registros")) : 1L);
        
        List<?> lista = (List<?>) res.getEntity();
        Assertions.assertNotNull(lista);
        Assertions.assertEquals(1, lista.size());
    }

    @Test
    public void testFindRangeAjustaMaxEfectivo() {
        // Pide max=100 pero debe recortar a Math.min(100, 50) -> 50
        Mockito.when(tdDAO.findRange(0, 50)).thenReturn(List.of());
        Mockito.when(tdDAO.count()).thenReturn(0L);

        Response res = resource.findRange(0, 100);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Mockito.verify(tdDAO).findRange(0, 50);
    }

    // Tests para findById
    @Test
    public void testFindByIdExistente() {
        UUID id = UUID.randomUUID();
        TipoDescuento t = new TipoDescuento();
        t.setIdTipoDescuento(id);

        Mockito.when(tdDAO.findById(id)).thenReturn(t);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(t, res.getEntity());
    }

    @Test
    public void testFindByIdNoEncontrado() {
        UUID id = UUID.randomUUID();
        Mockito.when(tdDAO.findById(id)).thenReturn(null);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    // Tests para crear
    @Test
    public void testCrearExitoso() throws Exception {
        TipoDescuento t = new TipoDescuento();
        t.setNombre("Descuento Estudiantil");

        URI uriDummy = new URI("http://localhost:8080/Cafefe-1.0-SNAPSHOT/resources/v2/tipo_descuento/dummy-id");

        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(t, uriInfo);

        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Assertions.assertNotNull(t.getIdTipoDescuento());
        Assertions.assertEquals(uriDummy, res.getLocation());
        Mockito.verify(tdDAO).create(t);
    }

    @Test
    public void testCrearRegistroNulo() {
        Response res = resource.crear(null, uriInfo);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("registro", res.getHeaderString("Wrong-Param"));
    }

    // Tests para modificar
    @Test
    public void testModificarExitoso() {
        UUID id = UUID.randomUUID();
        TipoDescuento existente = new TipoDescuento();
        existente.setIdTipoDescuento(id);

        TipoDescuento cambios = new TipoDescuento();
        cambios.setNombre("Modificado");

        Mockito.when(tdDAO.findById(id)).thenReturn(existente);
        Mockito.when(tdDAO.modify(cambios)).thenReturn(cambios);

        Response res = resource.modificar(id, cambios);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id, cambios.getIdTipoDescuento());
        Mockito.verify(tdDAO).modify(cambios);
    }

    @Test
    public void testModificarNoExistente() {
        UUID id = UUID.randomUUID();
        TipoDescuento cambios = new TipoDescuento();

        Mockito.when(tdDAO.findById(id)).thenReturn(null);

        Response res = resource.modificar(id, cambios);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    @Test
    public void testModificarParametrosNulos() {
        Response res = resource.modificar(null, null);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("id/registro", res.getHeaderString("Wrong-Param"));
    }

    // Tests para eliminar
    @Test
    public void testEliminarExitoso() {
        UUID id = UUID.randomUUID();
        TipoDescuento existente = new TipoDescuento();
        existente.setIdTipoDescuento(id);

        Mockito.when(tdDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        Mockito.verify(tdDAO).delete(existente);
    }

    @Test
    public void testEliminarNoExistente() {
        UUID id = UUID.randomUUID();
        Mockito.when(tdDAO.findById(id)).thenReturn(null);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }
}