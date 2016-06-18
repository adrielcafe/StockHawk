package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.db.chart.model.LineSet;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class ChartActivity extends AppCompatActivity {
    public static final String EXTRA_SYMBOL = "symbol";
    public static final String EXTRA_QUOTE  = "quote";

    private static final OkHttpClient HTTP  = new OkHttpClient();
    private static final String YQL_URL     =
            "HTTP://query.yahooapis.com/v1/public/yql?q=" +
            "select * from yahoo.finance.historicaldata " +
            "where symbol = \"%s\" " +
            "and startDate = \"%s\" " +
            "and endDate = \"%s\" " +
            "&format=json" +
            "&diagnostics=false" +
            "&env=store://datatables.org/alltableswithkeys" +
            "&callback=";

    private final SublimeListenerAdapter datePickerListener = new SublimeListenerAdapter() {
        @Override
        public void onDateTimeRecurrenceSet(SublimePicker sublimeMaterialPicker, SelectedDate selectedDate, int hourOfDay, int minute, SublimeRecurrencePicker.RecurrenceOption recurrenceOption, String recurrenceRule) {
            startDate = getYQLDate(selectedDate.getStartDate().getTime());
            endDate = getYQLDate(selectedDate.getEndDate().getTime());
            dateRangeView.setText(getPrettyDate(selectedDate.getStartDate().getTime()) + "  >  " + getPrettyDate(selectedDate.getEndDate().getTime()));
            datePicker.dismiss();
            if(startDate.equals(endDate)){
                Toast.makeText(ChartActivity.this, R.string.select_two_different_dates, Toast.LENGTH_SHORT).show();
            } else {
                loadQuoteAndChart();
            }
        }

        @Override
        public void onCancelled() {
            datePicker.dismiss();
        }
    };

    private DatePickerFragment datePicker;
    private Button dateRangeView;
    private LineChartView chartView;
    private ArrayList<Quote> quotes;
    private String symbol;
    private String startDate;
    private String endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState != null){
            symbol = savedInstanceState.getString(EXTRA_SYMBOL);
            quotes = (ArrayList<Quote>) savedInstanceState.getSerializable(EXTRA_QUOTE);
        } else if(getIntent() != null) {
            symbol = getIntent().getStringExtra(EXTRA_SYMBOL);
        }

        setTitle(symbol);

        dateRangeView = (Button) findViewById(R.id.date_range);
        dateRangeView.setText(getPrettyDate(getStartDate().toDate()) + "  >  " + getPrettyDate(getEndDate().toDate()));
        dateRangeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show(getFragmentManager(), "datePicker");
            }
        });

        chartView = (LineChartView) findViewById(R.id.chart);
        chartView.setLabelsFormat(new DecimalFormat("$#"));

        datePicker = new DatePickerFragment();
        datePicker.setListener(datePickerListener);
        datePicker.setStartDate(getStartDate());
        datePicker.setEndDate(getEndDate());

        startDate = getYQLDate(getStartDate().toDate());
        endDate = getYQLDate(getEndDate().toDate());

        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_SYMBOL, symbol);
        outState.putSerializable(EXTRA_QUOTE, quotes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        if (Util.isConnected(this)) {
            if(quotes != null){
                loadChart();
            } else {
                loadQuoteAndChart();
            }
        }
    }

    private void loadQuoteAndChart(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(String.format(YQL_URL, symbol, startDate, endDate))
                        .build();
                try {
                    Response response = HTTP.newCall(request).execute();
                    JSONObject json = new JSONObject(response.body().string());
                    JSONArray resultsJson = json.getJSONObject("query").getJSONObject("results").getJSONArray("quote");
                    quotes = new ArrayList<>();
                    for(int i = 0; i < resultsJson.length(); i++){
                        JSONObject quoteJson = resultsJson.getJSONObject(i);
                        Quote quote = new Quote();
                        quote.bidPrice = quoteJson.getString("Close");
                        quote.date = quoteJson.getString("Date");
                        quotes.add(quote);
                    }
                    Collections.reverse(quotes);
                    loadChart();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadChart(){
        final LineSet dataSet = new LineSet();
        dataSet.setColor(Color.WHITE);
        dataSet.setSmooth(true);
        for(Quote quote : quotes){
            dataSet.addPoint("", Float.parseFloat(quote.bidPrice));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!chartView.getData().isEmpty()) {
                    chartView.getData().clear();
                }
                chartView.setAxisBorderValues(getMinPrice(), getMaxPrice());
                chartView.addData(dataSet);
                chartView.show();
            }
        });
    }

    private String getPrettyDate(Date date){
        return DateTimeFormat.shortDate().print(date.getTime());
    }

    private String getYQLDate(Date date){
        return DateTimeFormat.forPattern("yyyy-MM-dd").print(date.getTime());
    }

    private DateTime getStartDate(){
        return DateTime.now().withDayOfMonth(1);
    }

    private DateTime getEndDate(){
        return DateTime.now();
    }

    private int getMinPrice(){
        int minPrice = Integer.MAX_VALUE;
        for (Quote quote : quotes){
            try {
                int price = (int) Float.parseFloat(quote.bidPrice);
                if (price < minPrice) {
                    minPrice = price;
                }
            } catch (Exception e){ }
        }
        minPrice--;
        if(minPrice < 0){
            minPrice = 0;
        }
        return minPrice;
    }

    private int getMaxPrice(){
        int maxPrice = Integer.MIN_VALUE;
        for (Quote quote : quotes){
            try {
                int price = (int) Float.parseFloat(quote.bidPrice);
                if(price > maxPrice){
                    maxPrice = price;
                }
            } catch (Exception e){ }
        }
        maxPrice++;
        return maxPrice;
    }


}