package ss.test.gamepieces;

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
    public void addAllStartingTiles() {
        // Do nothing.
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
}
