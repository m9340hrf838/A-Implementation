package impl.algo.astar.Exceptions;

public class CellMutationNotAllowed extends Exception {
    public CellMutationNotAllowed() {
        super();
    }

    public CellMutationNotAllowed(String message) {
        super(message);
    }

    public CellMutationNotAllowed(String message, Throwable cause) {
        super(message, cause);
    }

    public CellMutationNotAllowed(Throwable cause) {
        super(cause);
    }
}
