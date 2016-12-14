package com.zonesciences.pyrros.Timer;

/**
 * Created by Peter on 14/12/2016.
 */
public class TimerState {

    long mTimeRemaining;
    boolean mHasActiveTimer;
    boolean mTimerFirstStart;
    boolean mTimerRunning;
    int mCurrentProgress;
    int mCurrentProgressMax;

    public TimerState(){

    }

    public void setTimeRemaining(long timeRemaining) {
        mTimeRemaining = timeRemaining;
    }

    public void setHasActiveTimer(boolean hasActiveTimer) {
        mHasActiveTimer = hasActiveTimer;
    }

    public void setTimerFirstStart(boolean timerFirstStart) {
        mTimerFirstStart = timerFirstStart;
    }

    public void setTimerRunning(boolean timerRunning) {
        mTimerRunning = timerRunning;
    }

    public void setCurrentProgress(int currentProgress) {
        mCurrentProgress = currentProgress;
    }

    public void setCurrentProgressMax(int currentProgressMax) {
        mCurrentProgressMax = currentProgressMax;
    }

    public long getTimeRemaining() {
        return mTimeRemaining;
    }

    public boolean isHasActiveTimer() {
        return mHasActiveTimer;
    }

    public boolean isTimerFirstStart() {
        return mTimerFirstStart;
    }

    public boolean isTimerRunning() {
        return mTimerRunning;
    }

    public int getCurrentProgress() {
        return mCurrentProgress;
    }

    public int getCurrentProgressMax() {
        return mCurrentProgressMax;
    }
}
