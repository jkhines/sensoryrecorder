package edu.asu.jkhines1.dssr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.asu.jkhines1.dssr.models.AccessToken;
import edu.asu.jkhines1.dssr.models.Device;
import edu.asu.jkhines1.dssr.utils.ApiHelper;
import edu.asu.jkhines1.dssr.utils.ApplicationConfig;
import edu.asu.jkhines1.dssr.utils.TokenHelper;
import edu.asu.jkhines1.dssr.models.Tokens;

public class DeviceActivity extends AppCompatActivity {
    private View mProgressView;
    private View mDeviceFormView;
    private GetDevicesTask mAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the UI.
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDeviceFormView = findViewById(R.id.device_form);
        mProgressView = findViewById(R.id.device_progress);

        loadDevices();
    }

    // Show the progress bar and load the data.
    private void loadDevices() {
        showProgress(true);
        mAsyncTask = new GetDevicesTask();
        mAsyncTask.execute((Void) null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDeviceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDeviceFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDeviceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mDeviceFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void processResponse(String response) {
        try {
            // Deserialize the list of devices asking this user for permission.
            ObjectMapper mapper = new ObjectMapper();
            List<Device> devices = mapper.readValue(response, new TypeReference<List<Device>>() {});

            // create the adapters
            List<String> approvedDeviceNames = new ArrayList<String>();
            ArrayAdapter<String> approvedDevicesAdapter = new ArrayAdapter<String>(this,
                    R.layout.device_approved_listitem, R.id.approved_device_name_text, approvedDeviceNames);
            DeviceListAdapter pendingDevicesAdapter = new DeviceListAdapter(this, R.layout.device_pending_listitem, devices);

            // Associate the adapters with the list views.
            ListView pendingDevicesListView = (ListView)findViewById(R.id.pending_device_listview);
            pendingDevicesListView.setAdapter(pendingDevicesAdapter);
            ListView approvedDevicesListView = (ListView)findViewById(R.id.approved_device_listview);
            approvedDevicesListView.setAdapter(approvedDevicesAdapter);

            // Populate the adapters.
            for (Device device : devices) {
                if (device.getApproved()) {
                    approvedDevicesAdapter.add(device.getClientName());
                } else {
                    pendingDevicesAdapter.addItem(device);
                }
            }
        }
        catch (IOException ex) {
        }
    }

    public class GetDevicesTask extends AsyncTask<Void, Void, Boolean> {
        public String response;

        GetDevicesTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Get the access and refresh tokens.
                Tokens tokens = TokenHelper.getTokens(getApplicationContext());

                // Get the data.
                this.response = ApiHelper.getResponse(
                        ApplicationConfig.API_SERVER_URL + "/devices/" +
                                AccessToken.getInstance(tokens.getAccessToken()).getSubject(),
                        tokens.getAccessToken());
            } catch (Exception e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAsyncTask = null;
            showProgress(false);

            if (success) {
                processResponse(this.response);
                //finish();
            }
            mDeviceFormView.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mAsyncTask = null;
            showProgress(false);
        }
    }

    // Classes to support the drawing of a custom ListView.
    private class DeviceListAdapter extends BaseAdapter {
        private List<Device> mData = new ArrayList<Device>();
        private LayoutInflater mInflater;
        private UpdateDeviceTask mAsyncTask = null;

        public DeviceListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Device> objects) {
            mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(final Device item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void removeItem(int position) {
            mData.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Device getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.device_pending_listitem, null);

                holder.textView1 = (TextView)convertView.findViewById(R.id.pending_device_name_text);
                holder.textView2 = (TextView)convertView.findViewById(R.id.pending_device_date_text);
                holder.imageButton = (ImageButton)convertView.findViewById(R.id.device_approval_button);
                holder.imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get the specific item that was clicked on and mark it as approved.
                        Device device = getItem(position);
                        device.setApproved(true);

                        // Attempt to submit the updated item to the server.
                        try {
                            updateDevice(device);
                        } catch (Exception ex) {
                        }

                        // Remove the item from the ListView's collection and refresh the ListView.
                        //removeItem(position);
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }

            holder.textView1.setText(mData.get(position).getClientName());
            holder.textView2.setText(mData.get(position).getRecordedAt().toLocaleString());

            return convertView;
        }

        private void updateDevice(Device device) {
            showProgress(true);
            mAsyncTask = new UpdateDeviceTask(device);
            mAsyncTask.execute((Void) null);
        }

        public class UpdateDeviceTask extends AsyncTask<Void, Void, Boolean> {
            private Device mDevice;
            public String response;

            UpdateDeviceTask() {
            }

            UpdateDeviceTask(Device device) {
                this.mDevice = device;
            }

            @Override
            protected Boolean doInBackground(Void... params) {

                try {
                    // Get the access and refresh tokens.
                    Tokens tokens = TokenHelper.getTokens(getApplicationContext());

                    // Put the data.
                    this.response = ApiHelper.updateDevice(
                            ApplicationConfig.API_SERVER_URL + "/devices",
                            tokens.getAccessToken(), mDevice);
                } catch (Exception e) {
                    return false;
                }

                return true;
            }

            @Override
            protected void onPostExecute(final Boolean success) {
                mAsyncTask = null;
                showProgress(false);

                if (success) {
                    loadDevices();
                    //processResponse(this.response);
                    //finish();
                }
                mDeviceFormView.requestFocus();
            }

            @Override
            protected void onCancelled() {
                mAsyncTask = null;
                showProgress(false);
            }
        }
    }

    public static class ViewHolder {
        TextView textView1;
        TextView textView2;
        ImageButton imageButton;
    }
}
