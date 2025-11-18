import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.*;
import javax.swing.*;

public class SudokuSolver extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private int[][] board = new int[9][9];
    private JLabel statusLabel; // 用于显示回溯次数和时间

    public SudokuSolver() {
        setTitle("Sudoku Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 网格面板
        JPanel gridPanel = new JPanel(new GridLayout(9, 9, 2, 2));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = new JTextField();
                cell.setHorizontalAlignment(JTextField.CENTER);
                cell.setFont(new Font("Arial", Font.BOLD, 18));
                cell.setPreferredSize(new Dimension(50, 50));

                // 设置边框模拟 3x3 宫格
                int top = (i % 3 == 0 && i != 0) ? 3 : 1;
                int left = (j % 3 == 0 && j != 0) ? 3 : 1;
                cell.setBorder(BorderFactory.createMatteBorder(top, left, 1, 1, Color.BLACK));

                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton solveButton = new JButton("solve");
        JButton clearButton = new JButton("erease");
        JButton loadButton = new JButton("from .txt upload");

        solveButton.addActionListener(new SolveAction());
        clearButton.addActionListener(e -> clearBoard());
        loadButton.addActionListener(new LoadAction());

        buttonPanel.add(solveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(loadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 状态标签（显示回溯次数和时间）
        statusLabel = new JLabel("Backtrace: - | COST: - ms", JLabel.CENTER);
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
                        "cell (" + (i+1) + ", " + (j+1) + ") must input 1--9 or nothing",
                        "input error", JOptionPane.ERROR_MESSAGE);
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
        statusLabel.setText("Backtrace: - | COST: - ms");
    }

    // ========================
    // 数独求解器（带计数）
    // ========================
    private static int backtrackCount = 0;

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

    private boolean solveSudoku() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isValid(i, j, num)) {
                            board[i][j] = num;
                            if (solveSudoku()) {
                                return true;
                            }
                            board[i][j] = 0;
                        }
                    }
                    backtrackCount++; // 回溯发生！
                    return false;
                }
            }
        }
        return true;
    }

    // ========================
    // GUI 事件处理器
    // ========================

    class SolveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                loadBoardFromGUI();
                int[][] original = new int[9][9];
                for (int i = 0; i < 9; i++)
                    System.arraycopy(board[i], 0, original[i], 0, 9);

                backtrackCount = 0;
                long startTime = System.currentTimeMillis();

                boolean solved = solveSudoku();

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                if (solved) {
                    // 更新原为空的格子为蓝色
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            if (original[i][j] == 0 && board[i][j] != 0) {
                                cells[i][j].setText(String.valueOf(board[i][j]));
                                cells[i][j].setForeground(Color.BLUE);
                            }
                        }
                    }
                    statusLabel.setText(String.format("backtrace: %d | COST: %d ms", backtrackCount, duration));
                } else {
                    JOptionPane.showMessageDialog(SudokuSolver.this, "this sudoku have no solution!", "no solution", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("backtrace: - | COST: - ms");
                }
            } catch (IllegalArgumentException ex) {
                // 错误已在 loadBoardFromGUI 中提示
            }
        }
    }

    class LoadAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
            int result = fileChooser.showOpenDialog(SudokuSolver.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    java.util.List<String> lines = Files.readAllLines(file.toPath());
                    if (lines.size() < 9) {
                        JOptionPane.showMessageDialog(SudokuSolver.this, "The number of lines in the file is less than 9!", "error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    clearBoard(); // 先清空
                    for (int i = 0; i < 9; i++) {
                        String line = lines.get(i).trim();
                        if (line.length() != 9) {
                            JOptionPane.showMessageDialog(SudokuSolver.this,
                                "第 " + (i + 1) + " 行长度不是 9 个字符！", "格式错误", JOptionPane.ERROR_MESSAGE);
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
                                    "第 " + (i + 1) + " 行包含非法字符！", "格式错误", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(SudokuSolver.this, "读取文件失败: " + ex.getMessage(), "IO 错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // ========================
    // 主方法
    // ========================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new SudokuSolver().setVisible(true);
        });
    }
}