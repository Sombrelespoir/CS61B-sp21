package byow.Core;

import byow.TileEngine.Tileset;

import java.io.Serializable;

public class Room implements Serializable {
    private int x, y, width, height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCenterX() {
        return x + width / 2;
    }

    public int getCenterY() {
        return y + height / 2;
    }

    public boolean overlaps(Room other) {
        return x <= other.x + other.width && x + width >= other.x
                && y <= other.y + other.height && y + height >= other.y;
    }

    public void draw(World world) {
        for (int i = x; i < x + width; i++) {
            for (int j = y; j < y + height; j++) {
                if (i == x || i == x + width - 1 || j == y || j == y + height - 1) {
                    world.setTile(i, j, Tileset.WALL);
                } else {
                    world.setTile(i, j, Tileset.FLOOR);
                }
            }
        }
    }
}
