package edu.ncsu.wifilocator;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class QuestionFragment extends Fragment{


	View view;
	TextView tv;
	 @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	        // Inflate the layout for this fragment
		 view = inflater.inflate(R.layout.questions_fragment, container, false);
		 tv = (TextView) view.findViewById(R.id.textView4);
		 tv.setText("text");
		 
		 Button sButton = (Button)view.findViewById(R.id.searchButton);
		 
		 sButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.d("FRAGMENT", "Hey I was clicked");
				new roomsAsync().execute();
			}
		});
	     return view;
	    }
	 
	 public String getTemp()
	 { 
		return tv.getText().toString(); 
	 }
	 
	 
}

class roomsAsync extends AsyncTask<String,Void,String>
{
	private final String NAMESPACE = "http://tempuri.org/";
	private final String URL = "http://win-res02.csc.ncsu.edu/MediationService.svc";
	private final String SOAP_ACTION = "http://tempuri.org/IMediationService/GetMatchingRooms";
	private final String GET_ROOMS_METHOD_NAME = "GetMatchingRooms";
	
	@Override
	protected String doInBackground(String... params) {
		// TODO Auto-generated method stub
		String res = getRooms();
		return res;
	}
	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		Log.d("OnPostExe",result);
		//Intent i = new Intent(this,DisplayRoomsActivity.class);
		/*Toast.makeText(,"", Toast.LENGTH_LONG).show();
		Toast.*/
	}
	
	public String getRooms() {
		// TODO Auto-generated method stub
		
		SoapObject request = new SoapObject(NAMESPACE, GET_ROOMS_METHOD_NAME);
		//Property which holds input parameters
		PropertyInfo light = new PropertyInfo();
		light.setType(String.class);
		//light.setType(type);
		light.setName("light");
		light.setValue("99");
		
		PropertyInfo sound = new PropertyInfo();
		sound.setType(String.class);
		sound.setName("sound");
		sound.setValue("23");
		
		PropertyInfo tempHi = new PropertyInfo();
		tempHi.setType(int.class);
		tempHi.setName("tempHi");
		tempHi.setValue(85);
		
		PropertyInfo tempLo = new PropertyInfo();
		tempLo.setType(int.class);
		tempLo.setName("tempLo");
		tempLo.setValue(60);
		//tempLo.
		
		PropertyInfo type = new PropertyInfo();
		type.setType(String.class);
		type.setName("type");
		type.setValue("group_study_room");
		
		
		//Add the property to request object
		/*request.addProperty(light);
		request.addProperty(sound);
		*/
		request.addProperty(light);
		request.addProperty(sound);
		request.addProperty(tempHi);
		request.addProperty(tempLo);
		request.addProperty(type);
		
		
		//Create envelope
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
				SoapEnvelope.VER10);
		envelope.dotNet = true;
		//Set output SOAP object
		envelope.setOutputSoapObject(request);
		//Create HTTP call object
		HttpTransportSE androidHttpTransport = new HttpTransportSE(URL);
		androidHttpTransport.debug = true;
		androidHttpTransport.setXmlVersionTag("<?xml version=\"1.0\" encoding=\"utf-8\"?>");

		try {
			//Invoke web service
			androidHttpTransport.call(SOAP_ACTION, envelope);
			if (envelope.bodyIn instanceof SoapFault)
			{
			    final SoapFault sf = (SoapFault) envelope.bodyIn;
			    System.out.println(sf.faultstring);
			}
			//Get the response
			SoapObject resp = (SoapObject) envelope.bodyIn;
			//Assign it to path static variable
			String path1 = resp.toString();
			Log.d("ROOMS",path1);
			System.out.println(path1);
			
			return path1;
			
			}
			

		catch (Exception e) {
			e.printStackTrace();
		}
		return "";
		
	}
	 
}