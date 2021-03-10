package impl.algo.astar.service;

import impl.algo.astar.data.Constants;
import impl.algo.astar.data.Data;
import impl.algo.astar.dto.Cell;
import impl.algo.astar.dto.FinalPath;
import impl.algo.astar.dto.PathThread;
import impl.algo.astar.utils.UI;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedDeque;

public class AStarAlgorithm {

    public static void execute() {
        Thread executionThread = new Thread(AStarAlgorithm::start);
        executionThread.setDaemon(true);
        executionThread.setName("Execution Thread");
        executionThread.start();
    }

    private static void start() {

        // go through the end points
        for (Cell end : Data.END_POINTS) {
            Thread pathGroupdThread = new Thread(() -> {

                // calculate a color for all the paths that lead to this endpoint
                String endPointSpecificPathColor = UI.updatePathColor();

                // create storage for all teh paths to this endpoint
                ConcurrentLinkedDeque<FinalPath> pathsByEndPoint = new ConcurrentLinkedDeque<>();

                // go through all start points in parallel
                for (Cell start : Data.START_POINTS) {

                    // start a new thread for each path
                    Thread pathThread = new PathThread(pathsByEndPoint, () -> calculateOnePath(start, end, endPointSpecificPathColor));
                    pathThread.setName(String.format("Path for start point (%d, %d) and end point (%d, %d)", start.getX(), start.getY(), end.getX(), end.getY()));
                    pathThread.start();
                }

                // update Data with the newly calculated path
                Data.FINAL_PATHS.add(pathsByEndPoint);

            });
            pathGroupdThread.setDaemon(true);
            pathGroupdThread.setName(String.format("Path for end point (%d, %d)", end.getX(), end.getY()));
            pathGroupdThread.start();

        }
    }

    private static FinalPath calculateOnePath(final Cell start, final Cell end, final String pathColor) {

        Comparator<Cell> cellComparator = (Cell o1, Cell o2) -> {
            if (o1.compareCoordinates(o2)) {
                return 0;
            }
            if (o1.calculateF(start, end) > o2.calculateF(start, end)) {
                return 1;
            } else {
                return -1;
            }
        };

        // initialize open and closed
        TreeSet<Cell> open = new TreeSet<>(cellComparator);
        List<Cell> closed = new LinkedList<>();

        start.setG(start, end, 0);
        open.add(start);

        // execute the algorithm implementation
        ConcurrentLinkedDeque<Cell> finalPath = runTheAlgorithmLoop(pathColor, open, closed, start, end);

        // update the Data with the new OPEN and CLOSED cells
        open.forEach(Data::addToOpen);
        closed.forEach(Data::addToClosed);

        return new FinalPath(pathColor, finalPath);
    }

    private static ConcurrentLinkedDeque<Cell> runTheAlgorithmLoop(String pathColor, TreeSet<Cell> open, List<Cell> closed, final Cell start, final Cell end) {

        ConcurrentLinkedDeque<Cell> path = new ConcurrentLinkedDeque<>();

        while (true) {
            // get the node with the smallest F
            Cell nextCell = open.pollFirst();

            // if there are no more cells in the open then the calculation has come to an end without reaching the end point
            if (nextCell == null) break;

            // get the neighbours
            List<Cell> neighbours = nextCell.getNeighbours(start, end, open, closed);

            // check if the destination was reached
            if (neighbours.stream().anyMatch(end::compareCoordinates)) {

                // build the path the way back
                while (nextCell.getParent(start, end) != null) {

                    // add cell to the latest path
                    path.add(nextCell.markAsPathCell(pathColor));

                    // move one cell back
                    nextCell = nextCell.getParent(start, end);
                }

                // stop the calculation because the end point was reached
                break;
            }

            // add neighbours to open
            for (Cell neighbour : neighbours) {
                neighbour.setParent(start, end, nextCell);

                if (open.stream().noneMatch(cell -> neighbour.compareCoordinates(cell) && cell.calculateF(start, end) <= neighbour.calculateF(start, end))) {
                    open.add(neighbour);
                    Data.addToOpen(neighbour);
                }
            }

            // move node to closed
            closed.add(nextCell);
            Data.addToClosed(nextCell);

            try {
                Thread.sleep(Constants.PAUSE_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return path;
    }

}
