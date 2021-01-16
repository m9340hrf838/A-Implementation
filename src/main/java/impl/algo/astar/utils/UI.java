package impl.algo.astar.utils;

import impl.algo.astar.Exceptions.CellMutationNotAllowed;
import impl.algo.astar.data.Constants;
import impl.algo.astar.data.Data;
import impl.algo.astar.dto.Cell;
import impl.algo.astar.service.AStarAlgorithm;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.function.Consumer;

public final class UI {

    private static final Rectangle2D SCREEN_BOUNDS = Screen.getPrimary().getVisualBounds();
    private static final int CELL_WIDTH = Math.min((int) (SCREEN_BOUNDS.getWidth() * 0.9) / Constants.GRID_WIDTH, (int) (SCREEN_BOUNDS.getHeight() * 0.9) / Constants.GRID_HEIGHT);
    public static Stage stage;
    public static Scene scene;
    public volatile static GridPane gridPane;
    public volatile static Cursor cursorType = Cursor.DEFAULT;

    private static Consumer<Cell> cellAction;

    public static Background buildBackground(Cell.CellType cellType) {
        return buildBackground((cellType.equals(Cell.CellType.OBSTACLE) || cellType.equals(Cell.CellType.DEAD_BORDER)) ? Constants.CELL_COLOR : cellType.getColor());
    }

    public static Background buildBackground(String color) {
        return new Background(new BackgroundFill(Paint.valueOf(color), CornerRadii.EMPTY, Insets.EMPTY));
    }

    public static Border buildBorder(String color) {
        return new Border(
            new BorderStroke(
                Paint.valueOf(color),
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0.1, 0.1, 0.1, 0.1, false, false, false, false)));
    }

    public static Border buildBorder(String color, double top, double bottom, double left, double right) {
        return new Border(
            new BorderStroke(
                Paint.valueOf(color),
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(top, right, bottom, left, false, false, false, false)));
    }

    //==================================================================================================================

    public static void updateGridBorders() {

        Data.GROUPED_OBSTACLES
            .forEach(group ->
                group.forEach(cell -> updateCellBorder(Data.getCellFromTheGrid(cell.getX(), cell.getY()))));
        Data.DEAD_BORDER
            .forEach(group ->
                group.forEach(cell -> updateCellBorder(Data.getCellFromTheGrid(cell.getX(), cell.getY()))));
    }

    public static void updateCellBorder(Cell cell) {
        updateCellBorderImpl(cell);
    }

    private static void updateCellBorderImpl(Cell cell) {
        Platform.runLater(() -> {
            if (cell.isDeadBorder() || cell.isObstacle()) {

                double borderthickness = Constants.FENCE_THICKNESS;
                if (cell.getCellType().equals(Cell.CellType.OBSTACLE)) borderthickness *= 2;

                // top
                Cell topNeighbourCell = Data.getCellFromTheGrid(
                    (cell.getX()),
                    (Math.max((cell.getY() - 1), 0)));
                double topBorder = (cell.shouldDrawFence(topNeighbourCell)) ? borderthickness : 0.1;

                // bottom
                Cell bottomNeighbourCell = Data.getCellFromTheGrid(
                    (cell.getX()),
                    Math.min((cell.getY() + 1), (Constants.GRID_HEIGHT - 1))
                );
                double bottomBorder = (cell.shouldDrawFence(bottomNeighbourCell)) ? borderthickness : 0.1;

                // left
                Cell leftNeighbourCell = Data.getCellFromTheGrid(
                    Math.max((cell.getX() - 1), 0),
                    cell.getY());
                double leftBorder = (cell.shouldDrawFence(leftNeighbourCell)) ? borderthickness : 0.1;

                // right
                Cell rightNeighbourCell = Data.getCellFromTheGrid(
                    Math.min((cell.getX() + 1), (Constants.GRID_WIDTH - 1)),
                    cell.getY());
                double rightBorder = (cell.shouldDrawFence(rightNeighbourCell)) ? borderthickness : 0.1;


                cell.getFxNode()
                    .setBorder(
                        UI.buildBorder(
                            cell.getBorderColor(),
                            topBorder, bottomBorder, leftBorder, rightBorder));

                if (cell.isDeadBorder() || cell.isObstacle()) {
                    if (cell.getCellType().equals(Cell.CellType.OBSTACLE)) {
                        cell.getFxNode().setBackground(UI.buildBackground(Constants.OBSTACLE_CELL_BACKGROUND_COLOR));
                    } else {
                        cell.getFxNode().setBackground(UI.buildBackground(Constants.DEAD_BORDER_CELL_BACKGROUND_COLOR));
                    }
                }

            } else {
                double borderThickness = 0.1;
                cell.getFxNode()
                    .setBorder(
                        UI.buildBorder(
                            cell.getBorderColor(),
                            borderThickness, borderThickness, borderThickness, borderThickness));
            }
        });
    }

    //==================================================================================================================

    public static void initialize(Stage stage) {
        UI.stage = stage;
        UI.scene = new Scene(setUpLayout());
        scene.setCursor(UI.cursorType);
        stage.setTitle("A* algorithm");
        stage.setScene(scene);
        stage.show();
    }

    private static BorderPane setUpLayout() {

        final BorderPane layoutBorderPane = new BorderPane();

        layoutBorderPane.setTop(getMenuButtons());
        layoutBorderPane.setCenter(getGridPane());

        for (int x = 0; x < Constants.GRID_WIDTH; x++) {
            for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
                addAnEmptyCellToTheGrid(x, y);
            }
        }

        return layoutBorderPane;
    }

    private static HBox getMenuButtons() {
        final Button startButton = new Button("add start points");
        startButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            cellAction = Constants.START_ACTION;
            UI.scene.setCursor(Cursor.HAND);

        });
        startButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.START_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button endButton = new Button("add end points");
        endButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            cellAction = Constants.END_ACTION;
            UI.scene.setCursor(Cursor.HAND);
        });
        endButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.END_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button addBlockButton = new Button("add blocks");
        addBlockButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            cellAction = Constants.ADD_BLOCK_ACTION;
            UI.scene.setCursor(Cursor.HAND);
        });
        addBlockButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.BLOCK_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button addObstacleButton = new Button("add obstacle");
        addObstacleButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            cellAction = Constants.ADD_OBSTACLE_ACTION;
            UI.scene.setCursor(Cursor.HAND);
        });
        addObstacleButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.OBSTACLE_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button addDeadBorderButton = new Button("add dead border");
        addDeadBorderButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            cellAction = Constants.ADD_DEAD_BORDER_ACTION;
            UI.scene.setCursor(Cursor.HAND);
        });
        addDeadBorderButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.DEAD_BORDER_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button runButton = new Button("run");
        runButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            cellAction = Constants.EMPTY_ACTION;
            UI.scene.setCursor(Cursor.DEFAULT);
            AStarAlgorithm.execute();
        });

        final Button clearBoardButton = new Button("clear board");
        clearBoardButton.setOnMouseClicked(mouseEvent -> {
            UI.clearBoard();
            UI.scene.setCursor(Cursor.DEFAULT);
        });

        final Button clearStartPointsButton = new Button("clear start points");
        clearStartPointsButton.setOnMouseClicked(mouseEvent -> {
            UI.clearStartPoints();
            UI.scene.setCursor(Cursor.DEFAULT);
        });
        clearStartPointsButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.START_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button clearEndPointsButton = new Button("clear end points");
        clearEndPointsButton.setOnMouseClicked(mouseEvent -> {
            UI.clearEndPoints();
            UI.scene.setCursor(Cursor.DEFAULT);
        });
        clearEndPointsButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.END_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button clearBlocksButton = new Button("clear blocks");
        clearBlocksButton.setOnMouseClicked(mouseEvent -> {
            UI.clearBlocks();
            UI.scene.setCursor(Cursor.DEFAULT);
        });
        clearBlocksButton.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.BLOCK_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

        final Button clearCalculationsButton = new Button("clear calculations");
        clearCalculationsButton.setOnMouseClicked(mouseEvent -> {
            UI.clearCalculations();
            UI.scene.setCursor(Cursor.DEFAULT);
        });

        final HBox buttonsHBox = new HBox();
        buttonsHBox.setSpacing(10);
        buttonsHBox.setPadding(new Insets(10, 0, 0, 10));
        buttonsHBox.getChildren().addAll(
            runButton,
            clearBoardButton,
            clearCalculationsButton,
            startButton,
            clearStartPointsButton,
            endButton,
            clearEndPointsButton,
            addBlockButton,
            addObstacleButton,
            addDeadBorderButton,
            clearBlocksButton
        );

        return buttonsHBox;
    }

    private static GridPane getGridPane() {
        final GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setAlignment(Pos.CENTER);
        UI.gridPane = gridPane;
        return gridPane;
    }

    private static void addAnEmptyCellToTheGrid(int x, int y) {

        Cell cell = new Cell(x, y, getEmptyBorderPane());
        Data.addCellToTheGrid(cell);

        cell.getFxNode().setOnMousePressed(mouseEvent -> UI.activateCell(cell));

        cell.getFxNode().setOnMouseEntered(mouseEvent -> {
            if (mouseEvent.isControlDown()) {
                UI.activateCell(cell);
            }
        });
    }

    /**
     * To be called when a cell is clicked
     *
     * @param cell the cell which was clicked
     */
    public static void activateCell(Cell cell) {
        if (cellAction != null) cellAction.accept(cell);
    }

    private static BorderPane getEmptyBorderPane() {
        BorderPane cellBorderPane = new BorderPane();
        cellBorderPane.setBackground(new Background(new BackgroundFill(Paint.valueOf(Constants.CELL_COLOR), CornerRadii.EMPTY, Insets.EMPTY)));
        cellBorderPane.setMinWidth(Math.min(Constants.CELL_WIDTH, CELL_WIDTH));
        cellBorderPane.setMinHeight(Math.min(Constants.CELL_WIDTH, CELL_WIDTH));
        cellBorderPane.setBorder(new Border(new BorderStroke(Paint.valueOf(Constants.BORDER_COLOR), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0.1, 0.1, 0.1, 0.1, false, false, false, false))));
        return cellBorderPane;
    }

    //==================================================================================================================

    private static void clearBlocks() {
        Constants.multipleThreads.execute(() -> {
            Data.BLOCKS.forEach(cell -> {
                try {
                    cell.changeType(Cell.CellType.EMPTY);

                    if (cell.isObstacle()) cell.setObstacle(false);
                    if (cell.isDeadBorder()) cell.setDeadBorder(false);

                } catch (CellMutationNotAllowed e) {
                    System.out.println(e.getMessage());
                }
            });
            Data.BLOCKS.clear();

            Data.OBSTACLES.stream().forEach(cell -> {
                try {
                    if (cell.getCellType().equals(Cell.CellType.OBSTACLE)) cell.changeType(Cell.CellType.EMPTY);

                    if (cell.isObstacle()) cell.setObstacle(false);
                    if (cell.isDeadBorder()) cell.setDeadBorder(false);

                    cell.getFxNode().setBorder(buildBorder(Constants.BORDER_COLOR));
                } catch (CellMutationNotAllowed e) {
                    System.out.println(e.getMessage());
                }
            });
            Data.OBSTACLES.clear();
            Data.GROUPED_OBSTACLES.clear();

            Data.DEAD_BORDER.stream().flatMap(Collection::stream).forEach(cell -> {
                try {
                    if (cell.getCellType().equals(Cell.CellType.DEAD_BORDER)) cell.changeType(Cell.CellType.EMPTY);

                    if (cell.isObstacle()) cell.setObstacle(false);
                    if (cell.isDeadBorder()) cell.setDeadBorder(false);

                    cell.getFxNode().setBorder(buildBorder(Constants.BORDER_COLOR));
                } catch (CellMutationNotAllowed e) {
                    System.out.println(e.getMessage());
                }
            });
            Data.DEAD_BORDER.clear();

            clearCalculations();
            updateGridBorders();
        });
    }

    private static void clearStartPoints() {
        Data.START_POINTS.forEach(cell -> {
            try {
                cell.changeType(Cell.CellType.EMPTY);
            } catch (CellMutationNotAllowed e) {
                System.out.println(e.getMessage());
            }
        });
        Data.START_POINTS.clear();

        clearCalculations();
    }

    private static void clearEndPoints() {
        Data.END_POINTS.forEach(cell -> {
            try {
                cell.changeType(Cell.CellType.EMPTY);
            } catch (CellMutationNotAllowed e) {
                System.out.println(e.getMessage());
            }
        });
        Data.END_POINTS.clear();

        clearCalculations();
    }

    private static void clearBoard() {
        Data.OPEN.clear();
        Data.CLOSED.clear();
        Data.START_POINTS.clear();
        Data.END_POINTS.clear();
        Data.BLOCKS.clear();
        Data.OBSTACLES.clear();
        Data.GROUPED_OBSTACLES.clear();
        Data.DEAD_BORDER.clear();
        Data.FINAL_PATHS.clear();
        Data.FINAL_PATH_CELLS.clear();
        initialize(stage);
    }

    private static void clearCalculations() {
        Data.FINAL_PATH_CELLS.forEach(cell -> {
            try {
                cell.changeType(Cell.CellType.EMPTY);
            } catch (CellMutationNotAllowed e) {
                System.out.println(e.getMessage());
            }
        });
        Data.FINAL_PATH_CELLS.clear();

        Data.FINAL_PATHS.clear();

        Data.OPEN.forEach(cell -> {
            try {
                cell.changeType(Cell.CellType.EMPTY);
            } catch (CellMutationNotAllowed e) {
                System.out.println(e.getMessage());
            }
        });
        Data.OPEN.clear();

        Data.CLOSED.forEach(cell -> {
            try {
                cell.changeType(Cell.CellType.EMPTY);
            } catch (CellMutationNotAllowed e) {
                System.out.println(e.getMessage());
            }
        });
        Data.CLOSED.clear();
        Constants.rangeRatio = 0.0f;
    }

    //==================================================================================================================

    public static synchronized String updatePathColor() {

        String lowerBoundRed = Constants.LOWER_BOUND_PATH_COLOR.substring(1, 3);
        String lowerBoundGreen = Constants.LOWER_BOUND_PATH_COLOR.substring(3, 5);
        String lowerBoundBlue = Constants.LOWER_BOUND_PATH_COLOR.substring(5);

        String upperBoundRed = Constants.UPPER_BOUND_PATH_COLOR.substring(1, 3);
        String upperBoundGreen = Constants.UPPER_BOUND_PATH_COLOR.substring(3, 5);
        String upperBoundBlue = Constants.UPPER_BOUND_PATH_COLOR.substring(5);

        String currentRed = calculateHexInRange(lowerBoundRed, upperBoundRed);
        String currentGreen = calculateHexInRange(lowerBoundGreen, upperBoundGreen);
        String currentBlue = calculateHexInRange(lowerBoundBlue, upperBoundBlue);

        String result = String.format("#%s%s%s", currentRed, currentGreen, currentBlue);

        Constants.rangeRatio += (1.0 / Data.END_POINTS.size());

        return result;
    }

    private static String calculateHexInRange(String lowerBound, String upperBound) {
        Integer lower = Integer.valueOf(lowerBound, 16);
        Integer upper = Integer.valueOf(upperBound, 16);
        int resultInt = (int) (lower + ((upper - lower) * Constants.rangeRatio));
        String resultString = Integer.toHexString(resultInt);
        return resultString.length() == 1 ? "0" + resultString : resultString;
    }
}
