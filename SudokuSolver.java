import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class SudokuSolver extends JFrame {
    private JTextField[][] cells = new JTextField[9][9];
    private int[][] board = new int[9][9];

    public SudokuSolver() {
        setTitle("Sudoku Solver - 输入数独");
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

                // 模拟 3x3 宫格分隔（加粗边框）
                if (i % 3 == 0 && i != 0) {
                    cell.setBorder(BorderFactory.createMatteBorder(3, 1, 1, 1, Color.BLACK));
                } else {
                    cell.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
                }
                if (j % 3 == 0 && j != 0) {
                    cell.setBorder(BorderFactory.createMatteBorder(
                        i % 3 == 0 && i != 0 ? 3 : 1,
                        3, 1, 1, Color.BLACK
                    ));
                }

                cells[i][j] = cell;
                gridPanel.add(cell);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton solveButton = new JButton("求解");
        JButton clearButton = new JButton("清空");

        solveButton.addActionListener(new SolveAction());
        clearButton.addActionListener(e -> clearBoard());

        buttonPanel.add(solveButton);
        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private void loadBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText().trim();
                if (text.isEmpty()) {
                    board[i][j] = 0;
                } else if (text.matches("[1-9]")) {
                    board[i][j] = Integer.parseInt(text);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "位置 (" + (i+1) + ", " + (j+1) + ") 必须输入 1-9 或留空！", 
                        "输入错误", JOptionPane.ERROR_MESSAGE);
                    throw new IllegalArgumentException();
                }
            }
        }
    }

    private void updateBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] != 0) {
                    cells[i][j].setText(String.valueOf(board[i][j]));
                } else {
                    cells[i][j].setText("");
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
    }

    // ========================
    // 数独求解逻辑（Java 版）
    // ========================
    private boolean isValid(int row, int col, int num) {
        // 检查行
        for (int j = 0; j < 9; j++) {
            if (board[row][j] == num) return false;
        }
        // 检查列
        for (int i = 0; i < 9; i++) {
            if (board[i][col] == num) return false;
        }
        // 检查 3x3 宫格
        int startRow = 3 * (row / 3);
        int startCol = 3 * (col / 3);
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (board[i][j] == num) return false;
            }
        }
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
                    return false;
                }
            }
        }
        return true;
    }

    // 求解按钮事件
    class SolveAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                loadBoard(); // 从输入框加载数据到 board
                int[][] original = new int[9][9];
                for (int i = 0; i < 9; i++) {
                    System.arraycopy(board[i], 0, original[i], 0, 9);
                }

                if (solveSudoku()) {
                    // 只更新原本为空的格子，并设为蓝色
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 9; j++) {
                            if (original[i][j] == 0 && board[i][j] != 0) {
                                cells[i][j].setText(String.valueOf(board[i][j]));
                                cells[i][j].setForeground(Color.BLUE);
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(SudokuSolver.this, 
                        "该数独无解！", "无解", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IllegalArgumentException ex) {
                // 输入错误已在 loadBoard 中提示
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