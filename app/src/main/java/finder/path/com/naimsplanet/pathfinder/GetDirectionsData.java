package finder.path.com.naimsplanet.pathfinder;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetDirectionsData extends AsyncTask<Object , String , String>
{
    GoogleMap mMap;
    String url;
    String googleDirectionData;
    String duration,distance;
    LatLng latLng;

    @Override
    protected String doInBackground(Object... objects)
    {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latLng = (LatLng) objects[2];
        DownloadUrl downloadUrl = new DownloadUrl();
        try{
            googleDirectionData = downloadUrl.readURL(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleDirectionData;
    }

    @Override
    protected void onPostExecute(String s)
    {
        HashMap<String,String> directionList = null;
        DataParser parser = new DataParser();
        directionList = parser.parseDirections(s);

        duration = directionList.get("duration");
        distance = directionList.get("distance");

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.draggable(true);
        markerOptions.title("Duration = "+duration);
        markerOptions.snippet("Distance = "+distance);

        mMap.addMarker(markerOptions);


    }
}
