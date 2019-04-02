package org.subdivision.tool;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import org.subdivision.Image;

@SuppressWarnings("serial")
public class ImageDisplayComponent extends JComponent {

	private Image image;
	
	public ImageDisplayComponent(Image image) {
		this.setImage(image);
		setPreferredSize(new Dimension(image.width, image.height));
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.drawImage(getImage().output, 0, 0, this);
	}

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

}