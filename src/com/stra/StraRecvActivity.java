// Various open source code snippets were used, especially vincentyFormula
package com.stra;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class StraRecvActivity extends Activity {
	
	int selectedRadius;
	double latitude; //= 60.1840;
	double longitude; //= 24.8279;


	GPSTracker gps;

    /**
    * Calculate <a
    * href="http://en.wikipedia.org/wiki/Great-circle_distance">geodesic
    * distance</a> in Meters between this Object and a second Object passed to
    * this method using <a
    * href="http://en.wikipedia.org/wiki/Thaddeus_Vincenty">Thaddeus Vincenty's</a>
    * inverse formula See T Vincenty, "<a
    * href="http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf">Direct and Inverse
    * Solutions of Geodesics on the Ellipsoid with application of nested
    * equations</a>", Survey Review, vol XXII no 176, 1975.
    *
    * @param location
    *            the destination location
    * @param formula
    *            This formula calculates initial bearing ({@link #INITIAL_BEARING}),
    *            final bearing ({@link #FINAL_BEARING}) and distance ({@link #DISTANCE}).
    */
    private static double vincentyFormula(Double lat1, Double lon1, Double lat2, Double lon2 ) {
     double a = 6378137;
     double b = 6356752.3142;
     double f = 1 / 298.257223563; // WGS-84 ellipsiod
     double L = Math.toRadians(lon2 - lon1);
     double U1 = Math
         .atan((1 - f) * Math.tan(Math.toRadians(lat1)));
     double U2 = Math.atan((1 - f)
         * Math.tan(Math.toRadians(lat2)));
     double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
     double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

     double lambda = L;
     double lambdaP = 2 * Math.PI;
     double iterLimit = 20;
     double sinLambda = 0;
     double cosLambda = 0;
     double sinSigma = 0;
     double cosSigma = 0;
     double sigma = 0;
     double sinAlpha = 0;
     double cosSqAlpha = 0;
     double cos2SigmaM = 0;
     double C;
     while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0) {
       sinLambda = Math.sin(lambda);
       cosLambda = Math.cos(lambda);
       sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
           + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda)
           * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
       if (sinSigma == 0)
         return 0; // co-incident points
       cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
       sigma = Math.atan2(sinSigma, cosSigma);
       sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
       cosSqAlpha = 1 - sinAlpha * sinAlpha;
       cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
       if (Double.isNaN(cos2SigmaM))
         cos2SigmaM = 0; // equatorial line: cosSqAlpha=0
       C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
       lambdaP = lambda;
       lambda = L
           + (1 - C)
           * f
           * sinAlpha
           * (sigma + C
               * sinSigma
               * (cos2SigmaM + C * cosSigma
                   * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
     }
     if (iterLimit == 0)
       return Double.NaN; // formula failed to converge

     double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
     double A = 1 + uSq / 16384
         * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
     double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
     double deltaSigma = B
         * sinSigma
         * (cos2SigmaM + B
             / 4
             * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B
                 / 6 * cos2SigmaM
                 * (-3 + 4 * sinSigma * sinSigma)
                 * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
     double distance = b * A * (sigma - deltaSigma);

     // initial bearing
     double fwdAz = Math.toDegrees(Math.atan2(cosU2 * sinLambda, cosU1
         * sinU2 - sinU1 * cosU2 * cosLambda));
     // final bearing
     double revAz = Math.toDegrees(Math.atan2(cosU1 * sinLambda, -sinU1
         * cosU2 + cosU1 * sinU2 * cosLambda));
     //if (formula == DISTANCE) {

       return (distance/1000);

    }
	   

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recv);
        gps = new GPSTracker(this);
		if(gps.canGetLocation()){
			Constants.lat = gps.getLatitude();             
			Constants.lon = gps.getLongitude();            
                   	
        }else{
            gps.showSettingsAlert();
        }
    	//Toast.makeText(getBaseContext(), "sendBtn " , Toast.LENGTH_SHORT).show();
    	new DownloadMessages().execute();
    	//recvScampi();
    }
    
    private class DownloadMessages extends AsyncTask <Void, Void, String> {
    	protected String getASCIIContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException {
	    	InputStream in = entity.getContent();
	    	StringBuffer out = new StringBuffer();
	    	int n = 1;
	    	while (n>0) {
		    	byte[] b = new byte[4096];
		    	n =  in.read(b);
		    	if (n>0) out.append(new String(b, 0, n));
	    	}
	    		return out.toString();
	    	}
    	
    	protected String doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpGet httpGet = new HttpGet("http://ideastudio.in/rest/tweets");			
			String result = null;
			
			try {
	    		HttpResponse response = httpClient.execute(httpGet, localContext);
	    		HttpEntity entity = response.getEntity();
	    		result = getASCIIContentFromEntity(entity);
			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
		
			return result;
		}
    	
		protected void onPostExecute(String results) {
			Constants.recvlist.clear();
			
			if (results!=null) {

				JSONArray JSONarray;					
				try {
					JSONarray = new JSONArray(results);
				     for (int j = 0; j < JSONarray.length(); j++) {
				    	 	listDS lds = new listDS();
				            JSONObject mJsonObject = JSONarray.getJSONObject(j);
				            lds.lat =mJsonObject.getString("lat");
				            double recvLat = Double.parseDouble(lds.lat);
				            lds.lon=mJsonObject.getString("lon");
				            double recvLon = Double.parseDouble(lds.lon);
				            lds.radius=mJsonObject.getString("radius");
				            int radius = Integer.parseInt(lds.radius);
				            lds.tweet=mJsonObject.getString("tweet");	  
				            
				            //distance between the tweet orgin and my location
				            double dis = vincentyFormula(recvLat, recvLon, Constants.lat, Constants.lon );
				            if(dis<=radius)
				            {
				            	Constants.recvlist.add(lds);
				            	//Toast.makeText(getBaseContext(), "dis " +dis , Toast.LENGTH_SHORT).show();
				            }
				        }
				} catch (JSONException e) {
				    e.printStackTrace();
				}
				
				loadList();
			}
				
			}
	}
    
    
    public void loadList()
    {
 		recvAdapter adapter = null;     
    	System.out.println("Loading List view");    	
    	ListView lview = (ListView) findViewById(R.id.recvlist);
    	adapter = new recvAdapter(this,R.layout.recvlist); 	
    	
    	lview.setAdapter(adapter);
    	lview.setOnItemClickListener(new OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {	        	
	        	//Toast.makeText(StraRecvActivity.this, "You have chosen : "+position , Toast.LENGTH_LONG).show();	        	
	        }
	    });
    	
    }
    
    public void recvScampi()
    {  	
    	
    	
    }
    
    public double CalculationByDistance(Double lat1, Double lon1, Double lat2, Double lon2 ) {
	  double Radius=6372.7976;
	  double dLat = Math.toRadians(lat2-lat1);
	  double dLon = Math.toRadians(lon2-lon1);
	  double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	     Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	     Math.sin(dLon/2) * Math.sin(dLon/2);
	  double c = 2 * Math.asin(Math.sqrt(a));
      return Radius * c;
    }
     
    
}