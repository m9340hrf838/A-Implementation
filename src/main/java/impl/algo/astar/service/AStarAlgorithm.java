package impl.algo.astar.service;

import impl.algo.astar.data.Constants;
import impl.algo.astar.data.Data;
import impl.algo.astar.dto.Cell;
import impl.algo.astar.utils.UI;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AStarAlgorithm {

    private static final ExecutorService multipleThreads = Executors.newCachedThreadPool();
    private static final ExecutorService singleThread = Executors.newSingleThreadExecutor();

    public static void execute() {
        singleThread.submit(AStarAlgorithm::start);
    }

    private static void start() {
        // go through the end points
        for (Cell end : Data.END_POINTS) {
            multipleThreads.submit(() -> {

                // calculate a color for all the paths that lead to this endpoint
                String endPointSpecificPathColor = UI.updatePathColor();

                // create storage for all teh paths to this endpoint
                ConcurrentLinkedDeque<Data.FinalPath> pathsByEndPoint = new ConcurrentLinkedDeque<>();

                // go through all start points in parallel
                Data.START_POINTS.parallelStream().forEach(start -> {

                    // start a callable thread which will return a value (that is done only for the loop to wait for all paths to be calculated before changing endpoint)
                    try {
                        multipleThreads.submit(new PathThread(start, end, endPointSpecificPathColor, pathsByEndPoint)).get();
                    } catch (InterruptedException | ExecutionException e) {
                        System.out.println("\nAStarAlgorithm.java -> execute() -> multipleThreads.submit(new PathThread(start, end, endPointSpecificPathColor, pathsByEndPoint)).get()\n");
                        e.printStackTrace();
                    }
                });

                // update Data with the newly calculated path
                Data.FINAL_PATHS.add(pathsByEndPoint);

            });
        }
    }

    private static Data.FinalPath calculateOnePath(final Cell start, final Cell end, final String pathColor) {

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

        return new Data.FinalPath(pathColor, finalPath);
    }

    private static ConcurrentLinkedDeque<Cell> runTheAlgorithmLoop(String pathColor, TreeSet<Cell> open, List<Cell> closed, final Cell start, final Cell end) {

        ConcurrentLinkedDeque<Cell> path = new ConcurrentLinkedDeque<>();

        while (true) {
            // get the node with the smallest F
            Cell nextCell = open.pollFirst();
            System.out.println("polled OPEN x:" + nextCell.getX() + " y:" + nextCell.getY() + "\tf:" + nextCell.calculateF(start, end) + " g:" + nextCell.getG(start, end));
            System.out.println("size of OPEN:" + open.size());

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
                System.out.println("new OPEN x:" + neighbour.getX() + " y:" + neighbour.getY() + "\tf:" + neighbour.calculateF(start, end) + " g:" + neighbour.getG(start, end));
                System.out.println("size of OPEN:" + open.size());
                neighbour.setParent(start, end, nextCell);
                open.add(neighbour);
                Data.addToOpen(neighbour);
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

    private static class PathThread implements Callable<Object> {

        private Cell start;
        private Cell end;
        private String pathColor;
        private ConcurrentLinkedDeque<Data.FinalPath> pathsByEndPoint;

        public PathThread(Cell start, Cell end, String pathColor, ConcurrentLinkedDeque<Data.FinalPath> pathsByEndPoint) {
            this.start = start;
            this.end = end;
            this.pathColor = pathColor;
            this.pathsByEndPoint = pathsByEndPoint;
        }

        @Override
        public Object call() {
            pathsByEndPoint.add(calculateOnePath(start, end, pathColor));
            return null;
        }
    }
}
