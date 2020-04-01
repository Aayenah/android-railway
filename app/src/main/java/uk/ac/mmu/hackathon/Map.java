package uk.ac.mmu.hackathon;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class Map extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapBoxAccessToken));
        setContentView(R.layout.activity_map);

        //get intents passed from MainActivity and store them in ArrayList
        ArrayList<Double> latList = (ArrayList<Double>) getIntent().getSerializableExtra("LatList");
        ArrayList<Double> lngList = (ArrayList<Double>) getIntent().getSerializableExtra("LngList");

        String currentLatString = getIntent().getStringExtra("CurrentLat");
        String currentLngString = getIntent().getStringExtra("CurrentLng");

        double currentLat = Double.parseDouble(currentLatString);
        double currentLng = Double.parseDouble(currentLngString);

        System.out.println("LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAT");

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(currentLat, currentLng))
                .zoom(10)
                .tilt(20)
                .build();


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        mapboxMap.setCameraPosition(position);

                        //Add a marker for each coordinate from latList and lngList
                        for(int i = 0; i < latList.size(); i++){
                            mapboxMap.addMarker(new MarkerOptions().position(new LatLng(latList.get(i), lngList.get(i))));
                        }


                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

}
