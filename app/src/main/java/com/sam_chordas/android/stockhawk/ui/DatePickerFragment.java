package com.sam_chordas.android.stockhawk.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.appeaser.sublimepickerlibrary.SublimePicker;
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.sam_chordas.android.stockhawk.R;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends DialogFragment {
    private SublimePicker dateTimePicker;
    private SublimeListenerAdapter listener;
    private Calendar startDate;
    private Calendar endDate;

    public DatePickerFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SelectedDate selectedDate = new SelectedDate(startDate, endDate);

        SublimeOptions options = new SublimeOptions();
        options.setPickerToShow(SublimeOptions.Picker.DATE_PICKER);
        options.setDisplayOptions(SublimeOptions.ACTIVATE_DATE_PICKER);
        options.setDateParams(selectedDate);
        options.setDateRange(DateTimeFormat.forPattern("yyyy-MM-dd").parseMillis("2000-01-01"), DateTime.now().minusDays(1).getMillis());
        options.setCanPickDateRange(true);

        dateTimePicker = (SublimePicker) getActivity().getLayoutInflater().inflate(R.layout.sublime_picker, container);
        dateTimePicker.initializePicker(options, listener);
        return dateTimePicker;
    }

    public void setListener(SublimeListenerAdapter listener) {
        this.listener = listener;
    }

    public void setStartDate(DateTime date) {
        startDate = date.toCalendar(Locale.ENGLISH);
    }

    public void setEndDate(DateTime date) {
        endDate = date.toCalendar(Locale.ENGLISH);
    }
}