package com.hp.ilo2.remcons;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import javax.swing.JPanel;


public class dvcwin extends JPanel implements Runnable {
    protected MemoryImageSource image_source;
    protected int screen_x;
    protected int screen_y;
    protected int block_y;
    protected int block_x;
    protected Graphics clearScreenGc;
    public int[] pixel_buffer;
    public remcons remconsObj;
    protected static final int REFRESH_RATE = 15;
    protected Image offscreen_image = null;
    protected Image first_image = null;
    protected Graphics offscreen_gc = null;
    protected Image clearScreenImage = null;
    protected Thread screen_updater = null;
    private int refresh_count = 0;
    private int need_to_refresh = 1;
    private int need_to_refresh_r = 1;
    private int need_to_refresh_w = 1;
    public boolean mirror = false;
    private int frametime = 0;
    private int paint_count = 0;
    protected boolean updater_running = false;
    private boolean abs_dimen_initialized = false;
    private boolean clear_screen = false;
    private boolean firstResize = true;
    protected ColorModel cm = new DirectColorModel(32, 16711680, 65280, (int) telnet.TELNET_IAC, 0);

    public dvcwin(int i, int i2, remcons remconsVar) {
        this.screen_x = i;
        this.screen_y = i2;
        set_framerate(0);
        this.remconsObj = remconsVar;
    }

    public boolean isFocusTraversable() {
        return true;
    }

    public void addNotify() {
        dvcwin.super.addNotify();
        if (this.offscreen_image == null) {
            if (this.screen_x == 0 && this.screen_y == 0) {
                this.screen_x = 1;
                this.screen_y = 1;
            }
            this.offscreen_image = createImage(this.screen_x, this.screen_y);
        }
    }

    public boolean repaint_it(int i) {
        boolean z = false;
        if (i == 1) {
            this.need_to_refresh_w++;
        } else {
            int i2 = this.need_to_refresh_w;
            if (this.need_to_refresh_r != i2) {
                this.need_to_refresh_r = i2;
                z = true;
            }
        }
        return z;
    }

    public void paintComponent(Graphics graphics) {
        dvcwin.super.paintComponents(graphics);
        if (graphics == null) {
            System.out.println("dvcwin.paint() g is null");
        } else if (this.first_image != null) {
            graphics.drawImage(this.first_image, 0, 0, this);
        } else if (this.clearScreenImage != null) {
            graphics.drawImage(this.clearScreenImage, 0, 0, this);
        } else if (this.offscreen_image != null) {
            graphics.drawImage(this.offscreen_image, 0, 0, this);
        }
    }

    public void update(Graphics graphics) {
        if (this.offscreen_image == null || null == this.offscreen_gc) {
            this.offscreen_image = createImage(getSize().width, getSize().height);
            this.offscreen_gc = this.offscreen_image.getGraphics();
        }
        if (this.first_image != null) {
            if (null == this.offscreen_gc) {
                System.out.println("Message from offscreen_gc null detection");
            }
            this.offscreen_gc.drawImage(this.first_image, 0, 0, this);
        }
        graphics.drawImage(this.offscreen_image, 0, 0, this);
    }

    public void paste_array(int[] iArr, int i, int i2, int i3, int i4) {
        int i5;
        if (8 == i4) {
            i5 = 8;
        } else if (i2 + 16 > this.screen_y) {
            i5 = this.screen_y - i2;
        } else {
            i5 = 16;
        }
        for (int i6 = 0; i6 < i5; i6++) {
            try {
                System.arraycopy(iArr, i6 * 16, this.pixel_buffer, ((i2 + i6) * this.screen_x) + i, i3);
            } catch (Exception e) {
                return;
            }
        }
        this.image_source.newPixels(i, i2, i3, 16, false);
    }

    public void set_abs_dimensions(int i, int i2) {
        if (i != this.screen_x || i2 != this.screen_y || false == this.abs_dimen_initialized || this.clear_screen) {
            synchronized (this) {
                this.screen_x = i;
                this.screen_y = i2;
            }
            this.clear_screen = false;
            this.abs_dimen_initialized = true;
            this.offscreen_image = null;
            this.pixel_buffer = new int[this.screen_x * this.screen_y];
            this.image_source = new MemoryImageSource(this.screen_x, this.screen_y, this.cm, this.pixel_buffer, 0, this.screen_x);
            if (this.image_source != null) {
            }
            this.image_source.setAnimated(true);
            this.image_source.setFullBufferUpdates(false);
            this.first_image = createImage(this.image_source);
            invalidate();
            validate();
            Container parent = getParent();
            if (parent != null) {
                while (parent.getParent() != null) {
                    parent.invalidate();
                    parent = parent.getParent();
                }
                parent.invalidate();
                parent.validate();
            }
            System.gc();
            if (this.firstResize) {
                this.firstResize = false;
                this.remconsObj.ParentApp.dispFrame.pack();
                this.remconsObj.ParentApp.dispFrame.setLocationRelativeTo((Component) null);
            }
        }
    }

    public Dimension getPreferredSize() {
        Dimension dimension;
        synchronized (this) {
            dimension = new Dimension(this.screen_x, this.screen_y);
        }
        return dimension;
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void show_text(String str) {
        if (this.screen_updater == null) {
            System.out.println("Screen is no longer updating");
            return;
        }
        System.out.println(new StringBuffer().append("dvcwin:show_text ").append(str).toString());
        if (!(this.screen_x == 640 && this.screen_y == 100)) {
            set_abs_dimensions(640, 100);
            this.image_source = null;
            this.first_image = null;
            this.offscreen_image = null;
            this.offscreen_image = createImage(this.screen_x, this.screen_y);
        }
        if (this.offscreen_image != null) {
            Graphics graphics = this.offscreen_image.getGraphics();
            new Color(0);
            graphics.setColor(Color.black);
            graphics.fillRect(0, 0, this.screen_x, this.screen_y);
            Font font = new Font("Courier", 0, 20);
            new Color(0);
            graphics.setColor(Color.white);
            graphics.setFont(font);
            graphics.drawString(str, 10, 20);
            graphics.drawImage(this.offscreen_image, 0, 0, this);
            graphics.dispose();
            System.gc();
            dvcwin.super.repaint();
        }
    }

    public void set_framerate(int i) {
        if (i > 0) {
            this.frametime = 1000 / i;
        } else {
            this.frametime = 66;
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        while (this.updater_running) {
            try {
                Thread.sleep(this.frametime);
            } catch (InterruptedException e) {
            }
            if (repaint_it(0)) {
                dvcwin.super.repaint();
            }
        }
        System.out.println("Updater finished running");
    }

    public synchronized void start_updates() {
        this.screen_updater = new Thread(this, "dvcwin");
        this.updater_running = true;
        this.screen_updater.start();
        System.out.println("..screen update thread started..");
    }

    public synchronized void stop_updates() {
        System.out.println("dvcwin.stop_update");
        if (this.screen_updater != null && this.screen_updater.isAlive()) {
            this.updater_running = false;
        }
        this.screen_x = 0;
        this.screen_y = 0;
        this.screen_updater = null;
    }

    public void clearScreen() {
        if (this.screen_updater == null) {
            System.out.println("Screen is no longer updating");
            return;
        }
        this.clear_screen = true;
        if (this.screen_x == 0 && this.screen_y == 0) {
            System.out.println("clearScreen() EXCEPTION Screen_x = 0 Screen_y = 0");
            this.screen_x = 1;
            this.screen_y = 1;
        }
        set_abs_dimensions(this.screen_x, this.screen_y);
        this.offscreen_image = null;
        this.offscreen_image = createImage(this.screen_x, this.screen_y);
        Graphics graphics = this.offscreen_image.getGraphics();
        new Color(0);
        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, this.screen_x, this.screen_y);
        graphics.drawImage(this.offscreen_image, 0, 0, this);
        graphics.dispose();
        System.gc();
        dvcwin.super.repaint();
    }
}
