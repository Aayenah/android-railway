package uk.ac.mmu.hackathon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ListView locationsList; //ListView to display stations

    private String urlString = ""; //URL string
    private double lat, lng; //latitude and longitude
    private TextView currentLocationText; //current location coordinates
    private ArrayList<Station> stationList = new ArrayList<>(); //to store station objects

    private DecimalFormat df = new DecimalFormat("#.##"); //format double to 2 decimal points
    private DecimalFormat df4 = new DecimalFormat("#.####"); //format double to 4 decimal points

    private Button buttonMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        lat = 0;
        lng = 0;
        currentLocationText = findViewById(R.id.currentLocationText);
        locationsList = findViewById(R.id.locationsList);
        buttonMap = findViewById(R.id.buttonMap);

        if(networkInfo != null && networkInfo.isConnected()){
            //Connected
            locationPermission();
        }else {
            //Not connected
            showToast("You are NOT connected");
        }
    }


    /**
     * Calculates distance from two pairs of latitude and longitude
     * @param lat1 latitude for first location
     * @param lng1 longitude for first location
     * @param lat2 latitude for second location
     * @param lng2 longitude for second location
     * @return distance as double
     * **/
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2){
        final double K = 6372.8; //kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLng/2) * Math.sin(dLng/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return K * c;
    }


    /**
     * Asks user for permission then will do one of the following based on result:
     *      1- if not granted app will close
     *      2- if granted then proceed with LocationManager
     * **/
    @SuppressWarnings("ForLoopReplaceableByForEach")
    public void locationPermission(){
        String[] requiredPermissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
        };

        boolean granted = true;

        for(int i = 0; i < requiredPermissions.length; i++){
            int result = ActivityCompat.checkSelfPermission(this, requiredPermissions[i]);
            if(result != PackageManager.PERMISSION_GRANTED){
                granted = false;
            }
        }

        if(!granted){
            ActivityCompat.requestPermissions(this, requiredPermissions, 1);
            System.exit(0);
        }
        else{
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude(); //assign latitude from current location
                    lng = location.getLongitude(); //assign longitude from current location
                    currentLocationText.setText(df4.format(lat)+", "+df4.format(lng));
                    //getActionBar().setTitle(currentLocationText.getText());
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }


    /**
     * onClick method for "Find Nearby Stations" button
     * when called, it will first clear the stationList to prevent appending to old list
     * call locationPermission() method to update current location
     * update URL based on new lat and lng values after executing locationPermission()
     * execute the AsyncTask
     * **/
    public void find_onClick(View v){
        stationList.clear();
        locationPermission();
        urlString = "http://10.0.2.2:8080/stations?lat="+lat+"&lng="+lng;
        new NetworkingTask().execute();
        buttonMap.setEnabled(true);
    }


    public void showMap_onClick(View v){
        if(stationList.isEmpty()){
            return;
        }

        Intent i = new Intent(MainActivity.this, Map.class);
        ArrayList<Double> latList = new ArrayList<>();
        ArrayList<Double> lngList = new ArrayList<>();

        for(Station st : stationList){
            latList.add(st.getLat());
            lngList.add(st.getLng());
        }

        i.putExtra("LatList", latList);
        i.putExtra("LngList", lngList);
        i.putExtra("CurrentLat", lat+"");
        i.putExtra("CurrentLng", lng+"");

        startActivity(i);
    }


    private void showToast(String message){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message,  duration);
        toast.show();
    }


    /**
     * An inner class to handle multithreading using AsyncTask for more convenience
     * **/
    public class NetworkingTask extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            double distance = 0;
            for (Station st : stationList) {
                distance = calculateDistance(lat, lng, st.getLat(), st.getLng());
                st.setDistanceToUser(distance);
                System.out.println("DISTANCE: "+df.format(st.getDistanceToUser()));
            }

            ArrayAdapter<Station> a = new ArrayAdapter<Station>(MainActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, stationList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    TextView text1 = v.findViewById(android.R.id.text1);
                    TextView text2 = v.findViewById(android.R.id.text2);

                    text1.setText(stationList.get(position).getName()+"\t\t\t - \t\t\t"+df.format(stationList.get(position).getDistanceToUser())+"km away");
                    text2.setText(stationList.get(position).getCoordinates());

                    return v;
                }
            };

            locationsList.setAdapter(a);
        }

        /**
         * All networking is done in background thread
         * **/
        @Override
        protected String doInBackground(String... strings) {
            try{
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                String line = "";
                while((line = bufferedReader.readLine()) != null){
                    JSONArray ja = new JSONArray(line);
                    for (int i = 0; i < ja.length(); i++){
                        JSONObject jo = (JSONObject) ja.get(i);
                        Station s = new Station(jo.getString("StationName"), jo.getDouble("Latitude"), jo.getDouble("Longitude"));
                        stationList.add(s);
                    }
                }
                bufferedReader.close();
            }
            catch (IOException | JSONException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}


