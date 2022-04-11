package com.hp.ilo2.remcons;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;


interface MouseSyncListener {
    void serverMove(int i, int i2, int i3, int i4);

    void serverPress(int i);

    void serverRelease(int i);

    void serverClick(int i, int i2);

    void sendMouse(MouseEvent mouseEvent);

    void sendMouseScroll(MouseWheelEvent mouseWheelEvent);

    void requestScreenFocus(MouseEvent mouseEvent);

    void installKeyboardHook();

    void unInstallKeyboardHook();
}
