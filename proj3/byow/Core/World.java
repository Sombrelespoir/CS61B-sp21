package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.util.Random;

public class World implements Serializable {
    private final int width;
    private final int height;
    private final long seed;
    private TETile[][] tiles;

    public World(int w, int h, long s) {
        this.width = w;
        this.height = h;
        this.seed = s;
        initializeWorld(s);
    }

    public void initializeWorld(long seed) {
        Random random = new Random(seed);
        tiles = new TETile[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }

        WorldGenerator generator = new WorldGenerator(this, random);
        generator.generate();

    }

    public void setTile(int x, int y, TETile tile) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            tiles[x][y] = tile;
        }
    }

    public TETile[][] getTiles() {
        return tiles;
    }

    public TETile getTile(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return tiles[x][y];
        }
        return Tileset.NOTHING;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public long getSeed() {
        return seed;
    }
}
