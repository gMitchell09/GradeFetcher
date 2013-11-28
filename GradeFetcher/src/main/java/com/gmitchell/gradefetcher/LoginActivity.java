package com.gmitchell.gradefetcher;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
    /**
     * The default email to populate the email field with.
     */
    public static final String EXTRA_EMAIL = "com.gmitchell.gradefetcher.extra.EMAIL";
	public static final String EXTRA_URL = "com.gmitchell.gradefetcher.extra.URL";
	public static final String SCRIPT_URL = "http://gMitchell09.alwaysdata.net/angel/getGrades.py";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;
	private String mUrl;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
	private EditText mUrlView;

    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;

	private JSONObject mJSONObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setText(mEmail);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

	    mUrl = getIntent().getStringExtra(EXTRA_URL);
	    mUrlView = (EditText) findViewById(R.id.angel_url);
	    mEmailView.setText(mUrl);

        mLoginFormView = findViewById(R.id.login_form);
        mLoginStatusView = findViewById(R.id.login_status);
        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 4) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (false && !mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
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

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate()
                    .setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
	        if (mEmail.contentEquals("TEST")) return false;

	        AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
            final HttpPost postRequest = new HttpPost(SCRIPT_URL);
            postRequest.setHeader("username", mEmail);
            postRequest.setHeader("password", mPassword);
            postRequest.setHeader("angelUrl", mUrl);
            postRequest.setHeader("json", "on");
            postRequest.setHeader("class", "*");

            List<NameValuePair> form = new ArrayList<NameValuePair>();
            form.add(new BasicNameValuePair("username", mEmail));
            form.add(new BasicNameValuePair("password", mPassword));
            form.add(new BasicNameValuePair("angelUrl", mUrl));
            form.add(new BasicNameValuePair("json", "on"));
            form.add(new BasicNameValuePair("class", "*"));

            try {
                postRequest.setEntity(new UrlEncodedFormEntity(form, HTTP.UTF_8));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            try {
		        HttpResponse response = client.execute(postRequest);
		        final int statusCode = response.getStatusLine().getStatusCode();
		        if (statusCode != HttpStatus.SC_OK) {
			        Log.e("Angel Connect", "Error: " + statusCode + " while logging into Angel: " + SCRIPT_URL);
			        return false;
		        }

		        final HttpEntity entity = response.getEntity();
		        if (entity != null) {
			        InputStream inputStream = null;
			        String result = null;
			        try {
				        inputStream = entity.getContent();
				        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				        StringBuilder stringBuilder = new StringBuilder();

				        String line = null;
				        while((line = reader.readLine()) != null) {
							stringBuilder.append(line + "\n");
				        }
						result = stringBuilder.toString();
			        }
			        catch (Exception e) {
				        e.printStackTrace();
			        }
			        finally {
				        try {
					        if (inputStream != null) inputStream.close();
				        }
				        catch (Exception squish) {}
			        }
                    Log.e("Grade Fetcher", result);
			        mJSONObject = new JSONObject(result);
		        }
            }
            catch (Exception e) {
				Log.e("Angel Connect", e.toString());
			    return false;
		    } finally {
		        try {client.close();} catch (Exception squish) {}
	        }
	        return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                callNextStep();
            } else {
	            try {
		            mJSONObject = new JSONObject("{\"EE 384-01 DIG SIGNAL PROCESS LAB (FA13)\": [{\"assignments\": [{\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW1\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW2\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW3\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW4\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW5\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW6\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW7\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW8\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW9\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW10\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"HW11\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"In Class Assignment1\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"In Class Assignment2\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"In Class Assignment3\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"In Class Assignmenet4\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 60.0, \"score\": 0.0, \"name\": \"In Class Assignmenet5\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 0, \"score\": 0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"Homework\"}, {\"assignments\": [{\"maxPoints\": 0, \"score\": 0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"Quizzes\"}, {\"assignments\": [{\"maxPoints\": 0, \"score\": 0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"Attendence\"}, {\"assignments\": [{\"maxPoints\": 0, \"score\": 0, \"name\": \"Final Grade\", \"comments\": \"\"}], \"name\": \"FinalGrade\"}], \"CPE 323-01 INTRO TO EMBEDDED COMPUTER SYS (FA13)\": [{\"assignments\": [{\"maxPoints\": 100.0, \"score\": 98.0, \"name\": \"HW1\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 94.5, \"name\": \"HW2\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 97.0, \"name\": \"HW3\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 97.75, \"name\": \"HW4\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"HW5\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 500.0, \"score\": 387.25, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 1}], \"name\": \"Homework\"}, {\"assignments\": [{\"maxPoints\": 20.0, \"score\": 21.0, \"name\": \"Quiz 1\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 20.0, \"score\": 22.0, \"name\": \"Quiz 2\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 20.0, \"score\": 22.0, \"name\": \"Quiz 3\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 20.0, \"score\": 16.0, \"name\": \"Quiz 4\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 20.0, \"score\": 0.0, \"name\": \"Quiz 5\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 81.0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 1}], \"name\": \"Quizzes\"}, {\"assignments\": [{\"maxPoints\": 100.0, \"score\": 91.0, \"name\": \"Midterm\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"Final\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 200.0, \"score\": 91.0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 1}], \"name\": \"Exams\"}, {\"assignments\": [{\"maxPoints\": 800.0, \"score\": 559.25, \"name\": \"Final Grade\", \"comments\": \"\"}], \"name\": \"FinalGrade\"}], \"CPE 325-03 EMBEDDED SYSTEMS LAB (FA13)\": [{\"assignments\": [{\"maxPoints\": 100.0, \"score\": 100.0, \"name\": \"Assignment 1\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 100.0, \"name\": \"Assignment 2\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 100.0, \"name\": \"Assignment 3\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 100.0, \"name\": \"Assignment 4\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 100.0, \"name\": \"Assignment 5\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 95.0, \"name\": \"Assignment 6\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 92.0, \"name\": \"Assignment 7\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 100.0, \"name\": \"Assignment 8\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"Assignment 9\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"Assignment 10\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"Assignment 11\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"Assignment 12\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 100.0, \"score\": 0.0, \"name\": \"Assignment 13\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 800.0, \"score\": 787.0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"Lab Assignments\"}, {\"assignments\": [{\"maxPoints\": 10.0, \"score\": 10.0, \"name\": \"Quiz 1\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 10.0, \"score\": 10.0, \"name\": \"Quiz 2\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 10.0, \"score\": 9.0, \"name\": \"Quiz 3\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 10.0, \"score\": 9.25, \"name\": \"Quiz 4\", \"comments\": \"\", \"Graded\": 1}, {\"maxPoints\": 10.0, \"score\": 0.0, \"name\": \"Quiz 5\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 10.0, \"score\": 0.0, \"name\": \"Quiz 6\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 40.0, \"score\": 38.25, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"Quizzes\"}, {\"assignments\": [{\"maxPoints\": 840.0, \"score\": 825.25, \"name\": \"Final Grade\", \"comments\": \"\"}], \"name\": \"FinalGrade\"}], \"Classes\": [\"CPE 323-01 INTRO TO EMBEDDED COMPUTER SYS (FA13)\", \"EE 384-01 DIG SIGNAL PROCESS LAB (FA13)\", \"CPE 448-01 INTRO TO COMPUTER NETWORKS (FA13)\", \"CPE 353-01 SOFTWARE DESIGN & ENGINEERING (FA13)\", \"CPE 325-03 EMBEDDED SYSTEMS LAB (FA13)\"], \"CPE 353-01 SOFTWARE DESIGN & ENGINEERING (FA13)\": [{\"assignments\": [{\"maxPoints\": 10.0, \"score\": 8.5999999999999996, \"name\": \"Project01 Dropbox\", \"comments\": \"Messages output for Dequeue, Front, and Rear differ from sample solution for empty condition. Sample solution tells you exactly what I want to see. Had this been a production environment where I had a team writing documentation based upon the specification (sample solution), the docs and your code would now be different since you deviated from the interface specification, costing more $$$ to make the two match.\", \"Graded\": 1}, {\"maxPoints\": 10.0, \"score\": 10.0, \"name\": \"Project02 Dropbox\", \"comments\": \" Good work!!\", \"Graded\": 1}, {\"maxPoints\": 20.0, \"score\": 18.600000000000001, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 1}], \"name\": \"Individual Assignments\"}, {\"assignments\": [{\"maxPoints\": 0, \"score\": 0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"SQLMidterm\"}, {\"assignments\": [{\"maxPoints\": 10.0, \"score\": 0.0, \"name\": \"QtMidtermA\", \"comments\": \"\", \"Graded\": 0}, {\"maxPoints\": 10.0, \"score\": 10.0, \"name\": \"QtMidtermB\", \"comments\": \" Great job!\", \"Graded\": 1}, {\"maxPoints\": 10.0, \"score\": 10.0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"QtMidterm\"}, {\"assignments\": [{\"maxPoints\": 0, \"score\": 0, \"name\": \"Total\", \"comments\": \"\", \"Graded\": 0}], \"name\": \"FinalExam\"}, {\"assignments\": [{\"maxPoints\": 30.0, \"score\": 28.600000000000001, \"name\": \"Final Grade\", \"comments\": \"\"}], \"name\": \"FinalGrade\"}], \"CPE 448-01 INTRO TO COMPUTER NETWORKS (FA13)\": [{\"assignments\": [{\"maxPoints\": 0, \"score\": 0, \"name\": \"Final Grade\", \"comments\": \"\"}], \"name\": \"FinalGrade\"}]}");
	            } catch (JSONException e) {
		            e.printStackTrace();
	            }
	            callNextStep();
	            //mPasswordView.setError(getString(R.string.error_incorrect_password));
                //mPasswordView.requestFocus();
            }
        }

	    protected void callNextStep() {
		    Intent intent = new Intent(LoginActivity.this, ClassTableActivity.class);
		    intent.putExtra("json", mJSONObject.toString());
		    startActivity(intent);
		    //finish();
	    }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
