package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CajaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Caja> queryCaja;

    @Mock
    private TypedQuery<Long> queryCount;

    @InjectMocks
    private CajaDAO dao;

    @Test
    @DisplayName("findByActivo: Lanza IllegalArgumentException si activo es null o rango es inválido")
    void testFindByActivoArgumentosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> dao.findByActivo(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByActivo(true, -1, 10));
        assertThrows(IllegalArgumentException.class, () -> dao.findByActivo(true, 0, 0));
    }

    @Test
    @DisplayName("findByActivo: Ejecuta consulta correctamente con parámetros válidos")
    void testFindByActivoParametrosValidos() {
        when(em.createNamedQuery("Caja.findByActivo", Caja.class)).thenReturn(queryCaja);
        when(queryCaja.setParameter("activo", true)).thenReturn(queryCaja);
        when(queryCaja.setFirstResult(0)).thenReturn(queryCaja);
        when(queryCaja.setMaxResults(10)).thenReturn(queryCaja);
        when(queryCaja.getResultList()).thenReturn(List.of(new Caja()));

        List<Caja> resultado = dao.findByActivo(true, 0, 10);

        assertFalse(resultado.isEmpty());
        verify(queryCaja).setParameter("activo", true);
        verify(queryCaja).setFirstResult(0);
        verify(queryCaja).setMaxResults(10);
    }

    @Test
    @DisplayName("findByActivo: Propaga AccesoDatosException al ocurrir PersistenceException")
    void testFindByActivoLanzaExcepcionBD() {
        when(em.createNamedQuery("Caja.findByActivo", Caja.class)).thenReturn(queryCaja);
        when(queryCaja.setParameter("activo", true)).thenReturn(queryCaja);
        when(queryCaja.setFirstResult(0)).thenReturn(queryCaja);
        when(queryCaja.setMaxResults(10)).thenReturn(queryCaja);

        PersistenceException causa = new PersistenceException("Error conexion BD");
        when(queryCaja.getResultList()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () ->
            dao.findByActivo(true, 0, 10)
        );
        assertSame(causa, ex.getCause());
    }

    @Test
    @DisplayName("countByActivo: Lanza IllegalArgumentException si activo es null")
    void testCountByActivoArgumentoNull() {
        assertThrows(IllegalArgumentException.class, () -> dao.countByActivo(null));
    }

    @Test
    @DisplayName("countByActivo: Retorna total si la consulta es exitosa")
    void testCountByActivoExitoso() {
        when(em.createNamedQuery("Caja.countByActivo", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("activo", true)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(5L);

        int total = dao.countByActivo(true);

        assertEquals(5, total);
        verify(queryCount).setParameter("activo", true);
    }

    @Test
    @DisplayName("countByActivo: Retorna 0 si getSingleResult devuelve null")
    void testCountByActivoResultNull() {
        when(em.createNamedQuery("Caja.countByActivo", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("activo", false)).thenReturn(queryCount);
        when(queryCount.getSingleResult()).thenReturn(null);

        int total = dao.countByActivo(false);

        assertEquals(0, total);
    }

    @Test
    @DisplayName("countByActivo: Propaga AccesoDatosException al ocurrir PersistenceException")
    void testCountByActivoLanzaExcepcionBD() {
        when(em.createNamedQuery("Caja.countByActivo", Long.class)).thenReturn(queryCount);
        when(queryCount.setParameter("activo", true)).thenReturn(queryCount);

        PersistenceException causa = new PersistenceException("Error al contar registros");
        when(queryCount.getSingleResult()).thenThrow(causa);

        AccesoDatosException ex = assertThrows(AccesoDatosException.class, () ->
            dao.countByActivo(true)
        );
        assertSame(causa, ex.getCause());
    }
}