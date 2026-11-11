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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoCaracteristicaDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@ExtendWith(MockitoExtension.class)
public class TipoCaracteristicaResourceTest {

    @Mock
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private TipoCaracteristicaResource resource;

    @Test
    public void testFindRangeExitoso() {
        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setIdTipoCaracteristica(UUID.randomUUID());

        Mockito.when(tipoCaracteristicaDAO.findRange(0, 50)).thenReturn(List.of(tc));
        Mockito.when(tipoCaracteristicaDAO.count()).thenReturn(1L);

        Response res = resource.findRange(0, 50);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));
        List<?> lista = (List<?>) res.getEntity();
        Assertions.assertEquals(1, lista.size());
    }

    @Test
    public void testFindRangeParametrosInvalidos() {
        Response res = resource.findRange(-1, 50);
        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("first", res.getHeaderString("Wrong-Param"));

        res = resource.findRange(0, 0);
        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("max", res.getHeaderString("Wrong-Param"));
    }

    @Test
    public void testFindByIdExitoso() {
        UUID id = UUID.randomUUID();
        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setIdTipoCaracteristica(id);

        Mockito.when(tipoCaracteristicaDAO.findById(id)).thenReturn(tc);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(tc, res.getEntity());
    }

    @Test
    public void testFindByIdNoEncontrado() {
        UUID id = UUID.randomUUID();
        Mockito.when(tipoCaracteristicaDAO.findById(id)).thenReturn(null);

        Response res = resource.findById(id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    @Test
    public void testCrearExitoso() throws Exception {
        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setNombre("Talla");
        tc.setExpresionRegular("^[S|M|L|XL]$");

        URI uriDummy = new URI("http://localhost:8080/Cafefe-1.0-SNAPSHOT/resources/v2/tipo_caracteristica/dummy-id");

        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(tc, uriInfo);

        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Assertions.assertNotNull(tc.getIdTipoCaracteristica());
        Assertions.assertEquals(uriDummy, res.getLocation());
        Mockito.verify(tipoCaracteristicaDAO).create(tc);
    }

    @Test
    public void testCrearExpresionRegularInvalida() {
        TipoCaracteristica tc = new TipoCaracteristica();
        tc.setNombre("Talla");
        tc.setExpresionRegular("[A-Z"); // Expresión regular sintácticamente inválida

        Response res = resource.crear(tc, uriInfo);

        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("expresionRegular", res.getHeaderString("Wrong-Param"));
        Mockito.verifyNoInteractions(tipoCaracteristicaDAO);
    }

    @Test
    public void testModificarExitoso() {
        UUID id = UUID.randomUUID();
        TipoCaracteristica existente = new TipoCaracteristica();
        existente.setIdTipoCaracteristica(id);

        TipoCaracteristica cambios = new TipoCaracteristica();
        cambios.setExpresionRegular("^[0-9]+$");

        Mockito.when(tipoCaracteristicaDAO.findById(id)).thenReturn(existente);
        Mockito.when(tipoCaracteristicaDAO.modify(cambios)).thenReturn(cambios);

        Response res = resource.modificar(id, cambios);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id, cambios.getIdTipoCaracteristica());
        Mockito.verify(tipoCaracteristicaDAO).modify(cambios);
    }

    @Test
    public void testEliminarExitoso() {
        UUID id = UUID.randomUUID();
        TipoCaracteristica existente = new TipoCaracteristica();
        existente.setIdTipoCaracteristica(id);

        Mockito.when(tipoCaracteristicaDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(id);

        Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        Mockito.verify(tipoCaracteristicaDAO).delete(existente);
    }

    @Test
    public void testExpresionRegularValidaHelper() {
        Assertions.assertTrue(TipoCaracteristicaResource.expresionRegularValida(null));
        Assertions.assertTrue(TipoCaracteristicaResource.expresionRegularValida("   "));
        Assertions.assertTrue(TipoCaracteristicaResource.expresionRegularValida("^[a-zA-Z0-9]+$"));
        Assertions.assertFalse(TipoCaracteristicaResource.expresionRegularValida("*invalid[regex"));
    }
}