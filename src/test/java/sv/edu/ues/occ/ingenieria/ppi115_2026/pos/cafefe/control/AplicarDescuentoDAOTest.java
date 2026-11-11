package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.DescuentoProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Aplicar descuento en una sola transacción y validación de tipos duplicados. */
@ExtendWith(MockitoExtension.class)
class AplicarDescuentoDAOTest {

    @Mock
    EntityManager em;
    @Mock
    TypedQuery<OrdenProducto> lineas;
    @Mock
    TypedQuery<Long> conteo;

    private OrdenProductoDAO dao;
    private Orden orden;
    private Producto cafe;
    private Producto pie;
    private Descuento platinum;

    @BeforeEach
    void preparar() throws ReflectiveOperationException {
        dao = conEm(new OrdenProductoDAO());
        orden = new Orden(UUID.randomUUID());
        cafe = producto("2.50");
        pie = producto("5.00");
        TipoDescuento tipo = new TipoDescuento(UUID.randomUUID());
        tipo.setActivo(true);
        tipo.setDescuentoMaximo(100);
        platinum = new Descuento(UUID.randomUUID());
        platinum.setIdTipoDescuento(tipo);
        lenient().when(em.createNamedQuery("OrdenProducto.findByOrden", OrdenProducto.class)).thenReturn(lineas);
        lenient().when(lineas.setParameter(anyString(), any())).thenReturn(lineas);
    }

    @Test
    @DisplayName("Solo cambia el precio de las líneas cuyo producto tiene el descuento, y guarda todo junto")
    void aplicaSoloALosProductosConDescuento() {
        OrdenProducto lineaPie = linea(pie);
        OrdenProducto lineaCafe = linea(cafe);
        when(lineas.getResultList()).thenReturn(List.of(lineaPie, lineaCafe));

        int aplicados = dao.aplicarDescuento(orden, List.of(asignar(pie, 20)));

        assertEquals(1, aplicados);
        assertEquals(new BigDecimal("4.00"), lineaPie.getPrecio());
        assertEquals(new BigDecimal("2.50"), lineaCafe.getPrecio());
        verify(lineas).setParameter("orden", orden);
        verify(em).flush();
        verify(em, never()).merge(any());
    }

    @Test
    @DisplayName("Aplicar el mismo descuento dos veces no descuenta doble (parte del precio sugerido)")
    void noDescuentaDoble() {
        OrdenProducto lineaPie = linea(pie);
        when(lineas.getResultList()).thenReturn(List.of(lineaPie));
        List<DescuentoProducto> vigentes = List.of(asignar(pie, 20));

        dao.aplicarDescuento(orden, vigentes);
        dao.aplicarDescuento(orden, vigentes);

        assertEquals(new BigDecimal("4.00"), lineaPie.getPrecio());
    }

    @Test
    @DisplayName("Si la base falla se lanza AccesoDatosException (el contenedor revierte toda la transacción)")
    void errorDeBase() {
        when(lineas.getResultList()).thenThrow(new PersistenceException("caída"));

        assertThrows(AccesoDatosException.class, () -> dao.aplicarDescuento(orden, List.of(asignar(pie, 20))));
    }

    @Test
    @DisplayName("Rechaza orden sin ID o sin descuentos sin tocar la base")
    void argumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.aplicarDescuento(null, List.of(asignar(pie, 20))));
        assertThrows(IllegalArgumentException.class, () -> dao.aplicarDescuento(new Orden(), List.of(asignar(pie, 20))));
        assertThrows(IllegalArgumentException.class, () -> dao.aplicarDescuento(orden, List.of()));
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("existeAsignacion: consulta por producto y tipo excluyendo el registro en edición")
    void existeAsignacion() throws ReflectiveOperationException {
        ProductoTipoProductoDAO ptpDao = conEm(new ProductoTipoProductoDAO());
        TipoProducto tipo = new TipoProducto(UUID.randomUUID());
        UUID propio = UUID.randomUUID();
        when(em.createNamedQuery("ProductoTipoProducto.countByProductoYTipo", Long.class)).thenReturn(conteo);
        when(conteo.setParameter(anyString(), any())).thenReturn(conteo);
        when(conteo.getSingleResult()).thenReturn(1L, 0L);

        assertTrue(ptpDao.existeAsignacion(pie, tipo, propio));
        assertFalse(ptpDao.existeAsignacion(pie, tipo, null));
        verify(conteo).setParameter("excluir", propio);
        verify(conteo).setParameter("excluir", new UUID(0L, 0L));
        assertFalse(ptpDao.existeAsignacion(null, tipo, null));
        assertFalse(ptpDao.existeAsignacion(pie, null, null));
    }

    // ---------------- utilidades ----------------
    private <D> D conEm(D d) throws ReflectiveOperationException {
        Field f = d.getClass().getDeclaredField("em");
        f.setAccessible(true);
        f.set(d, em);
        return d;
    }

    private Producto producto(String precio) {
        Producto p = new Producto(UUID.randomUUID());
        p.setPrecioSugerido(new BigDecimal(precio));
        return p;
    }

    private DescuentoProducto asignar(Producto p, int valor) {
        DescuentoProducto dp = new DescuentoProducto(UUID.randomUUID());
        dp.setIdDescuento(platinum);
        dp.setIdProducto(p);
        dp.setValor(valor);
        return dp;
    }

    private OrdenProducto linea(Producto p) {
        OrdenProducto op = new OrdenProducto(UUID.randomUUID());
        op.setIdOrden(orden);
        op.setIdProducto(p);
        op.setPrecio(p.getPrecioSugerido());
        return op;
    }
}
