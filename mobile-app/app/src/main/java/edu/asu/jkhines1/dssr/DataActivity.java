package edu.asu.jkhines1.dssr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.asu.jkhines1.dssr.models.AccessToken;
import edu.asu.jkhines1.dssr.models.InputData;
import edu.asu.jkhines1.dssr.models.Tokens;
import edu.asu.jkhines1.dssr.utils.ApiHelper;
import edu.asu.jkhines1.dssr.utils.ApplicationConfig;
import edu.asu.jkhines1.dssr.utils.TokenHelper;

public class DataActivity extends AppCompatActivity {
    private View mProgressView;
    private View mDataFormView;
    private GetDataTask mAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the UI.
        setContentView(R.layout.activity_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDataFormView = findViewById(R.id.data_form);
        mProgressView = findViewById(R.id.data_progress);

        loadData();
    }

    // Show the progress bar and load the data.
    private void loadData() {
        showProgress(true);
        mAsyncTask = new GetDataTask();
        mAsyncTask.execute((Void) null);
    }

    public class GetDataTask extends AsyncTask<Void, Void, Boolean> {
        public String response;

        GetDataTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Get the access and refresh tokens.
                Tokens tokens = TokenHelper.getTokens(getApplicationContext());

                // Get the data.
                this.response = ApiHelper.getResponse(
                        ApplicationConfig.API_SERVER_URL + "/data/" +
                                AccessToken.getInstance(tokens.getAccessToken()).getSubject() +
                                "?filter=latest",
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
            mDataFormView.requestFocus();
        }

        @Override
        protected void onCancelled() {
            mAsyncTask = null;
            showProgress(false);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDataFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDataFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDataFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDataFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void processResponse(String response) {
        List<InputData> latestData = new ArrayList<InputData>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            latestData = mapper.readValue(response, new TypeReference<List<InputData>>(){ });
        } catch (IOException ex) { }

        InputData latestWakeData = null;
        InputData latestSleepData = null;
        InputData latestSoundData = null;
        InputData latestBehaviorData = null;

        for (InputData data : latestData) {
            switch (data.getType()) {
                case "wake":
                    latestWakeData = data;
                    break;
                case "sleep":
                    latestSleepData = data;
                    break;
                case "sound":
                    latestSoundData = data;
                    break;
                case "behavior":
                    latestBehaviorData = data;
                    break;
            }
        }

        if (latestWakeData != null) {
            TextView textView = (TextView)findViewById(R.id.wake_text);
            textView.setText(latestWakeData.getRecordedAt().toLocaleString());
        }

        if (latestSleepData != null) {
            TextView textView = (TextView)findViewById(R.id.sleep_text);
            textView.setText(latestSleepData.getRecordedAt().toLocaleString());
        }

        if (latestSoundData != null) {
            TextView textView = (TextView)findViewById(R.id.sound_text);
            textView.setText(latestSoundData.getQuantity() + " on " + latestSoundData.getRecordedAt().toLocaleString());
        }

        if (latestBehaviorData != null) {
            TextView textView = (TextView)findViewById(R.id.behavior_text);
            textView.setText(latestBehaviorData.getQuantity() + " on " + latestBehaviorData.getRecordedAt().toLocaleString());
        }
    }

    public void onTextViewClick(View v) {
        String dataType = null;
        switch (v.getId()) {
            case R.id.wake_header_text:
            case R.id.wake_text:
                dataType = "wake";
                break;
            case R.id.sleep_header_text:
            case R.id.sleep_text:
                dataType = "sleep";
                break;
            case R.id.sound_header_text:
            case R.id.sound_text:
                dataType = "sound";
                break;
            case R.id.behavior_header_text:
            case R.id.behavior_text:
                dataType = "behavior";
                break;
        }

        if (dataType != null) {
            Intent intent = new Intent(getApplicationContext(), DataDetailActivity.class);
            intent.putExtra(DataDetailActivity.DATA_TYPE, dataType);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up deviceApprovalImageButton, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Intent intent;
        switch (id) {
            case R.id.action_behavior:
                intent = new Intent(getApplicationContext(), BehaviorActivity.class);
                startActivity(intent);
                break;
            case R.id.action_data:
                intent = new Intent(getApplicationContext(), DataActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_devices:
                intent = new Intent(getApplicationContext(), DeviceActivity.class);
                startActivity(intent);
                break;
            case R.id.action_trends:
                intent = new Intent(getApplicationContext(), TrendsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_logout:
                TokenHelper.deleteTokens(getApplicationContext());
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
