package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Búsquedas de autocompletes que solo devuelven registros activos. */
@ExtendWith(MockitoExtension.class)
class BusquedaActivosDAOTest {

    @Mock
    EntityManager em;
    @Mock
    TypedQuery<Object> query;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void preparar() {
        lenient().when(em.createNamedQuery(anyString(), any(Class.class))).thenReturn(query);
        lenient().when(query.setParameter(anyString(), any())).thenReturn(query);
        lenient().when(query.setMaxResults(anyInt())).thenReturn(query);
    }

    private static <D> D conEm(D dao, EntityManager em) {
        try {
            Field f = dao.getClass().getDeclaredField("em");
            f.setAccessible(true);
            f.set(dao, em);
            return dao;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** consulta, parámetro y llamada al método de cada DAO. */
    static Stream<Arguments> daos() {
        return Stream.of(
            Arguments.of("TipoProducto.buscarActivosPorNombre", "texto",
                (BiFunction<EntityManager, String, List<?>>) (em, t) -> conEm(new TipoProductoDAO(), em).buscarActivosPorNombre(t, 10)),
            Arguments.of("TipoDescuento.buscarActivosPorNombre", "texto",
                (BiFunction<EntityManager, String, List<?>>) (em, t) -> conEm(new TipoDescuentoDAO(), em).buscarActivosPorNombre(t, 10)),
            Arguments.of("TipoCaracteristica.buscarActivosPorNombre", "texto",
                (BiFunction<EntityManager, String, List<?>>) (em, t) -> conEm(new TipoCaracteristicaDAO(), em).buscarActivosPorNombre(t, 10)),
            Arguments.of("Rol.buscarActivosPorNombre", "nombre",
                (BiFunction<EntityManager, String, List<?>>) (em, t) -> conEm(new RolDAO(), em).buscarActivosPorNombre(t, 10))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("daos")
    @DisplayName("Usa la consulta de activos con el texto normalizado y el límite pedido")
    void usaConsultaDeActivos(String consulta, String parametro, BiFunction<EntityManager, String, List<?>> llamada) {
        when(query.getResultList()).thenReturn(List.of(new Object()));

        List<?> resultado = llamada.apply(em, "  Bebidas ");

        assertEquals(1, resultado.size());
        verify(em).createNamedQuery(eq(consulta), any());
        verify(query).setParameter(parametro, "%bebidas%");
        verify(query).setMaxResults(10);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("daos")
    @DisplayName("Envuelve los errores de la base en AccesoDatosException")
    void envuelveErrores(String consulta, String parametro, BiFunction<EntityManager, String, List<?>> llamada) {
        when(query.getResultList()).thenThrow(new PersistenceException("caída"));

        assertThrows(AccesoDatosException.class, () -> llamada.apply(em, null));
    }

    @Test
    @DisplayName("Rechaza un máximo menor o igual a cero sin tocar la base")
    void rechazaMaximoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> conEm(new TipoProductoDAO(), em).buscarActivosPorNombre("x", 0));
        assertThrows(IllegalArgumentException.class, () -> conEm(new TipoDescuentoDAO(), em).buscarActivosPorNombre("x", 0));
        assertThrows(IllegalArgumentException.class, () -> conEm(new TipoCaracteristicaDAO(), em).buscarActivosPorNombre("x", 0));
        assertThrows(IllegalArgumentException.class, () -> conEm(new RolDAO(), em).buscarActivosPorNombre("x", -1));
        verifyNoInteractions(em);
    }

    @Test
    @DisplayName("inicioDeHoy devuelve hoy a las 00:00 para no descartar descuentos que vencen hoy")
    void inicioDeHoy() {
        assertEquals(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant(),
                DescuentoDAO.inicioDeHoy().toInstant());
    }
}
