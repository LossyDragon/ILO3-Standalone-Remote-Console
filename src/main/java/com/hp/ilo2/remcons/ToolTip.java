package com.hp.ilo2.remcons;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JPanel;


public class ToolTip extends JPanel implements MouseListener {
    protected String tip;
    protected Component owner;
    private Container mainContainer;
    private LayoutManager mainLayout;
    private boolean shown;
    private final int VERTICAL_OFFSET = 10;
    private final int HORIZONTAL_ENLARGE = 10;

    public ToolTip(String str, Component component) {
        this.tip = str;
        this.owner = component;
        component.addMouseListener(this);
        setBackground(new Color((int) telnet.TELNET_IAC, (int) telnet.TELNET_IAC, 220));
    }

    public void paint(Graphics graphics) {
        graphics.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
        graphics.drawString(this.tip, 3, getSize().height - 3);
    }

    private void addToolTip() {
        this.mainContainer.setLayout((LayoutManager) null);
        FontMetrics fontMetrics = getFontMetrics(this.owner.getFont());
        setSize(fontMetrics.stringWidth(this.tip) + 10, fontMetrics.getHeight());
        setLocation(this.owner.getLocationOnScreen().x - this.mainContainer.getLocationOnScreen().x, (this.owner.getLocationOnScreen().y - this.mainContainer.getLocationOnScreen().y) - 10);
        if (this.mainContainer.getSize().width < getLocation().x + getSize().width) {
            setLocation(this.mainContainer.getSize().width - (getSize().width + 10), getLocation().y - 10);
        }
        this.mainContainer.add(this, 0);
        this.mainContainer.validate();
        repaint();
        this.shown = true;
    }

    private void removeToolTip() {
        if (this.shown) {
            this.mainContainer.remove(0);
            this.mainContainer.setLayout(this.mainLayout);
            this.mainContainer.validate();
        }
        this.shown = false;
    }

    private void findMainContainer() {
        Container parent = this.owner.getParent();
        while (!(parent instanceof Applet) && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        this.mainContainer = parent;
        this.mainLayout = this.mainContainer.getLayout();
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        findMainContainer();
        addToolTip();
    }

    public void mouseExited(MouseEvent mouseEvent) {
        removeToolTip();
    }

    public void mousePressed(MouseEvent mouseEvent) {
        removeToolTip();
    }

    public void mouseClicked(MouseEvent mouseEvent) {
    }

    public void mouseReleased(MouseEvent mouseEvent) {
    }
}
