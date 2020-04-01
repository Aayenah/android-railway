package uk.ac.mmu.hackathon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ListView locationsList;
    private ArrayList<Station> stationList = new ArrayList<>();
    private String urlString = "";
    private double lat, lng;
    private TextView currentLocationText;

    private final double K = 6372.8; //kilometers
    private DecimalFormat df = new DecimalFormat("#.##");
    private DecimalFormat df4 = new DecimalFormat("#.####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()){
            //showToast("You are connected");
        }else {
            //showToast("You are NOT connected");
        }

        lat = 0;
        lng = 0;

        currentLocationText = findViewById(R.id.currentLocationText);

        locationPermission();
        locationsList = findViewById(R.id.locationsList);
    }


    public double calculateDistance(double lat1, double lng1, double lat2, double lng2){
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.sin(dLng/2) * Math.sin(dLng/2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return K * c;
    }


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
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
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


    public void find_onClick(View v){
        stationList.clear();
        locationPermission();
        urlString = "http://10.0.2.2:8080/stations?lat="+lat+"&lng="+lng;
        new NetworkingTask().execute();
    }


    private void showToast(String message){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, message,  duration);
        toast.show();
    }


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

        @Override
        protected String doInBackground(String... strings) {
            System.out.println("doing in BACKGROUND");
            try{
                System.out.println("Trying");
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                System.out.println("Trying2");
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
                System.out.println("Catching");
                e.printStackTrace();
            }
            System.out.println("DONEEEEEEEE");
            return "FINSHEDDDDDD!!!";
        }
    }
}


