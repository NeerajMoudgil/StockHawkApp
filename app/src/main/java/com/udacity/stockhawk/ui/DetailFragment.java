package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.common.collect.ImmutableList;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String DETAIL_URI = "DetailURI";
    private LineChart lineChart;
    private LineData lineData;
    private static final int DETAIL_LOADER = 1;
    final HashMap<Integer, String> labelMap = new HashMap<>();


    private static ImmutableList<String> DETAIL_COLUMNS = Contract.Quote.QUOTE_COLUMNS;


    private Uri mUri;


    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);

    }

    public static DetailFragment newInstance(Uri detailuri) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(DETAIL_URI, detailuri);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUri = getArguments().getParcelable(DETAIL_URI);
            Log.i("URIIRIRIIR", mUri.toString());
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        lineChart = (LineChart) rootView.findViewById(R.id.stock_chart);
        lineChart.setBackgroundColor(Color.TRANSPARENT);
        Description description = new Description();
        description.setText(getString(R.string.chart_descr));
        lineChart.setDescription(description);


        return rootView;


    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {

            String[] projection = new String[DETAIL_COLUMNS.size()];
            projection = DETAIL_COLUMNS.toArray(projection);
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    projection,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            String stockData = data.getString(Contract.Quote.POSITION_HISTORY);
            Log.d("got stoclkkksss", stockData);
            String chartDataArr[] = stockData.split("\\r?\\n");
            List<Entry> entries = new ArrayList<Entry>();
            int arrlength = chartDataArr.length;


            for (int iter = 0; iter < arrlength; iter++) {
                String chartData = chartDataArr[iter];
                String timeMilliSecondsStr = chartData.split(",")[0];

                String stockValue = chartData.split(",")[1];

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
                String date = simpleDateFormat.format(Long.parseLong(timeMilliSecondsStr));

                entries.add(new Entry(iter, Float.parseFloat(stockValue)));
                labelMap.put(iter, date);
            }

            LineDataSet dataSet = new LineDataSet(entries, getString(R.string.chart_label));
            dataSet.setLineWidth(4f);
            dataSet.setCircleRadius(5f);
            lineData = new LineData(dataSet);
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new MyCustomFormatter());

            lineChart.setData(lineData);

            lineChart.invalidate();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    /**
     * class to set labels of X Axis
     * taken help from https://github.com/PhilJay/MPAndroidChart/issues/2190
     */

    private class MyCustomFormatter implements IAxisValueFormatter {

        public MyCustomFormatter() {
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return labelMap.get((int) value);
        }
    }
}