package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;

/**
 *
 * @author hernandez
 */
@ExtendWith(MockitoExtension.class)
public class RolDAOTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Rol> typedQuery;

    @InjectMocks
    private RolDAO rolDAO;

    @Test
    @DisplayName("Verifica que el EntityManager está correctamente inyectado")
    void testGetEntityManager() {
        assertEquals(em, rolDAO.getEntityManager());
    }

    @Test
    @DisplayName("findByActivo usa la NamedQuery con el parámetro activo")
    void testFindByActivo() {
        List<Rol> esperados = Collections.singletonList(new Rol(UUID.randomUUID()));
        when(em.createNamedQuery("Rol.findByActivo", Rol.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("activo", true)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Rol> resultado = rolDAO.findByActivo(true);

        assertEquals(esperados, resultado);
        verify(typedQuery).setParameter("activo", true);
    }

    @Test
    @DisplayName("findByNombre usa la NamedQuery con el parámetro nombre")
    void testFindByNombre() {
        List<Rol> esperados = Collections.singletonList(new Rol(UUID.randomUUID()));
        when(em.createNamedQuery("Rol.findByNombre", Rol.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("nombre", "Camarero")).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(esperados);

        List<Rol> resultado = rolDAO.findByNombre("Camarero");

        assertEquals(esperados, resultado);
        verify(typedQuery).setParameter("nombre", "Camarero");
    }

    @Test
    @DisplayName("create guarda el rol con persist")
    void testCreate() {
        Rol rol = new Rol(UUID.randomUUID());

        rolDAO.create(rol);

        verify(em).persist(rol);
    }

    @Test
    @DisplayName("create con null lanza error y no toca la base de datos")
    void testCreateNulo() {
        assertThrows(IllegalArgumentException.class, () -> rolDAO.create(null));

        verifyNoInteractions(em);
    }
}
