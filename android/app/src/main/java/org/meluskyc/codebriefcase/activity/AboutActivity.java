package org.meluskyc.codebriefcase.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import org.meluskyc.codebriefcase.BuildConfig;
import org.meluskyc.codebriefcase.R;

public class AboutActivity extends BaseActivity {

    private static final String REPO_URL = "";

    private View.OnClickListener listener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.about_text_source:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(REPO_URL)));
                    break;
                case R.id.about_text_licenses:
                    startActivity(new Intent(AboutActivity.this, LicensesActivity.class));
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView main = (TextView) findViewById(R.id.about_text_version);
        TextView sourceLink = (TextView) findViewById(R.id.about_text_source);
        TextView licensesLink = (TextView) findViewById(R.id.about_text_licenses);
        main.setText(Html.fromHtml(getString(R.string.code_briefcase, BuildConfig.VERSION_NAME)));
        sourceLink.setOnClickListener(listener);
        licensesLink.setOnClickListener(listener);
    }
}
