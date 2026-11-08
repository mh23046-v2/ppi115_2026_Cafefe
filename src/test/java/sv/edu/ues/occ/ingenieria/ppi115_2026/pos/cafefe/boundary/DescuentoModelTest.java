package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.DescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.TipoDescuentoDAO;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

@ExtendWith(MockitoExtension.class)
class DescuentoModelTest {

    @Mock
    private DescuentoDAO descuentoDAO;

    @Mock
    private TipoDescuentoDAO tipoDescuentoDAO;

    @Spy
    @InjectMocks
    private DescuentoModel descuentoModel;

    @Test
    @DisplayName("getDAO debe retornar la instancia inyectada del DAO")
    void testGetDAO() {
        assertNotNull(descuentoModel.getDAO());
        assertEquals(descuentoDAO, descuentoModel.getDAO());
    }

    @Test
    @DisplayName("getId debe retornar el UUID de la entidad Descuento")
    void testGetId() {
        UUID expectedUuid = UUID.randomUUID();
        Descuento entidad = new Descuento(expectedUuid);

        assertEquals(expectedUuid, descuentoModel.getId(entidad));
    }

    @Test
    @DisplayName("nuevoRegistro debe inicializar una nueva entidad Descuento con ID y fechaDesde no nulos")
    void testNuevoRegistro() {
        Descuento resultado = descuentoModel.nuevoRegistro();

        assertNotNull(resultado);
        assertNotNull(resultado.getIdDescuento());
        assertNotNull(resultado.getFechaDesde());
    }

    @Test
    @DisplayName("nombreBean debe retornar el nombre descriptivo del bean")
    void testNombreBean() {
        assertEquals("Descuento", descuentoModel.nombreBean());
    }

    @Test
    @DisplayName("completarTipoDescuento debe invocar buscarPorNombre en el DAO con limite de 10")
    void testCompletarTipoDescuentoExitoso() {
        TipoDescuento tipo = new TipoDescuento(UUID.randomUUID());
        tipo.setNombre("Temporada");
        when(tipoDescuentoDAO.buscarPorNombre("Temp", 10)).thenReturn(List.of(tipo));

        List<TipoDescuento> resultado = descuentoModel.completarTipoDescuento("Temp");

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
        verify(tipoDescuentoDAO).buscarPorNombre("Temp", 10);
    }

    @Test
    @DisplayName("completarTipoDescuento retorna lista vacia ante excepciones del DAO")
    void testCompletarTipoDescuentoConExcepcion() {
        when(tipoDescuentoDAO.buscarPorNombre(anyString(), eq(10)))
                .thenThrow(new RuntimeException("Error simulado en DAO"));

        // Evita que la notificación intente acceder a FacesContext (Mojarra)
        doNothing().when(descuentoModel).notificarError(anyString(), any(Exception.class));

        List<TipoDescuento> resultado = descuentoModel.completarTipoDescuento("Temp");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(descuentoModel).notificarError(eq("No se pudieron cargar los tipos de descuento"), any(Exception.class));
    }

    @Test
    @DisplayName("validarRegistro evalua restricciones de nombre, TipoDescuento activo y coherencia de fechas")
    void testValidarRegistro() {
        Descuento d = new Descuento(UUID.randomUUID());
        Date ahora = new Date();

        // 1. Nombre nulo o en blanco
        d.setNombre("   ");
        String err1 = descuentoModel.validarRegistro(d);
        assertNotNull(err1);
        assertTrue(err1.contains("obligatorio"));

        // 2. TipoDescuento nulo
        d.setNombre("Descuento Black Friday");
        d.setIdTipoDescuento(null);
        String err2 = descuentoModel.validarRegistro(d);
        assertNotNull(err2);
        assertTrue(err2.contains("válido"));

        // 3. TipoDescuento inactivo
        TipoDescuento tipoInactivo = new TipoDescuento(UUID.randomUUID());
        tipoInactivo.setActivo(false);
        d.setIdTipoDescuento(tipoInactivo);
        String err3 = descuentoModel.validarRegistro(d);
        assertNotNull(err3);
        assertTrue(err3.contains("inactivo"));

        // Configurar TipoDescuento activo para continuar
        TipoDescuento tipoActivo = new TipoDescuento(UUID.randomUUID());
        tipoActivo.setActivo(true);
        d.setIdTipoDescuento(tipoActivo);

        // 4. FechaDesde nula
        d.setFechaDesde(null);
        String err4 = descuentoModel.validarRegistro(d);
        assertNotNull(err4);
        assertTrue(err4.contains("obligatoria"));

        // 5. FechaHasta anterior a FechaDesde
        d.setFechaDesde(ahora);
        d.setFechaHasta(new Date(ahora.getTime() - 86400000L)); // Ayer
        String err5 = descuentoModel.validarRegistro(d);
        assertNotNull(err5);
        assertTrue(err5.contains("no puede ser anterior"));

        // 6. Registro valido con fechaHasta nula (descuento indefinido)
        d.setFechaHasta(null);
        assertNull(descuentoModel.validarRegistro(d));

        // 7. Registro valido con rango de fechas correcto
        d.setFechaHasta(new Date(ahora.getTime() + 86400000L)); // Mañana
        assertNull(descuentoModel.validarRegistro(d));
    }
}