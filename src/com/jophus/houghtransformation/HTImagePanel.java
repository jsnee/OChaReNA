package com.jophus.houghtransformation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;


/**
 * Extends JPanel to automatically draw the assigned HTImage.
 * 
 * @author SUN2K
 *
 */
public class HTImagePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Image that is currently assigned to the panel.
     */
    HTImage htimage;

    /**
     * Constructor to build panel.
     */
    public HTImagePanel()
    {
        super();
        setBorder(new LineBorder(Color.BLACK));
        setBackground(Color.LIGHT_GRAY);
    }

    /**
     * Assign a new image to the panel.
     * @param image new image
     * @throws IllegalStateException if image is null
     */
    public void setImage(HTImage image)
    {
        if (image == null)
        {
                throw new IllegalStateException();
        }
        this.htimage = image;
    }
	    
    @Override
    public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
    	if (htimage != null)
    	{
            g.drawImage(htimage.getImage(), 1, 1, this.getWidth()-2, this.getHeight()-2, null);
    	}
    }
    
    public HTImage getImage()
    {
    	return htimage;
    }
    
    public BufferedImage getDrawnImage()
    {
    	BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), 
    			htimage.getImage().getType());
    	img.getGraphics().drawImage(htimage.getImage(), 0, 0, this.getWidth()-1, this.getHeight()-1, null);
    	return img;
    }
    
}
