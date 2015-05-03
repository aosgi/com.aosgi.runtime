package com.aosgi.runtime;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Dictionary;

import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BundleListActivity extends ListActivity implements OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getListView().setOnItemClickListener(this);
        this.setListAdapter(new BundleAdapter());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final org.osgi.framework.Bundle bundle = (org.osgi.framework.Bundle) parent.getItemAtPosition(position);
        final Intent intent = new Intent(this, BundleActivity.class);
        intent.putExtra(BundleActivity.EXTRA_BUNDLE_ID, bundle.getBundleId());
        startActivity(intent);
    }

    private static final class BundleAdapter extends BaseAdapter implements Comparator<org.osgi.framework.Bundle> {

        private final org.osgi.framework.Bundle[] bundles;

        private BundleAdapter() {
            final Framework framework = Launcher.getInstance().getFramework();
            this.bundles = framework.getBundleContext().getBundles();
            Arrays.sort(this.bundles, this);
        }

        @Override
        public int compare(org.osgi.framework.Bundle lhs, org.osgi.framework.Bundle rhs) {
            final long lid = lhs.getBundleId();
            final long rid = rhs.getBundleId();
            return (lid == rid) ? 0 : (lid > rid ? 1 : -1);
        }

        @Override
        public int getCount() {
            return null == this.bundles ? 0 : this.bundles.length;
        }

        @Override
        public Object getItem(int position) {
            try {
                return null == this.bundles ? null : this.bundles[position];
            } catch (NullPointerException e) {
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder vh;

            if (null == convertView) {
                convertView = View.inflate(parent.getContext(), android.R.layout.simple_list_item_2, null);
                vh = new ViewHolder(convertView);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.bind(getItem(position));

            return convertView;
        }

    }

    private static final class ViewHolder {

        private final TextView lblName;

        private final TextView lblSymbolicName;

        public ViewHolder(View view) {
            view.setTag(this);

            this.lblName = (TextView) view.findViewById(android.R.id.text1);
            this.lblSymbolicName = (TextView) view.findViewById(android.R.id.text2);
        }

        void bind(Object object) {
            if (!(object instanceof org.osgi.framework.Bundle)) {
                return;
            }

            final org.osgi.framework.Bundle bundle = (org.osgi.framework.Bundle) object;
            final Dictionary<String, String> headers = bundle.getHeaders();

            this.lblName.setText(headers.get(Constants.BUNDLE_NAME));
            this.lblSymbolicName.setText(bundle.getSymbolicName());
        }

    }

}
