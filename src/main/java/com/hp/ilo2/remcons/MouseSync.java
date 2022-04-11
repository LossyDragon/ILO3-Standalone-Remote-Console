package com.hp.ilo2.remcons;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public class MouseSync implements MouseListener, MouseMotionListener, MouseWheelListener, TimerListener {

    private MouseSyncListener listener;
    private Timer timer;
    private boolean debug_msg = false;
    private boolean dragging;
    private boolean sync_successful;
    private final Object mutex;
    private int client_dx;
    private int client_dy;
    private int client_x;
    private int client_y;
    private int pressed_button;
    private int send_dx_count;
    private int send_dx_index;
    private int send_dx_success;
    private int send_dy_count;
    private int send_dy_index;
    private int send_dy_success;
    private int server_h;
    private int server_w;
    private int server_x;
    private int server_y;
    private int state = 0;
    private int[] recv_dx;
    private int[] recv_dy;
    private int[] send_dx;
    private int[] send_dy;
    private static final int CMD_ALIGN = 14;
    private static final int CMD_CLICK = 7;
    private static final int CMD_DRAG = 12;
    private static final int CMD_ENTER = 8;
    private static final int CMD_EXIT = 9;
    private static final int CMD_MOVE = 13;
    private static final int CMD_PRESS = 10;
    private static final int CMD_RELEASE = 11;
    private static final int CMD_SERVER_DISABLE = 5;
    private static final int CMD_SERVER_MOVE = 3;
    private static final int CMD_SERVER_SCREEN = 4;
    private static final int CMD_START = 0;
    private static final int CMD_STOP = 1;
    private static final int CMD_SYNC = 2;
    private static final int CMD_TIMEOUT = 6;
    private static final int STATE_DISABLE = 3;
    private static final int STATE_ENABLE = 2;
    private static final int STATE_INIT = 0;
    private static final int STATE_SYNC = 1;
    private static final int SYNC_FAIL_COUNT = 4;
    private static final int SYNC_SUCCESS_COUNT = 2;
    private static final int TIMEOUT_DELAY = 5;
    private static final int TIMEOUT_MOVE = 200;
    private static final int TIMEOUT_SYNC = 2000;
    public static final int MOUSE_BUTTON_CENTER = 2;
    public static final int MOUSE_BUTTON_LEFT = 4;
    public static final int MOUSE_BUTTON_RIGHT = 1;

    public MouseSync(Object obj) {
        this.mutex = obj;
        state_machine(0, null, 0, 0);
    }

    public void setListener(MouseSyncListener mouseSyncListener) {
        this.listener = mouseSyncListener;
    }

    public void enableDebug() {
        this.debug_msg = true;
    }

    public void disableDebug() {
        this.debug_msg = false;
    }

    public void restart() {
        go_state(0);
    }

    public void align() {
        state_machine(CMD_ALIGN, null, 0, 0);
    }

    public void sync() {
        state_machine(2, null, 0, 0);
    }

    public void serverMoved(int i, int i2, int i3, int i4) {
        state_machine(3, null, i, i2);
    }

    public void serverScreen(int i, int i2) {
        state_machine(4, null, i, i2);
    }

    public void serverDisabled() {
        state_machine(5, null, 0, 0);
    }

    @Override
    public void timeout(Object obj) {
        state_machine(6, null, 0, 0);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        this.listener.requestScreenFocus(mouseEvent);
        this.listener.sendMouse(mouseEvent);
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        this.listener.installKeyboardHook();
    }

    public void mouseExited(MouseEvent mouseEvent) {
        this.listener.unInstallKeyboardHook();
    }

    public void mousePressed(MouseEvent mouseEvent) {
        this.listener.sendMouse(mouseEvent);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        this.listener.sendMouse(mouseEvent);
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        this.listener.sendMouse(mouseEvent);
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        this.listener.sendMouse(mouseEvent);
    }

    private void move_delay() {
        try {
            Thread.sleep(5L);
        } catch (InterruptedException ignored) {
        }
    }

    private void sync_default() {
        int[] iArr = {1, 4, 6, 8, 12, 16, 32, 64};
        this.send_dx = new int[iArr.length];
        this.send_dy = new int[iArr.length];
        this.recv_dx = new int[iArr.length];
        this.recv_dy = new int[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            this.send_dx[i] = iArr[i];
            this.send_dy[i] = iArr[i];
            this.recv_dx[i] = iArr[i];
            this.recv_dy[i] = iArr[i];
        }
        this.send_dx_index = 0;
        this.send_dy_index = 0;
        this.send_dx_count = 0;
        this.send_dy_count = 0;
        this.send_dx_success = 0;
        this.send_dy_success = 0;
        this.sync_successful = false;
    }

    private void sync_continue() {
        int i = 1;
        int i2 = 1;
        int i3 = 0;
        int i4 = 0;
        if (this.server_x > this.server_w / 2) {
            i = -1;
        }
        if (this.server_y < this.server_h / 2) {
            i2 = -1;
        }
        if (this.send_dx_index >= 0) {
            i3 = i * this.send_dx[this.send_dx_index];
        }
        if (this.send_dy_index >= 0) {
            i4 = i2 * this.send_dy[this.send_dy_index];
        }
        this.listener.serverMove(i3, i4, this.client_x, this.client_y);
        this.timer.start();
    }

    private void sync_update(int i, int i2) {
        this.timer.pause();
        int i3 = i - this.server_x;
        int i4 = this.server_y - i2;
        this.server_x = i;
        this.server_y = i2;
        if (i3 < 0) {
            i3 = -i3;
        }
        if (i4 < 0) {
            i4 = -i4;
        }
        if (this.send_dx_index >= 0) {
            if (this.recv_dx[this.send_dx_index] == i3) {
                this.send_dx_success++;
            }
            this.recv_dx[this.send_dx_index] = i3;
            this.send_dx_count++;
            if (this.send_dx_success >= 2) {
                this.send_dx_index--;
                this.send_dx_success = 0;
                this.send_dx_count = 0;
            } else if (this.send_dx_count >= 4) {
                if (this.debug_msg) {
                    System.out.println("no x sync:" + this.send_dx[this.send_dx_index]);
                }
                go_state(2);
                return;
            }
        }
        if (this.send_dy_index >= 0) {
            if (this.recv_dy[this.send_dy_index] == i4) {
                this.send_dy_success++;
            }
            this.recv_dy[this.send_dy_index] = i4;
            this.send_dy_count++;
            if (this.send_dy_success >= 2) {
                this.send_dy_index--;
                this.send_dy_success = 0;
                this.send_dy_count = 0;
            } else if (this.send_dy_count >= 4) {
                if (this.debug_msg) {
                    System.out.println("no y sync:" + this.send_dy[this.send_dy_index]);
                }
                go_state(2);
                return;
            }
        }
        if (this.send_dx_index >= 0 || this.send_dy_index >= 0) {
            sync_continue();
            return;
        }
        for (int length = this.send_dx.length - 1; length >= 0; length--) {
            if (this.recv_dx[length] == 0 || this.recv_dy[length] == 0) {
                if (this.debug_msg) {
                }
                go_state(2);
                return;
            } else if (length != 0 && (this.recv_dx[length] < this.recv_dx[length - 1] || this.recv_dy[length] < this.recv_dy[length - 1])) {
                if (this.debug_msg) {
                }
                go_state(2);
                return;
            }
        }
        this.sync_successful = true;
        this.send_dx_index = 0;
        this.send_dy_index = 0;
        go_state(2);
    }

    private void init_vars() {
        this.server_w = 640;
        this.server_h = 480;
        this.server_x = 0;
        this.server_y = 0;
        this.client_x = 0;
        this.client_y = 0;
        this.client_dx = 0;
        this.client_dy = 0;
        this.pressed_button = 0;
        this.dragging = false;
        sync_default();
    }

    private void move_server(boolean z, boolean z2) {
        int i;
        int i2;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        this.timer.pause();
        int i7 = this.client_dx;
        int i8 = this.client_dy;
        if (i7 >= 0) {
            i = 1;
        } else {
            i = -1;
            i7 = -i7;
        }
        if (i8 >= 0) {
            i2 = 1;
        } else {
            i2 = -1;
            i8 = -i8;
        }
        while (true) {
            if (i7 != 0) {
                int length = this.send_dx.length - 1;
                while (true) {
                    if (length < this.send_dx_index) {
                        break;
                    } else if (this.recv_dx[length] <= i7) {
                        i3 = i * this.send_dx[length];
                        i5 += this.recv_dx[length];
                        i7 -= this.recv_dx[length];
                        break;
                    } else {
                        length--;
                    }
                }
                if (length < this.send_dx_index) {
                    i3 = 0;
                    i5 += i7;
                    i7 = 0;
                }
            } else {
                i3 = 0;
            }
            if (i8 != 0) {
                int length2 = this.send_dy.length - 1;
                while (true) {
                    if (length2 < this.send_dy_index) {
                        break;
                    } else if (this.recv_dy[length2] <= i8) {
                        i4 = i2 * this.send_dy[length2];
                        i6 += this.recv_dy[length2];
                        i8 -= this.recv_dy[length2];
                        break;
                    } else {
                        length2--;
                    }
                }
                if (length2 < this.send_dy_index) {
                    i4 = 0;
                    i6 += i8;
                    i8 = 0;
                }
            } else {
                i4 = 0;
            }
            if (!(i3 == 0 && i4 == 0)) {
                this.listener.serverMove(i3, i4, this.client_x, this.client_y);
            }
            if (!z || (i7 == 0 && i8 == 0)) {
                break;
            }
        }
        this.client_dx -= i * i5;
        this.client_dy -= i2 * i6;
        if (!z2) {
            this.server_x += i * i5;
            this.server_y -= i2 * i6;
            if (this.debug_msg) {
            }
        }
        if (this.client_dx != 0 || this.client_dy != 0) {
            this.timer.start();
        }
    }

    private void go_state(int i) {
        synchronized (this.mutex) {
            state_machine(1, null, 0, 0);
            this.state = i;
            state_machine(0, null, 0, 0);
        }
    }

    private void state_machine(int i, MouseEvent mouseEvent, int i2, int i3) {
        synchronized (this.mutex) {
            switch (this.state) {
                case 0:
                    state_init(i, mouseEvent, i2, i3);
                    break;
                case 1:
                    state_sync(i, mouseEvent, i2, i3);
                    break;
                case 2:
                    state_enable(i, mouseEvent, i2, i3);
                    break;
                case 3:
                    state_disable(i, mouseEvent, i2, i3);
                    break;
            }
        }
    }

    private void state_init(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                init_vars();
                go_state(3);
                return;
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case CMD_ALIGN :
            default:
        }
    }

    private void state_sync(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                this.timer = new Timer(TIMEOUT_SYNC, false, this.mutex);
                this.timer.setListener(this, null);
                sync_default();
                this.send_dx_index = this.send_dx.length - 1;
                this.send_dy_index = this.send_dy.length - 1;
                sync_continue();
                return;
            case 1:
                this.timer.stop();
                this.timer = null;
                if (!this.sync_successful) {
                    if (this.debug_msg) {
                        System.out.println("fail");
                    }
                    sync_default();
                } else if (this.debug_msg) {
                }
                if (this.debug_msg) {
                    for (int i4 = 0; i4 < this.send_dx.length; i4++) {
                    }
                    for (int i5 = 0; i5 < this.send_dx.length; i5++) {
                    }
                    return;
                }
                return;
            case 2:
                go_state(1);
                return;
            case 3:
                if (i2 > TIMEOUT_SYNC || i3 > TIMEOUT_SYNC) {
                    go_state(3);
                } else {
                    sync_update(i2, i3);
                }
                return;
            case 4:
                this.server_w = i2;
                this.server_h = i3;
                return;
            case 5:
                go_state(3);
                return;
            case 6:
                go_state(2);
                return;
            case 7:
            case 10:
            case 11:
            case CMD_ALIGN :
            default:
                return;
            case 8:
            case 9:
            case 12:
            case 13:
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
        }
    }

    private void state_enable(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                if (this.debug_msg) {
                }
                this.timer = new Timer(TIMEOUT_MOVE, false, this.mutex);
                this.timer.setListener(this, null);
                return;
            case 1:
                this.timer.stop();
                this.timer = null;
                return;
            case 2:
                go_state(1);
                return;
            case 3:
                if (this.debug_msg) {
                }
                if (i2 > TIMEOUT_SYNC || i3 > TIMEOUT_SYNC) {
                    go_state(3);
                    return;
                }
                this.server_x = i2;
                this.server_y = i3;
                return;
            case 4:
                this.server_w = i2;
                this.server_h = i3;
                return;
            case 5:
                go_state(3);
                return;
            case 6:
                move_server(true, true);
                return;
            case 7:
                if (this.dragging) {
                    return;
                }
                if ((mouseEvent.getModifiers() & 16) != 0) {
                    this.listener.serverClick(4, 1);
                    return;
                } else if ((mouseEvent.getModifiers() & 8) != 0) {
                    this.listener.serverClick(2, 1);
                    return;
                } else if ((mouseEvent.getModifiers() & 4) != 0) {
                    this.listener.serverClick(1, 1);
                    return;
                } else {
                    return;
                }
            case 8:
            case 9:
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
                if (this.client_x < 0) {
                    this.client_x = 0;
                }
                if (this.client_x > this.server_w) {
                    this.client_x = this.server_w;
                }
                if (this.client_y < 0) {
                    this.client_y = 0;
                }
                if (this.client_y > this.server_h) {
                    this.client_y = this.server_h;
                }
                if (this.debug_msg) {
                }
                if (this.pressed_button != 1 && (mouseEvent.getModifiers() & 2) == 0) {
                    align();
                    return;
                }
                return;
            case 10:
                if (this.pressed_button == 0) {
                    if ((mouseEvent.getModifiers() & 4) != 0) {
                        this.pressed_button = 1;
                    } else if ((mouseEvent.getModifiers() & 8) != 0) {
                        this.pressed_button = 2;
                    } else {
                        this.pressed_button = 4;
                    }
                    this.dragging = false;
                    return;
                }
                return;
            case 11:
                if (this.pressed_button == -4) {
                    this.listener.serverRelease(4);
                } else if (this.pressed_button == -2) {
                    this.listener.serverRelease(2);
                } else if (this.pressed_button == -1) {
                    this.listener.serverRelease(1);
                }
                this.pressed_button = 0;
                return;
            case 12:
                if (this.pressed_button != 1) {
                    if (this.pressed_button > 0) {
                        this.pressed_button = -this.pressed_button;
                        this.listener.serverPress(this.pressed_button);
                    }
                    this.client_dx += mouseEvent.getX() - this.client_x;
                    this.client_dy += this.client_y - mouseEvent.getY();
                    move_server(false, true);
                }
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
                if (this.debug_msg) {
                }
                this.dragging = true;
                return;
            case 13:
                if ((mouseEvent.getModifiers() & 2) == 0) {
                    this.client_dx += mouseEvent.getX() - this.client_x;
                    this.client_dy += this.client_y - mouseEvent.getY();
                    move_server(false, true);
                }
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
                if (this.debug_msg) {
                }
                return;
            case CMD_ALIGN :
                this.client_dx = this.client_x - this.server_x;
                this.client_dy = this.server_y - this.client_y;
                move_server(true, true);
                return;
            default:
        }
    }

    private void state_disable(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                if (this.debug_msg) {
                }
                this.timer = new Timer(TIMEOUT_MOVE, false, this.mutex);
                this.timer.setListener(this, null);
                return;
            case 1:
                this.timer.stop();
                this.timer = null;
                return;
            case 2:
                sync_default();
                return;
            case 3:
                if (this.debug_msg) {
                }
                if (i2 < TIMEOUT_SYNC && i3 < TIMEOUT_SYNC) {
                    this.server_x = i2;
                    this.server_y = i3;
                    go_state(2);
                    return;
                }
                return;
            case 4:
                this.server_w = i2;
                this.server_h = i3;
                return;
            case 5:
            default:
                return;
            case 6:
                move_server(true, false);
                return;
            case 7:
                if (this.dragging) {
                    return;
                }
                if ((mouseEvent.getModifiers() & 16) != 0) {
                    this.listener.serverClick(4, 1);
                    return;
                } else if ((mouseEvent.getModifiers() & 8) != 0) {
                    this.listener.serverClick(2, 1);
                    return;
                } else if ((mouseEvent.getModifiers() & 4) != 0) {
                    this.listener.serverClick(1, 1);
                    return;
                } else {
                    return;
                }
            case 8:
            case 9:
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
                if (this.client_x < 0) {
                    this.client_x = 0;
                }
                if (this.client_x > this.server_w) {
                    this.client_x = this.server_w;
                }
                if (this.client_y < 0) {
                    this.client_y = 0;
                }
                if (this.client_y > this.server_h) {
                    this.client_y = this.server_h;
                }
                if (this.debug_msg) {
                }
                if (this.pressed_button != 1 && (mouseEvent.getModifiers() & 2) == 0) {
                    align();
                    return;
                }
                return;
            case 10:
                if (this.pressed_button == 0) {
                    if ((mouseEvent.getModifiers() & 4) != 0) {
                        this.pressed_button = 1;
                    } else if ((mouseEvent.getModifiers() & 8) != 0) {
                        this.pressed_button = 2;
                    } else {
                        this.pressed_button = 4;
                    }
                    this.dragging = false;
                    return;
                }
                return;
            case 11:
                if (this.pressed_button == -4) {
                    this.listener.serverRelease(4);
                } else if (this.pressed_button == -2) {
                    this.listener.serverRelease(2);
                } else if (this.pressed_button == -1) {
                    this.listener.serverRelease(1);
                }
                this.pressed_button = 0;
                return;
            case 12:
                if (this.pressed_button != 1) {
                    if (this.pressed_button > 0) {
                        this.pressed_button = -this.pressed_button;
                        this.listener.serverPress(this.pressed_button);
                    }
                    this.client_dx += mouseEvent.getX() - this.client_x;
                    this.client_dy += this.client_y - mouseEvent.getY();
                    move_server(false, false);
                } else {
                    this.server_x = mouseEvent.getX();
                    this.server_y = mouseEvent.getY();
                }
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
                if (this.debug_msg) {
                }
                this.dragging = true;
                return;
            case 13:
                if ((mouseEvent.getModifiers() & 2) == 0) {
                    this.client_dx += mouseEvent.getX() - this.client_x;
                    this.client_dy += this.client_y - mouseEvent.getY();
                    move_server(false, false);
                } else {
                    this.server_x = mouseEvent.getX();
                    this.server_y = mouseEvent.getY();
                }
                this.client_x = mouseEvent.getX();
                this.client_y = mouseEvent.getY();
                if (this.debug_msg) {
                }
                return;
            case CMD_ALIGN :
                this.client_dx = this.client_x - this.server_x;
                this.client_dy = this.server_y - this.client_y;
                move_server(true, false);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
    }
}
