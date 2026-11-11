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

    // Tests de findByProducto (GET producto/{idProducto}/descuento)
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
    public void testFindByProductoParametrosInvalidos() {
        UUID idProducto = UUID.randomUUID();

        Response res = resource.findByProducto(idProducto, -1, 50);
        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("first", res.getHeaderString("Wrong-Param"));

        res = resource.findByProducto(idProducto, 0, 0);
        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("max", res.getHeaderString("Wrong-Param"));
    }

    // Tests de findById (GET producto/{idProducto}/descuento/{id})
    @Test
    public void testFindByIdExistenteYPerteneciente() {
        UUID idProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        
        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuentoProducto(id);
        dp.setIdProducto(new Producto(idProducto));

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(dp);

        Response res = resource.findById(idProducto, id);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(dp, res.getEntity());
    }

    @Test
    public void testFindByIdNoPerteneceAProducto() {
        UUID idProducto = UUID.randomUUID();
        UUID otroProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        
        DescuentoProducto dp = new DescuentoProducto();
        dp.setIdDescuentoProducto(id);
        dp.setIdProducto(new Producto(otroProducto));

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(dp);

        Response res = resource.findById(idProducto, id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    @Test
    public void testFindByIdNoEncontrado() {
        UUID idProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        
        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(null);

        Response res = resource.findById(idProducto, id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    // Tests de crear (POST producto/{idProducto}/descuento)
    @Test
    public void testCrearExitoso() throws Exception {
        UUID idProducto = UUID.randomUUID();
        DescuentoProducto dp = new DescuentoProducto();
        URI uriDummy = new URI("http://localhost:8080/Cafefe-1.0-SNAPSHOT/resources/v2/producto/" + idProducto + "/descuento/dummy-id");

        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(idProducto, dp, uriInfo);

        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Assertions.assertNotNull(dp.getIdDescuentoProducto());
        Assertions.assertEquals(idProducto, dp.getIdProducto().getIdProducto());
        Assertions.assertEquals(uriDummy, res.getLocation());
        Mockito.verify(descuentoProductoDAO).create(dp);
    }

    @Test
    public void testCrearRegistroNulo() {
        UUID idProducto = UUID.randomUUID();
        Response res = resource.crear(idProducto, null, uriInfo);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("registro", res.getHeaderString("Wrong-Param"));
    }

    // Tests de modificar (PUT producto/{idProducto}/descuento/{id})
    @Test
    public void testModificarExitoso() {
        UUID idProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        
        DescuentoProducto existente = new DescuentoProducto();
        existente.setIdDescuentoProducto(id);
        existente.setIdProducto(new Producto(idProducto));

        DescuentoProducto cambios = new DescuentoProducto();

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(existente);
        Mockito.when(descuentoProductoDAO.modify(cambios)).thenReturn(cambios);

        Response res = resource.modificar(idProducto, id, cambios);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id, cambios.getIdDescuentoProducto());
        Assertions.assertEquals(idProducto, cambios.getIdProducto().getIdProducto());
        Mockito.verify(descuentoProductoDAO).modify(cambios);
    }

    @Test
    public void testModificarNoPertenece() {
        UUID idProducto = UUID.randomUUID();
        UUID otroProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        DescuentoProducto existente = new DescuentoProducto();
        existente.setIdDescuentoProducto(id);
        existente.setIdProducto(new Producto(otroProducto));

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(existente);

        Response res = resource.modificar(idProducto, id, new DescuentoProducto());

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    @Test
    public void testModificarRegistroNulo() {
        UUID idProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        
        Response res = resource.modificar(idProducto, id, null);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("registro", res.getHeaderString("Wrong-Param"));
    }

    // Tests de eliminar (DELETE producto/{idProducto}/descuento/{id})
    @Test
    public void testEliminarExitoso() {
        UUID idProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        
        DescuentoProducto existente = new DescuentoProducto();
        existente.setIdDescuentoProducto(id);
        existente.setIdProducto(new Producto(idProducto));

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(idProducto, id);

        Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        Mockito.verify(descuentoProductoDAO).delete(existente);
    }

    @Test
    public void testEliminarNoPertenece() {
        UUID idProducto = UUID.randomUUID();
        UUID otroProducto = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        DescuentoProducto existente = new DescuentoProducto();
        existente.setIdDescuentoProducto(id);
        existente.setIdProducto(new Producto(otroProducto));

        Mockito.when(descuentoProductoDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(idProducto, id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }
}