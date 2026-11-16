package sv.edu.ues.occ.ingenieria.ppi115_2026.pos.cafefe.control;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
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
}
