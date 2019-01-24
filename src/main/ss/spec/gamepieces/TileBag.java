package ss.spec.gamepieces;

public interface TileBag {

    void addTile(Tile tile);

    void addAllStartingTiles();

    Tile takeTile() throws EmptyTileBagException;

    int getNumTilesLeft();
}
