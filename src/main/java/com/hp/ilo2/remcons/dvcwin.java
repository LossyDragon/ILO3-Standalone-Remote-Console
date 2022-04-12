package com.hp.ilo2.remcons;

import java.awt.Color;
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

    private boolean abs_dimen_initialized = false;
    private boolean clear_screen = false;
    private boolean firstResize = true;
    private int frametime = 0;
    private int need_to_refresh_r = 1;
    private int need_to_refresh_w = 1;
    protected ColorModel cm = new DirectColorModel(32, 16711680, 65280, telnet.TELNET_IAC, 0);
    protected Graphics offscreen_gc = null;
    protected Image clearScreenImage = null;
    protected Image first_image = null;
    protected Image offscreen_image = null;
    protected MemoryImageSource image_source;
    protected Thread screen_updater = null;
    protected boolean updater_running = false;
    protected int screen_x;
    protected int screen_y;
    public int[] pixel_buffer;
    public remcons remconsObj;

    public dvcwin(int i, int i2, remcons remconsVar) {
        screen_x = i;
        screen_y = i2;
        set_framerate(0);
        remconsObj = remconsVar;
    }

    public void addNotify() {
        dvcwin.super.addNotify();

        if (offscreen_image == null) {
            if (screen_x == 0 && screen_y == 0) {
                screen_x = 1;
                screen_y = 1;
            }

            offscreen_image = createImage(screen_x, screen_y);
        }
    }

    public boolean repaint_it(int i) {
        boolean z = false;

        if (i == 1) {
            need_to_refresh_w++;
        } else {
            int i2 = need_to_refresh_w;

            if (need_to_refresh_r != i2) {
                need_to_refresh_r = i2;

                z = true;
            }
        }

        return z;
    }

    public void paintComponent(Graphics graphics) {
        dvcwin.super.paintComponents(graphics);

        if (graphics == null) {
            System.out.println("dvcwin.paint() g is null");
        } else if (first_image != null) {
            graphics.drawImage(first_image, 0, 0, this);
        } else if (clearScreenImage != null) {
            graphics.drawImage(clearScreenImage, 0, 0, this);
        } else if (offscreen_image != null) {
            graphics.drawImage(offscreen_image, 0, 0, this);
        }
    }

    public void update(Graphics graphics) {
        if (offscreen_image == null || null == offscreen_gc) {
            offscreen_image = createImage(getSize().width, getSize().height);
            offscreen_gc = offscreen_image.getGraphics();
        }

        if (first_image != null) {
            if (null == offscreen_gc) {
                System.out.println("Message from offscreen_gc null detection");
            }

            offscreen_gc.drawImage(first_image, 0, 0, this);
        }

        graphics.drawImage(offscreen_image, 0, 0, this);
    }

    public void paste_array(int[] iArr, int i, int i2, int i3, int i4) {
        int i5;

        if (8 == i4) {
            i5 = 8;
        } else if (i2 + 16 > screen_y) {
            i5 = screen_y - i2;
        } else {
            i5 = 16;
        }

        for (int i6 = 0; i6 < i5; i6++) {
            try {
                System.arraycopy(iArr, i6 * 16, pixel_buffer, ((i2 + i6) * screen_x) + i, i3);
            } catch (Exception e) {
                return;
            }
        }

        image_source.newPixels(i, i2, i3, 16, false);
    }

    public void set_abs_dimensions(int i, int i2) {
        if (i != screen_x || i2 != screen_y || !abs_dimen_initialized || clear_screen) {
            synchronized (this) {
                screen_x = i;
                screen_y = i2;
            }

            clear_screen = false;
            abs_dimen_initialized = true;
            offscreen_image = null;
            pixel_buffer = new int[screen_x * screen_y];

            image_source = new MemoryImageSource(screen_x, screen_y, cm, pixel_buffer, 0, screen_x);
            image_source.setAnimated(true);
            image_source.setFullBufferUpdates(false);

            first_image = createImage(image_source);

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

            if (firstResize) {
                firstResize = false;
                remconsObj.ParentApp.dispFrame.pack();
                remconsObj.ParentApp.dispFrame.setLocationRelativeTo(null);
            }
        }
    }

    public Dimension getPreferredSize() {
        Dimension dimension;

        synchronized (this) {
            dimension = new Dimension(screen_x, screen_y);
        }

        return dimension;
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void show_text(String str) {
        if (screen_updater == null) {
            System.out.println("Screen is no longer updating");
            return;
        }

        System.out.println("dvcwin:show_text " + str);

        if (!(screen_x == 640 && screen_y == 100)) {
            set_abs_dimensions(640, 100);
            image_source = null;
            first_image = null;
            offscreen_image = null;
            offscreen_image = createImage(screen_x, screen_y);
        }

        if (offscreen_image != null) {
            Graphics graphics = offscreen_image.getGraphics();
            Font font = new Font("Courier", Font.PLAIN, 20);

            graphics.setColor(Color.black);
            graphics.fillRect(0, 0, screen_x, screen_y);
            graphics.setColor(Color.white);
            graphics.setFont(font);
            graphics.drawString(str, 10, 20);
            graphics.drawImage(offscreen_image, 0, 0, this);
            graphics.dispose();

            System.gc();

            dvcwin.super.repaint();
        }
    }

    public void set_framerate(int i) {
        if (i > 0) {
            frametime = 1000 / i;
        } else {
            frametime = 66;
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        while (updater_running) {
            try {
                Thread.sleep(frametime);
            } catch (InterruptedException ignored) {
            }

            if (repaint_it(0)) {
                dvcwin.super.repaint();
            }
        }

        System.out.println("Updater finished running");
    }

    public synchronized void start_updates() {
        screen_updater = new Thread(this, "dvcwin");
        updater_running = true;

        screen_updater.start();

        System.out.println("..screen update thread started..");
    }

    public synchronized void stop_updates() {
        System.out.println("dvcwin.stop_update");

        if (screen_updater != null && screen_updater.isAlive()) {
            updater_running = false;
        }

        screen_x = 0;
        screen_y = 0;
        screen_updater = null;
    }

    public void clearScreen() {
        if (screen_updater == null) {
            System.out.println("Screen is no longer updating");
            return;
        }

        clear_screen = true;

        if (screen_x == 0 && screen_y == 0) {
            System.out.println("clearScreen() EXCEPTION Screen_x = 0 Screen_y = 0");
            screen_x = 1;
            screen_y = 1;
        }

        set_abs_dimensions(screen_x, screen_y);

        offscreen_image = null;
        offscreen_image = createImage(screen_x, screen_y);

        Graphics graphics = offscreen_image.getGraphics();

        graphics.setColor(Color.black);
        graphics.fillRect(0, 0, screen_x, screen_y);
        graphics.drawImage(offscreen_image, 0, 0, this);
        graphics.dispose();

        System.gc();
        dvcwin.super.repaint();
    }
}
