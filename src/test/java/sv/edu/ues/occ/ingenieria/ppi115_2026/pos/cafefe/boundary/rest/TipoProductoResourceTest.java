package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.ProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoProductoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TipoProductoResourceTest {

    private TipoProductoDAO tipoProductoDAO;
    private ProductoDAO productoDAO;
    private UriInfo uriInfo;
    private TipoProductoResource resource;

    private static final UUID ID_TIPO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        tipoProductoDAO = Mockito.mock(TipoProductoDAO.class);
        productoDAO = Mockito.mock(ProductoDAO.class);
        uriInfo = Mockito.mock(UriInfo.class);

        resource = new TipoProductoResource();
        resource.tipoProductoDAO = tipoProductoDAO;
        resource.productoDAO = productoDAO;
    }

    private TipoProducto crearTipo(UUID id, String nombre, Boolean activo) {
        TipoProducto tp = new TipoProducto(id);
        tp.setNombre(nombre);
        tp.setActivo(activo);
        return tp;
    }

    @Test
    void findRangeDiferenciaSoloActivosYTodosAplicandoTope() {
        TipoProducto bebidas = crearTipo(ID_TIPO, "Bebidas", true);
        when(tipoProductoDAO.findActivos(0, 50)).thenReturn(List.of(bebidas));
        when(tipoProductoDAO.countActivos()).thenReturn(1L);

        Response respActivos = resource.findRange(0, 200, true);
        assertEquals(200, respActivos.getStatus());
        assertEquals("1", respActivos.getHeaderString("Total-registros"));
        assertEquals(List.of(bebidas), respActivos.getEntity());
        verify(tipoProductoDAO, never()).findRange(0, 50);

        TipoProducto inactiva = crearTipo(UUID.randomUUID(), "Descontinuados", false);
        when(tipoProductoDAO.findRange(0, 10)).thenReturn(List.of(bebidas, inactiva));
        when(tipoProductoDAO.count()).thenReturn(2L);

        Response respTodos = resource.findRange(0, 10, false);
        assertEquals(200, respTodos.getStatus());
        assertEquals("2", respTodos.getHeaderString("Total-registros"));
        assertEquals(List.of(bebidas, inactiva), respTodos.getEntity());
    }

    @Test
    void productosActivosDevuelve404SiCategoriaNoExisteOListaSiExiste() {
        UUID idInexistente = UUID.randomUUID();
        when(tipoProductoDAO.findById(idInexistente)).thenReturn(null);

        Response resp404 = resource.productosActivos(idInexistente, 0, 50);
        assertEquals(404, resp404.getStatus());
        assertEquals(idInexistente.toString(), resp404.getHeaderString("Entity-Not-Found"));
        verify(productoDAO, never()).findActivosByTipo(any(), Mockito.anyInt(), Mockito.anyInt());

        TipoProducto bebidas = crearTipo(ID_TIPO, "Bebidas", true);
        Producto latte = new Producto(UUID.randomUUID());
        latte.setNombre("Latte Vainilla");
        latte.setPrecioSugerido(new BigDecimal("3.75"));
        latte.setActivo(true);

        when(tipoProductoDAO.findById(ID_TIPO)).thenReturn(bebidas);
        when(productoDAO.findActivosByTipo(bebidas, 0, 50)).thenReturn(List.of(latte));
        when(productoDAO.countActivosByTipo(bebidas)).thenReturn(1L);

        Response resp200 = resource.productosActivos(ID_TIPO, 0, 100);
        assertEquals(200, resp200.getStatus());
        assertEquals("1", resp200.getHeaderString("Total-registros"));
        assertEquals(List.of(latte), resp200.getEntity());
    }

    @Test
    void crearRechazaNombreVacioYAsignaUuidYActivoPorDefecto() {
        TipoProducto invalido = new TipoProducto();
        invalido.setNombre("   ");
        Response resp422 = resource.crear(invalido, uriInfo);
        assertEquals(422, resp422.getStatus());
        assertEquals("entity", resp422.getHeaderString("Wrong-Param"));
        verify(tipoProductoDAO, never()).create(any());

        TipoProducto valido = new TipoProducto();
        valido.setNombre("Postres");
        when(uriInfo.getAbsolutePathBuilder())
                .thenReturn(UriBuilder.fromUri("http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/tipo_producto"));

        Response resp201 = resource.crear(valido, uriInfo);
        assertEquals(201, resp201.getStatus());
        assertNotNull(valido.getIdTipoProducto());
        assertTrue(valido.getActivo());
        assertEquals(
                "http://localhost:9080/Cafefe-1.0-SNAPSHOT/resources/v2/tipo_producto/" + valido.getIdTipoProducto(),
                resp201.getHeaderString("Location")
        );
        verify(tipoProductoDAO).create(valido);
    }

    @Test
    void modificarValidaCoincidenciaDeIdEnRutaYEntidad() {
        TipoProducto cuerpoConOtroId = crearTipo(UUID.randomUUID(), "Bebidas Frías", true);

        Response resp422 = resource.modificar(ID_TIPO, cuerpoConOtroId);
        assertEquals(422, resp422.getStatus());
        assertEquals("id", resp422.getHeaderString("Wrong-Param"));
        verify(tipoProductoDAO, never()).findById(any());

        TipoProducto existente = crearTipo(ID_TIPO, "Bebidas", true);
        TipoProducto cambios = crearTipo(ID_TIPO, "Bebidas Calientes", true);
        when(tipoProductoDAO.findById(ID_TIPO)).thenReturn(existente);
        when(tipoProductoDAO.modify(cambios)).thenReturn(cambios);

        Response resp200 = resource.modificar(ID_TIPO, cambios);
        assertEquals(200, resp200.getStatus());
        assertEquals(cambios, resp200.getEntity());
    }

    @Test
    void eliminarVerificaExistenciaAntesDeBorrar() {
        when(tipoProductoDAO.findById(ID_TIPO)).thenReturn(null);
        Response resp404 = resource.eliminar(ID_TIPO);
        assertEquals(404, resp404.getStatus());
        verify(tipoProductoDAO, never()).delete(any());

        TipoProducto existente = crearTipo(ID_TIPO, "Bebidas", true);
        when(tipoProductoDAO.findById(ID_TIPO)).thenReturn(existente);
        Response resp204 = resource.eliminar(ID_TIPO);
        assertEquals(204, resp204.getStatus());
        verify(tipoProductoDAO).delete(existente);
    }
}