package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.udacity.stockhawk.R;

import timber.log.Timber;

import static com.udacity.stockhawk.data.Contract.Quote;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent.hasExtra("symbol")) {

                String symbol = intent.getStringExtra("symbol");
                Uri detailuri=Quote.makeUriForStock(symbol);


            DetailFragment fragment =  DetailFragment.newInstance(detailuri);


            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_detail, fragment)
                    .commit();






                Timber.d("Symbol clicked: %s", symbol);


            }

        }
    }
}
