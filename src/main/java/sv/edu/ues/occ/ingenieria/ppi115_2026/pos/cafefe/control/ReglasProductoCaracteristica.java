package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.Caracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.ProductoCaracteristica;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.entity.TipoCaracteristica;

public final class ReglasProductoCaracteristica {

    private ReglasProductoCaracteristica() {
    }

    public static String validar(ProductoCaracteristica registro) {
        if (registro.getIdProducto() == null || registro.getIdProducto().getIdProducto() == null) {
            return "Debe seleccionar un producto válido";
        }
        Caracteristica c = registro.getIdCaracteristica();
        if (c == null || c.getIdCaracteristica() == null) {
            return "Debe seleccionar una característica válida";
        }
        if (!Boolean.TRUE.equals(c.getActivo())) {
            return "La característica seleccionada se encuentra inactiva";
        }
        if (registro.getValor() == null || registro.getValor().isBlank()) {
            return "El valor de la característica es obligatorio";
        }
        String regex = regexDe(c);
        if (regex != null) {
            try {
                if (!Pattern.matches(regex, registro.getValor())) {
                    return "El valor no cumple con la regla o patrón requerido: " + regex;
                }
            } catch (PatternSyntaxException ex) {
                return "La expresión regular asociada al tipo de característica es inválida";
            }
        }
        return null;
    }

    // La expresión regular del tipo de la característica, o null si no hay restricción.
    static String regexDe(Caracteristica c) {
        TipoCaracteristica tipo = c.getIdTipoCaracteristica();
        if (tipo == null) {
            return null;
        }
        String regex = tipo.getExpresionRegular();
        return regex == null || regex.isBlank() ? null : regex;
    }
}