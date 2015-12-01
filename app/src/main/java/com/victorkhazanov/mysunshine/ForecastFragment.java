package com.victorkhazanov.mysunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ForecastFragment extends Fragment {

    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ArrayAdapter<String> mForecastAdapter;
    private ListView mListView;


    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();

    }

    private void  updateWeather(){

        FetchWeatherTask fwt = new FetchWeatherTask();
        String location = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        fwt.execute(location);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);


        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //

        int id=item.getItemId();

        if(id==R.id.action_refresh){

//            FetchWeatherTask fwt = new FetchWeatherTask();
//            fwt.execute();

            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


//        String[] forecast={
//                "Today",
//                "Tomorrow",
//                "Weds"
//        };
//
//        List<String> weekForecast = new ArrayList<String>(
//
//                Arrays.asList(forecast)
//        );

        mForecastAdapter =new ArrayAdapter<String>(

                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>()
        );


        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String forecast = mForecastAdapter.getItem(position);

             //   Toast.makeText(getActivity(),forecast,Toast.LENGTH_SHORT).show();


                Intent intent = new  Intent(getActivity(),DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,forecast);

                startActivity(intent);
            }
        });




        // Inflate the layout for this fragment
        return rootView;
    }

    public  class FetchWeatherTask extends AsyncTask<String,Void,String[]> {


        @Override
        protected String[] doInBackground(String... params) {


            if (params == null) {

                return null;
            }

            int numDays=7;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {

                String format = "json";
                String units = "metric";

                String locationQuery = WeatherDataParser.getPreferredLocation(getContext());

                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?appid=2de143494c0b295cca9337e1e96b00e0";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, locationQuery)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))

                        .build();

                URL url = new URL(builtUri.toString());

             //   URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?id=6693679&mode=json&units=metric&cnt=7&APPID=61f7050bfa5683b334cd7f394f79b78f");

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                LogUtil.d(forecastJsonStr);

                try {

                    return WeatherDataParser.getWeatherDataFromJson(forecastJsonStr, numDays,getActivity());

                }
                catch (JSONException ex){
                    LogUtil.e(ex.getMessage());
                }

            } catch (IOException e) {
                LogUtil.e(e.getMessage());
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        LogUtil.e(e.getMessage());
                    }
                }
            }

            return null;

        }


        @Override
        protected void onPostExecute(String[] result) {
            //super.onPostExecute(strings);
            if(result != null){

                mForecastAdapter.clear();

                for (String dayStr:result){
                    mForecastAdapter.add(dayStr);
                }

            }
        }
    }
}
