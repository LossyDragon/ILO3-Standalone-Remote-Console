package com.hp.ilo2.remcons;

import java.util.Date;

public class Timer implements Runnable {
    
    private Object callback_info;
    private TimerListener callback;
    private final Object mutex;
    private final boolean one_shot;
    private final int timeout_max;
    private int state = 0;
    private int timeout_count;
    private long start_time_millis;
    private long stop_time_millis;
    private static final int POLL_PERIOD = 50;

    public Timer(int i, boolean z, Object obj) {
        timeout_max = i;
        one_shot = z;
        mutex = obj;
    }

    public void setListener(TimerListener timerListener, Object obj) {
        synchronized (mutex) {
            callback = timerListener;
            callback_info = obj;
        }
    }

    public void start() {
        synchronized (mutex) {
            switch (state) {
                case 0:
                    state = 1;
                    timeout_count = 0;
                    new Thread(this).start();
                    break;
                case 1:
                    timeout_count = 0;
                    break;
                case 2:
                case 3:
                    timeout_count = 0;
                    state = 1;
                    break;
            }
        }
    }

    public void stop() {
        synchronized (mutex) {
            if (state != 0) {
                state = 3;
            }
        }
    }

    public void pause() {
        synchronized (mutex) {
            if (state == 1) {
                state = 2;
            }
        }
    }

    @SuppressWarnings("BusyWait")
    @Override
    public void run() {
        do {
            Date date = new Date();
            start_time_millis = date.getTime();
            
            try {
                Thread.sleep(50L);
            } catch (InterruptedException ignored) {
            }
            
            date = new Date();
            
            stop_time_millis = date.getTime();
        } while (process_state());
    }

    private boolean process_state() {
        boolean z = true;
        synchronized (mutex) {
            switch (state) {
                case 1:
                    if (stop_time_millis > start_time_millis) {
                        timeout_count = (int) (timeout_count + (stop_time_millis - start_time_millis));
                    } else {
                        timeout_count += POLL_PERIOD;
                    }
                    if (timeout_count >= timeout_max) {
                        if (callback != null) {
                            callback.timeout(callback_info);
                        }
                        if (!one_shot) {
                            timeout_count = 0;
                        } else {
                            state = 0;
                            z = false;
                        }
                        break;
                    }
                    break;
                case 3:
                    state = 0;
                    z = false;
                    break;
            }
        }
        return z;
    }
}
