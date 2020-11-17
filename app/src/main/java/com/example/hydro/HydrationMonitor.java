package com.example.hydro;

import android.util.Pair;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HydrationMonitor implements Serializable {
    private int dailyHydrationCount;
    private String currentDate;
    private ArrayList<Pair<String, Integer>> hydrationHistory = null;

    public HydrationMonitor() {
        super();
        this.dailyHydrationCount = 0;
        this.currentDate = "";
        this.hydrationHistory = new ArrayList<>();
    }

    public HydrationMonitor(int dailyHydrationCount, String currentDate, ArrayList<Pair<String, Integer>> hydrationHistory) {
        super();
        this.dailyHydrationCount = dailyHydrationCount;

        if(isNewDay(currentDate)) {
            this.startNewDay(currentDate, dailyHydrationCount);
            this.currentDate = this.getTodaysDate();
            this.dailyHydrationCount = 0;
        } else {
            this.currentDate = currentDate;
            this.hydrationHistory = hydrationHistory;
        }
    }

    private void startNewDay(String currentDate, int dailyHydrationCount) {
        this.hydrationHistory.add(new Pair<String, Integer>(currentDate, dailyHydrationCount));
    }

    public void startNewDay() {
        this.hydrationHistory.add(new Pair<String, Integer>(this.currentDate, this.dailyHydrationCount));
        this.currentDate = this.getTodaysDate();
        this.dailyHydrationCount = 0;
    }

    private boolean isNewDay(String currentDate) {
        return !currentDate.equals(this.getTodaysDate());
    }

    public boolean isNewDay() {
        return !this.currentDate.equals(this.getTodaysDate());
    }

    private String getTodaysDate() {
        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return formatter.format(today);
    }

    public ArrayList<Pair<String, Integer>> getHydrationHistory() {
        return this.hydrationHistory;
    }

    public int getDailyHydrationCount() {
        return this.dailyHydrationCount;
    }

    public void incrementDailyCount() {
        this.dailyHydrationCount += 1;
    }

}
