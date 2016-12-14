package com.zonesciences.pyrros.Timer;

/**
 * Created by Peter on 14/12/2016.
 */
public class TimerState {

    long mTimeRemaining;
    int mTimerDuration;
    int mTimerStartTime;
    boolean mHasActiveTimer;
    boolean mTimerFirstStart;
    boolean mTimerRunning;
    int mCurrentProgress;
    int mCurrentProgressMax;

    public TimerState(){
        mTimerFirstStart = true;
    }


    public void setTimerDuration(int timerDuration) {
        mTimerDuration = timerDuration;
    }

    public void setTimerStartTime(int timerStartTime) {
        mTimerStartTime = timerStartTime;
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


    public int getTimerDuration() {
        return mTimerDuration;
    }

    public boolean isHasActiveTimer() {
        return mHasActiveTimer;
    }

    public int getTimerStartTime() {
        return mTimerStartTime;
    }

    public long getTimeRemaining() {
        return mTimeRemaining;
    }

    public boolean hasActiveTimer() {
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

    // RESET TIMER STATE

    public void reset() {

         mTimeRemaining = 0;
         mTimerDuration = 0;
         mTimerStartTime = 0;
         mHasActiveTimer = false;
         mTimerFirstStart = true;
         mTimerRunning = false;
         mCurrentProgress = 0;
         mCurrentProgressMax = 0;
    }
}
