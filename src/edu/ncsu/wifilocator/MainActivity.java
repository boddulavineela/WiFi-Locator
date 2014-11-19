package edu.ncsu.wifilocator;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.os.AsyncTask;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import edu.ncsu.wifilocator.R;
import android.widget.EditText;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;

import java.util.Locale;

public class MainActivity extends FragmentActivity implements OnInitListener {
	
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc";
	private final String SOAP_ACTION = "http://tempuri.org/IMediationService/GetPlan";
	private final String METHOD_NAME = "GetPlan";
	private String TAG = "NCSU";
	private static String location;
	private static String destination;
	private static String path;
	Button b;
	TextView tv;
	EditText et;
	
	//TTS object
    private TextToSpeech myTTS;
        //status check code
    private int MY_DATA_CHECK_CODE = 0;
	
	static final LatLng CENTER = new LatLng(35.769301, -78.676406);
	private GoogleMap map;
	private MainApplication application;
	private int NUM_BUTTONS = 4;
	private Button interval[], calibrate[];
    
	ArrayList<LatLng> pointsList;
	 
    // url to get all existing points list
    private static String url_points = "http://people.engr.ncsu.edu/vboddul/gen_json_for_android.php";
    
    // JSON Node names
    private static final String TAG_POINTS = "points";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    private static final String TAG_LOC = "loc";
  
    // contacts JSONArray
    JSONArray points = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pointsList = new ArrayList<LatLng>();
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		
		//map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		
		// Move the camera instantly to the center with a zoom of 20.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 20));
		
		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
		
		application = (MainApplication) MainActivity.this.getApplication();
        application.mainActivity = this;
        
        //destination Edit Control
      		et = (EditText) findViewById(R.id.editText1);
      		
      	//Button to trigger web service invocation
    		b = (Button) findViewById(R.id.button1);
    		
//    		b.setOnClickListener(new OnClickListener() {
//    			public void onClick(View v) {
//    				//Check if location text control is not empty
//    				if (et.getText().length() != 0 && et.getText().toString() != "" && location.length() != 0 && location.toString() != "") {
//    					//Get the text control value
//    					destination = et.getText().toString();
//    					//Create instance for AsyncCallWS
//    					AsyncCallWS task = new AsyncCallWS();
//    					//Call execute 
//    					task.execute();
//    				//If text control is empty
//    				} else {
//    					//tv.setText("Please enter location");
//    				}
//    			}
//    		});
    		
    		//check for TTS data
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        
//        interval = new Button[NUM_BUTTONS];
//        interval[0] = (Button) findViewById(R.id.button1);
//        interval[1] = (Button) findViewById(R.id.button2);
//        interval[2] = (Button) findViewById(R.id.button3);
//        interval[3] = (Button) findViewById(R.id.button4);
//        
            calibrate = new Button[1];
            calibrate[0] = (Button) findViewById(R.id.button1);
            //calibrate[1] = (Button) findViewById(R.id.disable_exist);
        
        //setTimeInterval();
        showPoints(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onMapClick(LatLng point) {
		// TODO Auto-generated method stub
		map.addMarker(new MarkerOptions().position(point));
	}
	
	public void updateLocation(LatLng cor, String loc){
		map.clear();
		Marker marker = map.addMarker(new MarkerOptions()
		.position(cor)
		.title(loc));
		//.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
		marker.showInfoWindow();
	}
	
	public void getRouteLocation(LatLng cor, String loc){
		Marker marker = map.addMarker(new MarkerOptions()
		.position(cor)
		.title(loc));
		//.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
		marker.showInfoWindow();
	}
	
	public void getCurrentloc(String loc){
		location = loc;
	}
	
	public void updateStatus(String s){
		Toast.makeText(getBaseContext(), s, Toast.LENGTH_LONG).show();
	}

	public void getPath(String location, String destination) {
		//Create request
		SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo locationPI = new PropertyInfo();
		PropertyInfo destinationPI = new PropertyInfo();
		//Set Name
		locationPI.setName("source");
		destinationPI.setName("destination");
		//Set Value
		locationPI.setValue(location);
		destinationPI.setValue(destination);
		//Set dataType
		locationPI.setType(double.class);
		destinationPI.setType(double.class);
		//Add the property to request object
		request.addProperty(locationPI);
		request.addProperty(destinationPI);
		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER11);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);
		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		androidHttpTransport.debug = true;
		//androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		try {
			//Invoke web service
			androidHttpTransport.call(SOAP_ACTION, envelope);
			//Get the response
			SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			//Assign it to path static variable
			path = response.toString();

			   String[] result = path.split("move ");
			   for(int i = 0; i<result.length; i++){
				   //showPoints(this,result);
			   }
			Log.i("textResult", path);	
			
			//List<GeoPoint> route = new ArrayList<GeoPoint>();
			//add your points somehow...
			//GeoPoint q1 = route.get(lat);
			//mapView.getOverlays().add(new RoutePathOverlay(route));

			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    //act on result of TTS data check
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// the user has the necessary data - create the TTS
				myTTS = new TextToSpeech(this, this);
			} else {
				// no data - install it now
				Intent installTTSIntent = new Intent();
				installTTSIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}
	}
	
    //setup TTS
    public void onInit(int initStatus) {
     
            //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US)==TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
 
	
	private class AsyncCallWS extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			Log.i(TAG, "doInBackground");
			getPath(location,destination);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			Log.i(TAG, "onPostExecute");
			//tv.setText(path);
		}

		@Override
		protected void onPreExecute() {
			Log.i(TAG, "onPreExecute");
			//tv.setText("Loading...");
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			Log.i(TAG, "onProgressUpdate");
		}

	}
	
//	public void setTimeInterval(){
//		for (int i = 0; i < NUM_BUTTONS; i++) {
//			final Button theButton = interval[i];
//			theButton.setOnClickListener(new View.OnClickListener() {
//	            public void onClick(View v) {
//	                // TODO Auto-generated method stub
//	            	switch (v.getId()) {
//	                	case R.id.button1:
//	                		application.updateTimerInterval(5000);
//	                		break;
//	                	case R.id.button2:
//	                		application.updateTimerInterval(10000);
//	                		break;
//	                	case R.id.button3:
//	                		application.updateTimerInterval(15000);
//	                		break;
//	                	case R.id.button4:
//	                		application.updateTimerInterval(20000);
//	                		break;
//	            	}
//	            }
//	        }); 
//	    } 
//	}
	
	public void showPoints(final Context c){
		for (int i = 0; i < 1; i++) {
			final Button button = calibrate[i];
			button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                // TODO Auto-generated method stub
	            	switch (v.getId()) {
	            	case R.id.button1:
							new PostJSONDataAsyncTask(c, null, url_points, false){
								@Override
					            protected void onPreExecute()
					            {
					                super.onPreExecute();
					            }
					            
					            // Override the onPostExecute to do whatever you want
					            @Override
					            protected void onPostExecute(String response)
					            {
					            	application = (MainApplication) MainActivity.this.getApplication();
					                super.onPostExecute(response);
					                Log.d("wifiloc", response);
					                if (response != null)
					                {
					                	JSONObject json = null;
					                	try{
					                		json = new JSONObject(response);
					                	} catch (JSONException e){
					                		e.printStackTrace();
					                        Log.d("wifiloc", "Error parsing JSON");
					                	}
					        			
					        			if(json == null){
					                    	Log.d("wifiloc", "Error parsing server response");
					                        return;
					                    }
					                    
					        			//pointsList.clear();
					        			
					                    // If returned object length is 
					                    if(json.length() > 0){
					            			try {
					            	            // Getting Array of existing points
					            	            points = json.getJSONArray(TAG_POINTS);
					            	             
					            	            // looping through All points
					            	            for(int i = 0; i < points.length(); i++){
					            	                JSONObject c = points.getJSONObject(i);
					            	                 
					            	                // Storing each json item in variable
					            	                double lat = c.getDouble(TAG_LAT);
					            	                double lng = c.getDouble(TAG_LNG);
					            	                String loc = c.getString(TAG_LOC);
					            	                
					            	                // adding each coordinate to ArrayList
					            	                LatLng temp = new LatLng(lat, lng);
					            	                //pointsList.add(temp);
					            	                getRouteLocation(temp,loc);
					            	                
					            	            }
					            	        } catch (JSONException e) {
					            	            e.printStackTrace();
					            	        }
					                    }
					                    else {
					                        //TODO Do something here if no teams have been made yet
					                    }
					                    
					                    Log.d("wifiloc", "Update Success");
					                    
//					                    for(int i = 0; i < pointsList.size(); i++){
//					            			map.addMarker(new MarkerOptions()
//					            			.position(pointsList.get(i))
//					            			.title(""+pointsList.get(i)));
//					            			//.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//					            		}
					                }
					                else
					                {
					                    // Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show();
					                	Log.d("wifiloc", "Error Connecting to Server");
					                }

					            }
							}.execute();
							break;
	             	}
		            }
		        });
						
		}
	
	}
}
