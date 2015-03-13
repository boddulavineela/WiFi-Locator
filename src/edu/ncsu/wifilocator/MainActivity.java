package edu.ncsu.wifilocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.IndoorBuilding;
import com.google.android.gms.maps.model.IndoorLevel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends FragmentActivity implements OnInitListener, ListView.OnItemClickListener {
	
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc";
	private final String SOAP_ACTION = "http://tempuri.org/IMediationService/GetPlan";
	private final String METHOD_NAME = "GetPlan";
	private String TAG = "NCSU";
	private static String location;
	private static LatLng CurrentCoords;
	private static String destination;
	private static String path;
	private static String NoRouteString = "GetPlanResponse{GetPlanResult=anyType{}; }";
	private MapFragment map1;
	Button b;
	TextView tv;
	EditText et;
	FrameLayout f;
	Button roomSearch;
	//TTS object
    private TextToSpeech myTTS;
        //status check code
    private int MY_DATA_CHECK_CODE = 0;
	
	static final LatLng CENTER = new LatLng(35.769301, -78.676406);
	LatLng lines[] = new LatLng[100];
	private GoogleMap map;
	private MainApplication application;
	private int NUM_BUTTONS = 4;
	private Button interval[], calibrate[];
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private String[] mPlaces;
	ArrayAdapter<String> adapter;
    private String[] drawerListViewItems;
	IndoorBuilding building;
    
	ArrayList<String> pointsList;
	HashMap<String, LatLng> places = new HashMap<String,LatLng>();
	String[] result;
	 
    // url to get all existing points list
    private static String url_points = "http://people.engr.ncsu.edu/vboddul/gen_json_for_android.php";
    
    // JSON Node names
    private static final String TAG_POINTS = "points";
    private static final String TAG_LAT = "lat";
    private static final String TAG_LNG = "lng";
    private static final String TAG_LOC = "loc";
	protected static final String GET_ROOMS_METHOD_NAME = "GetMatchingRooms";
  
    // contacts JSONArray
    JSONArray points = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MapsInitializer.initialize(this);
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		
		// Create a tab listener that is called when the user changes tabs.
	    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
	        @Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
	        	
	        	android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
	            
	        	if(tab.getText().equals("Suggest"))
	        	{
	            // show the given tab
	        	Toast.makeText(MainActivity.this, tab.getText(), Toast.LENGTH_SHORT).show();
	        	QuestionFragment fh = new QuestionFragment();
	        	Log.d("TAB",fh.getId()+"");
	        	android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	            //FragmentTransaction ft = fm.beginTransaction();
	            //fragmentTransaction.
	            fragmentTransaction.add(R.id.parent, fh);
	            fragmentTransaction.commit();
	            f.setVisibility(android.view.View.INVISIBLE);
	            //fragmentTransaction.add(fh, "question");
	            et.setVisibility(android.view.View.INVISIBLE);
	            b.setVisibility(android.view.View.INVISIBLE);
	            
	            //TextView tv = (TextView) MainActivity.this.findViewById(R.id.textView4);
	            //tv.setText("Foo bar");
	            
	            android.support.v4.app.Fragment f = fragmentManager.findFragmentById(fh.getId());
	            //f.getView();
//	            Log.d("TAB",fh.getTemp());
	            /*View suggestView = f.getView();
	            TextView tv_s = (TextView)suggestView.findViewById(R.id.textView4);
	            Log.d("Tab",tv_s.getText().toString());*/
	            /*Log.d("Tab",tv.getCurrentTextColor()+"");
	            roomSearch = (Button) findViewById(R.id.searchButton);*/
	            
	            /*roomSearch.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						getRooms();
					}
					private void getRooms() {
						// TODO Auto-generated method stub
						
						SoapObject request = new SoapObject(NAMESPACE, GET_ROOMS_METHOD_NAME);
						//Property which holds input parameters
						PropertyInfo locationPI = new PropertyInfo();
						PropertyInfo destinationPI = new PropertyInfo();
						
						PropertyInfo light = new PropertyInfo();
						light.setType(String.class);
						light.setName("light");
						light.setValue("");
						
						PropertyInfo sound = new PropertyInfo();
						sound.setType(String.class);
						sound.setName("sound");
						sound.setValue("");
						
						PropertyInfo tempHi = new PropertyInfo();
						tempHi.setType(int.class);
						tempHi.setName("tempHi");
						tempHi.setValue(0);
						
						PropertyInfo tempLo = new PropertyInfo();
						tempLo.setType(int.class);
						tempLo.setName("tempLo");
						tempLo.setValue(0);
						
						PropertyInfo type = new PropertyInfo();
						type.setType(String.class);
						type.setName("type");
						
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
						request.addProperty(light);
						request.addProperty(sound);
						
						request.addProperty(light);
						request.addProperty(sound);
						request.addProperty(tempHi);
						request.addProperty(tempLo);
						request.addProperty(type);
						
						
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
							SoapObject resp = (SoapObject) envelope.bodyIn;
							//Assign it to path static variable
							String path1 = resp.toString();
							Log.d("ROOMS",path1);
							System.out.println(path1);
							
							}
							

						catch (Exception e) {
							e.printStackTrace();
						}
						
					}
				});*/
	        	}
	            
	            
	        	else
	        	{
	        		if(f!=null)
	        		{
	        			f.setVisibility(android.view.View.VISIBLE);
	        			et.setVisibility(android.view.View.VISIBLE);
	        			b.setVisibility(android.view.View.VISIBLE);
	        			android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	        			android.support.v4.app.Fragment fRemove = fragmentManager.findFragmentById(R.id.parent);
	        			//Log.d("FRAG", fRemove.getTag());
	        			if(fRemove == null)
	        			{
	        				Log.d("FRAG", "null");
	        			}
	        			fragmentTransaction.detach(fRemove);
	        			fragmentTransaction.commit();
	        			
	        		}
	        	}
	        }

	        @Override
			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
	            // hide the given tab
	        }

	        @Override
			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
	            // probably ignore this event
	        }

	    };
	    
	    actionBar.addTab(
                actionBar.newTab()
                        .setText("Map")
                        .setTabListener(tabListener));

	    actionBar.addTab(
                actionBar.newTab()
                        .setText("Suggest")
                        .setTabListener(tabListener));

		setContentView(R.layout.activity_main);
		/*map1 = MapFragment.newInstance();
		FragmentTransaction fragTrans = getFragmentManager().beginTransaction();
		fragTrans.add(R.id.parent, map1);
		fragTrans.commit();*/
		
		pointsList = new ArrayList<String>();
		
		map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
				.getMap();
		//map = map1.getMap();
		/*if(map1.getMap() != null){
            Log.v(TAG, "Map ready for use!");
            //mMap = getMap();
        }
		if (map == null)
		{
			Log.d("MAP", "null");
			MapsInitializer.initialize(MainActivity.this);
		}*/
		//map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		// Move the camera instantly to the center with a zoom of 20.
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTER, 20));
		
		//ronak TODO:
		//location = "FRONTHALL";
		
		// Zoom in, animating the camera.
		map.animateCamera(CameraUpdateFactory.zoomTo(20), 2000, null);
		
		application = (MainApplication) MainActivity.this.getApplication();
        application.mainActivity = this;
        
      //Ronak
		
		mPlaces = new String[]{"FLoor 0","FLoor 1","Floor 2"};
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		//mDrawerLayout.setScrimColor(00111010);
		mDrawerLayout.setBackgroundColor(00110000);
	    mDrawerList = (ListView) findViewById(R.id.left_drawer);
	    mDrawerList.setBackgroundColor(11110000);

	    drawerListViewItems = getResources().getStringArray(R.array.items);
	     	
	    //adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.drawer_listview_item,drawerListViewItems);
	    // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(MainActivity.this,R.layout.drawer_listview_item,drawerListViewItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(this);
        

        //destination Edit Control
      		et = (EditText) findViewById(R.id.editText1);
      		
      		f = (FrameLayout) findViewById(R.id.frameLayout);
      	//Button to trigger web service invocation
    		b = (Button) findViewById(R.id.button1);
    		
    		b.setOnClickListener(new OnClickListener() {
    			@Override
				public void onClick(View v) {
    				
    				map.clear();
    				Marker marker = map.addMarker(new MarkerOptions()
    				.position(CurrentCoords)
    				.title(location));
    				//.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
    				marker.showInfoWindow();
    				
    				building = map.getFocusedBuilding();

    				
    				//IndoorLevel il = building.getLevels().get(0);
    				//il.activate();
    				if (building == null) {
    				 // return null;
    					Log.d("Indoor","NULL");
    				}
    				//Log.d("Indoor",building.getActiveLevelIndex()+"");
    				//Lod.d(building.getLevels().get(building.getActiveLevelIndex()));
    				//Toast.makeText(getApplicationContext(), "Active Level Index is "+building.getActiveLevelIndex(), Toast.LENGTH_LONG).show();
    				//Check if location text control is not empty
    				if (et.getText().length() != 0 && et.getText().toString() != "" && location.length() != 0 && location.toString() != "") {
    					//Get the text control value
    					destination = et.getText().toString();
    					
    					if(destination.equalsIgnoreCase(location))
    					{
    						new AlertDialog.Builder(MainActivity.this)
    					    .setTitle("Destination Reached")
    					    .setMessage("You have reached your destination")
    					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
    					        @Override
								public void onClick(DialogInterface dialog, int which) { 
    					        	dialog.cancel();
    					        	return;
    					        }
    					     })
    					    .setIcon(android.R.drawable.checkbox_on_background)
    					     .show();
    					}
    					else
    					{
	    					AsyncCallWS task = new AsyncCallWS();
	    					task.execute();
    					}
    					
    					//hide soft keyboard
    					InputMethodManager imm = (InputMethodManager)getSystemService(
    						      Context.INPUT_METHOD_SERVICE);
    						imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    				//If text control is empty
    				} else {
    					//tv.setText("Please enter location");
    				}
    			}
    		});
    		
    		//if(roomSearch != null)
    		/*roomSearch.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					getRooms();
				}*/

				/*private void getRooms() {
					// TODO Auto-generated method stub
					
					SoapObject request = new SoapObject(NAMESPACE, GET_ROOMS_METHOD_NAME);
					//Property which holds input parameters
					PropertyInfo locationPI = new PropertyInfo();
					PropertyInfo destinationPI = new PropertyInfo();
					
					PropertyInfo light = new PropertyInfo();
					light.setType(String.class);
					light.setName("light");
					light.setValue("");
					
					PropertyInfo sound = new PropertyInfo();
					sound.setType(String.class);
					sound.setName("sound");
					sound.setValue("");
					
					PropertyInfo tempHi = new PropertyInfo();
					tempHi.setType(int.class);
					tempHi.setName("tempHi");
					tempHi.setValue(0);
					
					PropertyInfo tempLo = new PropertyInfo();
					tempLo.setType(int.class);
					tempLo.setName("tempLo");
					tempLo.setValue(0);
					
					PropertyInfo type = new PropertyInfo();
					type.setType(String.class);
					type.setName("type");
					
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
					request.addProperty(light);
					request.addProperty(sound);
					
					request.addProperty(light);
					request.addProperty(sound);
					request.addProperty(tempHi);
					request.addProperty(tempLo);
					request.addProperty(type);
					
					
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
						SoapObject resp = (SoapObject) envelope.bodyIn;
						//Assign it to path static variable
						String path1 = resp.toString();
						Log.d("ROOMS",path1);
						System.out.println(path1);
						
						}
						

					catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			});*/
    		
    		//check for TTS data
            Intent checkTTSIntent = new Intent();
            checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
           // startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
        
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
		boolean isSame = false;
		if(cor.equals(CurrentCoords))
		{
			isSame = true;
		}
		CurrentCoords = cor;
		location = loc;
		//Toast.makeText(getApplicationContext(), "Location Changed: loc "+location+" , "
		//is		+ "isSame "+isSame, Toast.LENGTH_SHORT).show();
		map.clear();
		Marker marker = map.addMarker(new MarkerOptions()
		.position(cor)
		.title(loc));
		//.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
		marker.showInfoWindow();
		//String a[] = {"1"};
		//plotLines(a);
		plotLines(MainActivity.this.result);
		
		
	}
	
	public void getRouteLocation(LatLng cor, String loc){
		Marker marker = map.addMarker(new MarkerOptions()
		.position(cor)
		.title(loc));
		//.icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow)));
		marker.showInfoWindow();
	}
	
	public void getCurrentloc(LatLng cor, String loc){
		location = loc;
		CurrentCoords = cor;
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
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL,80000);
		androidHttpTransport.debug = true;
		//androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		try {
			//Invoke web service
			androidHttpTransport.call(SOAP_ACTION, envelope);
			//Get the response
			if (envelope.bodyIn instanceof SoapFault)
			{
			    final SoapFault sf = (SoapFault) envelope.bodyIn;
			    System.out.println(sf.faultstring);
			}
			SoapObject resp = (SoapObject) envelope.bodyIn;
			//Assign it to path static variable
			String path1 = resp.toString();
			
			if(path1.equalsIgnoreCase(NoRouteString))
			{
				result = null;
				return;
				//call dialog box and return
				
			}
			else
			{
			int start = path1.indexOf('=');
			int end = path1.indexOf(';');
			String path = path1.substring(start+1, end);
			System.out.println("path1 : "+path1);
			System.out.println("path (extracted) "+path);
			//SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
			//path = response.toString();

			   result = path.split("\\r?\\n");
			   System.out.println(result.length);
			   System.out.println(result);
			  
			//speakWords(path);
			//List<GeoPoint> route = new ArrayList<GeoPoint>();
			//add your points somehow...
			
			//plot points
			
			//plotLines(result);
			}
			
			//GeoPoint q1 = route.get(lat)
			//mapView.getOverlays().add(new RoutePathOverlay(route));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	   //speak the user text
	
    private void speakWords(String path) {
 
            //speak straight away
            myTTS.speak(path, TextToSpeech.QUEUE_FLUSH, null);
    }
    
    private void plotLines(String[] results)
    {
    	
    	results = result;
    	if(!places.isEmpty() && results!=null)
    	{
    		//results = result;
    		String temp[];
        	for(int i=0; i<result.length ; i++)
        	{
        		temp = result[i].split(" ");
        		if(temp.length!=2)
        		{
        			System.out.println(result[i]);
        			results[i] = temp[0];
        		}
        		else
        		{
        			results[i] = temp[1];
        		}
        	}
    		
    	//temp = results[0].split(" ");
    	map.addPolyline(new PolylineOptions()
        .add(places.get(location), places.get(results[0]))
        .width(5)
        .color(Color.RED));
    	
    	
    	
    	for(int i = 0; i< results.length - 1; i++)
    	{
    		//temp = results[i].split(" ");
    		LatLng t = places.get(results[i]);
    		map.addPolyline(new PolylineOptions()
            .add(places.get(results[i]), places.get(results[i+1]))
            .width(5)
            .color(Color.RED));
    		
    		map.addMarker(new MarkerOptions()
        	.position(places.get(results[i]))
        	.title(results[i])
        	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    	}
    	
    	map.addMarker(new MarkerOptions()
    	.position(places.get(results[results.length-1]))
    	.title(results[results.length-1])
    	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    	}
    	
    	// stub
    	/*LatLng  p2 = new LatLng(35.76943504820285,-78.67655072361231);// backhall2 -- change
    	LatLng  p3 = new LatLng(35.76933167486591,-78.67652926594019); // commons
    	LatLng  p1 = new LatLng(35.768967690836426,-78.67671132087708);// room
    	LatLng  p4 = new LatLng(35.76929603823704,-78.67639515548944);// stairs2
    	LatLng  p5 = new LatLng(35.769496528071116,-78.67620304226875);// ent hall
    	LatLng  p6 = new LatLng(35.76945381861016,-78.67634654045105);// ask us
    	
    	map.addPolyline(new PolylineOptions()
        .add(p1, p2)
        .width(5)
        .color(Color.RED));
    	

    	/*map.addPolyline(new PolylineOptions()
        .add(places.get(results[i]), places.get(results[i+1]))
        .width(5)
        .color(Color.RED));*/
  
    }
    
    //act on result of TTS data check
	@Override
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
    @Override
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
		protected void onPostExecute(Void res) {
			Log.i(TAG, "onPostExecute");
			if(result == null)
			{
			new AlertDialog.Builder(MainActivity.this)
		    .setTitle("No Route found")
		    .setMessage("Sorry, We were not able to find a route for this location")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        @Override
				public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        	dialog.cancel();
		        	return;
		        }
		     })
		    .setIcon(android.R.drawable.ic_menu_close_clear_cancel)
		     .show();
			
			}
			//tv.setText(path);
			showPoints(getApplicationContext(),result);	
			//plotLines(result);
		}

	}
	
	
	public void showPoints(final Context c, String[] result){
		//for (int i = 0; i < 1; i++) {
			//final Button button = calibrate[i];
							new PostJSONDataAsyncTask(c, null, url_points, false){
								/*@Override
					            protected void onPreExecute()
					            {
					                super.onPreExecute();
					            }*/
					            
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
					            	             
					            	            System.out.println(points);
					            	            // looping through All points
					            	            for(int i = 0; i < points.length(); i++){
					            	                JSONObject c = points.getJSONObject(i);
					            	                 
					            	                // Storing each json item in variable
					            	                double lat = c.getDouble(TAG_LAT);
					            	                double lng = c.getDouble(TAG_LNG);
					            	                String loc = c.getString(TAG_LOC);
					            	                
					            	                // adding each coordinate to ArrayList
					            	                LatLng temp = new LatLng(lat, lng);
					            	                places.put(loc, temp);
					            	               // pointsList.add(loc);
					            	            }
					            	        } catch (JSONException e) {
					            	            e.printStackTrace();
					            	        }
					                    }
					                    else {
					                        //TODO Do something here if no teams have been made yet
					                    }
					                    
					                    Log.d("wifiloc", "Update Success");
					                    
					                    //Iterator it = places.keySet().iterator();				          
					                    for(int i = 0; i < pointsList.size(); i++){
					            			map.addMarker(new MarkerOptions()
					            			.position(places.get(pointsList.get(i)))
					            			.title(""+pointsList.get(i)));
					            			//.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
					            		}
					                    
					                    //String a[] = {"a","b"};
					                    plotLines(MainActivity.this.result);
					                }
					                else
					                {
					                    // Toast.makeText(context, "Error connecting to server", Toast.LENGTH_LONG).show();
					                	Log.d("wifiloc", "Error Connecting to Server");
					                }

					            }
							}.execute();
							//break;
						
		//}
							//plotLines(MainActivity.this.result);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "Clicked "+(5 - position), Toast.LENGTH_LONG).show();
		IndoorLevel il = building.getLevels().get(position);
		il.activate();
		mDrawerLayout.closeDrawers();
		
	}
}
