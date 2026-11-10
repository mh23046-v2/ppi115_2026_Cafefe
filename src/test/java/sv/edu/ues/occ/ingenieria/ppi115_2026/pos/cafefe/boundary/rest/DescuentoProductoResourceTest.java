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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@ExtendWith(MockitoExtension.class)
public class DescuentoProductoResourceTest {

    @Mock
    private DescuentoProductoDAO descuentoProductoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private DescuentoProductoResource resource;

    // Tests de findRange
    @Test
    public void testFindRangeExitoso() {
        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuentoProducto(UUID.randomUUID());

        Mockito.when(descuentoProductoDAO.findRange(0, 50)).thenReturn(List.of(dp));
        Mockito.when(descuentoProductoDAO.count()).thenReturn(1L);

        Response res = resource.findRange(0, 50);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));

        List<?> lista = (List<?>) res.getEntity();
        Assertions.assertNotNull(lista);
        Assertions.assertEquals(1, lista.size());
    }

    @Test
    public void testFindRangeAjustaMaxEfectivo() {
        Mockito.when(descuentoProductoDAO.findRange(0, 50)).thenReturn(List.of());
        Mockito.when(descuentoProductoDAO.count()).thenReturn(0L);

        Response res = resource.findRange(0, 100);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Mockito.verify(descuentoProductoDAO).findRange(0, 50);
    }

    // Tests de findById
    @Test
    public void testFindByIdExistente() {
        UUID id = UUID.randomUUID();
        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuentoProducto(id);

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(dp);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(dp, res.getEntity());
    }

    @Test
    public void testFindByIdNoEncontrado() {
        UUID id = UUID.randomUUID();
        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(null);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    // Tests de findByProducto
    @Test
    public void testFindByProductoExitoso() {
        UUID idProducto = UUID.randomUUID();
        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuentoProducto(UUID.randomUUID());

        Mockito.when(descuentoProductoDAO.findByProducto(Mockito.any(Producto.class), Mockito.eq(0), Mockito.eq(50)))
                .thenReturn(List.of(dp));
        Mockito.when(descuentoProductoDAO.countByProducto(Mockito.any(Producto.class))).thenReturn(1);

        Response res = resource.findByProducto(idProducto, 0, 50);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));

        List<?> lista = (List<?>) res.getEntity();
        Assertions.assertNotNull(lista);
        Assertions.assertEquals(1, lista.size());
    }

    @Test
    public void testFindByProductoIdNulo() {
        Response res = resource.findByProducto(null, 0, 50);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("idProducto", res.getHeaderString("Wrong-Param"));
    }

    // Tests de crear
    @Test
    public void testCrearExitoso() throws Exception {
        DescuentoProducto dp = new DescuentoProducto();
        URI uriDummy = new URI("http://localhost:8080/Cafefe-1.0-SNAPSHOT/resources/v2/descuento_producto/dummy-id");

        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(dp, uriInfo);

        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Assertions.assertNotNull(dp.getIdDescuentoProducto());
        Assertions.assertEquals(uriDummy, res.getLocation());
        Mockito.verify(descuentoProductoDAO).create(dp);
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
        DescuentoProducto existente = new DescuentoProducto();
        existente.setIdDescuentoProducto(id);

        DescuentoProducto cambios = new DescuentoProducto();

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(existente);
        Mockito.when(descuentoProductoDAO.modify(cambios)).thenReturn(cambios);

        Response res = resource.modificar(id, cambios);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id, cambios.getIdDescuentoProducto());
        Mockito.verify(descuentoProductoDAO).modify(cambios);
    }

    @Test
    public void testModificarNoExistente() {
        UUID id = UUID.randomUUID();
        DescuentoProducto cambios = new DescuentoProducto();

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(null);

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

    // Tests de eliminar
    @Test
    public void testEliminarExitoso() {
        UUID id = UUID.randomUUID();
        DescuentoProducto existente = new DescuentoProducto();
        existente.setIdDescuentoProducto(id);

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        Mockito.verify(descuentoProductoDAO).delete(existente);
    }

    @Test
    public void testEliminarNoExistente() {
        UUID id = UUID.randomUUID();
        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(null);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }
}