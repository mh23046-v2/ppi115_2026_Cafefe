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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

@ExtendWith(MockitoExtension.class)
public class CaracteristicaResourceTest {

    @Mock
    private CaracteristicaDAO caracteristicaDAO;

    @Mock
    private UriInfo uriInfo;

    @Mock
    private UriBuilder uriBuilder;

    @InjectMocks
    private CaracteristicaResource resource;

    @Test
    public void testFindByTipoExitoso() {
        UUID idTipo = UUID.randomUUID();
        Caracteristica c = new Caracteristica();
        c.setIdCaracteristica(UUID.randomUUID());

        Mockito.when(caracteristicaDAO.findByTipo(Mockito.any(TipoCaracteristica.class), Mockito.eq(0), Mockito.eq(50)))
                .thenReturn(List.of(c));
        Mockito.when(caracteristicaDAO.countByTipo(Mockito.any(TipoCaracteristica.class))).thenReturn(1L);

        Response res = resource.findByTipo(idTipo, 0, 50);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals("1", res.getHeaderString("Total-registros"));
        List<?> lista = (List<?>) res.getEntity();
        Assertions.assertEquals(1, lista.size());
    }

    @Test
    public void testFindByTipoParametrosInvalidos() {
        UUID idTipo = UUID.randomUUID();

        Response res = resource.findByTipo(idTipo, -1, 50);
        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("first", res.getHeaderString("Wrong-Param"));

        res = resource.findByTipo(idTipo, 0, 0);
        Assertions.assertEquals(422, res.getStatus());
        Assertions.assertEquals("max", res.getHeaderString("Wrong-Param"));
    }

    @Test
    public void testFindByIdExistenteYPerteneciente() {
        UUID idTipo = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Caracteristica c = new Caracteristica();
        c.setIdCaracteristica(id);
        c.setIdTipoCaracteristica(new TipoCaracteristica(idTipo));

        Mockito.when(caracteristicaDAO.findById(id)).thenReturn(c);

        Response res = resource.findById(idTipo, id);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(c, res.getEntity());
    }

    @Test
    public void testFindByIdNoPerteneceATipo() {
        UUID idTipo = UUID.randomUUID();
        UUID otroTipo = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Caracteristica c = new Caracteristica();
        c.setIdCaracteristica(id);
        c.setIdTipoCaracteristica(new TipoCaracteristica(otroTipo));

        Mockito.when(caracteristicaDAO.findById(id)).thenReturn(c);

        Response res = resource.findById(idTipo, id);

        Assertions.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id.toString(), res.getHeaderString("Entity-Not-Found"));
    }

    @Test
    public void testCrearExitoso() throws Exception {
        UUID idTipo = UUID.randomUUID();
        Caracteristica c = new Caracteristica();
        URI uriDummy = new URI("http://localhost:8080/Cafefe-1.0-SNAPSHOT/resources/v2/tipo_caracteristica/" + idTipo + "/caracteristica/dummy-id");

        Mockito.when(uriInfo.getAbsolutePathBuilder()).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.path(Mockito.anyString())).thenReturn(uriBuilder);
        Mockito.when(uriBuilder.build()).thenReturn(uriDummy);

        Response res = resource.crear(idTipo, c, uriInfo);

        Assertions.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        Assertions.assertNotNull(c.getIdCaracteristica());
        Assertions.assertEquals(idTipo, c.getIdTipoCaracteristica().getIdTipoCaracteristica());
        Mockito.verify(caracteristicaDAO).create(c);
    }

    @Test
    public void testModificarExitoso() {
        UUID idTipo = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Caracteristica existente = new Caracteristica();
        existente.setIdCaracteristica(id);
        existente.setIdTipoCaracteristica(new TipoCaracteristica(idTipo));

        Caracteristica cambios = new Caracteristica();

        Mockito.when(caracteristicaDAO.findById(id)).thenReturn(existente);
        Mockito.when(caracteristicaDAO.modify(cambios)).thenReturn(cambios);

        Response res = resource.modificar(idTipo, id, cambios);

        Assertions.assertEquals(Response.Status.OK.getStatusCode(), res.getStatus());
        Assertions.assertEquals(id, cambios.getIdCaracteristica());
        Assertions.assertEquals(idTipo, cambios.getIdTipoCaracteristica().getIdTipoCaracteristica());
        Mockito.verify(caracteristicaDAO).modify(cambios);
    }

    @Test
    public void testEliminarExitoso() {
        UUID idTipo = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        Caracteristica existente = new Caracteristica();
        existente.setIdCaracteristica(id);
        existente.setIdTipoCaracteristica(new TipoCaracteristica(idTipo));

        Mockito.when(caracteristicaDAO.findById(id)).thenReturn(existente);

        Response res = resource.eliminar(idTipo, id);

        Assertions.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), res.getStatus());
        Mockito.verify(caracteristicaDAO).delete(existente);
    }
}