package graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import adapters.Point;

/**
 * The Mine Sweeper Game.
 * Left-click to reveal a cell.
 * Right-click to plant/remove a flag for marking a suspected mine.
 * You win if all the cells not containing mines are revealed.
 * You lose if you reveal a cell containing a mine.
 */
public class MineSweeper extends JFrame {
	private static final long serialVersionUID = 1L;

	private String currentLevel = "Beginner"; // Current level
	public int numRows = 8, numCols = 8;
	public int cellSize = 60; // Cell width and height, in pixels
	public int canvasWidth, canvasHeigh; // Game board width/heigh

	// Name-constants for the game properties
	public static final int CELLS_CHANGED = 1;
	public static final int FLAGS_CHANGED = 2;
	public static final String BEGINNER = "Beginner";
	public static final String INTERMEDIATE = "Intermediate";
	public static final String EXPERT = "Expert";
	public static final String LEGEND = "Legend";

	// Name-constants for UI control (sizes, colors and fonts)
	public static final Color BGCOLOR_NOT_REVEALED = Color.GREEN; // Background
	public static final Color BGCOLOR_REVEALED_MINE = Color.RED; // Background of mine's cell
	public static final Color BGCOLOR_REVEALED = Color.DARK_GRAY; // Background of selected cell
	public static final Color FGCOLOR_REVEALED = Color.LIGHT_GRAY; // Foreground of selected cell
	public static final Font FONT_NUMBERS = new Font("Comic Sans MS", Font.TYPE1_FONT, 20);

	public List<Point> listCells = new ArrayList<>();
	// Buttons for user interaction
	public JButton[][] btnCells;
	// Number of mines in this game. Can vary to control the difficulty level.
	public int numMines = 10;
	public int cellsLeft, numFlags;
	// Image
	public Image minesImage, flagsImage;
	// Location of mines. True if mine is present on this cell.
	public boolean[][] mines;
	// User can right-click to plant/remove a flag to mark a suspicious cell
	public boolean[][] flags;

	public Container container;
	public JPanel gamePanel;
	public Timer timer;
	public int second;
	public JLabel message, minesLb, flagsLb, timeLb;
	public JMenuItem beginner, intermediate, expert;

	/**
	  * Constructor to setup the game and the UI Components
	  */
	public MineSweeper() {
		container = getContentPane();
		container.setLayout(new BorderLayout());
		// Create status bar, menu bar
		statusBar();
		createMenu();
		// Get image
		Image icon = null;
		try {
			minesImage = ImageIO.read(ClassLoader.getSystemResourceAsStream("mines.png"));
			flagsImage = ImageIO.read(ClassLoader.getSystemResource("flags.png"));
			icon = ImageIO.read(getClass().getClassLoader().getResource("minesweeper.png"));
		} catch (IOException e) {
			notification(this, e.toString(), 0);
			System.exit(1);
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(icon);
		setTitle("Minesweeper");
		setVisible(true);
		// Initialize for a new game
		initGame();
	}

	// Initialize and re-initialize a new game
	public void initGame() {
		// Menu bar, need to be located here
		contextLevelsMenu();
		// Construct ROWS*COLS JButtons and add to the content-pane
		gamePanel = new JPanel(new GridLayout(numRows, numCols, 2, 2), true);
		CellMouseListener listener = new CellMouseListener();
		btnCells = new JButton[numRows][numCols];
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				btnCells[row][col] = new JButton();
				btnCells[row][col].addMouseListener(listener);
				gamePanel.add(btnCells[row][col]);
			}
		}
		container.add(gamePanel, BorderLayout.CENTER);
		// Set size, location for game window
		canvasWidth = cellSize * numCols;
		canvasHeigh = cellSize * numRows;
		container.setPreferredSize(new Dimension(canvasWidth, canvasHeigh));
		pack();
		setLocationRelativeTo(null);
		// Reset cells, mines and flags
		mines = new boolean[numRows][numCols];
		flags = new boolean[numRows][numCols];
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				// Set all cells to un-revealed
				btnCells[row][col].setEnabled(true);
				btnCells[row][col].setFocusable(false);
				btnCells[row][col].setForeground(BGCOLOR_REVEALED_MINE);
				btnCells[row][col].setBackground(BGCOLOR_NOT_REVEALED);
				btnCells[row][col].setFont(FONT_NUMBERS);
				btnCells[row][col].setMargin(new Insets(0, 0, 0, 0));
				btnCells[row][col].setIcon(null); // clear all the flags icon
				btnCells[row][col].setText(""); // display blank
				flags[row][col] = false; // clear all the flags
				mines[row][col] = false; // clear all the mines
			}
		}

		// Set the number of mines and the mines' location
		cellsLeft = numRows * numCols - numMines;
		numFlags = 0;
		Random random = new Random();
		int minesPlaced = 0;
		while (minesPlaced < numMines) {
			int row = random.nextInt(numRows);
			int col = random.nextInt(numCols);
			if (!mines[row][col]) {
				mines[row][col] = true;
				minesPlaced++;
			}
		}
		// Initialize status bar
		message.setText("Cells remaining: " + cellsLeft);
		minesLb.setText("Mines: " + numMines);
		flagsLb.setText("Flags: " + numFlags);
		second = 0;
		timer.start();
	}

	// Refresh game when select Reset Game in menu
	public void reset() {
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				// Set all cells to un-revealed
				btnCells[row][col].setEnabled(true);
				btnCells[row][col].setFocusable(false);
				btnCells[row][col].setForeground(BGCOLOR_REVEALED_MINE);
				btnCells[row][col].setBackground(BGCOLOR_NOT_REVEALED);
				btnCells[row][col].setIcon(null); // clear all the flags icon
				btnCells[row][col].setText(""); // display blank
				flags[row][col] = false; // clear all the flags
			}
		}
	}

	public void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu mFile, mOptions, mHelp, mLevels;
		JMenuItem newGame, resetGame, exit;
		JMenuItem construction, about, highScore;

		// File
		mFile = new JMenu("File");
		newGame = new JMenuItem("New Game");
		resetGame = new JMenuItem("Reset Game");
		exit = new JMenuItem("Exit");
		mFile.add(newGame);
		mFile.add(resetGame);
		mFile.addSeparator();
		mFile.add(exit);

		// Options
		mOptions = new JMenu("Options");
		mLevels = new JMenu("Levels");
		highScore = new JMenuItem("High Score");
		beginner = new JMenuItem();
		intermediate = new JMenuItem();
		expert = new JMenuItem();
		mOptions.add(mLevels);
		mOptions.addSeparator();
		mOptions.add(highScore);
		mLevels.add(beginner);
		mLevels.addSeparator();
		mLevels.add(intermediate);
		mLevels.addSeparator();
		mLevels.add(expert);

		// Help
		mHelp = new JMenu("Help");
		construction = new JMenuItem("Construction");
		about = new JMenuItem("About");
		mHelp.add(construction);
		mHelp.addSeparator();
		mHelp.add(about);

		// Add to menu bar
		menuBar.add(mFile);
		menuBar.add(mOptions);
		menuBar.add(mHelp);
		setJMenuBar(menuBar);

		// Add event listener
		newGame.addActionListener(new MenuListener());
		resetGame.addActionListener(new MenuListener());
		exit.addActionListener(new MenuListener());
		beginner.addActionListener(new MenuListener());
		intermediate.addActionListener(new MenuListener());
		expert.addActionListener(new MenuListener());
		highScore.addActionListener(new MenuListener());
		construction.addActionListener(new MenuListener());
		about.addActionListener(new MenuListener());
	}

	// Change level in Menu when choose level, change attribute state
	public void contextLevelsMenu() {
		beginner.setText("Beginner");
		intermediate.setText("Intermediate");
		expert.setText("Expert");
		switch (currentLevel) {
		case "Beginner":
			beginner.setText("• Beginner");
			cellSize = 60;
			numRows = 8;
			numCols = 8;
			numMines = 10;
			break;
		case "Intermediate":
			intermediate.setText("• Intermediate");
			cellSize = 40;
			numRows = 16;
			numCols = 16;
			numMines = 40;
			break;
		case "Expert":
			expert.setText("• Expert");
			cellSize = 40;
			numRows = 16;
			numCols = 30;
			numMines = 99;
			break;
		default:
			break;
		}
	}

	// Status bar to show number of cells remaining, flags, mines, timer
	public void statusBar() {
		JPanel statusPanel = new JPanel(new GridLayout(1, 4));
		message = new JLabel();
		minesLb = new JLabel();
		flagsLb = new JLabel();
		timeLb = new JLabel();
		minesLb.setHorizontalAlignment(JLabel.CENTER);
		flagsLb.setHorizontalAlignment(JLabel.CENTER);
		timeLb.setHorizontalAlignment(JLabel.RIGHT);
		timer = new Timer(1000, new ActionListener() {
			String mesTimer = "Time: ";

			@Override
			public void actionPerformed(ActionEvent e) {
				second++;
				timeLb.setText(mesTimer + second + "s");
			}
		});
		// Add components to content-pane
		statusPanel.add(message);
		statusPanel.add(minesLb);
		statusPanel.add(flagsLb);
		statusPanel.add(timeLb);
		container.add(statusPanel, BorderLayout.SOUTH);
	}

	// Change the number of cells/flags in status bar
	public void setTextChanged(int type) {
		switch (type) {
		case CELLS_CHANGED:
			message.setText("Cells remaining: " + cellsLeft);
			break;
		case FLAGS_CHANGED:
			flagsLb.setText("Flags: " + numFlags);
			break;
		default:
			break;
		}
	}

	// Get construction in file as string
	public String getConstruction() {
		String line, data = "";
		InputStream in = ClassLoader.getSystemResourceAsStream("minesweeper construction");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
			while ((line = reader.readLine()) != null) {
				data += line + "\n";
			}
		} catch (Exception e) {
			data = e.toString() + 0; // "0" to check in notification() method
		}
		return data;
	}

	// Get about in file as string
	public String getAbout() {
		String line, data = "";
		InputStream in = ClassLoader.getSystemResourceAsStream("about");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
			while ((line = reader.readLine()) != null) {
				data += line + "\n";
			}
		} catch (Exception e) {
			data = e.toString() + 0; // "0" to check in notification() method
		}
		return data;
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
	public void openCellsAround(int row, int col) {
		int rowSelected, colSelected;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				rowSelected = row + i;
				colSelected = col + j;
				if (rowSelected >= 0 && rowSelected < numRows && colSelected >= 0 && colSelected < numCols) {
					if (btnCells[rowSelected][colSelected].isEnabled() && !flags[rowSelected][colSelected]) {
						btnCells[rowSelected][colSelected].setBackground(BGCOLOR_REVEALED);
						btnCells[rowSelected][colSelected].setForeground(FGCOLOR_REVEALED);
						btnCells[rowSelected][colSelected].setEnabled(false);
						cellsLeft--;
						setTextChanged(1);
						if (minesNear(rowSelected, colSelected) == 0) {
							listCells.add(new Point(rowSelected, colSelected));
						} else {
							btnCells[rowSelected][colSelected].setText(minesNear(rowSelected, colSelected) + "");
						}
					}
				}
			}
		}
		processCellsWaiting();
	}

	public void processCellsWaiting() {
		int rowSelected, colSelected;
		Point point;
		if (listCells.size() > 0) {
			point = listCells.get(0);
			listCells.remove(0);
			rowSelected = point.getRowSelected();
			colSelected = point.getColSelected();
			openCellsAround(rowSelected, colSelected);
		}
	}

	// return 1 if there's a mine at row,col or 0 if there isn't
	public int mineAt(int row, int col) {
		if (row >= 0 && row < numRows && col >= 0 && col < numCols && mines[row][col]) {
			return 1;
		} else {
			return 0;
		}
	}

	// Show message dialog
	public void notification(Component parent, Object message, int messageType) {
		String subString;
		if (messageType == 3) {
			int length = message.toString().length();
			subString = message.toString().substring(length - 1, length);
			if (subString.equals("0")) {
				message = message.toString().substring(0, length - 1);
				messageType = 0;
			} else {
				messageType = 1;
			}
		}
		JOptionPane.showMessageDialog(parent, message, "Notification", messageType);
	}

	/**
	 * Handle event when mouse click
	 * */
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
			for (int row = 0; row < numRows && !found; row++) {
				for (int col = 0; col < numCols && !found; col++) {
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
					timer.stop();
					btnCells[rowSelected][colSelected].setBackground(BGCOLOR_REVEALED_MINE);
					btnCells[rowSelected][colSelected].setIcon(new ImageIcon(minesImage));
					notification(MineSweeper.this, "Game over!", 1);
					container.remove(gamePanel);
					initGame();
				} else {
					btnCells[rowSelected][colSelected].setBackground(BGCOLOR_REVEALED);
					btnCells[rowSelected][colSelected].setForeground(FGCOLOR_REVEALED);
					btnCells[rowSelected][colSelected].setEnabled(false);
					cellsLeft--;
					setTextChanged(CELLS_CHANGED);
					if (minesNear(rowSelected, colSelected) != 0) {
						btnCells[rowSelected][colSelected].setText(minesNear(rowSelected, colSelected) + "");
					} else {
						openCellsAround(rowSelected, colSelected);
					}
				}
			} else if (e.getButton() == MouseEvent.BUTTON3) { // Right-button clicked
				if (btnCells[rowSelected][colSelected].isEnabled()) {
					if (!flags[rowSelected][colSelected]) {
						numFlags++;
						setTextChanged(FLAGS_CHANGED);
						flags[rowSelected][colSelected] = true;
						btnCells[rowSelected][colSelected].setIcon(new ImageIcon(flagsImage));
					} else {
						numFlags--;
						setTextChanged(FLAGS_CHANGED);
						flags[rowSelected][colSelected] = false;
						btnCells[rowSelected][colSelected].setIcon(null);
					}
				}

			}
			if (cellsLeft == 0) {
				timer.stop();
				notification(MineSweeper.this, "You win!", 1);
				container.remove(gamePanel);
				initGame();
			}
		}
	}

	/**
	 * Handle event when menu clicked
	 * */
	private class MenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String itemClicked = e.getActionCommand();
			switch (itemClicked) {
			case "New Game":
				container.remove(gamePanel);
				initGame();
				break;
			case "Reset Game":
				reset();
				break;
			case "Exit":
				System.exit(1);
				break;
			case "High Score":
				new TopPlayer();
				break;
			case "Beginner":
				container.remove(gamePanel);
				currentLevel = "Beginner";
				initGame();
				break;
			case "Intermediate":
				container.remove(gamePanel);
				currentLevel = "Intermediate";
				initGame();
				break;
			case "Expert":
				container.remove(gamePanel);
				currentLevel = "Expert";
				initGame();
				break;
			case "Construction":
				notification(MineSweeper.this, getConstruction(), 3);
				break;
			case "About":
				notification(MineSweeper.this, getAbout(), 3);
				break;
			default:
				break;
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
