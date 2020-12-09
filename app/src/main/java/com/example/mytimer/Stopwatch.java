package com.example.mytimer;

import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

public class Stopwatch {



    private long stopTime;
    private long totalTime;
    private long totalTimeOffset;
    private final Chronometer chronometer;
    private String name;

    public Stopwatch(Chronometer viewID, String name, long totalTime, long totalTimeOffset) {
        chronometer = viewID;
        this.name = name;
        this.totalTime = totalTime;
        this.totalTimeOffset = totalTimeOffset;
    }

    public Stopwatch(Chronometer viewID) {
        chronometer = viewID;
        this.name = "";
        this.totalTime = 0;
        this.totalTimeOffset = 0;
    }


    // At what time was the timer paused
    private void updatePauseOffset() {
        stopTime = SystemClock.elapsedRealtime() - chronometer.getBase();
    }

    // set saved time ie chronometer keeps counting even when 'stopped'
    private void setChronometerBase() {
        chronometer.setBase(SystemClock.elapsedRealtime() - stopTime);
    }

    // ##### PUBLIC METHODS #####

    public void start() {
        setChronometerBase();
        chronometer.start();
    }

    public void stop() {
        updatePauseOffset();

        // Increment total time, using totalTimeOffset as the last time it was stopped
        // Used for timer log
        totalTime += (stopTime - totalTimeOffset);
        totalTimeOffset = stopTime;
        chronometer.stop();
    }

    public void reset() {
        totalTimeOffset = 0;
        chronometer.setBase(SystemClock.elapsedRealtime());
        stopTime = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public void restoreStopwatch() {
        setChronometerBase();
    }

    /* Always forgetting how totalTime and totalTimeOffset work, so here to hopefully clear things
       up each time you come back.

    When a timer runs and then stopped the stopTime is saved.
    Let's say stopTime = 10
    The current totalTimeOffset = 0
    totalTime currently = 0

    So totalTime = totalTime + stopTime - totalTimeOffset = 10
    Then sets totalTimeOffset to stopTime = 10

    Run the timer for 10 more seconds
    stopTime = 20
    current totalTimeOffset = 10
    totalTime currently = 10

    totalTime = totalTime + stopTime - totalTimeOffset
    totalTime += 20 - 10 = 20

 */

    public long getTotalTime() { return totalTime; }

    public long getTotalTimeOffset() { return totalTimeOffset; }
}
