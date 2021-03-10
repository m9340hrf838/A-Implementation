package impl.algo.astar.dto;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Supplier;

public class PathThread extends Thread {

    private final ConcurrentLinkedDeque<FinalPath> pathsByEndPoint;
    private final Supplier<FinalPath> pathSupplier;

    public PathThread(ConcurrentLinkedDeque<FinalPath> pathsByEndPoint, Supplier<FinalPath> pathSupplier) {
        this.pathsByEndPoint = pathsByEndPoint;
        this.pathSupplier = pathSupplier;

        this.setDaemon(true);
    }

    @Override
    public void run() {
        pathsByEndPoint.add(pathSupplier.get());
    }
}
