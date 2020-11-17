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

        Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String todays_date = formatter.format(today);

        if(!currentDate.equals(todays_date)) {
            this.hydrationHistory.add(new Pair<String, Integer>(currentDate, dailyHydrationCount));
            this.currentDate = todays_date;
            this.dailyHydrationCount = 0;
        } else {
            this.currentDate = currentDate;
            this.hydrationHistory = hydrationHistory;
        }
    }

    public ArrayList<Pair<String, Integer>> getHydrationHistory() {
        return this.hydrationHistory;
    }

    public int getDailyHydrationCount() {
        return this.dailyHydrationCount;
    }

}
