package mypackage;

import mypackage.AStarSolver.OrdinalDirection;

import java.awt.*;

public class AStarCell {
    private int x;
    private int y;
    private double hCost = Double.POSITIVE_INFINITY;
    private double gCost = Double.POSITIVE_INFINITY;
    private AStarCell parent = null;
    private CellState state = CellState.OPEN;

    public AStarCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Calculate total cost of cell
     *
     * @return H cost plus G cost
     */
    public double getTotalCost() {
        return hCost + gCost;
    }

    /**
     * Update G cost based on neighbouring cell's G cost.
     * Set parent of current cell to neighbouring cell if new G cost is lower than the previous.
     *
     * @param v Neighbouring cell
     * @return Boolean stating whether or not the parent of the current cell was switched.
     */
    public boolean updateGCost(AStarCell v) {
        double addCost = this.x != v.x && this.y != v.y ? 14 : 10;
        double newCost = v.gCost + addCost;
        if (newCost < this.gCost) {
            this.gCost = newCost;
            this.parent = v;
            return true;
        }
        return false;
    }

    /**
     * Calculate H cost based on finishing cell
     *
     * @param v Finishing cell
     */
    public void updateHCost(AStarCell v) {
        this.hCost = this.distanceHeuristic(v) * 10.0;
    }

    /**
     * Check if this cell is traversable by the algorithm.
     * Blocked and start states are not traversable.
     *
     * @return boolean if cell is traversable
     */
    public boolean isTraversable() {
        return state == CellState.OPEN || state == CellState.SEEN || state == CellState.STOP;
    }

    /**
     * Simple distance heuristic to simplify distance calculation between 2 cells.
     *
     * @param v Destination cell
     * @return Heuristic distance
     */
    public double distanceHeuristic(AStarCell v) {
        double vx = (double) v.x - this.x;
        double vy = (double) v.y - this.y;
        return Math.abs(vx) + Math.abs(vy);
    }

    /**
     * Get neighbouring cell in the direction of the given enum value.
     *
     * @param direction Enum value to get position of new point
     * @return New point in the direction from the current cell.
     */
    public Point positionFromDirection(OrdinalDirection direction) {
        int newX = this.x;
        int newY = this.y;
        if (direction == OrdinalDirection.NORTH || direction == OrdinalDirection.NORTHEAST || direction == OrdinalDirection.NORTHWEST) {
            newY -= 1;
        }
        if (direction == OrdinalDirection.SOUTH || direction == OrdinalDirection.SOUTHWEST || direction == OrdinalDirection.SOUTHEAST) {
            newY += 1;
        }
        if (direction == OrdinalDirection.WEST || direction == OrdinalDirection.NORTHWEST || direction == OrdinalDirection.SOUTHWEST) {
            newX += 1;
        }
        if (direction == OrdinalDirection.EAST || direction == OrdinalDirection.NORTHEAST || direction == OrdinalDirection.SOUTHEAST) {
            newX -= 1;
        }
        return new Point(newX, newY);
    }

    /*
    GETTERS & SETTERS
     */

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public AStarCell getParent() {
        return parent;
    }

    public void setParent(AStarCell parent) {
        this.parent = parent;
    }

    public double getgCost() {
        return gCost;
    }

    public void setgCost(double gCost) {
        this.gCost = gCost;
    }

    public double gethCost() {
        return hCost;
    }

    public void sethCost(double hCost) {
        this.hCost = hCost;
    }

    public CellState getState() {
        return state;
    }

    public void setState(CellState state) {
        this.state = state;
    }

    public boolean equals(AStarCell v) {
        return this.x == v.x && this.y == v.y;
    }

    /*
    UTILITY METHODS
     */

    @Override
    public String toString() {
        return "Vector2DPath{" +
                "x=" + x +
                ", y=" + y +
                ", gCost=" + gCost +
                ", hCost=" + hCost +
                '}';
    }

    enum CellState {
        OPEN, BLOCK, PATH,
        SEEN, START, STOP
    }
}
