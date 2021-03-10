package impl.algo.astar.dto;

import impl.algo.astar.data.Constants;

public enum CellType {
    EMPTY(Constants.CELL_COLOR, 0),

    BLOCK(Constants.BLOCK_COLOR, 10),
    OBSTACLE(Constants.OBSTACLE_COLOR, 9),
    DEAD_BORDER(Constants.DEAD_BORDER_COLOR, 1),

    OPEN(Constants.OPEN_COLOR, 4),
    CLOSED(Constants.CLOSED_COLOR, 5),

    START(Constants.START_COLOR, 10),
    PATH(null, 6),
    END(Constants.END_COLOR, 10);

    private String color;
    private int priority;

    CellType(String color, int priority) {
        this.color = color;
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {

        if (!this.equals(CellType.PATH)) {
            throw new RuntimeException("Only the path cells can have their color changed");
        }

        this.color = color;
    }
}
