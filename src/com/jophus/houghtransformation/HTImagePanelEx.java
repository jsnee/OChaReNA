package com.jophus.houghtransformation;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.border.LineBorder;

public class HTImagePanelEx extends HTImagePanel {
	
    private static final long serialVersionUID = 1L;

    public enum DISPLAY_MODE { NORMAL, INTERACTIVE };

    private DISPLAY_MODE currentMode;
	
    private MovablePoint[] linePoints;
	
    /**
     * Image that is currently assigned to the panel.
     */
    HTImage htimage;

    BufferedImage htInteractiveImage;

    /**
     * Constructor to build panel.
     */
    public HTImagePanelEx()
    {
        super();
        setBorder(new LineBorder(Color.BLACK));
        setBackground(Color.LIGHT_GRAY);

        /* initialize the beginning points */
        linePoints = new MovablePoint[2];
        linePoints[0] = new MovablePoint(this, getWidth() / 10, getHeight() / 10);
        this.add(linePoints[0]);
        linePoints[1] = new MovablePoint(this, getWidth() / 10 * 8, getHeight() / 10 * 8);
        this.add(linePoints[1]);

        htInteractiveImage = null;
        setDisplayMode(DISPLAY_MODE.NORMAL);
    }

    @Override
    public void paintComponent(Graphics g)
    {
    	super.paintComponent(g);
    	
    	if (currentMode == DISPLAY_MODE.NORMAL)
    	{
            if (htimage != null)
            {
                g.drawImage(htimage.getImage(), 1, 1, this.getWidth()-2, this.getHeight()-2, null);
            }
    	}
    	else
    	{
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            g.drawLine(linePoints[0].getMidX(), linePoints[0].getMidY(), 
                       linePoints[1].getMidX(), linePoints[1].getMidY());
    	}
    }
    
    public void setDisplayMode(DISPLAY_MODE newMode)
    {
    	if (newMode == DISPLAY_MODE.NORMAL)
    	{
            linePoints[0].setVisible(false);
            linePoints[1].setVisible(false);
    	}
    	else
    	{
            linePoints[0] = new MovablePoint(this, getWidth() / 10, getHeight() / 10);
            this.add(linePoints[0]);
            linePoints[1] = new MovablePoint(this, getWidth() / 10 * 8, getHeight() / 10 * 8);
            this.add(linePoints[1]);
            linePoints[0].setVisible(true);
            linePoints[1].setVisible(true);
    	}
    	currentMode = newMode;
    	invalidate();
    	repaint();
    }
    
    public HTImage getInteractiveImage() 
    {
        if (htInteractiveImage == null)
        {
            htInteractiveImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        Graphics g = htInteractiveImage.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, htInteractiveImage.getWidth(), htInteractiveImage.getHeight());
        g.setColor(Color.BLACK);
        g.drawLine(linePoints[0].getMidX(), linePoints[0].getMidY(), 
                        linePoints[1].getMidX(), linePoints[1].getMidY());

        return new HTColorImage(htInteractiveImage) ;
    }
}
