package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.OrdenService.SolicitudOrdenInvalidaException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrdenServiceTest {

    private OrdenDAO ordenDAO;
    private OrdenProductoDAO ordenProductoDAO;
    private ProductoDAO productoDAO;
    private EmpleadoRolDAO empleadoRolDAO;
    private OrdenService service;

    private static final UUID ID_LATTE = UUID.fromString("00000002-0000-0000-0000-000000000000");
    private static final UUID ID_BROWNIE = UUID.fromString("00000007-0000-0000-0000-000000000000");
    private static final UUID ID_INACTIVO = UUID.fromString("00000008-0000-0000-0000-000000000000");

    @BeforeEach
    void setUp() {
        ordenDAO = Mockito.mock(OrdenDAO.class);
        ordenProductoDAO = Mockito.mock(OrdenProductoDAO.class);
        productoDAO = Mockito.mock(ProductoDAO.class);
        empleadoRolDAO = Mockito.mock(EmpleadoRolDAO.class);

        service = new OrdenService();
        service.ordenDAO = ordenDAO;
        service.ordenProductoDAO = ordenProductoDAO;
        service.productoDAO = productoDAO;
        service.empleadoRolDAO = empleadoRolDAO;
    }

    private EmpleadoRol crearMeseroVirtual() {
        Empleado emp = new Empleado(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        emp.setNombre("Mesero");
        emp.setApellido("Virtual");
        emp.setActivo(true);

        Rol rol = new Rol(UUID.randomUUID());
        rol.setNombre("Tienda en línea");
        rol.setActivo(true);

        EmpleadoRol er = new EmpleadoRol(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        er.setIdEmpleado(emp);
        er.setIdRol(rol);
        er.setActivo(true);
        return er;
    }

    private Producto crearProducto(UUID id, String nombre, String precio, boolean activo) {
        Producto p = new Producto(id);
        p.setNombre(nombre);
        p.setPrecioSugerido(new BigDecimal(precio));
        p.setActivo(activo);
        return p;
    }

    @Test
    void crearOrdenExpandeUnidadesYCalculaTotalDesdeServidor() {
        when(empleadoRolDAO.findActivoTiendaEnLinea()).thenReturn(crearMeseroVirtual());
        when(productoDAO.findById(ID_LATTE)).thenReturn(crearProducto(ID_LATTE, "Latte Vainilla", "3.75", true));
        when(productoDAO.findById(ID_BROWNIE)).thenReturn(crearProducto(ID_BROWNIE, "Brownie", "2.75", true));

        // Envia precio falso desde el cliente (99.99) para comprobar que se ignora
        Map<String, Object> solicitud = Map.of(
                "cliente", "  Ana  ",
                "items", List.of(
                        Map.of("idProducto", ID_LATTE.toString(), "cantidad", 2, "observaciones", "sin azúcar", "precio", 99.99),
                        Map.of("idProducto", ID_BROWNIE.toString())
                )
        );

        Map<String, Object> resultado = service.crearOrden(solicitud);

        assertNotNull(resultado.get("idOrden"));
        assertNotNull(resultado.get("fechaCreacion"));
        assertEquals("Ana", resultado.get("cliente"));
        assertEquals(new BigDecimal("10.25"), resultado.get("total"));
    
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineas = (List<Map<String, Object>>) resultado.get("lineas");
        assertEquals(3, lineas.size());
        assertEquals("Cliente: Ana | sin azúcar", lineas.get(0).get("observaciones"));
        assertEquals(new BigDecimal("3.75"), lineas.get(0).get("precio"));
        assertEquals("Cliente: Ana", lineas.get(2).get("observaciones"));
        assertEquals(new BigDecimal("2.75"), lineas.get(2).get("precio"));

        verify(ordenDAO, times(1)).create(any(Orden.class));
        ArgumentCaptor<OrdenProducto> captor = ArgumentCaptor.forClass(OrdenProducto.class);
        verify(ordenProductoDAO, times(3)).create(captor.capture());
        assertEquals(3, captor.getAllValues().size());
    }

    @Test
    void crearOrdenFallaCon422SiClienteVacioOMuyLargo() {
        SolicitudOrdenInvalidaException exVacio = assertThrows(
                SolicitudOrdenInvalidaException.class,
                () -> service.crearOrden(Map.of("cliente", "   ", "items", List.of()))
        );
        assertEquals("cliente", exVacio.getWrongParam());

        SolicitudOrdenInvalidaException exLargo = assertThrows(
                SolicitudOrdenInvalidaException.class,
                () -> service.crearOrden(Map.of("cliente", "A".repeat(101), "items", List.of()))
        );
        assertEquals("cliente", exLargo.getWrongParam());
        verify(ordenDAO, never()).create(any());
    }

    @Test
    void crearOrdenFallaCon422SiItemsVaciosOCantidadFueraDeRango() {
        SolicitudOrdenInvalidaException exVacios = assertThrows(
                SolicitudOrdenInvalidaException.class,
                () -> service.crearOrden(Map.of("cliente", "Ana", "items", List.of()))
        );
        assertEquals("items", exVacios.getWrongParam());

        SolicitudOrdenInvalidaException exCantidad = assertThrows(
                SolicitudOrdenInvalidaException.class,
                () -> service.crearOrden(Map.of(
                        "cliente", "Ana",
                        "items", List.of(Map.of("idProducto", ID_LATTE.toString(), "cantidad", 21))
                ))
        );
        assertEquals("items[0].cantidad", exCantidad.getWrongParam());
    }

    @Test
    void crearOrdenFallaCon422SiProductoNoExisteOEstaInactivo() {
        when(empleadoRolDAO.findActivoTiendaEnLinea()).thenReturn(crearMeseroVirtual());
        when(productoDAO.findById(ID_INACTIVO)).thenReturn(crearProducto(ID_INACTIVO, "Muffin", "2.50", false));

        SolicitudOrdenInvalidaException ex = assertThrows(
                SolicitudOrdenInvalidaException.class,
                () -> service.crearOrden(Map.of(
                        "cliente", "Ana",
                        "items", List.of(Map.of("idProducto", ID_INACTIVO.toString()))
                ))
        );
        assertEquals("items[0].idProducto", ex.getWrongParam());
        verify(ordenDAO, never()).create(any());
        verify(ordenProductoDAO, never()).create(any());
    }

    @Test
    void crearOrdenFallaCon500SiFaltaRolTiendaEnLinea() {
        when(empleadoRolDAO.findActivoTiendaEnLinea()).thenReturn(null);

        assertThrows(
                AccesoDatosException.class,
                () -> service.crearOrden(Map.of(
                        "cliente", "Ana",
                        "items", List.of(Map.of("idProducto", ID_LATTE.toString()))
                ))
        );
        verify(ordenDAO, never()).create(any());
    }
}