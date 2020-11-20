package com.example.hydro;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class HydrationMonitor implements Parcelable {
    private MutableLiveData<Integer> liveDailyHydrationCount = new MutableLiveData<>();
    private String currentDate;
    private HashMap<String, Integer> hydrationHistory = null;

    public static final Creator<HydrationMonitor> CREATOR = new Creator<HydrationMonitor>() {
        @Override
        public HydrationMonitor createFromParcel(Parcel in) {

            HashMap<String, Integer> hydrationHistory = new HashMap<>();

            int hydrationCount = in.readInt();
            String currentDate = in.readString();
            int historySize = in.readInt();

            if(historySize != 0) {
                int[] historyCounts = new int[historySize];
                String[] historyDates = new String[historySize];

                in.readStringArray(historyDates);
                in.readIntArray(historyCounts);

                for(int i = 0; i < historySize; i++) {
                    hydrationHistory.put(historyDates[i], historyCounts[i]);
                }
            }

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
        this.hydrationHistory = new HashMap<>();
    }

    public HydrationMonitor(int dailyHydrationCount, String currentDate, HashMap<String, Integer> hydrationHistory) {
        super();
        this.liveDailyHydrationCount.setValue(dailyHydrationCount);

        if(isNewDay(currentDate)) {
            this.startNewDay(currentDate, dailyHydrationCount);
            this.currentDate = this.getTodaysDate();
        } else {
            this.currentDate = currentDate;
            this.hydrationHistory = hydrationHistory;
        }
    }

    private void startNewDay(String currentDate, int dailyHydrationCount) {
        this.hydrationHistory.put(currentDate, dailyHydrationCount);
    }

    public void startNewDay() {
        this.hydrationHistory.put(this.currentDate, this.liveDailyHydrationCount.getValue());
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

    public HashMap<String, Integer> getHydrationHistory() {
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
        this.hydrationHistory.put(this.currentDate, this.liveDailyHydrationCount.getValue());
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

        int counter = 0;
        for (String dateKey: this.hydrationHistory.keySet()) {
            historyDates[counter] = dateKey;
            historyCounts[counter] = this.hydrationHistory.get(dateKey);
        }

        dest.writeInt(historySize);
        dest.writeStringArray(historyDates);
        dest.writeIntArray(historyCounts);

    }
}
