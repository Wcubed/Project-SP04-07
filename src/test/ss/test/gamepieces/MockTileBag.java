package ss.test.gamepieces;

import ss.spec.gamepieces.Color;
import ss.spec.gamepieces.EmptyTileBagException;
import ss.spec.gamepieces.Tile;
import ss.spec.gamepieces.TileBag;

import java.util.LinkedList;

/**
 * Just like the RandomTileBag, only this one is rigged to give all the tiles in order.
 */
public class MockTileBag implements TileBag {

    private LinkedList<Tile> tiles;

    public MockTileBag() {
        tiles = new LinkedList<>();
    }

    @Override
    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    @Override
    public Tile takeTile() throws EmptyTileBagException {
        if (getNumTilesLeft() == 0) {
            throw new EmptyTileBagException();
        }
        return tiles.poll();
    }

    @Override
    public int getNumTilesLeft() {
        return tiles.size();
    }


    /**
     * Pre-determined order.
     * Don't change this order, it is used to test the Game class' handling of the bag.
     */
    @Override
    public void addAllStartingTiles() {

        addTile(new Tile(Color.RED, Color.RED, Color.RED, 6));

        addTile(new Tile(Color.RED, Color.YELLOW, Color.BLUE, 1));

        // Two tiles of the same points, to see if the game can do tie brakes when
        // determining the player order.
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.GREEN, 4));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.YELLOW, 4));

        addTile(new Tile(Color.YELLOW, Color.PURPLE, Color.RED, 2));

        addTile(new Tile(Color.GREEN, Color.GREEN, Color.YELLOW, 4));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.PURPLE, 4));

        addTile(new Tile(Color.YELLOW, Color.PURPLE, Color.GREEN, 2));

        addTile(new Tile(Color.RED, Color.RED, Color.YELLOW, 5));
        addTile(new Tile(Color.RED, Color.RED, Color.PURPLE, 5));
        addTile(new Tile(Color.BLUE, Color.BLUE, Color.RED, 5));

        addTile(new Tile(Color.WHITE, Color.WHITE, Color.WHITE, 1));

        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.BLUE, 5));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.YELLOW, 5));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.GREEN, 5));

        addTile(new Tile(Color.BLUE, Color.BLUE, Color.BLUE, 6));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.GREEN, 6));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.YELLOW, 6));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.PURPLE, 6));


        addTile(new Tile(Color.RED, Color.RED, Color.BLUE, 4));
        addTile(new Tile(Color.RED, Color.RED, Color.GREEN, 4));

        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.RED, 4));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.PURPLE, 4));

        addTile(new Tile(Color.GREEN, Color.RED, Color.PURPLE, 1));
        addTile(new Tile(Color.BLUE, Color.YELLOW, Color.GREEN, 1));

        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.RED, 4));
        addTile(new Tile(Color.PURPLE, Color.PURPLE, Color.BLUE, 4));

        addTile(new Tile(Color.YELLOW, Color.BLUE, Color.PURPLE, 3));
        addTile(new Tile(Color.RED, Color.GREEN, Color.YELLOW, 3));
        addTile(new Tile(Color.BLUE, Color.GREEN, Color.PURPLE, 3));
        addTile(new Tile(Color.GREEN, Color.RED, Color.BLUE, 3));

        addTile(new Tile(Color.BLUE, Color.BLUE, Color.PURPLE, 5));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.RED, 5));
        addTile(new Tile(Color.GREEN, Color.GREEN, Color.BLUE, 5));
        addTile(new Tile(Color.YELLOW, Color.YELLOW, Color.GREEN, 5));


        addTile(new Tile(Color.BLUE, Color.RED, Color.PURPLE, 2));

    }
}
