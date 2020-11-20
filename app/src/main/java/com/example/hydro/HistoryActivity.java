package com.example.hydro;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        disable_action_bars();

        HashMap<String, Integer> hydrationHistory = ((HydrationMonitor)getIntent().getParcelableExtra(getResources().getString(R.string.shared_pref_hydration_monitor))).getHydrationHistory();
        AnyChartView anyChartView = findViewById(R.id.history_chart);

        Cartesian cartesian = AnyChart.line();

        cartesian.animation(true);

        cartesian.padding(10d, 20d, 5d, 20d);

        cartesian.crosshair().enabled(true);
        cartesian.crosshair()
                .yLabel(true)
                // TODO ystroke
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);

//        cartesian.title("Hydration History");

        cartesian.yAxis(0).title("Daily Hydration Counts");
        cartesian.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        List<DataEntry> seriesData = new ArrayList<>();
        seriesData.add(new CustomDataEntry("9/11/2020", 9));
        seriesData.add(new CustomDataEntry("10/11/2020", 22));
        seriesData.add(new CustomDataEntry("11/11/2020", 17));
        seriesData.add(new CustomDataEntry("12/11/2020", 6));
        seriesData.add(new CustomDataEntry("13/11/2020", 5));
        seriesData.add(new CustomDataEntry("14/11/2020", 5));
        seriesData.add(new CustomDataEntry("15/11/2020", 5));
        seriesData.add(new CustomDataEntry("16/11/2020", 7));
        seriesData.add(new CustomDataEntry("17/11/2020", 9));
        seriesData.add(new CustomDataEntry("18/11/2020", 2));
        seriesData.add(new CustomDataEntry("19/11/2020", 27));
        for (String dateData: hydrationHistory.keySet()) {
            seriesData.add(new CustomDataEntry(dateData, hydrationHistory.get(dateData)));
        }

        Set set = Set.instantiate();
        set.data(seriesData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        Line series1 = cartesian.line(series1Mapping);
        series1.name("User");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);


        cartesian.legend().enabled(true);
        cartesian.legend().fontSize(13d);
        cartesian.legend().padding(0d, 0d, 10d, 0d);

        anyChartView.setChart(cartesian);

        Button backToHomeButton = (Button)findViewById(R.id.button_to_home);
        backToHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void disable_action_bars() {
        ActionBar actionBar = getActionBar();
        androidx.appcompat.app.ActionBar supportActionBar = getSupportActionBar();

        if(actionBar != null) {
            actionBar.hide();
        }

        if(supportActionBar != null) {
            supportActionBar.hide();
        }
    }

    private class CustomDataEntry extends ValueDataEntry {

        CustomDataEntry(String x, Number value) {
            super(x, value);
        }

    }

}