package com.databaseandroidproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity{

	private static final String PRINT = "print";
	private static final String ADD = "add";
	private static final String DELETE = "delete";
	private static final String ARG = "arg";
	private static final String REQUEST = "request";
	private static final String URL = "http://192.168.1.5:9090/JavaServlet/test";
	
	private EditText inputValue = null;
	private Button wypiszBtn, dodajBtn, usunBtn;
	private LinearLayout ll;
	private ArrayList<TextView> textViewArray;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		inputValue = (EditText) findViewById(R.id.inputNum);
		wypiszBtn = (Button) findViewById(R.id.print);
		dodajBtn = (Button) findViewById(R.id.add);
		usunBtn = (Button) findViewById(R.id.remove);
		ll = (LinearLayout) findViewById(R.id.rl_with_textviews);
		textViewArray = new ArrayList<TextView>();
				
		wypiszBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				printList();
			}
		});
		
		dodajBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendRequest(ADD, inputValue.getText().toString());		
			}
		});
		
		usunBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendRequest(DELETE, inputValue.getText().toString());				
			}
		});
	}
	
	
	
	
	private void printList()
	{
		for(TextView TV : textViewArray)
			ll.removeView(TV);
		
		new Thread(new Runnable() {
			public void run() {

				try{
					URL url = new URL(URL);
					URLConnection connection = url.openConnection();

					connection.setDoOutput(true);
					OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
					out.write(PRINT);
					out.close();

					BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

					String returnString;

					while ((returnString = in.readLine()) != null) 
					{
						final String wynik = returnString;

						runOnUiThread(new Runnable() {
							public void run() {								
								TextView TV = createTextView(wynik);
								ll.addView(TV);
								textViewArray.add(TV);
							}
						});						
					}
					in.close();
					

				}catch(Exception e){}

			}
		}).start();
	}
	
	
	private TextView createTextView(String wynik)
	{
		TextView TV = (TextView) getLayoutInflater().inflate(R.layout.text_view, null);
		TV.setText(wynik);
		
		return TV;
	}
		
	
	private void sendRequest(String request, String arg)
	{
		 Uri.Builder b = Uri.parse(URL).buildUpon();
	        b.appendQueryParameter(REQUEST, request)
	        .appendQueryParameter(ARG, arg).build();
	        
	        DefaultHttpClient httpClient = new DefaultHttpClient();   
	        HttpGet httpGet = new HttpGet(b.toString());
	                
			try {
				httpClient.execute(httpGet);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}