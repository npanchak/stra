// Various open source code snippets were used
package com.stra;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class StraActivity extends Activity {

	double latitude; //= 60.1840;
	double longitude; //= 24.8279;
	GPSTracker gps;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gps = new GPSTracker(this);
		if(gps.canGetLocation()){
            latitude = gps.getLatitude();             
            longitude = gps.getLongitude();            
            TextView lat = (TextView)findViewById(R.id.lat);
            lat.setText("Latitude: "+String.valueOf(latitude));
            TextView lon = (TextView)findViewById(R.id.lon);
            lon.setText("Longitude: "+String.valueOf(longitude));        	
        }else{
            gps.showSettingsAlert();
        }

		Button recvBtn = (Button) findViewById(R.id.recvBtn);
		recvBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {    
            	//Toast.makeText(getBaseContext(), "recvBtn " , Toast.LENGTH_SHORT).show();
            	Intent recv = new Intent(StraActivity.this, StraRecvActivity.class);
            	startActivity(recv);
            }
        });
		
		
		Button sendBtn = (Button) findViewById(R.id.sendBtn);
		sendBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {    
            	//Toast.makeText(getBaseContext(), "sendBtn " , Toast.LENGTH_SHORT).show();
            	Intent send = new Intent(StraActivity.this, StraSendActivity.class);
            	startActivity(send);
            }
        });
    }    
}