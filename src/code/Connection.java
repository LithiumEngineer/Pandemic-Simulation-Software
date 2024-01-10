package code;

import java.awt.Color;
import java.awt.Graphics;

public class Connection {
	int x1, y1, x2, y2;

	public void display(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawLine(x1, y1, x2, y2);
	}
}
