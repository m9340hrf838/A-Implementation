package impl.algo.astar.dto;

import impl.algo.astar.Exceptions.CellMutationNotAllowed;
import impl.algo.astar.data.Constants;
import impl.algo.astar.data.Data;
import impl.algo.astar.utils.UI;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public final class Cell {

    BorderPane fxNode;
    private int x, y;
    private volatile CellType cellType = CellType.EMPTY;
    private volatile boolean obstacle = false;
    private volatile boolean deadBorder = false;

    private Map<Cell, Map<Cell, Cell>> parentValues = new ConcurrentHashMap<>();
    private Map<Cell, Map<Cell, Double>> gValues = new ConcurrentHashMap<>();

    public Cell(int x, int y, BorderPane fxNode) {
        this.fxNode = fxNode;
        this.x = x;
        this.y = y;
    }

    public List<Cell> getNeighbours(Cell start, Cell end, ConcurrentSkipListSet<Cell> open, ConcurrentLinkedQueue<Cell> closed) {

        List<Cell> neighbours = new LinkedList<>();

        // top
        if (this.y > 0) {
            Cell top = Data.getCellFromTheGrid(x, y - 1);
            top.setG(start, end, this.gValues.get(start).get(end) + 0.5);

            // if the neighbour is: not the current cell, not blocked, not in closed, not in open with a lower f
            if (!compareCoordinates(top)
                && Data.OBSTACLES.stream().noneMatch(top::compareCoordinates)
                && Data.BLOCKS.stream().noneMatch(top::compareCoordinates)
                && closed.stream().noneMatch(top::compareCoordinates)
                && open.stream().noneMatch(cell -> cell.compareCoordinates(top) && cell.calculateF(start, end) <= top.calculateF(start, end))
            ) {
                neighbours.add(top);
            }
        }

        // bottom
        if (this.y < Constants.GRID_HEIGHT) {
            Cell bottom = Data.getCellFromTheGrid(x, y + 1);
            bottom.setG(start, end, this.gValues.get(start).get(end) + 0.5);

            // if the neighbour is: not the current cell, not blocked, not in closed, not in open with a lower f
            if (!compareCoordinates(bottom)
                && Data.OBSTACLES.stream().noneMatch(bottom::compareCoordinates)
                && Data.BLOCKS.stream().noneMatch(bottom::compareCoordinates)
                && closed.stream().noneMatch(bottom::compareCoordinates)
                && open.stream().noneMatch(cell -> cell.compareCoordinates(bottom) && cell.calculateF(start, end) <= bottom.calculateF(start, end))
            ) {
                neighbours.add(bottom);
            }
        }

        // left
        if (this.x > 0) {
            Cell left = Data.getCellFromTheGrid(x - 1, y);
            left.setG(start, end, this.gValues.get(start).get(end) + 0.5);

            // if the neighbour is: not the current cell, not blocked, not in closed, not in open with a lower f
            if (!compareCoordinates(left)
                && Data.OBSTACLES.stream().noneMatch(left::compareCoordinates)
                && Data.BLOCKS.stream().noneMatch(left::compareCoordinates)
                && closed.stream().noneMatch(left::compareCoordinates)
                && open.stream().noneMatch(cell -> cell.compareCoordinates(left) && cell.calculateF(start, end) <= left.calculateF(start, end))
            ) {
                neighbours.add(left);
            }
        }

        // right
        if (this.y < Constants.GRID_WIDTH) {
            Cell right = Data.getCellFromTheGrid(x + 1, y);
            right.setG(start, end, this.gValues.get(start).get(end) + 0.5);

            // if the neighbour is: not the current cell, not blocked, not in closed, not in open with a lower f
            if (!compareCoordinates(right)
                && Data.OBSTACLES.stream().noneMatch(right::compareCoordinates)
                && Data.BLOCKS.stream().noneMatch(right::compareCoordinates)
                && closed.stream().noneMatch(right::compareCoordinates)
                && open.stream().noneMatch(cell -> cell.compareCoordinates(right) && cell.calculateF(start, end) <= right.calculateF(start, end))
            ) {
                neighbours.add(right);
            }
        }

        return neighbours;
    }

    public ConcurrentLinkedQueue<Cell> getFence(CellType type) {

        ConcurrentLinkedQueue<Cell> result = new ConcurrentLinkedQueue<>();

        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                Cell cellFromTheGrid = Data.getCellFromTheGrid(x + i, y + j);

                // if: not same cell; is not a fence already; type is undefined or the new type has bigger priority
                if (!(cellFromTheGrid.isObstacle() || cellFromTheGrid.isDeadBorder())
                    && (cellFromTheGrid.cellType == null || cellFromTheGrid.cellType.priority <= type.priority)) {

                    result.add(cellFromTheGrid);
                }
            }
        }

        return result;
    }

    /**
     * meant for the path which has colors varying in a range
     *
     * @param color the path color
     */
    public Cell markAsPathCell(String color) {
        Data.addToPath(this, color);
        return this;
    }

    /**
     * meant to be used when the cell type changes
     *
     * @param type the new cell type
     */
    public void changeType(CellType type) throws CellMutationNotAllowed {

        if (cellType != null && type.equals(CellType.EMPTY) && (isObstacle() || isDeadBorder())) {

            if (isObstacle())
                if ((Data.GROUPED_OBSTACLES.stream().anyMatch(group -> group.stream().anyMatch(this::compareCoordinates)))) {
                    cellType = CellType.OBSTACLE;
                    Platform.runLater(() -> fxNode.setBackground(UI.buildBackground(Constants.OBSTACLE_CELL_BACKGROUND_COLOR)));
                } else {
                    cellType = CellType.EMPTY;
                    Platform.runLater(() -> fxNode.setBackground(UI.buildBackground(cellType)));
                }

            if (isDeadBorder())
                if ((Data.DEAD_BORDER.stream().anyMatch(group -> group.stream().anyMatch(this::compareCoordinates)))) {
                    cellType = CellType.DEAD_BORDER;
                    Platform.runLater(() -> fxNode.setBackground(UI.buildBackground(Constants.DEAD_BORDER_CELL_BACKGROUND_COLOR)));
                } else {
                    cellType = CellType.EMPTY;
                    Platform.runLater(() -> fxNode.setBackground(UI.buildBackground(cellType)));
                }

        } else if (cellType == null
            || type.getPriority() > cellType.getPriority()
            || (cellType.equals(CellType.PATH) && type.equals(CellType.PATH))
            || type.equals(CellType.EMPTY)
        ) {

            if (type.equals(CellType.DEAD_BORDER)) {
                setObstacle(false);
                setDeadBorder(true);
            }
            if (type.equals(CellType.OBSTACLE)) {
                setDeadBorder(false);
                setObstacle(true);
            }

            cellType = type;

            Platform.runLater(() -> fxNode.setBackground(UI.buildBackground(cellType)));

        } else {
            throw new CellMutationNotAllowed(String.format("Cell mutation from %s to %s  on the coordinates x:%d y:%d is not allowed", cellType.toString(), type.toString(), x, y));
        }
    }

    public void changeTypeToPath(String color) throws CellMutationNotAllowed {

        if (CellType.PATH.getPriority() > cellType.getPriority() || cellType.equals(CellType.PATH)) {

            cellType = CellType.PATH;
            cellType.setColor(color);

            Platform.runLater(() -> {
                fxNode.setBackground(UI.buildBackground(cellType));
            });

        } else {
            throw new CellMutationNotAllowed(String.format("Cell mutation from %s to %s  on the coordinates x:%d y:%d is not allowed", cellType.toString(), "PATH", x, y));
        }
    }

    public double calculateF(Cell start, Cell end) {

        int deltaX = this.x - end.getX();
        if (deltaX < 0) deltaX *= -1;

        int deltaY = this.y - end.getY();
        if (deltaY < 0) deltaY *= -1;

        return Math.sqrt((deltaX * deltaX) + (deltaY * deltaY)) + getG(start, end);
    }

    public boolean compareCoordinates(Cell cell) {
        return x == cell.getX() && y == cell.getY();
    }

    public boolean shouldDrawFence(Cell neighbouringCell) {

        // don't draw border between two obstacle cells
        if (isDeadBorder() && neighbouringCell.isDeadBorder()) {
            if (Data.DEAD_BORDER.stream().anyMatch(group -> group.stream().filter(c -> c.compareCoordinates(neighbouringCell) || c.compareCoordinates(this)).count() > 1))
                return false;
        }

        // don't draw border between two dead border cells
        if (isObstacle() && neighbouringCell.isObstacle()) {
            if (Data.GROUPED_OBSTACLES.stream().anyMatch(group -> group.stream().filter(c -> c.compareCoordinates(neighbouringCell) || c.compareCoordinates(this)).count() > 1))
                return false;
        }

        return true;
    }

    public String getBorderColor() {
        if (isDeadBorder()) return Constants.DEAD_BORDER_COLOR;
        if (isObstacle()) return Constants.OBSTACLE_COLOR;
        return Constants.BORDER_COLOR;
    }

    public CellType getCellType() {
        return cellType;
    }

    public BorderPane getFxNode() {
        return fxNode;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getG(Cell start, Cell end) {
        Map<Cell, Double> startGValues = gValues.get(start);
        if (startGValues != null) {
            return startGValues.getOrDefault(end, 1000.0);
        }
        return 1000.0;
    }

    public void setG(Cell start, Cell end, double g) {
        Map<Cell, Double> gForEndPoints = gValues.get(start);

        if (gForEndPoints == null) {

            ConcurrentHashMap<Cell, Double> newGForEndPoints = new ConcurrentHashMap<>();
            newGForEndPoints.put(end, g);
            gValues.put(start, newGForEndPoints);

        } else {

            gForEndPoints.put(end, g);
        }
    }

    public Cell getParent(Cell start, Cell end) {
        Map<Cell, Cell> startGValues = parentValues.get(start);
        if (startGValues != null) {
            return startGValues.get(end);
        }
        return null;
    }

    public void setParent(Cell start, Cell end, Cell parent) {
        Map<Cell, Cell> gForEndPoints = parentValues.get(start);

        if (gForEndPoints == null) {

            ConcurrentHashMap<Cell, Cell> newGForEndPoints = new ConcurrentHashMap<>();
            newGForEndPoints.put(end, parent);
            parentValues.put(start, newGForEndPoints);

        } else {

            gForEndPoints.put(end, parent);
        }
    }

    public boolean isObstacle() {
        return obstacle;
    }

    public void setObstacle(boolean obstacle) {
        this.obstacle = obstacle;
        UI.updateCellBorder(this);
    }

    public boolean isDeadBorder() {
        return deadBorder;
    }

    public void setDeadBorder(boolean deadBorder) {
        this.deadBorder = deadBorder;
        UI.updateCellBorder(this);
    }

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
}