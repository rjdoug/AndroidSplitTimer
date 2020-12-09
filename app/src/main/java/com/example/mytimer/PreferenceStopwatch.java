package com.example.mytimer;

import android.widget.Chronometer;

/* Shared Preferences only allows saving of primitive and String data types.
    So unable to straight up save the stopwatch class. Hence this class is created as a variation
    that only contains primitives and strings. With the info we have we can create a new instance
    of a chronometer and set its base from there
 */
public class PreferenceStopwatch {

    private final long stopTime;
    private final String name;
    private long totalTime;
    private long totalTimeOffset;

    public PreferenceStopwatch (Stopwatch stopwatch) {
        this.stopTime = stopwatch.getStopTime();
        this.name = stopwatch.getName();
        this.totalTime = stopwatch.getTotalTime();
        this.totalTimeOffset = stopwatch.getTotalTimeOffset();
    }

    // Creates new instance of stopwatch with saved things
    public Stopwatch reloadStopwatch(Chronometer viewID) {
        Stopwatch stopwatch = new Stopwatch(viewID, name, totalTime, totalTimeOffset);
        stopwatch.setStopTime(this.stopTime);
        return stopwatch;
    }

    public String getName() {
        return name;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void resetTotalTime() { totalTime = 0; }
}
