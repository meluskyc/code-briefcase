package org.meluskyc.codebriefcase.activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import org.meluskyc.codebriefcase.R;

public class LicensesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView main = (TextView) findViewById(R.id.licenses_text_main);
        main.setText(Html.fromHtml(getString(R.string.third_party_licenses)));
    }
}