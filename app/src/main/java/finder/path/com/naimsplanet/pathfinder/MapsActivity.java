package finder.path.com.naimsplanet.pathfinder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient apiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;

    private int PROXIMITY_RADIUS = 10000;
    private double latitude,longitude;
    private double end_latitude, end_longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onClick(View view)
    {
        Object[] dataTransfer = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
        String url;
        if(view.getId() == R.id.search_button)
        {
            EditText mSearchText = findViewById(R.id.search_text);
            String locationText = mSearchText.getText().toString();


            List<Address> addressList = null;
            if(!TextUtils.isEmpty(locationText))
            {
                //Toast.makeText(this, "You enterde -> "+locationText, Toast.LENGTH_SHORT).show();
                Geocoder geocoder = new Geocoder(this);
                try
                {
                    addressList = geocoder.getFromLocationName(locationText,10);
                    if(addressList != null)
                    {
                        for(int i=0;i<addressList.size();i++)
                        {
                            LatLng latLng = new LatLng(addressList.get(i).getLatitude(),addressList.get(i).getLongitude());
                            MarkerOptions mo = new MarkerOptions();
                            mo.position(latLng);
                            mo.title(locationText);
                            mMap.addMarker(mo);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.animateCamera(CameraUpdateFactory.zoomBy(10));


                        }
                    }
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }

        else if(view.getId()== R.id.restaurant_button)
        {
            mMap.clear();
            String restaurant = "restaurant";
            url = getUrl(latitude,longitude,restaurant);
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;


            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(this, "Showing nearby Restaurant", Toast.LENGTH_SHORT).show();
        }

        else if(view.getId()==R.id.school_button)
        {
            mMap.clear();
            String school = "school";
            url = getUrl(latitude,longitude,school);
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;


            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(this, "Showing nearby Schools", Toast.LENGTH_SHORT).show();
        }
        else if(view.getId()==R.id.hospital_button)
        {
            mMap.clear();
            String hospital = "hospital";
            url = getUrl(latitude,longitude,hospital);
            dataTransfer[0] = mMap;
            dataTransfer[1] = url;

            getNearbyPlacesData.execute(dataTransfer);
            Toast.makeText(this, "Showing nearby Hospitals", Toast.LENGTH_SHORT).show();
        }

        else if(view.getId() == R.id.go_button)
        {
            mMap.clear();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(end_latitude,end_longitude));
            markerOptions.title("Destination");
            markerOptions.draggable(true);

            float result[] = new float[10];
            Location.distanceBetween(latitude,longitude,end_latitude,end_longitude,result);
            double r =  result[0]/1000;
            r = Double.parseDouble(new DecimalFormat(".##").format(r));
            markerOptions.snippet("Distance = "+r+" km" );
            mMap.addMarker(markerOptions);

        }
        else if(view.getId() == R.id.duration_button)
        {
            //mMap.clear();
            dataTransfer = new Object[3];
            url = getDirectionsUrl();
            GetDirectionsData getDirectionsData = new GetDirectionsData();

            dataTransfer[0] = mMap;
            dataTransfer[1] = url;
            dataTransfer[2] = new LatLng(end_latitude,end_longitude);
            getDirectionsData.execute(dataTransfer);

        }
    }

    private String getDirectionsUrl()
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("http://maps.googleapis.com/maps/api/directions/json?");
        googleDirectionsUrl.append("origin="+latitude+","+longitude);
        googleDirectionsUrl.append("&destination="+end_longitude+","+end_longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyDEeCKWMIgS6Vu0VYCPdiYVxlnb2niFGPs");

        return googleDirectionsUrl.toString();
    }

    private String getUrl(Double latitude,Double longitude,String nearbyPlace)
    {
        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+"AIzaSyBLuQbuqk_JQjtf4ZCq24Ex4ZS9U5anAwI");

        return googlePlaceUrl.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       switch (requestCode)
       {
           case REQUEST_LOCATION_CODE:
               if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
               {
                   //permission granted
                   if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
                   {
                       if(apiClient == null)
                       {
                           buildGoogleApiClient();
                       }
                       mMap.setMyLocationEnabled(true);
                   }
                   else
                   {
                       Toast.makeText(this, "Permission Denined", Toast.LENGTH_SHORT).show();
                   }
                   return;
               }
       }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); -> if we want to change map type then use this

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);


    }

    protected synchronized void buildGoogleApiClient()
    {
        apiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        apiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        lastLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if(currentLocationMarker != null)
        {
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        if(apiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(apiClient,this);

        }

    }

    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,  new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(apiClient,locationRequest,this);
        }

    }

    @Override
    public void onConnectionSuspended(int i)
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }





    @Override
    public boolean onMarkerClick(Marker marker)
    {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker)
    {

    }

    @Override
    public void onMarkerDrag(Marker marker)
    {

    }

    @Override
    public void onMarkerDragEnd(Marker marker)
    {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;

    }
}
