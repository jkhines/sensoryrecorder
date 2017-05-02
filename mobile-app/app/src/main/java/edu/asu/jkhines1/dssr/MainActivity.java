package edu.asu.jkhines1.dssr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

import java.io.IOException;

import edu.asu.jkhines1.dssr.utils.TokenHelper;
import edu.asu.jkhines1.dssr.models.Tokens;

public class MainActivity extends AppCompatActivity {
    private View mProgressView;
    private View mMainFormView;
    private CheckLoginTask mAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Draw the UI.
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMainFormView = findViewById(R.id.main_form);
        mProgressView = findViewById(R.id.main_progress);

        checkLogin();
    }

    // Show the progress bar and load the data.
    private void checkLogin() {
        showProgress(true);
        mAsyncTask = new CheckLoginTask();
        mAsyncTask.execute((Void) null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mMainFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class CheckLoginTask extends AsyncTask<Void, Void, Boolean> {
        public String response;

        CheckLoginTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            Tokens tokens = null;
            try {
                // Get the access and refresh tokens.
                tokens = TokenHelper.getTokens(getApplicationContext());
            } catch (Exception e) {
                return false;
            }

            return (tokens != null);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAsyncTask = null;
            showProgress(false);
            if (success) {
                Intent dataActivity = new Intent(getApplicationContext(), DataActivity.class);
                startActivity(dataActivity);
                finish();
            } else {
                mMainFormView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAsyncTask = null;
            showProgress(false);
        }
    }

    public void showLogin(View view) {
        Intent loginActivity = new Intent(this, LoginActivity.class);
        startActivity(loginActivity);
    }

    public void showRegister(View view) {
        Intent registerActivity = new Intent(this, RegisterActivity.class);
        startActivity(registerActivity);
    }
}
