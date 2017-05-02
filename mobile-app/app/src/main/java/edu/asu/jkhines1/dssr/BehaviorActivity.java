package edu.asu.jkhines1.dssr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import edu.asu.jkhines1.dssr.models.AccessToken;
import edu.asu.jkhines1.dssr.models.InputData;
import edu.asu.jkhines1.dssr.models.Tokens;
import edu.asu.jkhines1.dssr.utils.ApiHelper;
import edu.asu.jkhines1.dssr.utils.ApplicationConfig;
import edu.asu.jkhines1.dssr.utils.TokenHelper;

public class BehaviorActivity extends AppCompatActivity {
    private View mProgressView;
    private View mBehaviorFormView;
    private AutoCompleteTextView mRatingView;
    private SaveBehaviorRatingTask mAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the UI.
        setContentView(R.layout.activity_behavior);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRatingView = (AutoCompleteTextView) findViewById(R.id.rating);

        Button mEmailSignInButton = (Button) findViewById(R.id.save_rating_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSaveBehaviorRating();
            }
        });

        mBehaviorFormView = findViewById(R.id.behavior_form);
        mProgressView = findViewById(R.id.behavior_progress);
    }

    private void attemptSaveBehaviorRating() {
        if (mAsyncTask != null) {
            return;
        }

        // Reset errors.
        mRatingView.setError(null);

        // Store values at the time of the login attempt.
        String rating = mRatingView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(rating)) {
            mRatingView.setError(getString(R.string.error_field_required));
            focusView = mRatingView;
            cancel = true;
        } //else if (!isRatingValid(rating)) {
//            mRatingView.setError(getString(R.string.error_invalid_rating));
//            focusView = mRatingView;
//            cancel = true;
//        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAsyncTask = new SaveBehaviorRatingTask(rating);
            mAsyncTask.execute((Void) null);
        }
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mBehaviorFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mBehaviorFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBehaviorFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mBehaviorFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class SaveBehaviorRatingTask extends AsyncTask<Void, Void, Boolean> {

        private final String mRating;

        SaveBehaviorRatingTask(String rating) {
            mRating = rating;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Get the access and refresh tokens.
                Tokens tokens = TokenHelper.getTokens(getApplicationContext());
                AccessToken accessToken = AccessToken.getInstance(tokens.getAccessToken());

                // Save the data.
                InputData data = new InputData();
                data.setUsername(accessToken.getSubject());
                data.setType("behavior");
                data.setQuantity(Long.parseLong(mRating));
                ApiHelper.saveData(ApplicationConfig.API_SERVER_URL + "/data",
                        tokens.getAccessToken(), data);

            } catch (Exception e) {
                return false;
            }

            Intent dataActivity = new Intent(getApplicationContext(), DataActivity.class);
            startActivity(dataActivity);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAsyncTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mRatingView.setError(getString(R.string.error_invalid_rating));
                mRatingView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAsyncTask = null;
            showProgress(false);
        }
    }

}
