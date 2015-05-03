package com.aosgi.runtime;

import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public class BundleActivity extends Activity {

    public static final String EXTRA_BUNDLE_ID = "bundle_id";

    private org.osgi.framework.Bundle bundle;

    private ScrollView panel;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = getIntent();
        if (intent == null) {
            this.finish();
            return;
        }

        final long bundleId = intent.getLongExtra(EXTRA_BUNDLE_ID, -1);
        final Framework framework = Launcher.getInstance().getFramework();
        this.bundle = framework.getBundleContext().getBundle(bundleId);
        if (null == this.bundle) {
            this.finish();
            return;
        }

        this.panel = new ScrollView(this);
        this.content = new TextView(this);
        this.panel.addView(this.content, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        this.setContentView(this.panel);

        final Dictionary<String, String> headers = this.bundle.getHeaders();
        final String bundleName = headers.get(Constants.BUNDLE_NAME);
        if (!TextUtils.isEmpty(bundleName)) {
            this.setTitle(bundleName);
        } else {
            this.setTitle(this.bundle.getSymbolicName());
        }

        final StringBuilder builder = new StringBuilder();
        final Enumeration<String> keys = headers.keys();
        while (keys.hasMoreElements()) {
            final String key = keys.nextElement();

            builder.append(key).append(": ").append(headers.get(key));

            if (keys.hasMoreElements()) {
                builder.append("\n");
            }
        }

        this.content.setText(builder);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
