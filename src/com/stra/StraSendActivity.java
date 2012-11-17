// Various open source code snippets were used
// One of many: http://code.google.com/p/crw-cmu/source/browse/luis/PhotoIntentActivity
package com.stra;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.stra.BaseAlbumDirFactory;
import com.stra.FroyoAlbumDirFactory;
import com.stra.AlbumStorageDirFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class StraSendActivity extends Activity {
	
	private static final int ACTION_TAKE_PHOTO_B = 1;
	private static final int ACTION_TAKE_VIDEO = 2;
	int selectedRadius;
	double latitude; //= 60.1840;
	double longitude; //= 24.8279;
	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";	
	private String mCurrentPhotoPath;
	private String mCurrentVideoPath;	
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
	
	private String getAlbumName() {
		return getString(R.string.album_name);
	}
	
	private File getAlbumDir() {
		File storageDir = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
		} else {
			mAlbumStorageDirFactory = new BaseAlbumDirFactory();
		}
		
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
			if (storageDir != null) {
				if (! storageDir.mkdirs()) {
					if (! storageDir.exists()){
						Log.d("CameraSample", "failed to create directory");
						return null;
					}
				}
			}
			
		} else {
			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
		}		
		return storageDir;
	}
	
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}
	
	private File createVideoFile() throws IOException {
		// Create an video file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String videoFileName = "VID_" + timeStamp + "_";
		File albumF = getAlbumDir();
		File videoF = File.createTempFile(videoFileName, ".mp4", albumF);
		return videoF;
	}

	private File setUpPhotoFile() throws IOException {		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();		
		return f;
	}
	
	private void dispatchTakePictureIntent(int actionCode) {
		
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		switch(actionCode) {
		case ACTION_TAKE_PHOTO_B:
			File f = null;			
			try {
				f = setUpPhotoFile();
				mCurrentPhotoPath = f.getAbsolutePath();
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
			} catch (IOException e) {
				e.printStackTrace();
				f = null;
				mCurrentPhotoPath = null;
			}
			break;

		default:
			break;			
		} // switch

		startActivityForResult(takePictureIntent, actionCode);	
	}
	
	private File setUpVideoFile() throws IOException {
		
		File f = createVideoFile();
		mCurrentPhotoPath = f.getAbsolutePath();		
		return f;
	}

	private void dispatchTakeVideoIntent() {
		Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		
		File f = null;
		try {
		f = setUpVideoFile();
		mCurrentVideoPath= f.getAbsolutePath();
		takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
		}
		catch (IOException e) {
			e.printStackTrace();
			f = null;
			mCurrentVideoPath = null;
		}		
		
		startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ACTION_TAKE_PHOTO_B: {
				if (resultCode == RESULT_OK) {
					Toast.makeText(getBaseContext(), "CurrentPhotoPath: " + mCurrentPhotoPath,Toast.LENGTH_SHORT).show();
				}
				break;
			} // ACTION_TAKE_PHOTO_B			
			case ACTION_TAKE_VIDEO: {
				if (resultCode == RESULT_OK) {
					Toast.makeText(getBaseContext(), "CurrentVideoPath: " + mCurrentVideoPath,Toast.LENGTH_SHORT).show();
				}
				break;
			} // ACTION_TAKE_VIDEO
		} // switch
	}	
	
	Button.OnClickListener mTakePicOnClickListener = 
			new Button.OnClickListener() {
			public void onClick(View v) {
				dispatchTakePictureIntent(ACTION_TAKE_PHOTO_B);
			}
	   
	};
	
	Button.OnClickListener mTakeVidOnClickListener = 
				new Button.OnClickListener() {
				public void onClick(View v) {
					dispatchTakeVideoIntent();
				}
	};
			
	public static boolean isIntentAvailable(Context context, String action) {
			final PackageManager packageManager = context.getPackageManager();
			final Intent intent = new Intent(action);
			List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
	}
		
	private void setBtnListenerOrDisable( 
				Button btn, 
				Button.OnClickListener onClickListener,
				String intentName) {
			if (isIntentAvailable(this, intentName)) {
				btn.setOnClickListener(onClickListener);        	
			} else {
				btn.setText( 
					getText(R.string.cannot).toString() + " " + btn.getText());
				btn.setClickable(false);
			}
	}
	
	String[] radii = {"3","5","10","20",};	
	GPSTracker gps;	   
    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send);
        //Get my location
        gps = new GPSTracker(this);
		if(gps.canGetLocation()){
            latitude = gps.getLatitude();             
            longitude = gps.getLongitude(); 
            Constants.lat=latitude;
            Constants.lon=longitude;
            TextView lat = (TextView)findViewById(R.id.lat);
            lat.setText("Latitude: "+String.valueOf(latitude));
            TextView lon = (TextView)findViewById(R.id.lon);
            lon.setText("Longitude: "+String.valueOf(longitude));        	
        }else{
            gps.showSettingsAlert();
        }

		//Select the content radius
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (this, android.R.layout.simple_spinner_item,radii);
        
        final Spinner spinner = (Spinner) findViewById(R.id.Rspinner);
        spinner.setAdapter(adapter);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int item = spinner.getSelectedItemPosition();
                
                selectedRadius=Integer.valueOf(radii[item]);
                Constants.radius=radii[item];
                Toast.makeText(getBaseContext(), "Selected the radius: " + selectedRadius,Toast.LENGTH_SHORT).show();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        
        //Send image
        Button imageBtn = (Button) findViewById(R.id.sendImage);
		setBtnListenerOrDisable( 
				imageBtn, 
				mTakePicOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);
		//Send Video
		Button vidBtn = (Button) findViewById(R.id.sendVideo);
		setBtnListenerOrDisable( 
				vidBtn, 
				mTakeVidOnClickListener,
				MediaStore.ACTION_VIDEO_CAPTURE
		);
		//Send message/tweet
		Button sendTextBtn = (Button) findViewById(R.id.sendText);
		sendTextBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {    
            	EditText  message = (EditText) findViewById(R.id.message);
            	Constants.tweet= message.getText().toString();
            	Toast.makeText(getBaseContext(), "message: " + message.getText(), Toast.LENGTH_SHORT).show();
            	new WebPageTask().execute();
            	//sendScampi(Constants.tweet,Constants.latitude,Constants.longitude,Constants.radius);

            }
        });
    }
    
    public void sendScampi(String tweet,String latitude,String longitude,String radius)
    {  	
    	
    	
    }
    //Async task to run in the background
    private class WebPageTask extends AsyncTask <Void, Void, String> {
   	
    	protected String doInBackground(Void... params) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://ideastudio.in/rest/add");			
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	            nameValuePairs.add(new BasicNameValuePair("lat",  Double.toString(Constants.lat)));
	            nameValuePairs.add(new BasicNameValuePair("lon", Double.toString(Constants.lon)));
	            nameValuePairs.add(new BasicNameValuePair("tweet", Constants.tweet));
	            nameValuePairs.add(new BasicNameValuePair("radius", Constants.radius));	            
	            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	            HttpResponse response = httpClient.execute(httppost);

			} catch (Exception e) {
				return e.getLocalizedMessage();
			}
			return null;
		}
    	
		protected void onPostExecute(String results) {			
			Toast.makeText(getBaseContext(), "Message Sent", Toast.LENGTH_SHORT).show();
		}
	}
}