package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caja;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Descuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Empleado;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.EmpleadoRol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Factura;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Orden;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.OrdenProducto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Pago;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Producto;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Rol;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoDescuento;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoProducto;

/**
 *
 * @author hernandez
 */

@Named("etiquetas")
@ApplicationScoped
public class Etiquetas {

    public String caja(Caja c) {
        return c == null ? "" : nvl(c.getNombre());
    }

    public String caracteristica(Caracteristica c) {
        return c == null ? "" : nvl(c.getNombre());
    }

    public String descuento(Descuento d) {
        return d == null ? "" : nvl(d.getNombre());
    }

    public String empleado(Empleado e) {
        return e == null ? "" : (nvl(e.getNombre()) + " " + nvl(e.getApellido())).trim();
    }

    public String empleadoRol(EmpleadoRol e) {
        if (e == null) {
            return "";
        }
        return empleado(e.getIdEmpleado()) + " (" + rol(e.getIdRol()) + ")";
    }

    public String factura(Factura f) {
        if (f == null) {
            return "";
        }
        return nvl(f.getCliente()) + " - " + fecha(f.getFechaFacturacion()) + " [" + corto(f.getIdFactura()) + "]";
    }

    public String orden(Orden o) {
        if (o == null) {
            return "";
        }
        return "Orden " + fecha(o.getFechaCreacion()) + " [" + corto(o.getIdOrden()) + "]";
    }

    public String ordenProducto(OrdenProducto o) {
        if (o == null) {
            return "";
        }
        String orden = o.getIdOrden() == null ? "" : fecha(o.getIdOrden().getFechaCreacion());
        return producto(o.getIdProducto()) + " - " + orden + " [" + corto(o.getIdOrdenProducto()) + "]";
    }

    public String pago(Pago p) {
        if (p == null) {
            return "";
        }
        return "Pago " + nvl(p.getEstado()) + " [" + corto(p.getIdPago()) + "]";
    }

    public String producto(Producto p) {
        return p == null ? "" : nvl(p.getNombre());
    }

    public String rol(Rol r) {
        return r == null ? "" : nvl(r.getNombre());
    }

    public String tipoCaracteristica(TipoCaracteristica t) {
        return t == null ? "" : nvl(t.getNombre());
    }

    public String tipoDescuento(TipoDescuento t) {
        return t == null ? "" : nvl(t.getNombre());
    }

    public String tipoProducto(TipoProducto t) {
        return t == null ? "" : nvl(t.getNombre());
    }

    // ---- utilidades ----

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String fecha(Date d) {
        return d == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(d);
    }

    private static String corto(UUID id) {
        return id == null ? "" : id.toString().substring(0, 8);
    }
}
