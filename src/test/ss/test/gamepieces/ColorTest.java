package ss.test.gamepieces;

import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}