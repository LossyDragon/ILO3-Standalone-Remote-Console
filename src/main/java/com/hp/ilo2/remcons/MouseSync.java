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
    private static final int TIMEOUT_MOVE = 200;
    private static final int TIMEOUT_SYNC = 2000;

    public MouseSync(Object obj) {
        mutex = obj;
        state_machine(0, null, 0, 0);
    }

    public void setListener(MouseSyncListener mouseSyncListener) {
        listener = mouseSyncListener;
    }

    public void enableDebug() {
        debug_msg = true;
    }

    public void disableDebug() {
        debug_msg = false;
    }

    public void restart() {
        go_state(0);
    }

    public void align() {
        state_machine(CMD_ALIGN, null, 0, 0);
    }

    public void serverScreen(int i, int i2) {
        state_machine(4, null, i, i2);
    }

    @Override
    public void timeout(Object obj) {
        state_machine(6, null, 0, 0);
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        listener.requestScreenFocus(mouseEvent);
        listener.sendMouse(mouseEvent);
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        listener.installKeyboardHook();
    }

    public void mouseExited(MouseEvent mouseEvent) {
        listener.unInstallKeyboardHook();
    }

    public void mousePressed(MouseEvent mouseEvent) {
        listener.sendMouse(mouseEvent);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        listener.sendMouse(mouseEvent);
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        listener.sendMouse(mouseEvent);
    }

    public void mouseMoved(MouseEvent mouseEvent) {
        listener.sendMouse(mouseEvent);
    }

    private void sync_default() {
        int[] iArr = {1, 4, 6, 8, 12, 16, 32, 64};
        send_dx = new int[iArr.length];
        send_dy = new int[iArr.length];
        recv_dx = new int[iArr.length];
        recv_dy = new int[iArr.length];
        for (int i = 0; i < iArr.length; i++) {
            send_dx[i] = iArr[i];
            send_dy[i] = iArr[i];
            recv_dx[i] = iArr[i];
            recv_dy[i] = iArr[i];
        }
        send_dx_index = 0;
        send_dy_index = 0;
        send_dx_count = 0;
        send_dy_count = 0;
        send_dx_success = 0;
        send_dy_success = 0;
        sync_successful = false;
    }

    private void sync_continue() {
        int i = 1;
        int i2 = 1;
        int i3 = 0;
        int i4 = 0;
        if (server_x > server_w / 2) {
            i = -1;
        }
        if (server_y < server_h / 2) {
            i2 = -1;
        }
        if (send_dx_index >= 0) {
            i3 = i * send_dx[send_dx_index];
        }
        if (send_dy_index >= 0) {
            i4 = i2 * send_dy[send_dy_index];
        }
        listener.serverMove(i3, i4, client_x, client_y);
        timer.start();
    }

    private void sync_update(int i, int i2) {
        int i3 = i - server_x;
        int i4 = server_y - i2;
        server_x = i;
        server_y = i2;
        timer.pause();

        if (i3 < 0) {
            i3 = -i3;
        }

        if (i4 < 0) {
            i4 = -i4;
        }

        if (send_dx_index >= 0) {
            if (recv_dx[send_dx_index] == i3) {
                send_dx_success++;
            }

            recv_dx[send_dx_index] = i3;
            send_dx_count++;

            if (send_dx_success >= 2) {
                send_dx_index--;
                send_dx_success = 0;
                send_dx_count = 0;
            } else if (send_dx_count >= 4) {
                if (debug_msg) {
                    System.out.println("no x sync:" + send_dx[send_dx_index]);
                }

                go_state(2);

                return;
            }
        }

        if (send_dy_index >= 0) {
            if (recv_dy[send_dy_index] == i4) {
                send_dy_success++;
            }

            recv_dy[send_dy_index] = i4;
            send_dy_count++;

            if (send_dy_success >= 2) {
                send_dy_index--;
                send_dy_success = 0;
                send_dy_count = 0;
            } else if (send_dy_count >= 4) {
                if (debug_msg) {
                    System.out.println("no y sync:" + send_dy[send_dy_index]);
                }

                go_state(2);

                return;
            }
        }

        if (send_dx_index >= 0 || send_dy_index >= 0) {
            sync_continue();

            return;
        }

        for (int length = send_dx.length - 1; length >= 0; length--) {
            if (recv_dx[length] == 0 || recv_dy[length] == 0) {
                go_state(2);

                return;
            } else if (length != 0 && (recv_dx[length] < recv_dx[length - 1] || recv_dy[length] < recv_dy[length - 1])) {
                go_state(2);

                return;
            }
        }

        sync_successful = true;
        send_dx_index = 0;
        send_dy_index = 0;

        go_state(2);
    }

    private void init_vars() {
        client_dx = 0;
        client_dy = 0;
        client_x = 0;
        client_y = 0;
        dragging = false;
        pressed_button = 0;
        server_h = 480;
        server_w = 640;
        server_x = 0;
        server_y = 0;

        sync_default();
    }

    private void move_server(boolean z, boolean z2) {
        int i2;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = client_dx;
        int i8 = client_dy;
        int i;
        timer.pause();

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
                int length = send_dx.length - 1;

                while (true) {
                    if (length < send_dx_index) {
                        break;
                    } else if (recv_dx[length] <= i7) {
                        i3 = i * send_dx[length];
                        i5 += recv_dx[length];
                        i7 -= recv_dx[length];
                        break;
                    } else {
                        length--;
                    }
                }

                if (length < send_dx_index) {
                    i3 = 0;
                    i5 += i7;
                    i7 = 0;
                }
            } else {
                i3 = 0;
            }

            if (i8 != 0) {
                int length2 = send_dy.length - 1;

                while (true) {
                    if (length2 < send_dy_index) {
                        break;
                    } else if (recv_dy[length2] <= i8) {
                        i4 = i2 * send_dy[length2];
                        i6 += recv_dy[length2];
                        i8 -= recv_dy[length2];
                        break;
                    } else {
                        length2--;
                    }
                }

                if (length2 < send_dy_index) {
                    i4 = 0;
                    i6 += i8;
                    i8 = 0;
                }
            } else {
                i4 = 0;
            }

            if (!(i3 == 0 && i4 == 0)) {
                listener.serverMove(i3, i4, client_x, client_y);
            }

            if (!z || (i7 == 0 && i8 == 0)) {
                break;
            }
        }

        client_dx -= i * i5;
        client_dy -= i2 * i6;

        if (!z2) {
            server_x += i * i5;
            server_y -= i2 * i6;
        }

        if (client_dx != 0 || client_dy != 0) {
            timer.start();
        }
    }

    private void go_state(int i) {
        synchronized (mutex) {
            state_machine(1, null, 0, 0);

            state = i;

            state_machine(0, null, 0, 0);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void state_machine(int i, MouseEvent mouseEvent, int i2, int i3) {
        synchronized (mutex) {
            switch (state) {
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

    @SuppressWarnings("unused")
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
            case CMD_ALIGN:
            default:
        }
    }

    private void state_sync(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                timer = new Timer(TIMEOUT_SYNC, false, mutex);
                timer.setListener(this, null);

                sync_default();

                send_dx_index = send_dx.length - 1;
                send_dy_index = send_dy.length - 1;

                sync_continue();

                return;
            case 1:
                timer.stop();
                timer = null;

                if (!sync_successful) {
                    if (debug_msg) {
                        System.out.println("fail");
                    }

                    sync_default();
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
                server_w = i2;
                server_h = i3;
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
            case CMD_ALIGN:
            default:
                return;
            case 8:
            case 9:
            case 12:
            case 13:
                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();
        }
    }

    private void state_enable(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                timer = new Timer(TIMEOUT_MOVE, false, mutex);
                timer.setListener(this, null);
                return;
            case 1:
                timer.stop();
                timer = null;
                return;
            case 2:
                go_state(1);
                return;
            case 3:
                if (i2 > TIMEOUT_SYNC || i3 > TIMEOUT_SYNC) {
                    go_state(3);
                    return;
                }

                server_x = i2;
                server_y = i3;
                return;
            case 4:
                server_w = i2;
                server_h = i3;
                return;
            case 5:
                go_state(3);
                return;
            case 6:
                move_server(true, true);
                return;
            case 7:
                if (dragging) {
                    return;
                }

                if ((mouseEvent.getModifiersEx() & 16) != 0) {
                    listener.serverClick(4, 1);
                    return;
                } else if ((mouseEvent.getModifiersEx() & 8) != 0) {
                    listener.serverClick(2, 1);
                    return;
                } else if ((mouseEvent.getModifiersEx() & 4) != 0) {
                    listener.serverClick(1, 1);
                    return;
                } else {
                    return;
                }
            case 8:
            case 9:
                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();

                if (client_x < 0) {
                    client_x = 0;
                }

                if (client_x > server_w) {
                    client_x = server_w;
                }

                if (client_y < 0) {
                    client_y = 0;
                }

                if (client_y > server_h) {
                    client_y = server_h;
                }

                if (pressed_button != 1 && (mouseEvent.getModifiersEx() & 2) == 0) {
                    align();

                    return;
                }

                return;
            case 10:
                if (pressed_button == 0) {
                    if ((mouseEvent.getModifiersEx() & 4) != 0) {
                        pressed_button = 1;
                    } else if ((mouseEvent.getModifiersEx() & 8) != 0) {
                        pressed_button = 2;
                    } else {
                        pressed_button = 4;
                    }

                    dragging = false;

                    return;
                }

                return;
            case 11:
                if (pressed_button == -4) {
                    listener.serverRelease(4);
                } else if (pressed_button == -2) {
                    listener.serverRelease(2);
                } else if (pressed_button == -1) {
                    listener.serverRelease(1);
                }

                pressed_button = 0;

                return;
            case 12:
                if (pressed_button != 1) {
                    if (pressed_button > 0) {
                        pressed_button = -pressed_button;
                        listener.serverPress(pressed_button);
                    }

                    client_dx += mouseEvent.getX() - client_x;
                    client_dy += client_y - mouseEvent.getY();

                    move_server(false, true);
                }

                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();
                dragging = true;

                return;
            case 13:
                if ((mouseEvent.getModifiersEx() & 2) == 0) {
                    client_dx += mouseEvent.getX() - client_x;
                    client_dy += client_y - mouseEvent.getY();

                    move_server(false, true);
                }

                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();

                return;
            case CMD_ALIGN:
                client_dx = client_x - server_x;
                client_dy = server_y - client_y;

                move_server(true, true);

                return;
            default:
                /* no-op */
        }
    }

    private void state_disable(int i, MouseEvent mouseEvent, int i2, int i3) {
        switch (i) {
            case 0:
                timer = new Timer(TIMEOUT_MOVE, false, mutex);
                timer.setListener(this, null);

                return;
            case 1:
                timer.stop();
                timer = null;
                return;
            case 2:
                sync_default();
                return;
            case 3:
                if (i2 < TIMEOUT_SYNC && i3 < TIMEOUT_SYNC) {
                    server_x = i2;
                    server_y = i3;

                    go_state(2);

                    return;
                }
                return;
            case 4:
                server_w = i2;
                server_h = i3;
                return;
            case 5:
            default:
                return;
            case 6:
                move_server(true, false);
                return;
            case 7:
                if (dragging) {
                    return;
                }

                if ((mouseEvent.getModifiersEx() & 16) != 0) {
                    listener.serverClick(4, 1);
                    return;
                } else if ((mouseEvent.getModifiersEx() & 8) != 0) {
                    listener.serverClick(2, 1);
                    return;
                } else if ((mouseEvent.getModifiersEx() & 4) != 0) {
                    listener.serverClick(1, 1);
                    return;
                } else {

                    return;
                }
            case 8:
            case 9:
                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();

                if (client_x < 0) {
                    client_x = 0;
                }

                if (client_x > server_w) {
                    client_x = server_w;
                }

                if (client_y < 0) {
                    client_y = 0;
                }

                if (client_y > server_h) {
                    client_y = server_h;
                }

                if (pressed_button != 1 && (mouseEvent.getModifiersEx() & 2) == 0) {
                    align();

                    return;
                }

                return;
            case 10:
                if (pressed_button == 0) {
                    if ((mouseEvent.getModifiersEx() & 4) != 0) {
                        pressed_button = 1;
                    } else if ((mouseEvent.getModifiersEx() & 8) != 0) {
                        pressed_button = 2;
                    } else {
                        pressed_button = 4;
                    }

                    dragging = false;

                    return;
                }

                return;
            case 11:
                if (pressed_button == -4) {
                    listener.serverRelease(4);
                } else if (pressed_button == -2) {
                    listener.serverRelease(2);
                } else if (pressed_button == -1) {
                    listener.serverRelease(1);
                }

                pressed_button = 0;

                return;
            case 12:
                if (pressed_button != 1) {
                    if (pressed_button > 0) {
                        pressed_button = -pressed_button;
                        listener.serverPress(pressed_button);
                    }

                    client_dx += mouseEvent.getX() - client_x;
                    client_dy += client_y - mouseEvent.getY();

                    move_server(false, false);
                } else {
                    server_x = mouseEvent.getX();
                    server_y = mouseEvent.getY();
                }

                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();
                dragging = true;

                return;
            case 13:
                if ((mouseEvent.getModifiersEx() & 2) == 0) {
                    client_dx += mouseEvent.getX() - client_x;
                    client_dy += client_y - mouseEvent.getY();

                    move_server(false, false);
                } else {
                    server_x = mouseEvent.getX();
                    server_y = mouseEvent.getY();
                }

                client_x = mouseEvent.getX();
                client_y = mouseEvent.getY();

                return;
            case CMD_ALIGN:
                client_dx = client_x - server_x;
                client_dy = server_y - client_y;

                move_server(true, false);
        }
    }

    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
    }
}
