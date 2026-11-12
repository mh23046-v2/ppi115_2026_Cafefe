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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;

@ExtendWith(MockitoExtension.class)
public class DescuentoResourceTest {

    @Mock
    private DescuentoDAO descuentoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private DescuentoResource resource;

    // Tests de findRange
    @Test
    public void testFindRangeExitoso() {
        Descuento d1 = new Descuento();
        d1.setIdDescuento(UUID.randomUUID());
        d1.setNombre("Navideño");

        Mockito.when(descuentoDAO.findRange(0, 50)).thenReturn(List.of(d1));
        Mockito.when(descuentoDAO.count()).thenReturn(1L);

        Response res = resource.findRange(0, 50);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));
        
        List<?> lista = (List<?>) res.getEntity();
        Assertions.assertNotNull(lista);
        Assertions.assertEquals(1, lista.size());
    }

    @Test
    public void testFindRangeAjustaMaxEfectivo() {
        Mockito.when(descuentoDAO.findRange(0, 50)).thenReturn(List.of());
        Mockito.when(descuentoDAO.count()).thenReturn(0L);

        Response res = resource.findRange(0, 100);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Mockito.verify(descuentoDAO).findRange(0, 50);
    }

    // Tests de findById
    @Test
    public void testFindByIdExistente() {
        UUID id = UUID.randomUUID();
        Descuento d = new Descuento();
        d.setIdDescuento(id);

        Mockito.when(descuentoDAO.findById(id)).thenReturn(d);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(d, res.getEntity());
    }

    @Test
    public void testFindByIdNoEncontrado() {
        UUID id = UUID.randomUUID();
        Mockito.when(descuentoDAO.findById(id)).thenReturn(null);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    // Tests de crear
    @Test
    public void testCrearExitoso() throws Exception {
        Descuento d = new Descuento();
        d.setNombre("Descuento 10%");

        URI uriDummy = new URI("http://localhost:8080/Cafefe-1.0-SNAPSHOT/resources/v2/descuento/dummy-id");

        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(d, uriInfo);

        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Assertions.assertNotNull(d.getIdDescuento());
        Assertions.assertEquals(uriDummy, res.getLocation());
        Mockito.verify(descuentoDAO).create(d);
    }

    @Test
    public void testCrearRegistroNulo() {
        Response res = resource.crear(null, uriInfo);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("registro", res.getHeaderString("Wrong-Param"));
    }

    // Tests de modificar
    @Test
    public void testModificarExitoso() {
        UUID id = UUID.randomUUID();
        Descuento existente = new Descuento();
        existente.setIdDescuento(id);

        Descuento cambios = new Descuento();
        cambios.setNombre("Actualizado");

        Mockito.when(descuentoDAO.findById(id)).thenReturn(existente);
        Mockito.when(descuentoDAO.modify(cambios)).thenReturn(cambios);

        Response res = resource.modificar(id, cambios);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id, cambios.getIdDescuento());
        Mockito.verify(descuentoDAO).modify(cambios);
    }

    @Test
    public void testModificarNoExistente() {
        UUID id = UUID.randomUUID();
        Descuento cambios = new Descuento();

        Mockito.when(descuentoDAO.findById(id)).thenReturn(null);

        Response res = resource.modificar(id, cambios);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    @Test
    public void testModificarParametrosNulos() {
        Response res = resource.modificar(null, null);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("id", res.getHeaderString("Wrong-Param"));
    }

    // Tests de eliminar
    @Test
    public void testEliminarExitoso() {
        UUID id = UUID.randomUUID();
        Descuento existente = new Descuento();
        existente.setIdDescuento(id);

        Mockito.when(descuentoDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        Mockito.verify(descuentoDAO).delete(existente);
    }

    @Test
    public void testEliminarNoExistente() {
        UUID id = UUID.randomUUID();
        Mockito.when(descuentoDAO.findById(id)).thenReturn(null);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }
}