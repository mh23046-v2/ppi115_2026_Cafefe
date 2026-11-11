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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@ExtendWith(MockitoExtension.class)
public class ProductoCaracteristicaResourceTest {

    @Mock
    private ProductoCaracteristicaDAO productoCaracteristicaDAO;

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private ProductoCaracteristicaResource resource;

    @Test
    public void testFindByProductoExitoso() {
        UUID idProducto = UUID.randomUUID();
        ProductoCaracteristica pc = new ProductoCaracteristica();

        Mockito.when(productoCaracteristicaDAO.findByProducto(Mockito.any(Producto.class), Mockito.eq(0), Mockito.eq(50)))
                .thenReturn(List.of(pc));
        Mockito.when(productoCaracteristicaDAO.countByProducto(Mockito.any(Producto.class))).thenReturn(1);

        Response res = resource.findByProducto(idProducto, 0, 50);
        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));
    }

    @Test
    public void testCrearExitoso() throws Exception {
        UUID idProducto = UUID.randomUUID();
        UUID idCaracteristica = UUID.randomUUID();

        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setExpresionRegular("^[0-9]+$");

        Caracteristica c = new Caracteristica();
        c.setIdCaracteristica(idCaracteristica);
        c.setActivo(true);
        c.setIdTipoCaracteristica(tc);

        ProductoCaracteristica pc = new ProductoCaracteristica();
        pc.setIdCaracteristica(c);
        pc.setValor("100");

        URI uriDummy = new URI("http://localhost/dummy");
        Mockito.when(caracteristicaDAO.findById(idCaracteristica)).thenReturn(c);
        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(idProducto, pc, uriInfo);
        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Mockito.verify(productoCaracteristicaDAO).create(pc);
    }

    @Test
    public void testCrearReglaValorInvalido() {
        UUID idProducto = UUID.randomUUID();
        UUID idCaracteristica = UUID.randomUUID();

        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setExpresionRegular("^[0-9]+$");

        Caracteristica c = new Caracteristica();
        c.setIdCaracteristica(idCaracteristica);
        c.setActivo(true);
        c.setIdTipoCaracteristica(tc);

        ProductoCaracteristica pc = new ProductoCaracteristica();
        pc.setIdCaracteristica(c);
        pc.setValor("TextoInvalido"); // No cumple el regex numérico

        Mockito.when(caracteristicaDAO.findById(idCaracteristica)).thenReturn(c);

        Response res = resource.crear(idProducto, pc, uriInfo);
        Assertions.assertEquals(422, res.getStatus());
        Mockito.verifyNoInteractions(productoCaracteristicaDAO);
    }
}