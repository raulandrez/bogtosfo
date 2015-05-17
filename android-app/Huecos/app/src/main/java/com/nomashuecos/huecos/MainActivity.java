package com.nomashuecos.huecos;

import com.nomashuecos.huecos.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements LocationListener, SensorEventListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;


    private SensorManager mSensorManager;
    private Sensor mSensor;

    private float Px,Py,Pz;

    private float x,y,z;

    private float cal;

    private double Longitud;
    private double Latitud;

    private Long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        cal = 5;
        TextView Tcal = (TextView) findViewById(R.id.TextCal);
        Tcal.setText(String.valueOf(cal));


        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    public void onLocationChanged(Location loc) {

        Longitud = loc.getLongitude();
        Latitud =  loc.getLatitude();

        TextView Long = (TextView)findViewById(R.id.textView2);
        Long.setText(String.valueOf(Longitud));
        TextView Lat = (TextView)findViewById(R.id.textView4);
        Lat.setText(String.valueOf(Latitud));
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume()
    {
        super.onResume();
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size()>0)
        {
            mSensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    protected void onPause()
    {
        mSensorManager.unregisterListener(this, mSensor);
        super.onPause();
    }

    protected void onStop()
    {
        mSensorManager.unregisterListener(this, mSensor);
        super.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        time = System.currentTimeMillis();

        float[] values = event.values;
        // Movement
         x = Px - values[0];
         y = Py - values[1];
         z = Pz - values[2];

        boolean HUECO = false;

        if(x>cal) {
            TextView Tx = (TextView) findViewById(R.id.textView5);
            Tx.setText(String.valueOf(x));
            HUECO = true;
        }
        if(y>cal) {
             TextView Ty = (TextView) findViewById(R.id.textView6);
             Ty.setText(String.valueOf(y));
            HUECO = true;
         }
        if(z>cal) {
            TextView Tz = (TextView) findViewById(R.id.textView7);
            Tz.setText(String.valueOf(z));
            HUECO = true;
        }

        if(HUECO) {

            final JSONObject hole = new JSONObject();
            try{
                hole.put("track_id", "1");
                hole.put("x", Longitud);
                hole.put("y", Latitud);
                hole.put("t", time);
                hole.put("ax", new Float(x));
                hole.put("ay", new Float(y));
                hole.put("az", new Float(z));
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            TextView TextJason = (TextView) findViewById(R.id.TextJason);
            //TextJason.setText(String.valueOf(hole.toString()));

            //Thread myThread = new Thread(new Runnable(){
            //    @Override
            //    public void run()
            //    {
                    //SendJSON(hole);
                    String url = "http://192.168.1.16/bogtosfo/server-app/addpoint.php?track_id=1&x="+Longitud+"&y="+Latitud+"&t="+time+"&ax="+x+"&ay="+y+"&z="+z;

                    if (android.os.Build.VERSION.SDK_INT > 9) {
                        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                        StrictMode.setThreadPolicy(policy);
                    }

                    HttpClient client = new DefaultHttpClient();
                    HttpConnectionParams.setConnectionTimeout(client.getParams(), 100000);

                    HttpPost post = new HttpPost(url);
                    //HttpGet get = new HttpGet(url);
                    //TextView TextJason = (TextView) findViewById(R.id.TextJason);
                    try {
                        StringEntity se = new StringEntity("json="+hole.toString());
                        post.setHeader("Accept", "application/json");
                        post.setHeader("Content-type", "application/json");


                        //post.setEntity(se);

                        HttpResponse response;
                        response = client.execute(post);
                        HttpResponse httpResponse = client.execute(post);

                        // 9. receive response as inputStream
                        InputStream inputStream = null;
                        inputStream = httpResponse.getEntity().getContent();

                        // 10. convert inputstream to string
                        if(inputStream != null)

                        TextJason.setText(convertInputStreamToString(inputStream));
                        //Log.i("Response from server", jsonResponse.getString("msg"));
                    } catch (Exception e) {
                        e.printStackTrace();
                        //TextJason.setText(e.getMessage());
                    }



        //        }
        //    });


        //    myThread.start();


        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


    public void MenusButtonClick(View view) {
        cal = cal - (float)0.5;
        TextView Tcal = (TextView) findViewById(R.id.TextCal);
        Tcal.setText(String.valueOf(cal));

    }



    public void MoreButtonClick(View view) {
        cal = cal + (float)0.5;
        TextView Tcal = (TextView) findViewById(R.id.TextCal);
        Tcal.setText(String.valueOf(cal));
    }

    private void SendJSON(JSONObject hole) {
        String url = "http://192.168.1.16/bogtosfo/server-app/addpoint.php";
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(), 100000);

        HttpPost post = new HttpPost(url);
        TextView TextJason = (TextView) findViewById(R.id.TextJason);
        try {
            StringEntity se = new StringEntity("json="+hole.toString());
            post.addHeader("content-type", "application/x-www-form-urlencoded");
            post.setEntity(se);

            HttpResponse response;
            response = client.execute(post);
            //String resFromServer = org.apache.http.util.EntityUtils.toString(response.getEntity());

            //JSONObject jsonResponse = new JSONObject(resFromServer);
            //TextJason.setText(jsonResponse.toString());
            //Log.i("Response from server", jsonResponse.getString("msg"));
        } catch (Exception e) {
            e.printStackTrace();
            TextJason.setText(e.getMessage());
        }
    }
}
