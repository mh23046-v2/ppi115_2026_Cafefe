package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
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
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;

/**
 *
 * @author johnyv
 */
@ExtendWith(MockitoExtension.class)
public class TipoDescuentoDAOTest {
    
    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TipoDescuento> typedQuery;

    @InjectMocks
    private TipoDescuentoDAO tipoDescuentoDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertNotNull(tipoDescuentoDAO.getEntityManager());
        assertEquals(em, tipoDescuentoDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo debe retornar la lista correspondiente al filtro")
    void testFindByActivo() {
        Boolean activo = true;
        List<TipoDescuento> esperados = Collections.singletonList(new TipoDescuento());

        when(em.createNamedQuery("TipoDescuento.findByActivo", TipoDescuento.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", activo)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<TipoDescuento> resultado = tipoDescuentoDAO.findByActivo(activo);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("TipoDescuento.findByActivo", TipoDescuento.class);
        verify(typedQuery).setParameter("activo", activo);
    }

    @Test
    @DisplayName("findByNombre consulta con el parámetro nombre y devuelve el resultado")
    void testFindByNombre() {
        String nombre = "Navideño";
        List<TipoDescuento> esperados = Collections.singletonList(new TipoDescuento());

        when(em.createNamedQuery("TipoDescuento.findByNombre", TipoDescuento.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter(eq("nombre"), eq(nombre))).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<TipoDescuento> resultado = tipoDescuentoDAO.findByNombre(nombre);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(em).createNamedQuery("TipoDescuento.findByNombre", TipoDescuento.class);
        verify(typedQuery).setParameter("nombre", nombre);
    }
}
