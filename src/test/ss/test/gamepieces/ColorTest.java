package ss.test.gamepieces;

import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.networking.DecodeException;

import static org.junit.jupiter.api.Assertions.*;

class ColorTest {

    @Test
    void isValidNextToColors() {
        assertTrue(Color.BLUE.isValidNextTo(Color.BLUE));
        assertTrue(Color.RED.isValidNextTo(Color.RED));

        assertFalse(Color.RED.isValidNextTo(Color.PURPLE));
        assertFalse(Color.YELLOW.isValidNextTo(Color.GREEN));
    }

    @Test
    void isValidNextToWhite() {
        assertTrue(Color.BLUE.isValidNextTo(Color.WHITE));
        assertTrue(Color.WHITE.isValidNextTo(Color.PURPLE));
        assertTrue(Color.WHITE.isValidNextTo(Color.WHITE));
    }

    @Test
    void decode() throws DecodeException {
        assertEquals(Color.RED, Color.decode('R'));
        assertEquals(Color.PURPLE, Color.decode('P'));
        assertEquals(Color.GREEN, Color.decode('G'));
        assertEquals(Color.YELLOW, Color.decode('Y'));
        assertEquals(Color.BLUE, Color.decode('B'));
        assertEquals(Color.WHITE, Color.decode('W'));

        assertThrows(DecodeException.class, () -> Color.decode(' '));
        assertThrows(DecodeException.class, () -> Color.decode('4'));
        assertThrows(DecodeException.class, () -> Color.decode('D'));
        assertThrows(DecodeException.class, () -> Color.decode('r'));
        assertThrows(DecodeException.class, () -> Color.decode('w'));
        assertThrows(DecodeException.class, () -> Color.decode('Q'));
        assertThrows(DecodeException.class, () -> Color.decode('-'));
    }
}