package com.zonesciences.pyrros.fragment.Routine;

import com.zonesciences.pyrros.models.Routine;

import java.util.List;

/**
 * Created by peter on 28/12/2016.
 */

public interface RoutineLoadListener {
    void onLoadStart();
    void onLoadComplete(List<Routine> routinesList);
}
