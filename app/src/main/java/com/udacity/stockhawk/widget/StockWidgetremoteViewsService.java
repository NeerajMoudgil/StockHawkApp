package com.udacity.stockhawk.widget;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.common.collect.ImmutableList;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockWidgetremoteViewsService extends RemoteViewsService{

    private static ImmutableList<String> DETAIL_COLUMNS = Contract.Quote.QUOTE_COLUMNS;

    final private DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
    private DecimalFormat dollarFormatWithPlus = ((DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US));
    private DecimalFormat percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                dollarFormatWithPlus.setPositivePrefix("+$");
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                String[] projection = new String[DETAIL_COLUMNS.size()];
                projection = DETAIL_COLUMNS.toArray(projection);
                data = getContentResolver().query(Contract.Quote.URI,
                        projection,
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data != null){
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);
                String symbol=data.getString(Contract.Quote.POSITION_SYMBOL);
                views.setTextViewText(R.id.symbol_widget,symbol );
                float price =data.getFloat(Contract.Quote.POSITION_PRICE);
                views.setTextViewText(R.id.price_widget, dollarFormat.format(price));
                float rawAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.change_widget, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change_widget, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                String change = dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = percentageFormat.format(percentageChange / 100);
                if (PrefUtils.getDisplayMode(getApplicationContext())
                        .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                    views.setTextViewText(R.id.change_widget, change);
                } else {
                    views.setTextViewText(R.id.change_widget, percentage);
                }

                final Intent fillInIntent = new Intent();
                Uri stockUri = Contract.Quote.makeUriForStock(data.getString(Contract.Quote.POSITION_SYMBOL));
                fillInIntent.setData(stockUri);
                views.setOnClickFillInIntent(R.id.widget_list, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
