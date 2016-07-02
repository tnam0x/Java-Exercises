package graphics;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import adapters.HighScore;
import adapters.Player;

public class TopPlayer extends JDialog {
	private static final long serialVersionUID = 1L;

	public static final int WIDTH = 150;
	public static final int HEIGH = 30;
	public static final int CANVAS_WIDTH = WIDTH;
	public static final int CANVAS_HEIGH = HEIGH * 10;

	public Container container;
	public HighScore hScore = new HighScore();
	public Image icon;

	/**
	 * Create the dialog.
	 */
	public TopPlayer() {
		container = getContentPane();
		container.setLayout(new BorderLayout());
		setTopPlayer();
		try {
			icon = ImageIO.read(ClassLoader.getSystemResourceAsStream("star.png"));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString(), "High Score", JOptionPane.ERROR_MESSAGE);
			dispose();
		}
		JButton reset = new JButton("Reset high score");
		container.add(reset, BorderLayout.SOUTH);
		setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGH));
		pack();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setTitle("High Score");
		setIconImage(icon);
		setVisible(true);
	}

	public void setTopPlayer() {
		JPanel top = new JPanel(new GridLayout(10, 1), true);
		JLabel row;
		String line;
		List<Player> list = hScore.getListTopPlayer();
		for (int i = 0; i < list.size(); i++) {
			Player player = list.get(i);
			line = (i + 1) + ". " + player.getName() + ": " + player.getScore();
			row = new JLabel(line);
			top.add(row);
		}
		container.add(top, BorderLayout.CENTER);
	}

}
