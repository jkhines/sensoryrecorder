package edu.asu.jkhines1.dssr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

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

public class DataDetailActivity extends AppCompatActivity {
    public static final String DATA_TYPE = "dataType";
    private View mProgressView;
    private View mDataDetailFormView;
    private GetDataTask mAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the UI.
        setContentView(R.layout.activity_data_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDataDetailFormView = findViewById(R.id.data_detail_form);
        mProgressView = findViewById(R.id.data_detail_progress);

        loadData();
    }

    // Show the progress bar and load the data.
    private void loadData() {
        showProgress(true);
        String dataType = getIntent().getStringExtra(DATA_TYPE);

        // Set the Activity header text.
        TextView textView = (TextView)findViewById(R.id.data_detail_header_text);
        textView.setText(Character.toUpperCase(dataType.charAt(0)) + dataType.substring(1));

        // Retrieve the data and populate the UI.
        mAsyncTask = new GetDataTask(dataType);
        mAsyncTask.execute((Void) null);
    }

    public class GetDataTask extends AsyncTask<Void, Void, Boolean> {
        public String response;
        public String mDataType;

        GetDataTask(String dataType) {
            mDataType = dataType;
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
                                "?filter=" + mDataType,
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
            mDataDetailFormView.requestFocus();
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

            mDataDetailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDataDetailFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDataDetailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDataDetailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void processResponse(String response) {
        try {
            // Deserialize the list of data.
            ObjectMapper mapper = new ObjectMapper();
            List<InputData> inputData = mapper.readValue(response, new TypeReference<List<InputData>>() {});
            if (inputData.size() == 0) {
                return;
            }

            // create the adapters
            List<String> dataDetails = new ArrayList<String>();
            ArrayAdapter<String> dataDetailAdapter = new ArrayAdapter<String>(this,
                    R.layout.data_detail_listitem, R.id.data_detail_text, dataDetails);

            // Associate the adapters with the list views.
            ListView dataDetailListView = (ListView)findViewById(R.id.data_detail_listview);
            dataDetailListView.setAdapter(dataDetailAdapter);

            Long min = Long.MAX_VALUE;
            Long max = Long.MIN_VALUE;
            for (InputData data : inputData) {
                Long q = (data.getQuantity() == null) ? (data.getRecordedAt().getHours() * 60) + data.getRecordedAt().getMinutes() : data.getQuantity();
                if (q < min) {
                    min = q;
                }
                if (q > max) {
                    max = q;
                }
            }

            // Initialize the graph.
            GraphView graph = (GraphView) findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>();

            // Styling
            int color = 0;
            switch (inputData.get(0).getType()) {
                case "behavior":
                    color = Color.parseColor("#FE9A2E"); // orange
                    break;
                case "sleep":
                case "wake":
                    color = Color.parseColor("#088A29"); // green
                    break;
                case "sound":
                    color = Color.parseColor("#642EFE"); // blue
                    break;
            }
            series.setColor(color);
            series.setDrawDataPoints(true);
            series.setDrawBackground(true);

            // Customize the graph appearance.
            Viewport viewport = graph.getViewport();
            viewport.setYAxisBoundsManual(true);
            viewport.setMinY(min);
            viewport.setMaxY(max);
            viewport.setXAxisBoundsManual(true);
            viewport.setMinX(1);
            viewport.setMaxX(inputData.size());
            viewport.setScrollable(true);

            // Populate the adapters and graph.
            int currentItem = 1;
            for (InputData data : inputData) {
                if (data.getQuantity() != null) {
                    series.appendData(new DataPoint(currentItem++, data.getQuantity()), true, inputData.size());
                    dataDetails.add(data.getQuantity() + " on " + data.getRecordedAt().toLocaleString());
                } else {
                    series.appendData(new DataPoint(currentItem++,
                            (data.getRecordedAt().getHours() * 60) + data.getRecordedAt().getMinutes()),
                            true, inputData.size());
                    dataDetails.add(data.getRecordedAt().toLocaleString());
                }

            }
            graph.addSeries(series);
        }
        catch (IOException ex) {
        }
    }
}
