package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.Quote;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.List;

public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context context;
    private List<Quote> quotes = new ArrayList<>();
    private int appWidgetId;

    public WidgetViewsFactory(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        Cursor c = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.CHANGE},
                null, null, null);
        if (c != null) {
            if(c.getCount() != 0) {
                while (c.moveToNext()) {
                    Quote quote = new Quote();
                    quote.symbol = c.getString(0);
                    quote.bidPrice = c.getString(1);
                    quote.change = c.getString(2);
                    quotes.add(quote);
                }
            }
            c.close();
        }
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return quotes.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Quote quote = quotes.get(position);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.list_item_quote);
        rv.setTextViewText(R.id.stock_symbol, quote.symbol);
        rv.setTextViewText(R.id.bid_price, quote.bidPrice);
        rv.setTextViewText(R.id.change, quote.change);
        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
