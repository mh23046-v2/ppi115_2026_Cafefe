package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroDespachosTest {

    private static final Instant AHORA = Instant.parse("2026-11-15T18:00:00Z");
    private RegistroDespachos registro;

    @BeforeEach
    void setUp() {
        registro = new RegistroDespachos();
        registro.setReloj(Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void despacharYDeshacer() {
        UUID id = UUID.randomUUID();
        assertFalse(registro.estaDespachada(id));

        registro.despachar(id);
        assertTrue(registro.estaDespachada(id));

        registro.deshacer(id);
        assertFalse(registro.estaDespachada(id));
    }

    @Test
    void idNuloNoEstaDespachado() {
        assertFalse(registro.estaDespachada(null));
    }

    @Test
    void lasMarcasDeMasDeUnDiaSeBorran() {
        UUID vieja = UUID.randomUUID();
        UUID reciente = UUID.randomUUID();
        registro.despachar(vieja);
        registro.setReloj(Clock.fixed(AHORA.plus(Duration.ofHours(20)), ZoneOffset.UTC));
        registro.despachar(reciente);

        registro.setReloj(Clock.fixed(AHORA.plus(Duration.ofHours(25)), ZoneOffset.UTC));

        assertFalse(registro.estaDespachada(vieja));
        assertTrue(registro.estaDespachada(reciente));
        assertEquals(1, registro.cantidad());
    }

    // ---------------------------------------------------------------- pago por adelantado

    @Test
    void unaOrdenPagadaAntesDeDespacharSigueParaCocinaHastaDespacharla() {
        UUID id = UUID.randomUUID();
        registro.registrarCobro(id);
        assertEquals(List.of(id), registro.pagadasSinDespachar());

        registro.despachar(id);
        assertTrue(registro.pagadasSinDespachar().isEmpty());

        // "Volver a la lista" en Cocina: vuelve a aparecer
        registro.deshacer(id);
        assertEquals(List.of(id), registro.pagadasSinDespachar());
    }

    @Test
    void cobrarUnaOrdenYaDespachadaNoLaRegresaACocina() {
        UUID id = UUID.randomUUID();
        registro.despachar(id);
        registro.registrarCobro(id);
        assertTrue(registro.pagadasSinDespachar().isEmpty());
    }

    @Test
    void lasPagadasVanDeLaMasAntiguaALaMasNuevaYVencenAlDia() {
        UUID primera = UUID.randomUUID();
        UUID segunda = UUID.randomUUID();
        registro.registrarCobro(primera);
        registro.setReloj(Clock.fixed(AHORA.plus(Duration.ofHours(2)), ZoneOffset.UTC));
        registro.registrarCobro(segunda);
        assertEquals(List.of(primera, segunda), registro.pagadasSinDespachar());

        registro.setReloj(Clock.fixed(AHORA.plus(Duration.ofHours(25)), ZoneOffset.UTC));
        assertEquals(List.of(segunda), registro.pagadasSinDespachar());
    }

    @Test
    void olvidarQuitaLaOrdenDeTodo() {
        UUID id = UUID.randomUUID();
        registro.registrarCobro(id);
        registro.olvidar(id);
        assertTrue(registro.pagadasSinDespachar().isEmpty());
        registro.registrarCobro(null);
        assertTrue(registro.pagadasSinDespachar().isEmpty());
    }
}
