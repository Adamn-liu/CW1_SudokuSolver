import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class SudokuSolver extends JFrame {
    private final JTextField[][] cells = new JTextField[9][9];
    private final int[][] board = new int[9][9];
    private final JLabel statusLabel;
    private volatile boolean solving = false;
    private volatile boolean cancelRequested = false;
    private static int backtrackCount = 0;
    private final int delayMs = 1; // movie delay

    private final JCheckBox showResultOnlyCheckBox;

    public SudokuSolver() {
        setTitle("Sudoku Solver with Visualization");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(9, 9, 2, 2));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Arial", Font.BOLD, 18));
                cell.setPreferredSize(new Dimension(50, 50));

                int top = (i % 3 == 0 && i != 0) ? 3 : 1;
                int left = (j % 3 == 0 && j != 0) ? 3 : 1;
                cell.setBorder(BorderFactory.createMatteBorder(top, left, 1, 1, Color.BLACK));

                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        //button + rechooose
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton solveButton = new JButton("Solve");
        JButton clearButton = new JButton("erase");
        JButton loadButton = new JButton("Load .txt");

        showResultOnlyCheckBox = new JCheckBox("Show Result Only", false);

        solveButton.addActionListener(new SolveAction());
        clearButton.addActionListener(e -> clearBoard());
        loadButton.addActionListener(new LoadAction());

        controlPanel.add(solveButton);
        controlPanel.add(clearButton);
        controlPanel.add(loadButton);
        controlPanel.add(showResultOnlyCheckBox);
        add(controlPanel, BorderLayout.SOUTH);

        statusLabel = new JLabel("Backtrack: - | Time: - ms", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        add(statusLabel, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void loadBoardFromGUI() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText().trim();
                if (text.isEmpty()) {
                    board[i][j] = 0;
                } else if (text.matches("[1-9]")) {
                    board[i][j] = Integer.parseInt(text);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Cell (" + (i+1) + ", " + (j+1) + ") must be 1-9 or empty.",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setText("");
                cells[i][j].setForeground(Color.BLACK);
            }
        }
        statusLabel.setText("Backtrack: - | Time: - ms");
    }

    private boolean isValid(int row, int col, int num) {
        for (int j = 0; j < 9; j++) if (board[row][j] == num) return false;
        for (int i = 0; i < 9; i++) if (board[i][col] == num) return false;
        int startRow = 3 * (row / 3);
        int startCol = 3 * (col / 3);
        for (int i = startRow; i < startRow + 3; i++)
            for (int j = startCol; j < startCol + 3; j++)
                if (board[i][j] == num) return false;
        return true;
    }

    // Solve Action with Animation Toggle
    class SolveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (solving) return;

            try {
                loadBoardFromGUI();
                int[][] original = new int[9][9];
                for (int i = 0; i < 9; i++) {
                    System.arraycopy(board[i], 0, original[i], 0, 9);
                    for (int j = 0; j < 9; j++) {
                        if (original[i][j] != 0) {
                            cells[i][j].setForeground(Color.BLACK);
                        }
                    }
                }
                solving = true;
                cancelRequested = false;
                backtrackCount = 0;
                long startTime = System.currentTimeMillis();

                boolean showResultOnly = showResultOnlyCheckBox.isSelected();

                if (showResultOnly) {
                    // pattern1: result only 
                    boolean solved = solveWithoutAnimation(original);
                    long duration = System.currentTimeMillis() - startTime;
                    SwingUtilities.invokeLater(() -> {
                        solving = false;
                        if (solved) {
                            for (int i = 0; i < 9; i++) {
                                for (int j = 0; j < 9; j++) {
                                    if (original[i][j] == 0 && board[i][j] != 0) {
                                        cells[i][j].setText(String.valueOf(board[i][j]));
                                        cells[i][j].setForeground(Color.BLUE);
                                    }
                                }
                            }
                            statusLabel.setText(String.format("Backtrack: %d | Time: %d ms", backtrackCount, duration));
                        } else {
                            JOptionPane.showMessageDialog(SudokuSolver.this,
                                "This Sudoku has no solution!", "No Solution", JOptionPane.WARNING_MESSAGE);
                            statusLabel.setText("Backtrack: - | Time: - ms");
                        }
                    });
                } else {
                    // pattern2: movie
                    SwingWorker<Boolean, int[]> worker = new SwingWorker<Boolean, int[]>() {
                        private int[][] localBoard = board;

                        @Override
                        protected Boolean doInBackground() throws Exception {
                            return solveRecursive(original);
                        }

                        private boolean solveRecursive(int[][] original) {
                            for (int i = 0; i < 9; i++) {
                                if (cancelRequested) return false;
                                for (int j = 0; j < 9; j++) {
                                    if (cancelRequested) return false;
                                    if (localBoard[i][j] == 0) {
                                        for (int num = 1; num <= 9; num++) {
                                            if (cancelRequested) return false;
                                            if (isValidWorker(i, j, num)) {
                                                localBoard[i][j] = num;
                                                publish(new int[]{i, j, num});

                                                if (delayMs > 0) {
                                                    try {
                                                        Thread.sleep(delayMs);
                                                    } catch (InterruptedException ex) {
                                                        cancelRequested = true;
                                                        return false;
                                                    }
                                                }

                                                if (solveRecursive(original)) {
                                                    return true;
                                                }

                                                localBoard[i][j] = 0;
                                                publish(new int[]{i, j, 0});

                                                if (delayMs > 0) {
                                                    try {
                                                        Thread.sleep(delayMs);
                                                    } catch (InterruptedException ex) {
                                                        cancelRequested = true;
                                                        return false;
                                                    }
                                                }
                                            }
                                        }
                                        backtrackCount++;
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }

                        private boolean isValidWorker(int row, int col, int num) {
                            for (int j = 0; j < 9; j++) if (localBoard[row][j] == num) return false;
                            for (int i = 0; i < 9; i++) if (localBoard[i][col] == num) return false;
                            int startRow = 3 * (row / 3);
                            int startCol = 3 * (col / 3);
                            for (int i = startRow; i < startRow + 3; i++)
                                for (int j = startCol; j < startCol + 3; j++)
                                    if (localBoard[i][j] == num) return false;
                            return true;
                        }

                        @Override
                        protected void process(java.util.List<int[]> chunks) {
                            for (int[] update : chunks) {
                                int i = update[0], j = update[1], val = update[2];
                                cells[i][j].setText(val == 0 ? "" : String.valueOf(val));
                                if (val != 0) {
                                    cells[i][j].setForeground(Color.BLUE);
                                } else {
                                    cells[i][j].setForeground(Color.BLACK);
                                }
                            }
                        }

                        @Override
                        protected void done() {
                            solving = false;
                            long endTime = System.currentTimeMillis();
                            try {
                                boolean solved = get();
                                long duration = endTime - startTime;
                                if (solved) {
                                    statusLabel.setText(String.format("Backtrack: %d | Time: %d ms", backtrackCount, duration));
                                } else {
                                    JOptionPane.showMessageDialog(SudokuSolver.this,
                                        "This Sudoku has no solution!", "No Solution",
                                        JOptionPane.WARNING_MESSAGE);
                                    statusLabel.setText("Backtrack: - | Time: - ms");
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(SudokuSolver.this,
                                    "Error during solving.", "Error", JOptionPane.ERROR_MESSAGE);
                                statusLabel.setText("Backtrack: - | Time: - ms");
                            }
                        }
                    };
                    worker.execute();
                }

            } catch (IllegalArgumentException ex) {
                solving = false;
            }
        }

        // result only
        private boolean solveWithoutAnimation(int[][] original) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    if (board[i][j] == 0) {
                        for (int num = 1; num <= 9; num++) {
                            if (isValid(i, j, num)) {
                                board[i][j] = num;
                                if (solveWithoutAnimation(original)) {
                                    return true;
                                }
                                board[i][j] = 0;
                            }
                        }
                        backtrackCount++;
                        return false;
                    }
                }
            }
            return true;
        }
    }

    // Load .txt Action
    class LoadAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
            int result = fileChooser.showOpenDialog(SudokuSolver.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    List<String> lines = Files.readAllLines(file.toPath());
                    if (lines.size() < 9) {
                        JOptionPane.showMessageDialog(SudokuSolver.this,
                            "File must contain at least 9 lines.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    clearBoard();
                    for (int i = 0; i < 9; i++) {
                        String line = lines.get(i).trim();
                        if (line.length() != 9) {
                            JOptionPane.showMessageDialog(SudokuSolver.this,
                                "Line " + (i + 1) + " must be exactly 9 characters long.", "Format Error",
                                JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        for (int j = 0; j < 9; j++) {
                            char c = line.charAt(j);
                            if (c >= '0' && c <= '9') {
                                if (c != '0') {
                                    cells[i][j].setText(String.valueOf(c));
                                    cells[i][j].setForeground(Color.BLACK);
                                }
                            } else {
                                JOptionPane.showMessageDialog(SudokuSolver.this,
                                    "Line " + (i + 1) + " contains invalid character: '" + c + "'",
                                    "Format Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SudokuSolver.this,
                        "Failed to read file: " + ex.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new SudokuSolver().setVisible(true);
        });
    }
}