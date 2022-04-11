package com.hp.ilo2.remcons;

import java.util.Date;



public class Timer implements Runnable {
    private static final int STATE_INIT = 0;
    private static final int STATE_RUNNING = 1;
    private static final int STATE_PAUSED = 2;
    private static final int STATE_STOPPED = 3;
    private static final int POLL_PERIOD = 50;
    private int timeout_count;
    private int timeout_max;
    private boolean one_shot;
    private long start_time_millis;
    private long stop_time_millis;
    private TimerListener callback;
    private Object callback_info;
    private Object mutex;
    private int state = 0;
    private Date date = new Date();

    public Timer(int i, boolean z, Object obj) {
        this.timeout_max = i;
        this.one_shot = z;
        this.mutex = obj;
    }

    public void setListener(TimerListener timerListener, Object obj) {
        synchronized (this.mutex) {
            this.callback = timerListener;
            this.callback_info = obj;
        }
    }

    public void start() {
        synchronized (this.mutex) {
            switch (this.state) {
                case 0:
                    this.state = 1;
                    this.timeout_count = 0;
                    new Thread(this).start();
                    break;
                case 1:
                    this.timeout_count = 0;
                    break;
                case 2:
                    this.timeout_count = 0;
                    this.state = 1;
                    break;
                case 3:
                    this.timeout_count = 0;
                    this.state = 1;
                    break;
            }
        }
    }

    public void stop() {
        synchronized (this.mutex) {
            if (this.state != 0) {
                this.state = 3;
            }
        }
    }

    public void pause() {
        synchronized (this.mutex) {
            if (this.state == 1) {
                this.state = 2;
            }
        }
    }

    public void cont() {
        synchronized (this.mutex) {
            if (this.state == 2) {
                this.state = 1;
            }
        }
    }

    @Override // java.lang.Runnable
    public void run() {
        do {
            this.date = new Date();
            this.start_time_millis = this.date.getTime();
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
            }
            this.date = new Date();
            this.stop_time_millis = this.date.getTime();
        } while (process_state());
    }

    private boolean process_state() {
        boolean z = true;
        synchronized (this.mutex) {
            switch (this.state) {
                case 1:
                    if (this.stop_time_millis > this.start_time_millis) {
                        this.timeout_count = (int) (this.timeout_count + (this.stop_time_millis - this.start_time_millis));
                    } else {
                        this.timeout_count += POLL_PERIOD;
                    }
                    if (this.timeout_count >= this.timeout_max) {
                        if (this.callback != null) {
                            this.callback.timeout(this.callback_info);
                        }
                        if (!this.one_shot) {
                            this.timeout_count = 0;
                            break;
                        } else {
                            this.state = 0;
                            z = false;
                            break;
                        }
                    }
                    break;
                case 3:
                    this.state = 0;
                    z = false;
                    break;
            }
        }
        return z;
    }
}
