package ss.test.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.Tile;
import ss.spec.server.Player;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(null);
    }

    @Test
    void hasTileInHand() {
        Tile tile1 = new Tile(Color.GREEN, Color.BLUE, Color.YELLOW, 3);
        Tile tile2 = new Tile(Color.BLUE, Color.BLUE, Color.WHITE, 6);

        player.addTileToHand(tile1);
        player.addTileToHand(tile2);

        // Tiles not in hand.
        assertFalse(player.hasTileInHand(
                new Tile(Color.GREEN, Color.PURPLE, Color.YELLOW, 4)));
        assertFalse(player.hasTileInHand(
                new Tile(Color.BLUE, Color.BLUE, Color.WHITE, 5)));

        assertTrue(player.hasTileInHand(tile1));
        // Should recognize rotated tiles.
        assertTrue(player.hasTileInHand(tile2.rotate120()));
    }

    @Test
    void score() {
        assertEquals(0, player.getScore());

        player.addPoints(10);
        assertEquals(10, player.getScore());

        // This does not really happen, but check if it messes stuff up.
        player.addPoints(-25);
        assertEquals(-15, player.getScore());
    }

    @Test
    void endGameSubtractTilesFromScore() {
        player.addTileToHand(new Tile(Color.GREEN, Color.BLUE, Color.YELLOW, 3));
        player.addTileToHand(new Tile(Color.BLUE, Color.BLUE, Color.WHITE, 6));

        assertEquals(0, player.getScore());

        player.endGameSubtractTilesFromScore();

        assertEquals(-9, player.getScore());
    }

    @Test
    void removeTile() {
        Tile tile = new Tile(Color.GREEN, Color.BLUE, Color.YELLOW, 3);

        player.addTileToHand(tile);

        assertTrue(player.hasTileInHand(tile));

        player.removeTile(tile);

        assertFalse(player.hasTileInHand(tile));

        // Removing a nonexistent tile should not mess anything up.
        player.removeTile(new Tile(Color.GREEN, Color.BLUE, Color.YELLOW, 4));
        assertEquals(0, player.getTiles().size());
    }
}
