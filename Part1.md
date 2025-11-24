## Part 1A：

### 1.Formalize Sudoku as a CSP.

A Constraint Satisfaction Problem consists of three parts:**Variables** ,**Domains** and **Constraints**.The standard 9×9 Sudoku can be naturally modeled as a CSP.

### Variables

* Each unfilled cell represents a variable.

* All 81 squares can be regarded as variables, and they can be denoted as:
  
  $$
  X = \{x_{i,j} \mid i \in \{1,\dots,9\},\, j \in \{1,\dots,9\}\}
  $$

Here, $x_{i,j}$ represents the cell located in the $i$-th row and $j$-th column.

### Domains

* The domain of each variable is the set of numbers from 1 to 9.
  $$
  D(x_{i,j}) = \{1, 2, 3, 4, 5, 6, 7, 8, 9\}
  $$

### Constraints

Sudoku has three types of global constraints, all of which are  **All Different** constraints:

1. **row constraints**:Each row's 9 variables must be distinct from one another.
   
   $$
   \forall i \in \{1,\dots,9\},\quad \text{AllDifferent}(x_{i,1}, x_{i,2}, \dots, x_{i,9})
   $$

2. **colume constraints**:Each of the 9 variables in each column must be unique.
   
   $$
   \forall j \in \{1,\dots,9\},\quad \text{AllDifferent}(x_{1,j}, x_{2,j}, \dots, x_{9,j})
   $$

3. **Box constraints**:Each 3×3 sub-grid (with a total of 9 cells) must have 9 distinct variables. For example, the top-left grid:
   
   $$
   \text{AllDifferent}(x_{1,1}, x_{1,2}, x_{1,3}, x_{2,1}, x_{2,2}, x_{2,3}, x_{3,1}, x_{3,2}, x_{3,3})
   $$

These constraints collectively ensure the validity of the solution:**Each row, each column, and each 3×3 grid exactly contains each of the numbers 1 to 9 once.**.And this is precisely the basic requirement of Sudoku.

### 2. Time Complexity Comparison: Brute-force Search vs. Backtracking

1. **Brute-force Search  **
   * Tries all possible assignments by filling each empty cell with digits 1–9 sequentially, without any intermediate validity checks, until a complete assignment satisfying all constraints is found.  
   * Time complexity is \( O(9^n) \).
   * In the worst case, it would need to try 6,670,903,752,021,072,936,960 combinations.  
   * **Practically infeasible**—it cannot solve any non-trivial Sudoku puzzle within a reasonable time.
2. **Backtracking**
   * Fills cells one by one with valid digits, dynamically checking constraints at each step (i.e., whether the digit already exists in the current row, column, or 3×3 box).     
   * If a partial assignment violates any constraint, the algorithm immediately backtracks and abandons that branch, avoiding futile exploration.    
   * Worst-case time complexity remains  \( O(9^n) \) , but actual runtime is vastly superior to brute-force search.

## Part 1B：
### 1. Choice of Solving Method
This project employs **Backtracking with real-time constraint checking** as the core solving strategy. Specifically, within a standard depth-first search (DFS) framework, whenever attempting to place a digit $d \in \{1,\dots,9\}$ into an empty cell, the algorithm immediately verifies whether $d$ already appears in the current row, current column, or current $3\times3$ box. If a conflict is detected, that digit is skipped; if all digits conflict, the algorithm backtracks to the previous empty cell.
This approach is essentially a lightweight **Pruning strategy**. Although it does not implement advanced techniques like Forward Checking or Arc Consistency, it effectively avoids a large number of invalid search paths. Compared to brute-force search, it significantly improves efficiency while maintaining a simple implementation.
### 2. Technical Implementation (Java)
The program is implemented in Java, with the following key design choices:
- **Data structure**: A 2D integer array `int[9][9]` named `board` stores the Sudoku grid, with empty cells represented by `0`.![](photo/p.png)
- **Main backtracking function**: `boolean solve(int[][] board)` recursively traverses all empty cells and attempts to fill them with valid digits.![](photo/main.png)
- **Validity checker**: `boolean isValid(int[][] board, int row, int col, int num)` verifies whether placing `num` at position $(row, col)$ satisfies row, column, and box constraints.![](photo/isVild.png)
### 3. A\* Search vs. Pruned Backtracking
### A\* Search
- A heuristic search algorithm that uses an evaluation function $f(n) = g(n) + h(n)$ to guide the search, where $g(n)$ is the actual cost from the start to the current state, and $h(n)$ is an estimated cost from the current state to the goal.
- Requires designing an effective heuristic function $h(n)$ for Sudoku—for example, counting the number of remaining empty cells or the number of constraint violations.
- Theoretically guarantees an optimal solution, but in Sudoku, the notion of an “optimal solution” is ambiguous since all valid solutions are structurally equivalent.
- Requires maintaining an open list (priority queue) of candidate states, leading to significantly higher memory overhead than backtracking.
### Pruned Backtracking
- Extends standard backtracking by dynamically checking for row, column, and box conflicts at each assignment step, and immediately backtracking upon detecting any violation.
- Does not require a complex heuristic function; instead, it leverages the problem’s inherent constraints for pruning.
- Worst-case time complexity remains $O(9^n)$, but it solves most Sudoku puzzles in milliseconds in practice.
- Memory usage is minimal, requiring only the recursive call stack—making it highly suitable for large-scale CSP problems.


