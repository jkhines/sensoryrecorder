package edu.asu.jkhines1.dssr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.test.espresso.core.deps.guava.base.Strings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.asu.jkhines1.dssr.models.AccessToken;
import edu.asu.jkhines1.dssr.models.InputData;
import edu.asu.jkhines1.dssr.models.Tokens;
import edu.asu.jkhines1.dssr.utils.ApiHelper;
import edu.asu.jkhines1.dssr.utils.ApplicationConfig;
import edu.asu.jkhines1.dssr.utils.TokenHelper;

public class TrendsActivity extends AppCompatActivity {
    public static final String WEEK = "week";
    public static final String MONTHS = "week";
    private View mProgressView;
    private View mTrendsFormView;
    private GetDataTask mAsyncTask = null;
    private LineGraphSeries<DataPoint> series;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the UI.
        setContentView(R.layout.activity_trends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTrendsFormView = findViewById(R.id.trends_form);
        mProgressView = findViewById(R.id.trends_progress);

        GraphView combinedBarGraph = (GraphView) findViewById(R.id.all_data_sources_bar_graph);
        combinedBarGraph.getLegendRenderer().setFixedPosition(1, 1);
        combinedBarGraph.getLegendRenderer().setVisible(true);
        // Custom label formatter to show day of month instead of day of year.
        combinedBarGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // Convert x values into the date.
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_YEAR, (int)value);
                    int label = calendar.get(Calendar.DAY_OF_MONTH);
                    return super.formatLabel(label, isValueX);
                } else {
                    // Do not modify Y values.
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        GraphView combinedLineGraph = (GraphView) findViewById(R.id.all_data_sources_line_graph);
        combinedLineGraph.getLegendRenderer().setFixedPosition(1, 1);
        combinedLineGraph.getLegendRenderer().setVisible(true);
        // Custom label formatter to show day of month instead of day of year.
        combinedLineGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // Convert x values into the date.
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.DAY_OF_YEAR, (int)value);
                    int label = calendar.get(Calendar.DAY_OF_MONTH);
                    return super.formatLabel(label, isValueX);
                } else {
                    // Do not modify Y values.
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        // Load the data.

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        int selected = radioGroup.getCheckedRadioButtonId();
        onRadioButtonClicked(findViewById(selected));
    }

    public void onRadioButtonClicked(View view) {
        RadioButton radioButton = (RadioButton) view;
        loadData(radioButton.getText().toString().toLowerCase());
    }

    // Show the progress bar and load the data.
    private void loadData(String duration) {
        showProgress(true);

        // Retrieve the data and populate the UI.
        mAsyncTask = new GetDataTask(duration);
        mAsyncTask.execute((Void) null);
    }

    public class GetDataTask extends AsyncTask<Void, Void, Boolean> {
        public Map<String, String> responses = new HashMap<>();
        private String mDuration;

        GetDataTask(String duration) {
            mDuration = duration;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Get the access and refresh tokens.
                Tokens tokens = TokenHelper.getTokens(getApplicationContext());

                // Get the data.
                for (String dataType : new String[] {"behavior", "rest", "sound"}){
                    String response = ApiHelper.getResponse(
                            ApplicationConfig.API_SERVER_URL + "/reports/" +
                                    AccessToken.getInstance(tokens.getAccessToken()).getSubject() +
                                    "?filter=" + dataType +
                                    "&duration=" + mDuration,
                            tokens.getAccessToken());

                    responses.put(dataType, response);
                }
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
                processResponse(this.responses);
                //finish();
            }
            mTrendsFormView.requestFocus();
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

            mTrendsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mTrendsFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTrendsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mTrendsFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void processResponse(Map<String, String> responses) {
        if (responses == null) {
            return;
        }

        try {
            GraphView combinedBarGraph = (GraphView) findViewById(R.id.all_data_sources_bar_graph);
            combinedBarGraph.removeAllSeries();
            GraphView combinedLineGraph = (GraphView) findViewById(R.id.all_data_sources_line_graph);
            combinedLineGraph.removeAllSeries();

            Calendar minDate = Calendar.getInstance();
            Calendar maxDate = Calendar.getInstance();
            maxDate.setTimeInMillis(0);

            for (String dataType : new String[] { "behavior", "rest", "sound" }) {
                // Deserialize the list of data.
                ObjectMapper mapper = new ObjectMapper();
                if (responses.containsKey(dataType)) {
                    String response = responses.get(dataType);
                    if (!Strings.isNullOrEmpty(response)) {
                        List<InputData> inputData = mapper.readValue(response, new TypeReference<List<InputData>>() {
                        });
                        if (inputData.size() == 0) {
                            continue;
                        }

                        // Initialize date ranges.
                        Calendar minDataDate = Calendar.getInstance();
                        minDataDate.setTime(inputData.get(0).getRecordedAt());
                        if (minDate.compareTo(minDataDate) > 0) {
                            minDate = minDataDate;
                        }
                        Calendar maxDataDate = Calendar.getInstance();
                        maxDataDate.setTime(inputData.get(inputData.size() - 1).getRecordedAt());
                        if (maxDate.compareTo(maxDataDate) < 0) {
                            maxDate = maxDataDate;
                        }

                        GraphView dataDetailGraph = null;
                        int color = 0;
                        double goal = 0;
                        switch (dataType) {
                            case "behavior":
                                dataDetailGraph = (GraphView) findViewById(R.id.behavior_graph);
                                color = Color.parseColor("#FE9A2E"); // orange
                                goal = 8;
                                break;
                            case "rest":
                                dataDetailGraph = (GraphView) findViewById(R.id.rest_graph);
                                color = Color.parseColor("#088A29"); // green
                                goal = 10;
                                break;
                            case "sound":
                                dataDetailGraph = (GraphView) findViewById(R.id.sound_graph);
                                color = Color.parseColor("#642EFE"); // blue
                                goal = 125;
                                break;
                        }

                        if (dataDetailGraph == null) {
                            return;
                        }

                        // data
                        BarGraphSeries<DataPoint> combinedBarGraphSeries = new BarGraphSeries<>();
                        LineGraphSeries<DataPoint> combinedLineGraphSeries = new LineGraphSeries<>();
                        BarGraphSeries<DataPoint> dataDetailBarGraphSeries = new BarGraphSeries<>();
                        LineGraphSeries<DataPoint> dataDetailLineGraphSeries = new LineGraphSeries<>();

                        Double minValue = Double.MAX_VALUE;
                        Double maxValue = Double.MIN_VALUE;
                        for (InputData data : inputData) {
                            Double value = data.getType().equals("rest") ?  ((double)data.getQuantity()) / 60 : data.getQuantity();

                            if (value < minValue) {
                                minValue = value;
                            }
                            if (value > maxValue) {
                                maxValue = value;
                            }
                        }
                        int lastX = 1;
                        for (InputData data : inputData) {
                            Double value = data.getType().equals("rest") ? ((double)data.getQuantity()) / 60 : data.getQuantity();
                            dataDetailBarGraphSeries.appendData(new DataPoint(lastX++, value), true, 50);

                            // Scaled quantity
                            Long scaledQuantity = (maxValue - minValue == 0) ? 0 : (long)(((value - minValue) * 10) / (maxValue - minValue));
                            // Day of year
                            Calendar recordedAt = Calendar.getInstance();
                            recordedAt.setTime(data.getRecordedAt());

                            combinedBarGraphSeries.appendData(new DataPoint(recordedAt.get(Calendar.DAY_OF_YEAR), scaledQuantity), true, 50);
                            combinedLineGraphSeries.appendData(new DataPoint(recordedAt.get(Calendar.DAY_OF_YEAR), scaledQuantity), true, 50);
                        }

                        dataDetailLineGraphSeries.appendData(new DataPoint(0, goal), true, 50);
                        dataDetailLineGraphSeries.appendData(new DataPoint(lastX, goal), true, 50);

                        // Add the series
                        combinedBarGraph.addSeries(combinedBarGraphSeries);
                        combinedLineGraph.addSeries(combinedLineGraphSeries);
                        dataDetailGraph.removeAllSeries();
                        dataDetailGraph.addSeries(dataDetailBarGraphSeries);
                        dataDetailGraph.addSeries(dataDetailLineGraphSeries);

                        // styling
                        Viewport combinedBarGraphViewport = combinedBarGraph.getViewport();
                        combinedBarGraphViewport.setYAxisBoundsManual(true);
                        combinedBarGraphViewport.setMinY(0);
                        combinedBarGraphViewport.setMaxY(10);
                        combinedBarGraphViewport.setXAxisBoundsManual(true);
                        combinedBarGraphViewport.setMinX(minDate.get(Calendar.DAY_OF_YEAR) - 1);
                        combinedBarGraphViewport.setMaxX(maxDate.get(Calendar.DAY_OF_YEAR) + 1);
                        combinedBarGraphViewport.setScrollable(true);

                        combinedBarGraphSeries.setTitle(dataType);
                        combinedBarGraphSeries.setColor(color);
                        combinedBarGraphSeries.setSpacing(50);

                        // draw values on top
                        combinedBarGraphSeries.setDrawValuesOnTop(true);
                        combinedBarGraphSeries.setValuesOnTopColor(color);
                        //series.setValuesOnTopSize(50);

                        // styling
                        Viewport combinedLineGraphViewport = combinedLineGraph.getViewport();
                        combinedLineGraphViewport.setYAxisBoundsManual(true);
                        combinedLineGraphViewport.setMinY(0);
                        combinedLineGraphViewport.setMaxY(10);
                        combinedLineGraphViewport.setXAxisBoundsManual(true);
                        combinedLineGraphViewport.setMinX(minDate.get(Calendar.DAY_OF_YEAR) - 1);
                        combinedLineGraphViewport.setMaxX(maxDate.get(Calendar.DAY_OF_YEAR) + 1);
                        combinedLineGraphViewport.setScrollable(true);

                        combinedLineGraphSeries.setTitle(dataType);
                        combinedLineGraphSeries.setColor(color);
                        combinedLineGraphSeries.setDrawDataPoints(true);

                        // styling
                        Viewport dataDetailViewport = dataDetailGraph.getViewport();
                        dataDetailViewport.setYAxisBoundsManual(true);
                        dataDetailViewport.setMinY(minValue - 5);
                        dataDetailViewport.setMaxY(maxValue);
                        dataDetailViewport.setXAxisBoundsManual(true);
                        dataDetailViewport.setMinX(0);
                        dataDetailViewport.setMaxX(inputData.size() + 1);
                        dataDetailViewport.setScrollable(true);

                        dataDetailBarGraphSeries.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                            @Override
                            public int get(DataPoint data) {
                                return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
                            }
                        });
                        dataDetailBarGraphSeries.setSpacing(50);

                        // draw values on top
                        dataDetailBarGraphSeries.setDrawValuesOnTop(true);
                        dataDetailBarGraphSeries.setValuesOnTopColor(Color.BLACK);
                        //series.setValuesOnTopSize(50);

                        // styling
                        dataDetailLineGraphSeries.setColor(Color.parseColor("#FF0000"));
                    }
                }
            }
        }
        catch (IOException ex) {
        }
    }
}
