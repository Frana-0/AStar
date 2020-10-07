package mypackage;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class AStarSolver {
    private static PriorityQueue<AStarCell> pQueue;

    /**
     * Solve pathfinding problem given a map of cells with a start and stop position.
     *
     * @param cellMap Matrix of cells
     * @param start   Start position
     * @param finish  Stop position
     */
    public static void solve(AStarCell[][] cellMap, AStarCell start, AStarCell finish) {
        // Create priority queue with comparator comparing costs
        pQueue = new PriorityQueue<>(
                Comparator.comparingDouble(AStarCell::getTotalCost));
        pQueue.add(start);

        AStarCell path;
        do {
            // Check if current thread has been interrupted from main
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            // If our queue is empty we have run out of viable paths to follow. This algorithm was not able to find a solution.
            if (pQueue.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No solution found!", "Error", JOptionPane.ERROR_MESSAGE, UIManager.getIcon("OptionPane.errorIcon"));
                return;
            }
            // Check all surrounding cells of the highest priority entry in the queue
            path = selectNeighbours(cellMap, pQueue.poll(), finish);
        } while (path == null);

        // Path found, trace path using parent references from finish to start.
        path = path.getParent();
        while (path.getParent() != null) {
            path.setState(AStarCell.CellState.PATH);
            Main.setLabelColor(path, Color.yellow);
            path = path.getParent();
        }
    }

    /**
     * Check all cells surrounding given cell in the matrix.
     * Returns neighbouring cell if at the position of the finishing cell.
     *
     * @param cellMap  Matrix of cells representing map
     * @param position Position where we are looking around
     * @param finish   Finishing position
     * @return Cell if finishing cell is found
     */
    private static AStarCell selectNeighbours(AStarCell[][] cellMap, AStarCell position, AStarCell finish) {
        for (int i = 0; i < 8; i++) {  // all ordinal directions.
            OrdinalDirection dir = OrdinalDirection.getDirection(i);
            Point p = position.positionFromDirection(dir);
            if (isPositionTraversable(cellMap, p)) {
                AStarCell newPosition = cellMap[p.y][p.x];
                if (newPosition.equals(finish)) {
                    newPosition.setParent(position);
                    return newPosition;
                }
                boolean gCostUpdated = newPosition.updateGCost(position);
                if (gCostUpdated) {
                    newPosition.updateHCost(finish);
                    pQueue.add(newPosition);
                    newPosition.setState(AStarCell.CellState.SEEN);
                    Main.setLabelColor(newPosition, Color.red);
                }
            }
        }
        return null;
    }

    /**
     * Check if given point on matrix map is a traversable cell
     *
     * @param map Matrix of cells representing map
     * @param p   Point to check at in matrix
     * @return True if position is traversable (not start or blocked)
     */
    private static boolean isPositionTraversable(AStarCell[][] map, Point p) {
        int rows = map.length;
        int columns = map[0].length;
        int x = p.x;
        int y = p.y;

        if ((x >= columns || x < 0) || (y >= rows || y < 0)) { // Check out of bounds
            return false;
        }
        return map[y][x].isTraversable();
    }

    enum OrdinalDirection {  // Enum to assign index to direction
        NORTH, NORTHWEST,
        WEST, SOUTHWEST,
        SOUTH, SOUTHEAST,
        EAST, NORTHEAST;

        private static final OrdinalDirection[] list = OrdinalDirection.values();

        public static OrdinalDirection getDirection(int i) {
            return list[i];
        }
    }
}
