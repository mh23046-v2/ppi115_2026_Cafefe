package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class TipoCaracteristicaDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoCaracteristica> typedQuery;

    @InjectMocks
    private TipoCaracteristicaDAO tipoCaracteristicaDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(tipoCaracteristicaDAO.getEntityManager());
        assertEquals(em, tipoCaracteristicaDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo debe retornar la lista correspondiente al filtro")
    void testFindByActivo() {
        Boolean activo = true;
        List<TipoCaracteristica> esperados = Collections.singletonList(new TipoCaracteristica());

        when(em.createNamedQuery("TipoCaracteristica.findByActivo", TipoCaracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", activo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<TipoCaracteristica> resultado = tipoCaracteristicaDAO.findByActivo(activo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("TipoCaracteristica.findByActivo", TipoCaracteristica.class);
        verify(typedQuery).setParameter("activo", activo);
    }

    @Test
    @DisplayName("findByNombre consulta con el paramétro nombre y devuelve el resultado")
    void testFindByNombre() {
        String nombre = "Volumen";
        List<TipoCaracteristica> esperados = Collections.singletonList(new TipoCaracteristica());

        when(em.createNamedQuery("TipoCaracteristica.findByNombre", TipoCaracteristica.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("nombre"), eq(nombre))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<TipoCaracteristica> resultado = tipoCaracteristicaDAO.findByNombre(nombre);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("TipoCaracteristica.findByNombre", TipoCaracteristica.class);
        verify(typedQuery).setParameter("nombre", nombre);
    }
}
