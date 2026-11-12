package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

@ExtendWith(MockitoExtension.class)
public class ProductoResourceTest {

    @Mock
    private ProductoDAO productoDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private ProductoResource resource;

    @Test
    public void testFindRangeExitoso() {
        Producto p = new Producto(UUID.randomUUID());
        p.setNombre("Café Americano");

        Mockito.when(productoDAO.findByNombreActivos(Mockito.anyString(), Mockito.eq(0), Mockito.eq(50)))
                .thenReturn(List.of(p));
        Mockito.when(productoDAO.countByNombreActivos(Mockito.anyString())).thenReturn(1);

        Response res = resource.findRange(0, 50, "Café");
        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));
    }

    @Test
    public void testFindByIdExitoso() {
        UUID id = UUID.randomUUID();
        Producto p = new Producto(id);
        
        Mockito.when(productoDAO.findById(id)).thenReturn(p);

        Response res = resource.findById(id);
        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
    }

    @Test
    public void testFindByIdNoEncontrado() {
        UUID id = UUID.randomUUID();
        Mockito.when(productoDAO.findById(id)).thenReturn(null);

        Response res = resource.findById(id);
        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
    }

    @Test
    public void testCrearExitoso() throws Exception {
        Producto p = new Producto();
        p.setNombre("Espresso");
        p.setPrecioSugerido(new BigDecimal("2.50"));

        URI uriDummy = new URI("http://localhost/dummy");
        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(p, uriInfo);
        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Mockito.verify(productoDAO).create(p);
    }
}