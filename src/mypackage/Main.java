package mypackage;

import mypackage.AStarCell.CellState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

public class Main {
    // Defaults
    private static final Color PANEL_COLOR = new JPanel().getBackground();
    private static final int ROWS = 50, COLS = 50;
    private static final int WIDTH = 800, HEIGHT = 800;
    private static final int INSTRUCT_HEIGHT = 40;
    private static final String START_INSTRUCT = "Instructions: Select start (blue) and stop (green) positions by clicking on the desired blocks.";
    // Matrices
    private static JLabel[][] labelMap;
    private static AStarCell[][] cellMap;
    // GUI Components
    private static Thread solveThread;
    private static JPanel displayPanel;
    private static JButton solveButton;
    private static JLabel instructLabel;
    // start & stop points
    private static AStarCell start = null;
    private static AStarCell stop = null;

    public static void main(String[] args) {
        initGUI();
        roundSetup();
    }

    /**
     * Setup components and arrays for new round.
     */
    private static void roundSetup() {
        addComponentListeners();

        cellMap = new AStarCell[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                cellMap[i][j] = new AStarCell(j, i);
            }
        }
    }

    /**
     * Find path on map using a thread for AStar
     */
    private static void findPath() {
        // Set costs of start and stop points
        start.updateHCost(stop);
        start.setgCost(0.0);
        stop.sethCost(0.0);
        stop.updateGCost(start);
        // Create and run daemon thread to find path
        solveThread = new Thread(() -> AStarSolver.solve(cellMap, start, stop));
        solveThread.setDaemon(true);
        solveThread.start();
    }

    /**
     * Find indices of given JLabel object on label matrix.
     *
     * @param label Label object to look for.
     * @return Point representing position of label in matrix.
     */
    private static Point findLabelIndices(JLabel label) {
        for (int i = 0; i < labelMap.length; i++) {
            for (int j = 0; j < labelMap[0].length; j++) {
                if (label == labelMap[i][j]) {
                    return new Point(j, i);
                }
            }
        }
        return null;
    }

    /**
     * Change state of cell at point in matrix.
     *
     * @param p         Position of cell.
     * @param cellState New state of cell.
     */
    private static void setCellState(Point p, CellState cellState) {
        cellMap[p.y][p.x].setState(cellState);
    }

    /**
     * Set background color of label in label matrix at a given position to a given color.
     *
     * @param cell  Cell to change state of.
     * @param color Color to change background to.
     */
    public static void setLabelColor(AStarCell cell, Color color) {
        labelMap[cell.getY()][cell.getX()].setBackground(color);
    }

    // GUI related functions

    /**
     * Initialize GUI all components.
     */
    private static void initGUI() {
        // Create panel to contain instructions panel and matrix display table
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Panel to show instructions and options
        JPanel instructPanel = new JPanel();
        instructPanel.setLayout(new BoxLayout(instructPanel, BoxLayout.X_AXIS));

        // Create components we interact with
        solveButton = new JButton("Solve!");
        instructLabel = new JLabel(START_INSTRUCT);
        solveButton.setEnabled(false);

        // Add & space components
        instructPanel.add(Box.createHorizontalGlue());
        instructPanel.add(instructLabel);
        instructPanel.add(Box.createHorizontalGlue());
        instructPanel.add(solveButton);
        instructPanel.add(Box.createHorizontalGlue());
        instructPanel.setMinimumSize(new Dimension(WIDTH, INSTRUCT_HEIGHT));
        instructPanel.setPreferredSize(new Dimension(WIDTH, INSTRUCT_HEIGHT));
        instructPanel.setSize(WIDTH, INSTRUCT_HEIGHT);

        // Create matrix of labels representing the map
        labelMap = new JLabel[ROWS][COLS];
        displayPanel = new JPanel(new GridLayout(ROWS, COLS, -1, -1));
        displayPanel.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        displayPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JFrame frame = new JFrame();

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                JLabel label = new JLabel();
                label.setOpaque(true);
                label.setBorder(BorderFactory.createLineBorder(Color.black, 1));
                labelMap[i][j] = label;
                displayPanel.add(label);
            }
        }

        // Add final components and set sizes
        contentPanel.add(instructPanel);
        contentPanel.add(displayPanel);
        frame.setContentPane(contentPanel);
        frame.setSize(WIDTH, HEIGHT + INSTRUCT_HEIGHT);
        frame.pack();
        // Calculate and account for space used by the window frame
        Insets insets = frame.getInsets();
        int addH = insets.top + insets.bottom;
        int addW = insets.right + insets.left;
        frame.setSize(WIDTH + addW, HEIGHT + INSTRUCT_HEIGHT + addH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Add relevant listeners to components in JFrame.
     * Listeners are removed once they are no longer necessary for a round.
     */
    private static void addComponentListeners() {
        // Mouse listener for setting or unsetting cells as obstacles with a single click
        MouseAdapter blockListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get selected component & ensure it is of type JLabel.
                Component component = displayPanel.getComponentAt(e.getPoint());
                if (!(component instanceof JLabel)) {
                    return;
                }
                JLabel selectedLabel = (JLabel) component;
                // Can't block start and stop positions.
                if (selectedLabel.getBackground() == Color.blue || selectedLabel.getBackground() == Color.green) {
                    return;
                }
                // Get position of selected label in matrix
                Point p = findLabelIndices(selectedLabel);
                if (p != null) {
                    // Either set or unset an obstacle cell based on the current state of the cell.
                    if (selectedLabel.getBackground() == Color.black) {
                        setCellState(p, CellState.OPEN);
                        selectedLabel.setBackground(PANEL_COLOR);
                    } else if (selectedLabel.getBackground() == PANEL_COLOR) {
                        setCellState(p, CellState.BLOCK);
                        selectedLabel.setBackground(Color.black);
                    }
                }
            }
        };

        // Mouse listener for tracking mouse drag to create obstacles
        MouseMotionAdapter movementListener = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Get selected component & ensure it is of type JLabel.
                Component component = displayPanel.getComponentAt(e.getPoint());
                if (!(component instanceof JLabel)) {
                    return;
                }
                JLabel selectedLabel = (JLabel) component;
                // Can't overwrite start, stop or other obstacle cells.
                if (selectedLabel.getBackground() == Color.black || selectedLabel.getBackground() == Color.blue || selectedLabel.getBackground() == Color.green) {
                    return;
                }
                // Get position of selected label in matrix
                Point p = findLabelIndices(selectedLabel);
                if (p != null) {
                    // Set cell to obstacle
                    setCellState(p, CellState.BLOCK);
                    selectedLabel.setBackground(Color.black);
                }
            }
        };

        // Mouse listener to check clicks at start of round for start and finish positions.
        MouseAdapter startStopListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Get selected component & ensure it is of type JLabel.
                Component component = displayPanel.getComponentAt(e.getPoint());
                if (!(component instanceof JLabel)) {
                    return;
                }
                JLabel selectedLabel = (JLabel) component;
                // Unset start or stop cells if selected cell is set to either.
                if (selectedLabel.getBackground() == Color.blue) {
                    start = null;
                    selectedLabel.setBackground(PANEL_COLOR);
                } else if (selectedLabel.getBackground() == Color.green) {
                    stop = null;
                    selectedLabel.setBackground(PANEL_COLOR);
                } else {
                    // Otherwise set selected cell to start or stop (respectively)
                    Point p = findLabelIndices(selectedLabel);
                    if (p != null) {
                        AStarCell cell = cellMap[p.y][p.x];
                        if (start == null) {
                            cell.setState(CellState.START);
                            start = cell;
                            selectedLabel.setBackground(Color.blue);
                        } else if (stop == null) {
                            cell.setState(CellState.STOP);
                            stop = cell;
                            selectedLabel.setBackground(Color.green);
                        }
                    }
                }

                if (start != null && stop != null) {
                    instructLabel.setText(fillString("Click & drag to place obstacles. If done, click 'solve.'"));
                    solveButton.setEnabled(true);
                    displayPanel.removeMouseListener(this);
                    displayPanel.addMouseListener(blockListener);
                    displayPanel.addMouseMotionListener(movementListener);
                }
            }
        };

        // Button action listener to start and reset a round.
        solveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // Text of button determines the action
                String butText = solveButton.getText();
                if (butText.equals("Reset")) {
                    // Interrupt solve thread if still running, otherwise start new round
                    if (solveThread.isAlive()) {
                        solveThread.interrupt();
                    }
                    // Remove this listener since we will be adding it again in the new round
                    solveButton.removeActionListener(this);
                    solveButton.setEnabled(false);
                    resetGUI();
                    roundSetup();
                } else {
                    // Remove listeners to set obstacle cells
                    displayPanel.removeMouseListener(blockListener);
                    displayPanel.removeMouseMotionListener(movementListener);
                    // Find path
                    findPath();
                    instructLabel.setText(fillString("Click reset to restart."));
                    solveButton.setText("Reset");
                }
            }
        });

        // Finally add listener to check for selected start and stop positions.
        displayPanel.addMouseListener(startStopListener);
    }

    /**
     * Reset components in GUI to initial states.
     */
    private static void resetGUI() {
        instructLabel.setText(START_INSTRUCT);
        solveButton.setText("Solve!");
        start = null;
        stop = null;
        // Clear JLabel background colors.
        for (JLabel[] row : labelMap) {
            for (JLabel cell : row) {
                cell.setBackground(PANEL_COLOR);
            }
        }
    }

    /**
     * Fill text with spaces to keep consistent spacing
     * @param text Text to fill with spaces
     * @return Filled text
     */
    private static String fillString(String text) {
        int len = START_INSTRUCT.length() - text.length();
        if (len > 0) {
            char[] repeat = new char[len];
            Arrays.fill(repeat, ' ');
            text += new String(repeat);
        }
        return text;
    }

}
