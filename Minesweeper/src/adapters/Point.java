package adapters;

/**
 * Use to save a cells' location in Minesweeper game
 */
public class Point {
	private int rowSelected;
	private int colSelected;

	public Point(int rowSelected, int colSelected) {
		this.rowSelected = rowSelected;
		this.colSelected = colSelected;
	}

	public int getRowSelected() {
		return rowSelected;
	}

	public void setRowSelected(int rowSelected) {
		this.rowSelected = rowSelected;
	}

	public int getColSelected() {
		return colSelected;
	}

	public void setColSelected(int colSelected) {
		this.colSelected = colSelected;
	}
}
