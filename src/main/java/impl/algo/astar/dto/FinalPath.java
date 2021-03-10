package impl.algo.astar.dto;

import java.util.concurrent.ConcurrentLinkedDeque;

public class FinalPath {
    private String color;
    private ConcurrentLinkedDeque<Cell> path;

    public FinalPath(String color, ConcurrentLinkedDeque<Cell> path) {
        this.color = color;
        this.path = path;
    }

    public String getColor() {
        return color;
    }

    public ConcurrentLinkedDeque<Cell> getPath() {
        return path;
    }
}
