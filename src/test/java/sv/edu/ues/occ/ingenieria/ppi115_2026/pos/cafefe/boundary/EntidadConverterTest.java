package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntidadConverterTest {

    @Mock
    private EntityManager mockEntityManager;

    @Mock
    private EntityManagerFactory mockEntityManagerFactory;

    @Mock
    private PersistenceUnitUtil mockPersistenceUnitUtil;

    @Mock
    private FacesContext mockFacesContext;

    @Mock
    private UIComponent mockUIComponent;

    @Mock
    private ValueExpression mockValueExpression;

    @Mock
    private ELContext mockELContext;

    @InjectMocks
    private EntidadConverter converter;

    private UUID idPrueba;
    private Producto productoPrueba;

    @BeforeEach
    public void setUp() {
        idPrueba = UUID.randomUUID();
        productoPrueba = new Producto(idPrueba);
    }

    @Nested
    @DisplayName("Pruebas de getAsObject (String -> Entidad)")
    class GetAsObjectTests {

        @Test
        @DisplayName("getAsObject con cadena nula, vacía o con espacios devuelve null")
        public void testGetAsObjectNuloOVacio() {
            assertNull(converter.getAsObject(mockFacesContext, mockUIComponent, null));
            assertNull(converter.getAsObject(mockFacesContext, mockUIComponent, ""));
            assertNull(converter.getAsObject(mockFacesContext, mockUIComponent, "   "));

            verify(mockEntityManager, never()).find(any(), any());
        }

        @Test
        @DisplayName("getAsObject con UUID válido encuentra y retorna la entidad")
        public void testGetAsObjectExito() {
            when(mockUIComponent.getValueExpression("value")).thenReturn(mockValueExpression);
            when(mockFacesContext.getELContext()).thenReturn(mockELContext);
            doReturn(Producto.class).when(mockValueExpression).getType(mockELContext);

            when(mockEntityManager.find(eq(Producto.class), eq(idPrueba))).thenReturn(productoPrueba);

            Object resultado = converter.getAsObject(mockFacesContext, mockUIComponent, idPrueba.toString());

            assertNotNull(resultado);
            assertEquals(productoPrueba, resultado);
            verify(mockEntityManager, times(1)).find(Producto.class, idPrueba);
        }

        @Test
        @DisplayName("getAsObject con un UUID mal formado lanza ConverterException")
        public void testGetAsObjectUUIDInvalido() {
            when(mockUIComponent.getValueExpression("value")).thenReturn(mockValueExpression);
            when(mockFacesContext.getELContext()).thenReturn(mockELContext);
            doReturn(Producto.class).when(mockValueExpression).getType(mockELContext);

            String stringInvalido = "uuid-invalido-1234";

            ConverterException excepcion = assertThrows(
                    ConverterException.class,
                    () -> converter.getAsObject(mockFacesContext, mockUIComponent, stringInvalido)
            );

            assertTrue(excepcion.getMessage().contains("Valor no válido: " + stringInvalido));
        }
    }

    @Nested
    @DisplayName("Pruebas de getAsString (Entidad -> String)")
    class GetAsStringTests {

        @Test
        @DisplayName("getAsString con valor nulo devuelve cadena vacía")
        public void testGetAsStringNulo() {
            String resultado = converter.getAsString(mockFacesContext, mockUIComponent, null);
            assertEquals("", resultado);

            verify(mockEntityManager, never()).getEntityManagerFactory();
        }

        @Test
        @DisplayName("getAsString con entidad válida devuelve el identificador UUID en String")
        public void testGetAsStringExito() {
            when(mockEntityManager.getEntityManagerFactory()).thenReturn(mockEntityManagerFactory);
            when(mockEntityManagerFactory.getPersistenceUnitUtil()).thenReturn(mockPersistenceUnitUtil);
            when(mockPersistenceUnitUtil.getIdentifier(productoPrueba)).thenReturn(idPrueba);

            String resultado = converter.getAsString(mockFacesContext, mockUIComponent, productoPrueba);

            assertNotNull(resultado);
            assertEquals(idPrueba.toString(), resultado);
        }

        @Test
        @DisplayName("getAsString cuando PersistenceUnitUtil no encuentra ID devuelve cadena vacía")
        public void testGetAsStringSinIdentifier() {
            when(mockEntityManager.getEntityManagerFactory()).thenReturn(mockEntityManagerFactory);
            when(mockEntityManagerFactory.getPersistenceUnitUtil()).thenReturn(mockPersistenceUnitUtil);
            when(mockPersistenceUnitUtil.getIdentifier(productoPrueba)).thenReturn(null);

            String resultado = converter.getAsString(mockFacesContext, mockUIComponent, productoPrueba);

            assertEquals("", resultado);
        }
    }
}
