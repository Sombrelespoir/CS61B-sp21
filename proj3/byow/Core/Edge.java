package byow.Core;

import java.io.Serializable;

public class Edge implements Comparable<Edge>, Serializable {
    private Room room1, room2;
    private int distance;

    public Edge(Room room1, Room room2) {
        this.room1 = room1;
        this.room2 = room2;
        this.distance = calculateDistance(room1, room2);
    }

    private int calculateDistance(Room r1, Room r2) {
        return Math.abs(r1.getCenterX() - r2.getCenterX())
                + Math.abs(r1.getCenterY() - r2.getCenterY());
    }

    public Room getRoom1() {
        return room1;
    }

    public Room getRoom2() {
        return room2;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Edge other) {
        return Integer.compare(this.distance, other.distance);
    }
}
