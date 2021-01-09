package impl.algo.astar.data;

import impl.algo.astar.dto.Cell;

import java.util.function.Consumer;

public interface CellActions {
    Consumer<Cell> EMPTY_ACTION = (Cell cell) -> { };
    Consumer<Cell> START_ACTION = Data::addToStart;
    Consumer<Cell> END_ACTION = Data::addToEnd;
    Consumer<Cell> ADD_BLOCK_ACTION = Data::addToBlock;
    Consumer<Cell> ADD_OBSTACLE_ACTION = Data::addToObstacle;
    Consumer<Cell> ADD_DEAD_BORDER_ACTION = Data::addToDeadBorder;
}
