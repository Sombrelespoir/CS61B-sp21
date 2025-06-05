package byow.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenerator {
    private final World world;
    private final Random random;
    private final List<Room> rooms;

    public WorldGenerator(World world, Random random) {
        this.world = world;
        this.random = random;
        this.rooms = new ArrayList<>();
    }

    public void generate() {
        generateRooms();

        if (!rooms.isEmpty()) {
            generateCorridors();
        }
    }

    public void generateRooms() {
        int attempts = 40;
        int maxRooms = 20;

        for (int i = 0; i < attempts && rooms.size() < maxRooms; i++) {
            int roomWidth = random.nextInt(6) + 4;
            int roomHeight = random.nextInt(6) + 4;
            int roomX = random.nextInt(world.getWidth() - roomWidth - 2) + 1;
            int roomY = random.nextInt(world.getHeight() - roomHeight - 2) + 1;

            Room newRoom = new Room(roomX, roomY, roomWidth, roomHeight);

            boolean isOverlap = false;
            for (Room room : rooms) {
                if (newRoom.overlaps(room)) {
                    isOverlap = true;
                    break;
                }
            }

            if (!isOverlap) {
                rooms.add(newRoom);
                newRoom.draw(world);
            }
        }
    }

    public void generateCorridors() {
        if (rooms.size() < 2) {
            return;
        }
        KruskalMST mst = new KruskalMST(rooms);
        List<Edge> corridors = mst.getMininumSpanningTree();

        for (Edge edge : corridors) {
            connectRooms(edge.getRoom1(), edge.getRoom2());
        }
    }

    private void connectRooms(Room room1, Room room2) {
        int x1 = room1.getCenterX();
        int y1 = room1.getCenterY();
        int x2 = room2.getCenterX();
        int y2 = room2.getCenterY();

        CorridorBuilder builder = new CorridorBuilder(world);
        builder.buildCorridor(x1, y1, x2, y2);
    }
}
