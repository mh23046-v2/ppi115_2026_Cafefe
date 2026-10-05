package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EtiquetasTest {

    private Etiquetas etiquetas;

    @Mock
    private Caja mockCaja;
    @Mock
    private Caracteristica mockCaracteristica;
    @Mock
    private Descuento mockDescuento;
    @Mock
    private Empleado mockEmpleado;
    @Mock
    private EmpleadoRol mockEmpleadoRol;
    @Mock
    private Factura mockFactura;
    @Mock
    private Orden mockOrden;
    @Mock
    private OrdenProducto mockOrdenProducto;
    @Mock
    private Pago mockPago;
    @Mock
    private Producto mockProducto;
    @Mock
    private Rol mockRol;
    @Mock
    private TipoCaracteristica mockTipoCaracteristica;
    @Mock
    private TipoDescuento mockTipoDescuento;
    @Mock
    private TipoProducto mockTipoProducto;

    @BeforeEach
    public void setUp() {
        etiquetas = new Etiquetas();
    }

    @Nested
    @DisplayName("Pruebas de Entidades Simples con Atributo Nombre")
    class EntidadesNombreTests {

        @Test
        @DisplayName("caja() debe retornar el nombre o cadena vacía si es nulo")
        public void testCaja() {
            assertEquals("", etiquetas.caja(null));

            when(mockCaja.getNombre()).thenReturn("Caja Principal");
            assertEquals("Caja Principal", etiquetas.caja(mockCaja));

            when(mockCaja.getNombre()).thenReturn(null);
            assertEquals("", etiquetas.caja(mockCaja));
        }

        @Test
        @DisplayName("caracteristica() debe retornar el nombre o cadena vacía si es nulo")
        public void testCaracteristica() {
            assertEquals("", etiquetas.caracteristica(null));

            when(mockCaracteristica.getNombre()).thenReturn("Sin Azúcar");
            assertEquals("Sin Azúcar", etiquetas.caracteristica(mockCaracteristica));
        }

        @Test
        @DisplayName("descuento() debe retornar el nombre o cadena vacía si es nulo")
        public void testDescuento() {
            assertEquals("", etiquetas.descuento(null));

            when(mockDescuento.getNombre()).thenReturn("Descuento Navideño");
            assertEquals("Descuento Navideño", etiquetas.descuento(mockDescuento));
        }

        @Test
        @DisplayName("producto() debe retornar el nombre o cadena vacía si es nulo")
        public void testProducto() {
            assertEquals("", etiquetas.producto(null));

            when(mockProducto.getNombre()).thenReturn("Café Americano");
            assertEquals("Café Americano", etiquetas.producto(mockProducto));
        }

        @Test
        @DisplayName("rol() debe retornar el nombre o cadena vacía si es nulo")
        public void testRol() {
            assertEquals("", etiquetas.rol(null));

            when(mockRol.getNombre()).thenReturn("Cajero");
            assertEquals("Cajero", etiquetas.rol(mockRol));
        }

        @Test
        @DisplayName("tipoCaracteristica(), tipoDescuento() y tipoProducto() deben retornar nombre o cadena vacía")
        public void testTipos() {
            assertEquals("", etiquetas.tipoCaracteristica(null));
            assertEquals("", etiquetas.tipoDescuento(null));
            assertEquals("", etiquetas.tipoProducto(null));

            when(mockTipoCaracteristica.getNombre()).thenReturn("Tamaño");
            when(mockTipoDescuento.getNombre()).thenReturn("Porcentual");
            when(mockTipoProducto.getNombre()).thenReturn("Bebida");

            assertEquals("Tamaño", etiquetas.tipoCaracteristica(mockTipoCaracteristica));
            assertEquals("Porcentual", etiquetas.tipoDescuento(mockTipoDescuento));
            assertEquals("Bebida", etiquetas.tipoProducto(mockTipoProducto));
        }
    }

    @Nested
    @DisplayName("Pruebas de Entidades Compuestas y Formatos Complejos")
    class EntidadesCompuestasTests {

        @Test
        @DisplayName("empleado() debe concatenar nombre y apellido eliminando espacios sobrantes")
        public void testEmpleado() {
            assertEquals("", etiquetas.empleado(null));

            when(mockEmpleado.getNombre()).thenReturn("Juan");
            when(mockEmpleado.getApellido()).thenReturn("Pérez");
            assertEquals("Juan Pérez", etiquetas.empleado(mockEmpleado));

            when(mockEmpleado.getNombre()).thenReturn("Juan");
            when(mockEmpleado.getApellido()).thenReturn(null);
            assertEquals("Juan", etiquetas.empleado(mockEmpleado));
        }

        @Test
        @DisplayName("empleadoRol() debe concatenar la etiqueta de empleado y rol")
        public void testEmpleadoRol() {
            assertEquals("", etiquetas.empleadoRol(null));

            when(mockEmpleadoRol.getIdEmpleado()).thenReturn(mockEmpleado);
            when(mockEmpleadoRol.getIdRol()).thenReturn(mockRol);
            when(mockEmpleado.getNombre()).thenReturn("Carlos");
            when(mockEmpleado.getApellido()).thenReturn("Gómez");
            when(mockRol.getNombre()).thenReturn("Administrador");

            assertEquals("Carlos Gómez (Administrador)", etiquetas.empleadoRol(mockEmpleadoRol));
        }

        @Test
        @DisplayName("factura() debe formatear cliente, fecha e ID abreviado")
        public void testFactura() {
            assertEquals("", etiquetas.factura(null));

            UUID id = UUID.fromString("12345678-1234-1234-1234-123456789abc");
            Date fecha = new Date();
            String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(fecha);

            when(mockFactura.getCliente()).thenReturn("María López");
            when(mockFactura.getFechaFacturacion()).thenReturn(fecha);
            when(mockFactura.getIdFactura()).thenReturn(id);

            String esperado = "María López - " + fechaFormateada + " [12345678]";
            assertEquals(esperado, etiquetas.factura(mockFactura));
        }

        @Test
        @DisplayName("orden() debe formatear fecha e ID abreviado")
        public void testOrden() {
            assertEquals("", etiquetas.orden(null));

            UUID id = UUID.fromString("87654321-1234-1234-1234-123456789abc");
            Date fecha = new Date();
            String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(fecha);

            when(mockOrden.getFechaCreacion()).thenReturn(fecha);
            when(mockOrden.getIdOrden()).thenReturn(id);

            String esperado = "Orden " + fechaFormateada + " [87654321]";
            assertEquals(esperado, etiquetas.orden(mockOrden));
        }

        @Test
        @DisplayName("ordenProducto() debe formatear producto, fecha de orden e ID abreviado")
        public void testOrdenProducto() {
            assertEquals("", etiquetas.ordenProducto(null));

            UUID id = UUID.fromString("abcdef12-1234-1234-1234-123456789abc");
            Date fecha = new Date();
            String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(fecha);

            when(mockOrdenProducto.getIdProducto()).thenReturn(mockProducto);
            when(mockProducto.getNombre()).thenReturn("Latte");
            when(mockOrdenProducto.getIdOrden()).thenReturn(mockOrden);
            when(mockOrden.getFechaCreacion()).thenReturn(fecha);
            when(mockOrdenProducto.getIdOrdenProducto()).thenReturn(id);

            String esperado = "Latte - " + fechaFormateada + " [abcdef12]";
            assertEquals(esperado, etiquetas.ordenProducto(mockOrdenProducto));
        }

        @Test
        @DisplayName("pago() debe formatear el estado y el ID abreviado")
        public void testPago() {
            assertEquals("", etiquetas.pago(null));

            UUID id = UUID.fromString("98765432-1234-1234-1234-123456789abc");
            when(mockPago.getEstado()).thenReturn("COMPLETADO");
            when(mockPago.getIdPago()).thenReturn(id);

            assertEquals("Pago COMPLETADO [98765432]", etiquetas.pago(mockPago));
        }
    }
}
