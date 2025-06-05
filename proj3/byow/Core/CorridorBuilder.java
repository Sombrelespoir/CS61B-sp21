package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class CorridorBuilder {
    private World world;

    public void CorridorBuilder(World world) {
        this.world = world;
    }

    public void buildCorridor(int x1, int y1, int x2, int y2) {
        int currentX = x1;
        int currentY = y1;

        while (currentX != x2) {
            placeTile(currentX, currentY, Tileset.FLOOR);

            if (!isFloor(currentX, currentY + 1)) {
                placeTile(currentX, currentY + 1, Tileset.WALL);
            }
            if (!isFloor(currentX, currentY - 1)) {
                placeTile(currentX, currentY - 1, Tileset.WALL);
            }

            currentX += Integer.compare(x2, currentX);
        }

        while (currentY != y2) {
            placeTile(currentX, currentY, Tileset.FLOOR);

            if (!isFloor(currentX + 1, currentY)) {
                placeTile(currentX + 1, currentY, Tileset.WALL);
            }
            if (!isFloor(currentX - 1, currentY)) {
                placeTile(currentX - 1, currentY, Tileset.WALL);
            }

            currentY += Integer.compare(y2, currentY);
        }
    }

    private void placeTile(int x, int y, TETile tile) {
        if (!isWall(x, y)) {
            world.setTile(x, y, tile);
        }
    }

    private boolean isWall(int x, int y) {
        return world.getTile(x, y) == Tileset.WALL;
    }

    private boolean isFloor(int x, int y) {
        return world.getTile(x, y) == Tileset.FLOOR;
    }
}
