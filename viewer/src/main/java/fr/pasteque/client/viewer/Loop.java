package fr.pasteque.client.viewer;

import android.os.Handler;

/**
 * Created by svirch_n on 14/04/16
 * Last edited at 16:54.
 */
public class Loop {


    private boolean started = false;
    private Handler handler = new Handler();

    private Runnable runnable;
    private long delay;

    public Loop(long delay, final Runnable runnable) {
        this.delay = delay;
        this.runnable = new Runnable() {
            @Override
            public void run() {
                runnable.run();
                if (started) {
                    start();
                }
            }
        };
    }

    public void stop() {
        started = false;
        handler.removeCallbacks(runnable);
    }

    public void start() {
        started = true;
        handler.postDelayed(runnable, delay);
    }}
