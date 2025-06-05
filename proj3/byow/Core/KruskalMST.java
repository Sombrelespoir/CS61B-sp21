package byow.Core;

import java.util.*;

public class KruskalMST {
    private List<Room> rooms;
    private List<Edge> edges;

    public KruskalMST(List<Room> rooms) {
        this.rooms = rooms;
        this.edges = new ArrayList<>();

        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                edges.add(new Edge(rooms.get(i), rooms.get(j)));
            }
        }
    }

    public List<Edge> getMininumSpanningTree() {
        List<Edge> mst = new ArrayList<>();

        Collections.sort(edges);

        DisjointSet disjointSet = new DisjointSet();
        for (Room room : rooms) {
            disjointSet.makeSet(room);
        }

        for (Edge edge : edges) {
            Room room1 = edge.getRoom1();
            Room room2 = edge.getRoom2();

            if (disjointSet.find(room1) != disjointSet.find(room2)) {
                mst.add(edge);
                disjointSet.union(room1, room2);
            }
        }

        return mst;
    }

    private class DisjointSet {
        private Map<Room, Room> parent = new HashMap<>();

        void makeSet(Room room) {
            parent.put(room, room);
        }

        Room find(Room room) {
            if (parent.get(room) != room) {
                parent.put(room, find(parent.get(room)));
            }
            return parent.get(room);
        }

        void union(Room r1, Room r2) {
            Room root1 = find(r1);
            Room root2 = find(r2);
            if (root1 != root2) {
                parent.put(root1, root2);
            }
        }
    }
}
