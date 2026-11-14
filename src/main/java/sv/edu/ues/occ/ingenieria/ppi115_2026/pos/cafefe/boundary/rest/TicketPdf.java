package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.boundary.rest;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.LineaTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.PagoTicket;
import sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control.CobroService.Ticket;

/**
 * Dibuja el ticket con OpenPDF (librería libre, fork de iText 4).
 *
 * Tiene el ancho de un rollo de impresora térmica (80 mm) y el alto se
 * calcula según cuántas líneas lleve, como un ticket de verdad.
 * Solo dibuja: los datos los arma CobroService.ticket().
 */
public final class TicketPdf {

    /** 80 mm en puntos de PDF (1 pt = 1/72 de pulgada). */
    static final float ANCHO = 226f;
    static final float MARGEN = 12f;
    /** Hoja provisional para medir; cabe un ticket de cientos de productos. */
    static final float ALTO_MEDICION = 14400f;
    /** Cuánto alto se agrega en cada nuevo intento. */
    static final float HOLGURA = 16f;

    static final Color CAFE = new Color(0x6f, 0x4e, 0x37);
    static final Color GRIS = new Color(0x77, 0x77, 0x77);

    static final ZoneId ZONA = ZoneId.of("America/El_Salvador");
    static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.of("es", "SV"));

    private TicketPdf() {
    }

    /**
     * Primero dibuja el ticket en una hoja muy alta solo para medir cuánto
     * ocupa. Luego lo dibuja en una hoja de ese alto; si OpenPDF aún pasa la
     * última línea a otra página, agrega un poco de alto y lo intenta de nuevo.
     * Así el ticket siempre sale en una sola hoja y sin papel en blanco.
     */
    public static byte[] generar(Ticket t) {
        float alto = dibujar(t, ALTO_MEDICION).ocupado() + 2 * MARGEN;
        for (int intento = 0; intento < 10; intento++) {
            Dibujo d = dibujar(t, alto);
            if (d.paginas() == 1) {
                return d.pdf();
            }
            alto += HOLGURA;
        }
        return dibujar(t, ALTO_MEDICION).pdf();
    }

    /** Resultado de dibujar: el PDF, cuántos puntos de alto usó y cuántas páginas salieron. */
    record Dibujo(byte[] pdf, float ocupado, int paginas) {
    }

    static Dibujo dibujar(Ticket t, float alto) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(new Rectangle(ANCHO, alto), MARGEN, MARGEN, MARGEN, MARGEN);
        float ocupado;
        int paginas;
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, salida);
            doc.addTitle("Ticket " + t.idFactura());
            doc.addCreator("Cafefe");
            doc.open();

            Font titulo = fuente(BaseFont.HELVETICA_BOLD, 16, CAFE);
            Font normal = fuente(BaseFont.HELVETICA, 8, Color.BLACK);
            Font negrita = fuente(BaseFont.HELVETICA_BOLD, 8, Color.BLACK);
            Font pequena = fuente(BaseFont.HELVETICA, 7, GRIS);
            Font total = fuente(BaseFont.HELVETICA_BOLD, 11, CAFE);

            doc.add(centrado("Cafefe", titulo));
            doc.add(centrado("Ticket de venta", pequena));
            doc.add(separador());

            doc.add(dato("Factura", corto(t.idFactura()), normal, negrita));
            doc.add(dato("Fecha", t.fecha() != null ? FORMATO_FECHA.format(t.fecha().atZone(ZONA)) : "-", normal, negrita));
            doc.add(dato("Cliente", t.cliente(), normal, negrita));
            doc.add(dato("Caja", t.caja(), normal, negrita));
            doc.add(dato("Atendió", t.cajero(), normal, negrita));
            if (!"PAGADA".equals(t.estado())) {
                doc.add(centrado("Estado: " + t.estado(), fuente(BaseFont.HELVETICA_BOLD, 9, Color.RED)));
            }
            doc.add(separador());

            // Productos: Cant | Producto | Importe
            PdfPTable productos = new PdfPTable(new float[]{1.2f, 6f, 2.6f});
            productos.setWidthPercentage(100);
            productos.addCell(celda("Cant", negrita, Element.ALIGN_LEFT));
            productos.addCell(celda("Producto", negrita, Element.ALIGN_LEFT));
            productos.addCell(celda("Importe", negrita, Element.ALIGN_RIGHT));
            for (LineaTicket l : t.lineas()) {
                productos.addCell(celda(String.valueOf(l.cantidad()), normal, Element.ALIGN_LEFT));
                String detalle = l.cantidad() > 1 ? l.producto() + "\n" + dinero(l.precioUnitario()) + " c/u" : l.producto();
                productos.addCell(celda(detalle, normal, Element.ALIGN_LEFT));
                productos.addCell(celda(dinero(l.subtotal()), normal, Element.ALIGN_RIGHT));
            }
            doc.add(productos);
            doc.add(separador());

            PdfPTable totales = new PdfPTable(new float[]{6f, 3.8f});
            totales.setWidthPercentage(100);
            totales.addCell(celda("TOTAL", total, Element.ALIGN_LEFT));
            totales.addCell(celda(dinero(t.total()), total, Element.ALIGN_RIGHT));
            for (PagoTicket p : t.pagos()) {
                String forma = capitalizar(p.tipoPago()) + (p.referencia() != null ? " (" + p.referencia() + ")" : "");
                totales.addCell(celda(forma, normal, Element.ALIGN_LEFT));
                totales.addCell(celda(dinero(p.monto()), normal, Element.ALIGN_RIGHT));
            }
            totales.addCell(celda("Pagado", negrita, Element.ALIGN_LEFT));
            totales.addCell(celda(dinero(t.pagado()), negrita, Element.ALIGN_RIGHT));
            totales.addCell(celda("Cambio", negrita, Element.ALIGN_LEFT));
            totales.addCell(celda(dinero(t.cambio()), negrita, Element.ALIGN_RIGHT));
            doc.add(totales);
            doc.add(separador());

            doc.add(centrado("¡Gracias por su visita!", fuente(BaseFont.HELVETICA_OBLIQUE, 8, CAFE)));
            // getVerticalPosition: dónde quedó el cursor, medido desde abajo de la hoja.
            ocupado = (alto - MARGEN) - writer.getVerticalPosition(true);
            paginas = writer.getPageNumber();
            doc.close();
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo generar el PDF del ticket", ex);
        }
        return new Dibujo(salida.toByteArray(), ocupado, paginas);
    }

    static String dinero(BigDecimal valor) {
        return "$" + (valor == null ? "0.00" : valor.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }

    /** Las 8 primeras letras del UUID bastan para identificar el ticket a la vista. */
    static String corto(String id) {
        return id == null ? "-" : id.substring(0, Math.min(8, id.length())).toUpperCase(Locale.ROOT);
    }

    /** "TARJETA" → "Tarjeta". */
    static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        String t = texto.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }

    private static Font fuente(String nombre, float tamano, Color color) throws Exception {
        // CP1252 incluye las tildes, la ñ y los signos ¡ ¿
        return new Font(BaseFont.createFont(nombre, BaseFont.CP1252, BaseFont.NOT_EMBEDDED), tamano, Font.NORMAL, color);
    }

    private static Paragraph centrado(String texto, Font f) {
        Paragraph p = new Paragraph(texto, f);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private static Paragraph dato(String etiqueta, String valor, Font normal, Font negrita) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(etiqueta + ": ", negrita));
        p.add(new Chunk(valor == null || valor.isBlank() ? "-" : valor, normal));
        return p;
    }

    /** Línea horizontal del ancho completo (una tabla de una celda con solo el borde de abajo). */
    private static PdfPTable separador() {
        PdfPTable linea = new PdfPTable(1);
        linea.setWidthPercentage(100);
        linea.setSpacingBefore(2f);
        linea.setSpacingAfter(4f);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(GRIS);
        c.setBorderWidth(0.5f);
        c.setFixedHeight(4f);
        linea.addCell(c);
        return linea;
    }

    private static PdfPCell celda(String texto, Font f, int alineacion) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(alineacion);
        c.setPaddingTop(1.5f);
        c.setPaddingBottom(1.5f);
        c.setPaddingLeft(0f);
        c.setPaddingRight(0f);
        return c;
    }
}
