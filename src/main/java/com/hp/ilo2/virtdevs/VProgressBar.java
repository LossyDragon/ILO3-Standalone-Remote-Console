package com.hp.ilo2.virtdevs;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;


public class VProgressBar extends Canvas {
    private int progressWidth;
    private int progressHeight;
    private float percentage;
    private Image offscreenImg;
    private Graphics offscreenG;
    private Color progressColor;
    private Color progressBackground;

    public VProgressBar(int i, int i2) {
        this.progressColor = Color.red;
        this.progressBackground = Color.white;
        setFont(new Font("Dialog", 0, 15));
        this.progressWidth = i;
        this.progressHeight = i2;
        setSize(i, i2);
    }

    public VProgressBar(int i, int i2, Color color, Color color2, Color color3) {
        this.progressColor = Color.red;
        this.progressBackground = Color.white;
        setFont(new Font("Dialog", 0, 12));
        this.progressWidth = i;
        this.progressHeight = i2;
        this.progressColor = color2;
        this.progressBackground = color3;
        setSize(i, i2);
        setBackground(color);
    }

    public void updateBar(float f) {
        this.percentage = f;
        repaint();
    }

    public void setCanvasColor(Color color) {
        setBackground(color);
    }

    public void setProgressColor(Color color) {
        this.progressColor = color;
    }

    public void setBackGroundColor(Color color) {
        this.progressBackground = color;
    }

    public void paint(Graphics graphics) {
        if (this.offscreenImg == null) {
            this.offscreenImg = createImage(this.progressWidth - 4, this.progressHeight - 4);
        }
        this.offscreenG = this.offscreenImg.getGraphics();
        int width = this.offscreenImg.getWidth(this);
        int height = this.offscreenImg.getHeight(this);
        this.offscreenG.setColor(this.progressBackground);
        this.offscreenG.fillRect(0, 0, width, height);
        this.offscreenG.setColor(this.progressColor);
        this.offscreenG.fillRect(0, 0, (int) (width * this.percentage), height);
        this.offscreenG.drawString(new StringBuffer().append(Integer.toString((int) (this.percentage * 100.0f))).append("%").toString(), (width / 2) - 8, (height / 2) + 5);
        this.offscreenG.clipRect(0, 0, (int) (width * this.percentage), height);
        this.offscreenG.setColor(this.progressBackground);
        this.offscreenG.drawString(new StringBuffer().append(Integer.toString((int) (this.percentage * 100.0f))).append("%").toString(), (width / 2) - 8, (height / 2) + 5);
        graphics.setColor(this.progressBackground);
        graphics.draw3DRect((getSize().width / 2) - (this.progressWidth / 2), 0, this.progressWidth - 1, this.progressHeight - 1, false);
        graphics.drawImage(this.offscreenImg, 4 / 2, 4 / 2, this);
    }

    public void update(Graphics graphics) {
        paint(graphics);
    }
}
