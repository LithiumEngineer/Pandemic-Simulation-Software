package code;

import java.awt.Color;
import java.awt.Graphics;

public class Country {
	int x1, y1, x2, y2;
	boolean holdingConference = false;
	
	public void display(Graphics g) {
		if(!holdingConference)
			g.setColor(new Color(255, 229, 204));
		
		else 
			g.setColor(new Color(173, 71, 250));
		
		g.fillRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));
	}

}
