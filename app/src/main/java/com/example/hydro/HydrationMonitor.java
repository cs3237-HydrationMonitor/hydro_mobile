package com.example.hydro;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HydrationMonitor implements Parcelable {
    private int dailyHydrationCount;
    private MutableLiveData<Integer> liveDailyHydrationCount = new MutableLiveData<>();
    private String currentDate;
    private ArrayList<Pair<String, Integer>> hydrationHistory = null;

    public static final Creator<HydrationMonitor> CREATOR = new Creator<HydrationMonitor>() {
        @Override
        public HydrationMonitor createFromParcel(Parcel in) {

            ArrayList<Pair<String, Integer>> hydrationHistory = new ArrayList<>();

            int historySize = in.readInt();
            if(historySize != 0) {
                int[] historyCounts = new int[historySize];
                String[] historyDates = new String[historySize];

                in.readIntArray(historyCounts);
                in.readStringArray(historyDates);



                for(int i = 0; i < historySize; i++) {
                    hydrationHistory.add(new Pair<String, Integer>(historyDates[i], historyCounts[i]));
                }
            }

            String currentDate = in.readString();
            int hydrationCount = in.readInt();

            return new HydrationMonitor(hydrationCount, currentDate, hydrationHistory);
        }

        @Override
        public HydrationMonitor[] newArray(int size) {
            return new HydrationMonitor[size];
        }
    };

    public HydrationMonitor() {
        super();
        this.liveDailyHydrationCount.setValue(0);
        this.currentDate = this.getTodaysDate();
        this.hydrationHistory = new ArrayList<>();
    }

    public HydrationMonitor(int dailyHydrationCount, String currentDate, ArrayList<Pair<String, Integer>> hydrationHistory) {
        super();
        this.liveDailyHydrationCount.setValue(dailyHydrationCount);

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
        this.liveDailyHydrationCount.setValue(0);
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
        return this.liveDailyHydrationCount.getValue();
    }

    public String getCurrentDate() {
        return this.currentDate;
    }

    public MutableLiveData<Integer> getLiveDailyHydrationCountData() {
        return this.liveDailyHydrationCount;
    }

    public void incrementDailyCount() {
        this.liveDailyHydrationCount.setValue(this.liveDailyHydrationCount.getValue() + 1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(liveDailyHydrationCount.getValue());
        dest.writeString(currentDate);

        int historySize = this.hydrationHistory.size();
        String[] historyDates = new String[historySize];
        int[] historyCounts = new int[historySize];

        for(int i = 0; i < historySize; i++) {
            Pair<String, Integer> dailyPair = this.hydrationHistory.get(i);
            historyDates[i] = dailyPair.first;
            historyCounts[i] = dailyPair.second;
        }

        dest.writeStringArray(historyDates);
        dest.writeIntArray(historyCounts);
        dest.writeInt(historySize);
    }
}
