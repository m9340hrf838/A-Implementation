package impl.algo.astar.data;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Constants implements Colors, Numbers, CellActions {

    public static Executor multipleThreads = Executors.newCachedThreadPool();
    public static volatile float rangeRatio = 0.0f;

}
