package com.amabdelsalam.cityweather2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    EditText cityNameEt;
    TextView resultTextView;
    TextView summaryTextView;
    ImageView iconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityNameEt = (EditText) findViewById(R.id.cityName);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        summaryTextView = (TextView) findViewById(R.id.summaryTextView);
        iconImage = (ImageView) findViewById(R.id.iconImage);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void findWeather(View view) {

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(cityNameEt.getWindowToken(), 0);
        if (isNetworkConnected()) {

        String encodedCityName = null;
        try {
            encodedCityName = URLEncoder.encode(cityNameEt.getText().toString(), "UTF-8");
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute("http://api.openweathermap.org/data/2.5/weather?q=" + encodedCityName + "&appid=a63a29f1f792127b4ba232a4651bc5a1");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(getApplicationContext(), "Could not find weather...", Toast.LENGTH_LONG);
        }
        }else{
            Toast.makeText(getApplicationContext(), "Device not connected to internet...", Toast.LENGTH_LONG);
        }


        Log.i("cityName", cityNameEt.getText().toString());
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                Log.i("urls[0]", urls[0]);
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;

                    result += current;

                    data = reader.read();
                }

                Log.i("result", result);

                return result;

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Could not find weather...", Toast.LENGTH_LONG);
            }

            return null;
        }

//        public Bitmap getImage(String imageUrl){
//            try {
//                URL url = new URL(imageUrl);
//                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//                connection.connect();
//                InputStream inputStream = connection.getInputStream();
//                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
//                return myBitmap;
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("Enter", "onPostExecute");

            super.onPostExecute(result);

            String weatherInfo = "";
            try {
                String message = "";
                JSONObject jsonObject = new JSONObject(result);
                weatherInfo = jsonObject.getString("weather");

                JSONArray arr = new JSONArray(weatherInfo);
                String main = "";
                String description = "";
                String icon = "";
                //int length = arr.length();
                Log.i("arr.length", Integer.toString(arr.length()));

                for (int i = 0; i < arr.length(); i++) {
                    JSONObject jsonPart = arr.getJSONObject(i);

                    main = jsonPart.getString("main");
                    Log.i("main", main);

                    description = jsonPart.getString("description");
                    Log.i("description", description);

                    icon = jsonPart.getString("icon");
                    Log.i("icon", icon);

                    if (main != "" && description != "") {
                        summaryTextView.setText(main + ": " + description);
                    }

                    Log.i("message", message);

                    if (icon != "") {
                        String imageUrl = "http://openweathermap.org/img/w/" + icon + ".png";
                        Log.i("imageUrl", imageUrl);

                        ImageDownloader task = new ImageDownloader();
                        Bitmap myImage = null;
                        try {
                            myImage = task.execute(imageUrl).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }


                        // Setting the ImageView
                        iconImage.setImageBitmap(myImage);

                    }

                }

                String DEGREE = "\u00b0";


                //JSONObject jsonObject = new JSONObject(result);
                String tempInfo = jsonObject.getString("main");

                //Log.i("tempInfo", tempInfo);

                //JSONArray tempArr = new JSONArray(tempInfo);

                //Log.i("tempArr", Integer.toString(tempArr.length()));

                //for (int i=0 ; i < tempArr.length() ;i++){
                JSONObject jsonPart = new JSONObject(tempInfo);

                String tempFahrenheit = jsonPart.getString("temp");
                double tempCelsius = Double.parseDouble(tempFahrenheit) - 273.15;

                Log.i("temp", tempFahrenheit);
                Log.i("temp_c", tempFahrenheit);
                if (tempFahrenheit != "") {
                    String tempCelsiusString = new DecimalFormat("##.#").format(tempCelsius);
                    message += "Temperature: \t\t\t\t\t" + tempCelsiusString + DEGREE + "C - " + tempFahrenheit + DEGREE + "F \r\n";
                }

                String humidity = jsonPart.getString("humidity");
                Log.i("humidity", humidity);

                if (humidity != "") {
                    message += "Humidity: \t\t\t\t\t\t\t\t" + humidity + "% \r\n";
                }

                String tempMinFahrenheit = jsonPart.getString("temp_min");
                double tempMinCelsius = Double.parseDouble(tempMinFahrenheit) - 273.15;

                Log.i("temp_min", tempMinFahrenheit);
                Log.i("temp_min_c", Double.toString(tempMinCelsius));
                if (tempMinFahrenheit != "") {
                    String tempMinCelsiusString = new DecimalFormat("##.#").format(tempMinCelsius);
                    message += "Minimum Temp: \t\t" + tempMinCelsiusString + DEGREE + "C - " + tempMinFahrenheit + DEGREE + "C \r\n";
                }

                String tempMaxFahrenheit = jsonPart.getString("temp_max");
                double tempMaxCelsius = Double.parseDouble(tempMaxFahrenheit) - 273.15;
                Log.i("temp_max", tempMaxFahrenheit);
                Log.i("temp_max_c", Double.toString(tempMaxCelsius));
                if (tempMaxFahrenheit != "") {

                    String tempMaxCelsiusString = new DecimalFormat("##.#").format(tempMaxCelsius);
                    message += "Maximum Temp: \t" + tempMaxCelsiusString + DEGREE + "C - " + tempMaxFahrenheit + DEGREE + "F \r\n";
                }


                String windInfo = jsonObject.getString("wind");
                jsonPart = new JSONObject(windInfo);
                String speed = jsonPart.getString("speed");

                Log.i("Wind speed", speed);

                if (speed != "") {
                    message += "Wind Speed: \t\t\t\t\t\t" + speed + " m/s\r\n";
                }

                String direction = jsonPart.getString("deg");
                Log.i("Wind direction", direction);

                if (direction != "") {
                    message += "Wind Direction: \t\t\t" + direction + DEGREE + " \r\n";
                }


                if (message != "") {
                    resultTextView.setText(message);
                } else {
                    Toast.makeText(getApplicationContext(), "Could not find weather...", Toast.LENGTH_LONG);
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Could not find weather...", Toast.LENGTH_LONG);
            }

            //Log.i("Website content", weatherInfo);
        }
    }
}
