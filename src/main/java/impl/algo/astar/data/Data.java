package impl.algo.astar.data;

import impl.algo.astar.Exceptions.CellMutationNotAllowed;
import impl.algo.astar.dto.Cell;
import impl.algo.astar.dto.CellType;
import impl.algo.astar.dto.FinalPath;
import impl.algo.astar.utils.UI;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class Data {
    public static volatile ConcurrentLinkedQueue<Cell> START_POINTS = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<Cell> END_POINTS = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<Cell> BLOCKS = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<ConcurrentLinkedQueue<Cell>> GROUPED_OBSTACLES = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<Cell> OBSTACLES = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<ConcurrentLinkedQueue<Cell>> DEAD_BORDER = new ConcurrentLinkedQueue<>();

    public static volatile ConcurrentLinkedQueue<Cell> OPEN = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<Cell> CLOSED = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedDeque<Cell> FINAL_PATH_CELLS = new ConcurrentLinkedDeque<>();
    public static volatile ConcurrentLinkedDeque<ConcurrentLinkedDeque<FinalPath>> FINAL_PATHS = new ConcurrentLinkedDeque<>();
    private static volatile Cell[][] grid = new Cell[Constants.GRID_HEIGHT][Constants.GRID_WIDTH];

    public static void addToStart(Cell cell) {
        try {
            if (START_POINTS.stream().anyMatch(cell::compareCoordinates)) {
                cell.changeType(CellType.EMPTY);
                START_POINTS.removeIf(cell::compareCoordinates);
            } else {
                cell.changeType(CellType.START);
                START_POINTS.add(cell);
            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addToEnd(Cell cell) {
        try {
            if (END_POINTS.stream().anyMatch(cell::compareCoordinates)) {
                cell.changeType(CellType.EMPTY);
                END_POINTS.removeIf(cell::compareCoordinates);
            } else {
                cell.changeType(CellType.END);
                END_POINTS.add(cell);
            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addToBlock(Cell cell) {
        try {
            if (BLOCKS.stream().anyMatch(cell::compareCoordinates)) {
                cell.changeType(CellType.EMPTY);
                BLOCKS.removeIf(cell::compareCoordinates);
            } else {
                cell.changeType(CellType.BLOCK);
                BLOCKS.add(cell);
            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addToObstacle(Cell cell) {
        try {

            List<ConcurrentLinkedQueue<Cell>> existingObstacles = GROUPED_OBSTACLES.stream().filter(group -> group.stream().anyMatch(cell::compareCoordinates)).collect(Collectors.toList());

            if (!existingObstacles.isEmpty()) {
                // REMOVE

                existingObstacles.forEach(group -> {
                    GROUPED_OBSTACLES.remove(group);
                    group.forEach(c -> {
                        OBSTACLES.remove(c);
                        try {
                            if (c.getCellType().equals(CellType.OBSTACLE)) {
                                c.changeType(CellType.EMPTY);
                            }
                            if (c.isObstacle()) c.setObstacle(false);
                            if (c.isDeadBorder()) c.setDeadBorder(false);
                            UI.updateCellBorder(c);
                        } catch (CellMutationNotAllowed e) {
                            System.out.println(e.getMessage());
                        }
                    });
                });

            } else if (!(cell.isObstacle() || cell.isDeadBorder())) {
                // ADD

                ConcurrentLinkedQueue<Cell> fenceCells = cell.getFence(CellType.OBSTACLE);

                // if the square is complete
                if (fenceCells.size() == 9) {
                    for (Cell c : fenceCells) {
                        c.changeType(CellType.OBSTACLE);
                        OBSTACLES.add(c);
                        UI.updateCellBorder(c);
                    }
                    GROUPED_OBSTACLES.add(fenceCells);
                }

            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }

    }

    public static void addToDeadBorder(Cell cell) {
        try {

            List<ConcurrentLinkedQueue<Cell>> existingDeadBorders = DEAD_BORDER.stream().filter(group -> group.stream().anyMatch(cell::compareCoordinates)).collect(Collectors.toList());

            if (!existingDeadBorders.isEmpty()) {
                // REMOVE

                existingDeadBorders.forEach(group -> {
                    DEAD_BORDER.remove(group);
                    group.forEach(c -> {
                        try {

                            if (c.getCellType().equals(CellType.DEAD_BORDER)) {
                                c.changeType(CellType.EMPTY);
                            }
                            if (c.isObstacle()) c.setObstacle(false);
                            if (c.isDeadBorder()) c.setDeadBorder(false);

                            UI.updateCellBorder(c);
                        } catch (CellMutationNotAllowed e) {
                            System.out.println(e.getMessage());
                        }
                    });

                });

            } else if (!(cell.isDeadBorder() || cell.isObstacle())) {
                // ADD
                ConcurrentLinkedQueue<Cell> fenceCells = cell.getFence(CellType.DEAD_BORDER);

                // if the square is complete
                if (fenceCells.size() == 9) {
                    for (Cell c : fenceCells) {
                        c.changeType(CellType.DEAD_BORDER);
                        UI.updateCellBorder(c);
                    }
                    DEAD_BORDER.add(fenceCells);
                }

            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }

    }

    public static void addToPath(Cell cell, String color) {
        try {
            cell.changeTypeToPath(color);
            FINAL_PATH_CELLS.add(cell);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addToOpen(Cell cell) {
        try {
            if (OPEN.stream().noneMatch(cell::compareCoordinates)) {
                cell.changeType(CellType.OPEN);
                OPEN.add(cell);
            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }

    }

    public static void addToClosed(Cell cell) {
        try {
            if (CLOSED.stream().noneMatch(cell::compareCoordinates)) {
                cell.changeType(CellType.CLOSED);
                CLOSED.add(cell);
                OPEN.removeIf(cell::compareCoordinates);
            }
        } catch (CellMutationNotAllowed e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addCellToTheGrid(Cell cell) {

        int x = cell.getX();
        int y = cell.getY();

        if (x < 0
            || y < 0
            || x >= Constants.GRID_WIDTH
            || y >= Constants.GRID_HEIGHT
        ) {
            throw new RuntimeException("The coordinates of the cell are out of range");
        }

        // add the cell to the grid
        Data.grid[y][x] = cell;

        // add the cell into javafx nodes structure
        UI.gridPane.add(cell.getFxNode(), x, y);
    }

    public static Cell getCellFromTheGrid(int x, int y) {

        if (x < 0) x = 0;
        if (y < 0) y = 0;

        if (x >= Constants.GRID_WIDTH) x = Constants.GRID_WIDTH - 1;
        if (y >= Constants.GRID_HEIGHT) y = Constants.GRID_HEIGHT - 1;

        return grid[y][x];
    }

}
