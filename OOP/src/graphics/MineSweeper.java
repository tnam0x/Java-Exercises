package graphics;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * The Mine Sweeper Game.
 * Left-click to reveal a cell.
 * Right-click to plant/remove a flag for marking a suspected mine.
 * You win if all the cells not containing mines are revealed.
 * You lose if you reveal a cell containing a mine.
 */
public class MineSweeper extends JFrame {
	private static final long serialVersionUID = 1L;

	// Name-constants for the game properties
	public static final int ROWS = 10;
	public static final int COLS = 10;

	// Name-constants for UI control (sizes, colors and fonts)
	public static final int CELL_SIZE = 60; // Cell width and height, in pixels
	public static final int CANVAS_WIDTH = CELL_SIZE * COLS; // Game board width/heigh
	public static final int CANVAS_HEIGH = CELL_SIZE * ROWS;
	public static final Color BGCOLOR_NOT_REVEALED = Color.GREEN;
	public static final Color FGCOLOR_NOT_REVEALED = Color.RED; // flag
	public static final Color BGCOLOR_REVEALED = Color.DARK_GRAY;
	public static final Color FGCOLOR_REVEALED = Color.LIGHT_GRAY; // number of mines
	public static final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 20);

	List<Point> listCells = new ArrayList<>();
	int sizeOf;
	// Buttons for user interaction
	JButton[][] btnCells = new JButton[ROWS][COLS];

	// Number of mines in this game. Can vary to control the difficulty level.
	int numMines;
	// Image
	Image minesIcon, flagsIcon;

	// Location of mines. True if mine is present on this cell.
	boolean[][] mines = new boolean[ROWS][COLS];

	// User can right-click to plant/remove a flag to mark a suspicious cell
	boolean[][] flags = new boolean[ROWS][COLS];

	/**
	  * Constructor to setup the game and the UI Components
	  */
	public MineSweeper() {
		Container container = this.getContentPane();
		container.setLayout(new GridLayout(ROWS, COLS, 2, 2));

		// get icon
		try {
			minesIcon = ImageIO.read((new File("resources/mines.png")));
			flagsIcon = ImageIO.read((new File("resources/flags.png")));
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		CellMouseListener listener = new CellMouseListener();
		// Construct 10x10 JButtons and add to the content-pane
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				btnCells[row][col] = new JButton();
				btnCells[row][col].addMouseListener(listener);
				container.add(btnCells[row][col]);
			}
		}
		container.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGH));
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(getToolkit().getImage("resources/minesweeper.png"));
		setLocationRelativeTo(null);
		setTitle("Minesweeper");
		setVisible(true);
		// Initialize for a new game
		initGame();
	}

	// Initialize and re-initialize a new game
	public void initGame() {
		// Reset cells, mines and flags
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				// Set all cells to un-revealed
				btnCells[row][col].setEnabled(true);
				btnCells[row][col].setForeground(FGCOLOR_NOT_REVEALED);
				btnCells[row][col].setBackground(BGCOLOR_NOT_REVEALED);
				btnCells[row][col].setFont(FONT_NUMBERS);
				btnCells[row][col].setIcon(null); // clear all the flags icon
				btnCells[row][col].setText(""); // display blank
				mines[row][col] = false; // clear all the mines
				flags[row][col] = false; // clear all the flags
			}
		}

		// Set the number of mines and the mines' location
		numMines = 10;
		Random random = new Random();
		int minesPlaced = 0;
		while (minesPlaced < numMines) {
			int row = random.nextInt(ROWS);
			int col = random.nextInt(COLS);
			if (!mines[row][col]) {
				mines[row][col] = true;
				minesPlaced++;
			}
		}
	}

	// return the number of mines near the selected location
	public int minesNear(int row, int col) {
		int mines = 0;
		// check mines in all directions
		// can use for loops instead
		mines += mineAt(row - 1, col - 1);
		mines += mineAt(row - 1, col);
		mines += mineAt(row - 1, col + 1);
		mines += mineAt(row, col - 1);
		mines += mineAt(row, col + 1);
		mines += mineAt(row + 1, col - 1);
		mines += mineAt(row + 1, col);
		mines += mineAt(row + 1, col + 1);
		return mines;
	}

	// Open all the mines near blank cell
	public void openCells(int row, int col) {
		int rowSelected, colSelected;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				rowSelected = row + i;
				colSelected = col + j;
				if (rowSelected != colSelected && rowSelected >= 0 && rowSelected <= ROWS && colSelected >= 0
						&& colSelected <= COLS && btnCells[rowSelected][colSelected].isEnabled())
					if (minesNear(rowSelected, colSelected) == 0) {
						listCells.add(new Point(rowSelected, colSelected));
					} else {
						btnCells[rowSelected][colSelected].setBackground(BGCOLOR_REVEALED);
						btnCells[rowSelected][colSelected].setForeground(FGCOLOR_REVEALED);
						btnCells[rowSelected][colSelected].setEnabled(false);
						btnCells[rowSelected][colSelected].setText(minesNear(rowSelected, colSelected) + "");
					}
			}
		}
		openCellsWaiting();
	}

	public void openCellsWaiting() {
		int rowSelected, colSelected;
		Point point;
		sizeOf = listCells.size();
		if (sizeOf >= 0) {
			point = listCells.get(0);
			listCells.remove(0);
			rowSelected = point.getRowSelected();
			colSelected = point.getColSelected();
			openCells(rowSelected, colSelected);
		}
	}

	// return 1 if there's a mine at row,col or 0 if there isn't
	public int mineAt(int row, int col) {
		if (row >= 0 && row < ROWS && col >= 0 && col < COLS && mines[row][col]) {
			return 1;
		} else {
			return 0;
		}
	}

	private class CellMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			// Determine the (row, col) of the JButton that triggered the event
			int rowSelected = -1;
			int colSelected = -1;

			// Get the source object that fired the Event
			JButton source = (JButton) e.getSource();
			// Scan all rows and columns and match with the source object
			boolean found = false;
			for (int row = 0; row < ROWS && !found; row++) {
				for (int col = 0; col < COLS && !found; col++) {
					if (source == btnCells[row][col]) {
						rowSelected = row;
						colSelected = col;
						found = true;
					}
				}
			}

			// Left-click to reveal a cell; Right-click to plant/remove the flag.
			if (e.getButton() == MouseEvent.BUTTON1 && !flags[rowSelected][colSelected]) {// Left-button clicked
				if (mines[rowSelected][colSelected]) {
					btnCells[rowSelected][colSelected].setBackground(FGCOLOR_NOT_REVEALED);
					btnCells[rowSelected][colSelected].setIcon(new ImageIcon(minesIcon));
					JOptionPane.showMessageDialog(MineSweeper.this, "Game over!", "Notification",
							JOptionPane.INFORMATION_MESSAGE);
					initGame();
				} else {
					btnCells[rowSelected][colSelected].setBackground(BGCOLOR_REVEALED);
					btnCells[rowSelected][colSelected].setForeground(FGCOLOR_REVEALED);
					btnCells[rowSelected][colSelected].setEnabled(false);
					if (minesNear(rowSelected, colSelected) != 0) {
						btnCells[rowSelected][colSelected].setText(minesNear(rowSelected, colSelected) + "");
					} else {
						openCells(rowSelected, colSelected);
					}
				}
			} else if (e.getButton() == MouseEvent.BUTTON3) { // Right-button clicked
				if (flags[rowSelected][colSelected]) {
					flags[rowSelected][colSelected] = false;
					btnCells[rowSelected][colSelected].setBackground(BGCOLOR_NOT_REVEALED);
					btnCells[rowSelected][colSelected].setIcon(null);
					btnCells[rowSelected][colSelected].setFocusable(false);
				} else {
					flags[rowSelected][colSelected] = true;
					btnCells[rowSelected][colSelected].setForeground(FGCOLOR_NOT_REVEALED);
					btnCells[rowSelected][colSelected].setBackground(BGCOLOR_NOT_REVEALED);
					btnCells[rowSelected][colSelected].setIcon(new ImageIcon(flagsIcon));
					btnCells[rowSelected][colSelected].setFocusable(false);
				}
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new MineSweeper();
	}

}