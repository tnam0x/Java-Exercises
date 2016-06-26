package graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

/**
 * The Sudoku game.
 * To solve the number puzzle, each row, each column, and each of the
 * nine 3×3 sub-grids shall contain all of the digits from 1 to 9
 */
public class Sudoku extends JFrame {
	private static final long serialVersionUID = 1L;

	public static String Level = "Beginner"; // Current level
	public static boolean newGame = true; // create a new game
	public int numberOfCellsRemaining; // show in status bar

	// Name-constants for the game properties (levels and status bar)
	public static final String STATUS = "Number of cells remaining: ";
	public static final String BEGINNER = "Beginner";
	public static final String INTERMEDIATE = "Intermediate";
	public static final String EXPERT = "Expert";

	public static final int GRID_SIZE = 9; // Size of the board
	public static final int SUBGRID_SIZE = 3; // Size of the sub-grid

	// Name-constants for UI control (sizes, colors and fonts)
	public static final int CELL_SIZE = 60; // Cell width/height in pixels
	public static final int CANVAS_WIDTH = CELL_SIZE * GRID_SIZE; // Board width/height in pixels
	public static final int CANVAS_HEIGHT = CELL_SIZE * GRID_SIZE; // Board width/height in pixels
	public static final Color OPEN_CELL_BGCOLOR = Color.YELLOW;
	public static final Color OPEN_CELL_TEXT_YES = new Color(0, 255, 0); // RGB
	public static final Color OPEN_CELL_TEXT_NO = Color.RED;
	public static final Color CLOSED_CELL_BGCOLOR = new Color(240, 240, 240); // RGB
	public static final Color CLOSED_CELL_TEXT = Color.BLACK;
	public static final Font FONT_NUMBERS = new Font("Monospaced", Font.BOLD, 20);

	// The game board composes of 9x9 JTextFields,
	// each containing String "1" to "9", or empty String
	private JTextField[][] tfCells = new JTextField[GRID_SIZE][GRID_SIZE];

	// Puzzle to be solved and the mask (which can be used to control the difficulty level).
	// Hardcoded here. Extra credit for automatic puzzle generation with various difficulty levels.
	private static int[][] puzzle = new int[GRID_SIZE][GRID_SIZE];
	private boolean[][] masks = { { false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false },
			{ false, false, false, false, false, false, false, false, false } };

	/**
	  * Constructor to setup the game and the UI Components
	  */
	Container container;

	public Sudoku() {
		container = getContentPane();
		container.setLayout(new BorderLayout());
		container.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
		getPuzzle(container);
		statusBar(container);
		createMenu();
		pack();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		setIconImage(getToolkit().getImage("resources/sudoku.png"));
		setTitle("Sudoku");
		setVisible(true);
	}

	JMenuItem easy, medium, difficult;

	public void createMenu() {
		JMenuBar menuBar;
		JMenu mFile, mOptions, mHelp;
		JMenuItem newGame, resetGame, exit;
		JMenuItem about, construction;
		JMenu mLevel;

		// initial
		menuBar = new JMenuBar();
		newGame = new JMenuItem("New Game");
		resetGame = new JMenuItem("Reset Game");
		exit = new JMenuItem("Exit");
		easy = new JMenuItem();
		medium = new JMenuItem();
		difficult = new JMenuItem();
		contextLevelsMenu();
		about = new JMenuItem("About");
		construction = new JMenuItem("Construction");

		// File
		mFile = new JMenu("File");
		mFile.add(newGame);
		mFile.add(resetGame);
		mFile.addSeparator();
		mFile.add(exit);

		// Options
		mOptions = new JMenu("Options");
		mLevel = new JMenu("Levels");
		mOptions.add(mLevel);
		mLevel.add(easy);
		mLevel.add(medium);
		mLevel.add(difficult);

		// Help
		mHelp = new JMenu("Help");
		mHelp.add(construction);
		mHelp.addSeparator();
		mHelp.add(about);

		menuBar.add(mFile);
		menuBar.add(mOptions);
		menuBar.add(mHelp);
		setJMenuBar(menuBar);

		// add event listener
		newGame.addActionListener(new MenuListener());
		resetGame.addActionListener(new MenuListener());
		exit.addActionListener(new MenuListener());
		easy.addActionListener(new MenuListener());
		medium.addActionListener(new MenuListener());
		difficult.addActionListener(new MenuListener());
		construction.addActionListener(new MenuListener());
		about.addActionListener(new MenuListener());
	}

	public void getPuzzle(Container container) {
		numberOfCellsRemaining = 0;
		if (newGame) {
			shufflePuzzle();
			shuffleMasks();
			newGame = false;
		}
		InputListener listener = new InputListener();
		JPanel puzzlePanel = new JPanel();
		puzzlePanel.setLayout(new GridLayout(GRID_SIZE, GRID_SIZE));
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				tfCells[row][col] = new JTextField(); // Allocate element of array
				puzzlePanel.add(tfCells[row][col]);
				if (masks[row][col]) {
					tfCells[row][col].setText("");
					tfCells[row][col].setEditable(true);
					tfCells[row][col].setBackground(OPEN_CELL_BGCOLOR);
					tfCells[row][col].setDocument(new DigitsDocument());
					tfCells[row][col].addKeyListener(listener);
					numberOfCellsRemaining++;
				} else {
					tfCells[row][col].setText(puzzle[row][col] + "");
					tfCells[row][col].setEditable(false);
					tfCells[row][col].setBackground(CLOSED_CELL_BGCOLOR);
					tfCells[row][col].setForeground(CLOSED_CELL_TEXT);
				}
				// Beautify all the cells
				tfCells[row][col].setHorizontalAlignment(JTextField.CENTER);
				tfCells[row][col].setFont(FONT_NUMBERS);
			}
		}
		container.add(puzzlePanel, BorderLayout.CENTER);
	}

	public void shufflePuzzle() {
		Random r = new Random();
		int firstVal = r.nextInt(8);
		int x = firstVal, v = 1;
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				if ((x + col + v) <= 9) {
					puzzle[row][col] = x + col + v;
				} else {
					puzzle[row][col] = x + col + v - 9;
				}
				if (puzzle[row][col] == 10) {
					puzzle[row][col] = 1;
				}
			}
			x += 3;
			if (x >= 9) {
				x -= 9;
			}
			if (row == 2) {
				v = 2;
				x = firstVal;
			}
			if (row == 5) {
				v = 3;
				x = firstVal;
			}
		}
	}

	public void shuffleMasks() {
		Random random = new Random();
		int randomRow = -1, randomCol = -1;
		// reset masks
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				if (masks[row][col]) {
					masks[row][col] = false;
				}
			}
		}

		// Set the number of empty cells
		boolean temp;
		int cellsLevel = 0;
		switch (Level) {
		case "Beginner":
			cellsLevel = 4;
			break;
		case "Intermediate":
			cellsLevel = 8;
			break;
		case "Expert":
			cellsLevel = 16;
			break;
		default:
			break;
		}

		// set the empty cells' location
		for (int i = 0; i < cellsLevel; i++) {
			randomRow = random.nextInt(GRID_SIZE);
			randomCol = random.nextInt(GRID_SIZE);
			if (!masks[randomRow][randomCol]) {
				masks[randomRow][randomCol] = true;
			} else {
				i--;
			}
		}

		// shuffle the empty cells' location
		for (int row = 0; row < GRID_SIZE; row++) {
			for (int col = 0; col < GRID_SIZE; col++) {
				randomRow = random.nextInt(GRID_SIZE);
				randomCol = random.nextInt(GRID_SIZE);
				temp = masks[row][col];
				masks[row][col] = masks[randomRow][randomCol];
				masks[randomRow][randomCol] = temp;
			}
		}
	}

	// Status bar, to show number of cells remaining, timer
	JLabel message;
	Timer timer;

	public void statusBar(Container container) {
		JPanel statusBar = new JPanel(new GridLayout(0, 2));
		message = new JLabel(STATUS + numberOfCellsRemaining);
		JLabel showTime = new JLabel();
		timer = new Timer(1000, new ActionListener() {
			int time = 0;
			String mesTimer = "Time: ";

			@Override
			public void actionPerformed(ActionEvent arg0) {
				time++;
				showTime.setText(mesTimer + time + "s");
			}
		});
		timer.setRepeats(true);
		timer.start();

		showTime.setHorizontalAlignment(JLabel.RIGHT);
		statusBar.add(message);
		statusBar.add(showTime);
		container.add(statusBar, BorderLayout.SOUTH);
	}

	// reset game when change game level
	public void switchLevel(String level) {
		newGame = true;
		switch (level) {
		case "Beginner":
			Level = "Beginner";
			// this.dispose();
			// new Sudoku();
			refresh();
			break;
		case "Intermediate":
			Level = "Intermediate";
			// this.dispose();
			// new Sudoku();
			refresh();
			break;
		case "Expert":
			Level = "Expert";
			// this.dispose();
			// new Sudoku();
			refresh();
			break;
		default:
			break;
		}
	}

	// change level in Menu when choose level
	public void contextLevelsMenu() {
		easy.setText("Beginner");
		medium.setText("Intermediate");
		difficult.setText("Expert");
		switch (Level) {
		case "Beginner":
			easy.setText("• Beginner");
			break;
		case "Intermediate":
			medium.setText("• Intermediate");
			break;
		case "Expert":
			difficult.setText("• Expert");
			break;
		default:
			break;
		}
	}

	// get construction in file to string
	public String getConstruction() {
		String line;
		String data = "";
		try (BufferedReader reader = new BufferedReader(new FileReader("construction"))) {
			while ((line = reader.readLine()) != null) {
				data += line + "\n";
			}
		} catch (Exception e) {
			data = e.getMessage();
		}
		return data;
	}

	// show about game
	public void showAbout() {
		String msg = "Chào các bạn, cảm ơn đã dùng thử ứng dụng của mình.\nỨng dụng này mình làm trong quá trình rảnh rỗi, để luyện tập cũng như nâng cao trình độ code của mình.\nNếu có góp ý hay thắc mắc gì liên hệ với mình qua email hoặc fb nhé.\nEmail: namtran4194@gmail.com\nFacebook: fb.com/namtran4194";
		JOptionPane.showMessageDialog(this, msg, "About Sudoku", JOptionPane.INFORMATION_MESSAGE);
	}

	// refresh game component when change game level, select new game, reset game
	public void refresh() {
		container.removeAll();
		getPuzzle(container);
		statusBar(container);
		contextLevelsMenu();
		container.validate();
		container.repaint();
	}

	// handle click menu
	private class MenuListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			switch (command) {
			case "New Game":
				newGame = true;
				// Sudoku.this.dispose();
				// new Sudoku();
				refresh();
				break;
			case "Reset Game":
				newGame = false;
				// Sudoku.this.dispose();
				// new Sudoku();
				refresh();
				break;
			case "Exit":
				System.exit(1);
				break;
			case "Beginner":
				switchLevel("Beginner");
				break;
			case "Intermediate":
				switchLevel("Intermediate");
				break;
			case "Expert":
				switchLevel("Expert");
				break;
			case "Construction":
				JOptionPane.showMessageDialog(Sudoku.this, getConstruction(), "How to play",
						JOptionPane.INFORMATION_MESSAGE);
				break;
			case "About":
				showAbout();
				break;
			default:
				break;
			}
		}

	}

	// handle typing keyboard
	private class InputListener extends KeyAdapter {

		@Override
		public void keyPressed(KeyEvent e) {
			int rowSelected = -1;
			int colSelected = -1;

			// Get the source object that fired the event
			JTextField source = (JTextField) e.getSource();
			// Scan JTextFileds for all rows and columns, and match with the source object
			boolean found = false;
			for (int row = 0; row < GRID_SIZE && !found; row++) {
				for (int col = 0; col < GRID_SIZE && !found; col++) {
					if (tfCells[row][col] == source) {
						rowSelected = row;
						colSelected = col;
						found = true;
					}
				}
			}

			char c = e.getKeyChar();
			int inputNumder = c - '0';
			boolean cellsChange = false;
			if (inputNumder == puzzle[rowSelected][colSelected]) {
				tfCells[rowSelected][colSelected].setText(String.valueOf(c));
				tfCells[rowSelected][colSelected].setBackground(Color.GREEN);
				tfCells[rowSelected][colSelected].setEditable(false);
				tfCells[rowSelected][colSelected].transferFocus();
				masks[rowSelected][colSelected] = false;
				numberOfCellsRemaining--;
				cellsChange = true;
			} else {
				tfCells[rowSelected][colSelected].setBackground(OPEN_CELL_TEXT_NO);
				tfCells[rowSelected][colSelected].setEditable(true);
			}

			if (cellsChange) {
				message.setText(STATUS + numberOfCellsRemaining);
			}

			boolean solved = true;
			for (int row = 0; row < GRID_SIZE; row++) {
				for (int col = 0; col < GRID_SIZE; col++) {
					if (masks[row][col]) {
						solved = false;
						break;
					}
				}
			}

			if (solved) {
				timer.stop();
				JOptionPane.showMessageDialog(Sudoku.this, "Congratulation!", "Notification",
						JOptionPane.INFORMATION_MESSAGE);
				// Sudoku.this.dispose();
				// new Sudoku();
				newGame = true;
				refresh();
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		new Sudoku();
	}

}