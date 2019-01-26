package ss.test.server;

import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Tile;
import ss.spec.server.Player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlayerTest {

    @Test
    void hasTileInHand() {
        Player player = new Player(null);

        player.addTileToHand(new Tile(Color.GREEN, Color.BLUE, Color.YELLOW, 3));
        player.addTileToHand(new Tile(Color.BLUE, Color.BLUE, Color.WHITE, 6));

        // Tiles not in hand.
        assertFalse(player.hasTileInHand(new Tile(Color.GREEN, Color.PURPLE, Color.YELLOW, 4)));
        assertFalse(player.hasTileInHand(new Tile(Color.BLUE, Color.BLUE, Color.WHITE, 5)));

        assertTrue(player.hasTileInHand(new Tile(Color.GREEN, Color.BLUE, Color.YELLOW, 3)));
        // Should recognize rotated tiles.
        assertTrue(player.hasTileInHand(new Tile(Color.WHITE, Color.BLUE, Color.BLUE, 6)));
    }
}
