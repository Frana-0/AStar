# A* Pathfinding in Java
Small personal A* pathfinding algorithm written in Java 11.
Currently map size is fixed for better UX.

Start algoritm by clicking on cells in grid to select start (blue) and stop (green) positions.
Once the positions are both selected, they are fixed.
The user can then add decide to obstacles on the grid to complicate the path.
Obstacles are created by clicking on open cells or dragging the mouse over the grid.
Obstacles can also be removed by clicking on the cell.

If the used is satisfied with the layout, clicking the *solve* button.
The algorithm will then attempt to find the optimal path.
If found, the optimal path will be shown in yellow and all visited cells in red.
