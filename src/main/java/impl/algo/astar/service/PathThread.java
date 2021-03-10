package impl.algo.astar.service;

import impl.algo.astar.dto.FinalPath;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class PathThread extends Thread {

    private final ConcurrentLinkedDeque<FinalPath> pathsByEndPoint;
    private final Supplier<FinalPath> pathCalculator;

    public PathThread(ConcurrentLinkedDeque<FinalPath> pathsByEndPoint, Supplier<FinalPath> pathCalculator) {
        this.pathsByEndPoint = pathsByEndPoint;
        this.pathCalculator = pathCalculator;
    }

    @Override
    public void run() {
        pathsByEndPoint.add(pathCalculator.get());
    }
}
