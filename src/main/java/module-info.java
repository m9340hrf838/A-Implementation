module impl.algo.astar {
    requires javafx.controls;
    requires javafx.fxml;

    opens impl.algo.astar to javafx.fxml;
    exports impl.algo.astar;
}