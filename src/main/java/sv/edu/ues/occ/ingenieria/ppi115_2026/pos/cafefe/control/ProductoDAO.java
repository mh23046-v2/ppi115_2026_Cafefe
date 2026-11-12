package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import java.util.List;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

@Stateless
public class ProductoDAO extends AbstractDataAccess<Producto> {

    @PersistenceContext(unitName = "CafefePU")
    private EntityManager em;

    public ProductoDAO() {
        super(Producto.class);
    }

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected List<Order> ordenar(CriteriaBuilder cb, Root<Producto> root) {
        return List.of(
                cb.asc(root.get("nombre")),
                cb.asc(root.get("idProducto"))
        );
    }

    public List<Producto> buscarPorNombre(String texto, int max) {
        if (max <= 0) {
            throw new IllegalArgumentException("El máximo debe ser mayor a cero");
        }
        return ejecutar("No se pudieron buscar los productos", ()
                -> em.createNamedQuery("Producto.buscarPorNombre", Producto.class)
                        .setParameter("texto", patronLike(texto))
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    public List<Producto> findByNombreActivos(String texto, int first, int max) {
        if (first < 0 || max <= 0) {
            throw new IllegalArgumentException("Parámetros de paginación inválidos");
        }
        String filtro = patronLike(texto);
        return ejecutar("No se pudieron consultar los productos activos", ()
                -> em.createNamedQuery("Producto.findByNombreActivos", Producto.class)
                        .setParameter("nombre", filtro)
                        .setFirstResult(first)
                        .setMaxResults(max)
                        .getResultList()
        );
    }

    public List<Producto> findByNombreActivos(String texto, int max) {
        return findByNombreActivos(texto, 0, max);
    }

    public int countByNombreActivos(String texto) {
        String filtro = patronLike(texto);
        return ejecutar("No se pudo contar los productos activos", () -> {
            Long total = em.createNamedQuery("Producto.countByNombreActivos", Long.class)
                    .setParameter("nombre", filtro)
                    .getSingleResult();
            return total != null ? total.intValue() : 0;
        });
    }
    
    /** Catálogo del cliente: productos activos de una categoría, paginados. */
    public List<Producto> findActivosByTipo(TipoProducto tipo, int first, int max) {
        validarTipo(tipo);
        validarRango(first, max);
        return ejecutar("No se pudieron cargar los productos de la categoría", () ->
            em.createNamedQuery("Producto.findActivosByTipo", Producto.class)
                .setParameter("tipo", tipo)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList()
        );
    }

    public long countActivosByTipo(TipoProducto tipo) {
        validarTipo(tipo);
        return ejecutar("No se pudo contar los productos de la categoría", () ->
            em.createNamedQuery("Producto.countActivosByTipo", Long.class)
                .setParameter("tipo", tipo)
                .getSingleResult()
        );
    }

    private static void validarTipo(TipoProducto tipo) {
        if (tipo == null || tipo.getIdTipoProducto() == null) {
            throw new IllegalArgumentException("El tipo de producto es obligatorio");
        }
    }
}