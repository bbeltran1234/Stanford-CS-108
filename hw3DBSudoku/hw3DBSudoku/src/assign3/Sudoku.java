package assign3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/*
 * Encapsulates a Sudoku grid to be solved.
 * CS108 Stanford.
 */
public class Sudoku {
    // Provided grid data for main/testing
    // The instance variable strategy is up to you.

    // Provided easy 1 6 grid
    // (can paste this text into the GUI too)
    public static final int[][] easyGrid = Sudoku.stringsToGrid(
            "1 6 4 0 0 0 0 0 2", "2 0 0 4 0 3 9 1 0", "0 0 5 0 8 0 4 0 7",
            "0 9 0 0 0 6 5 0 0", "5 0 0 1 0 2 0 0 8", "0 0 8 9 0 0 0 3 0",
            "8 0 9 0 4 0 2 0 0", "0 7 3 5 0 9 0 0 1", "4 0 0 0 0 0 6 7 9");

    // Provided medium 5 3 grid
    public static final int[][] mediumGrid = Sudoku.stringsToGrid("530070000",
            "600195000", "098000060", "800060003", "400803001", "700020006",
            "060000280", "000419005", "000080079");

    // Provided hard 3 7 grid
    // 1 solution this way, 6 solutions if the 7 is changed to 0
    public static final int[][] hardGrid = Sudoku.stringsToGrid(
            "3 7 0 0 0 0 0 8 0", "0 0 1 0 9 3 0 0 0", "0 4 0 7 8 0 0 0 3",
            "0 9 3 8 0 0 0 1 2", "0 0 0 0 4 0 0 0 0", "5 2 0 0 0 6 7 9 0",
            "6 0 0 0 2 1 0 4 0", "0 0 0 5 3 0 9 0 0", "0 3 0 0 0 0 0 5 1");

    public static final int SIZE = 9; // size of the whole 9x9 puzzle
    public static final int PART = 3; // size of each 3x3 part
    public static final int MAX_SOLUTIONS = 100;

    // Provided various static utility methods to
    // convert data formats to int[][] grid.

    /**
     * Returns a 2-d grid parsed from strings, one string per row. The "..." is
     * a Java 5 feature that essentially makes "rows" a String[] array.
     * (provided utility)
     * 
     * @param rows
     *            array of row strings
     * @return grid
     */
    public static int[][] stringsToGrid(String... rows) {
        int[][] result = new int[rows.length][];
        for (int row = 0; row < rows.length; row++) {
            result[row] = stringToInts(rows[row]);
        }
        return result;
    }

    /**
     * Given a single string containing 81 numbers, returns a 9x9 grid. Skips
     * all the non-numbers in the text. (provided utility)
     * 
     * @param text
     *            string of 81 numbers
     * @return grid
     */
    public static int[][] textToGrid(String text) {
        int[] nums = stringToInts(text);
        if (nums.length != SIZE * SIZE) {
            throw new RuntimeException("Needed 81 numbers, but got:"
                    + nums.length);
        }

        int[][] result = new int[SIZE][SIZE];
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                result[row][col] = nums[count];
                count++;
            }
        }
        return result;
    }

    /**
     * Given a string containing digits, like "1 23 4", returns an int[] of
     * those digits {1 2 3 4}. (provided utility)
     * 
     * @param string
     *            string containing ints
     * @return array of ints
     */
    public static int[] stringToInts(String string) {
        int[] a = new int[string.length()];
        int found = 0;
        for (int i = 0; i < string.length(); i++) {
            if (Character.isDigit(string.charAt(i))) {
                a[found] = Integer.parseInt(string.substring(i, i + 1));
                found++;
            }
        }
        int[] result = new int[found];
        System.arraycopy(a, 0, result, 0, found);
        return result;
    }

    // Provided -- the deliverable main().
    // You can edit to do easier cases, but turn in
    // solving hardGrid.
    public static void main(String[] args) {
        Sudoku sudoku;
        sudoku = new Sudoku(hardGrid);

        System.out.println(sudoku); // print the raw problem
        int count = sudoku.solve();
        System.out.println("solutions:" + count);
        System.out.println("elapsed:" + sudoku.getElapsed() + "ms");
        System.out.println(sudoku.getSolutionText());
    }

    // My code begins here.
    /**
     * Given a 2D array, return its string representation.
     * 
     * @param ints
     */
    public static String intsToString(int[][] ints) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : ints) {
            for (int ele : row) {
                sb.append(ele).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1).append(System.lineSeparator());
        }
        sb.delete(sb.lastIndexOf(System.lineSeparator()), sb.length());
        return sb.toString();
    }

    /**
     * A Spot represents a spot in Sudoku grid.
     * 
     * @author WEIYUNSHENG
     *
     */
    private class Spot {
        // Represent the coordinate of the Spot in grid
        int x;
        int y;

        Set<Integer> candidates;

        Spot(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void set(int value) {
            solution[x][y] = value;
        }
    }

    /**
     * 
     * @param grid
     *            9 * 9 2D int array
     * @return a list of 81 Spots with constraints contained in the Spot itself
     */
    private List<Spot> getSpotList(int[][] grid) {
        List<Spot> spotList = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                spotList.add(new Spot(i, j));
            }
        }
        return spotList;
    }

    // grid should never be changed (include its content).
    private final int[][] grid;
    private String solutionText;
    private int[][] tempSolution;
    private int countSolutions = 0;
    private long elapsedTime;

    /**
     * Sets up Sudoku based on the given 2-D array. Require ints to be a legal 9
     * * 9 grid. Empty spots are represented by 0.
     * 
     * @param ints
     */
    public Sudoku(int[][] ints) {
        grid = ints;
    }

    /**
     * Sets up Sudoku based on the given string.
     * 
     * @param puzzle
     *            string of 81 numbers
     */
    public Sudoku(String puzzle) {
        int[][] ints = Sudoku.textToGrid(puzzle);
        grid = ints;
    }

    @Override
    public String toString() {
        return Sudoku.intsToString(grid);
    }

    // Detect whether the given Sudoku puzzle has conflicts itself.
    // If yes, then no need to solve.
    private boolean detectConflicts() {
        // Check rows
        for (int[] row : grid) {
            boolean[] used = new boolean[Sudoku.SIZE + 1];
            for (int ele : row) {
                if (ele != 0 && used[ele]) {
                    return true;
                }
                used[ele] = true;
            }
        }
        
        // Check columns
        for (int j = 0; j < Sudoku.SIZE; j++) {
            boolean[] used = new boolean[Sudoku.SIZE + 1];
            for (int i = 0; i < Sudoku.SIZE; i++) {
                int ele = grid[i][j];
                if (ele != 0 && used[ele]) {
                    return true;
                }
                used[ele] = true;
            }
        }
        
        // Check 3 * 3 neighborhoods
        for (int i = 0; i < Sudoku.SIZE / Sudoku.PART; i++) {
            for (int j = 0; j < Sudoku.SIZE / Sudoku.PART; j++) {
                boolean[] used = new boolean[Sudoku.SIZE + 1];
                for (int ix = i * Sudoku.PART; ix < i * Sudoku.PART + Sudoku.PART; ix++) {
                    for (int jy = j * Sudoku.PART; jy < j * Sudoku.PART + Sudoku.PART; jy++) {
                        int ele = grid[ix][jy];
                        if (ele != 0 && used[ele]) {
                            return true;
                        }
                        used[ele] = true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Solves the puzzle, invoking the underlying recursive search.
     */
    public int solve() {
        long startTime = System.currentTimeMillis();
        
        if (!detectConflicts()) {
            List<Spot> spotList = getSpotList(grid);
            Collections.sort(spotList, new Comparator<Spot>() {
                @Override
                public int compare(Spot lhs, Spot rhs) {
                    return lhs.candidates.size() - rhs.candidates.size();
                }
            });
        }
        

        elapsedTime = System.currentTimeMillis() - startTime;
        return 0;
    }

    /**
     * Get the solution text.
     * 
     * @return the first found solution if any, otherwise an empty string
     */
    public String getSolutionText() {
        if (0 == countSolutions) {
            return "";
        } else {
            return solutionText;
        }
    }

    /**
     * 
     * @return the elapsed time for the last call of <code>solve()</code>,
     *         measured in milliseconds
     */
    public long getElapsed() {
        return elapsedTime;
    }

}
