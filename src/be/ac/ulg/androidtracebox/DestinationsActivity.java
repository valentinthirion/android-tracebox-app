/*
 * Tracebox for Android has been developed by Valentin THIRION
 * in the context of his Master Thesis
 * at the University of Liege (ULg) in Belgium in june 2014.
 * This work has been partially funded by the
 * European Commission funded mPlane ICT-318627 project
 * (http://www.ict-mplane.eu).
 * 
 * All information, copyrights and code about
 * this project can be found at: www.androidtracebox.com
 */

package be.ac.ulg.androidtracebox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import be.ac.ulg.androidtracebox.core.MiscUtilities;
import be.ac.ulg.androidtracebox.core.TraceboxUtility;
import be.ac.ulg.androidtracebox.data.DatabaseHandler;
import be.ac.ulg.androidtracebox.data.Destination;

public class DestinationsActivity extends Activity {
	private Vector<Destination> destinations;

	private EditText newDestinationEditText;
	private ListView destinations_table;
	private DatabaseHandler db;
	private ProgressDialog progressDialog;

	private String postURL;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_destinations);

		postURL = getResources().getString(R.string.api_prefix) + "resolveURL.php";

		setTitle("Tracebox for Android - Destinations");

		newDestinationEditText = (EditText) findViewById(R.id.destinations_page_add_edittext);
		destinations_table = (ListView) findViewById(R.id.destinations_table);

		db = new DatabaseHandler(this);
		newDestinationEditText.clearFocus();

		destinations = db.getAllDestinations();
		final ArrayList<Destination> list = new ArrayList<Destination>();
	    for (Destination d:destinations)
	    {
	    	list.add(d);
	    }

	    final DestinationArrayAdaptater adapter = new DestinationArrayAdaptater(this, android.R.layout.simple_list_item_1, list);
	    destinations_table.setAdapter(adapter);
	    setNumberOfDestinationsText();
	}

	private void setNumberOfDestinationsText()
	{
		TextView numberOfDestinations = (TextView) findViewById(R.id.number_of_destinations_label);
	    numberOfDestinations.setText(destinations.size() + " destinations");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.destinations, menu);
		return true;
	}

	private class DestinationArrayAdaptater extends ArrayAdapter<Destination>
	{
		private final Context context;
		private final List<Destination> destinations;

		public DestinationArrayAdaptater(Context c, int textViewResourceId, List<Destination> d)
		{
			super(c, textViewResourceId, d);
			context = c;
			destinations = d;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			final Destination currentDest = destinations.get(position);

		    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = inflater.inflate(R.layout.destination_row, parent, false);
		    TextView nameView = (TextView) rowView.findViewById(R.id.destination_name);
		    nameView.setText(currentDest.getName());
		    TextView addressView = (TextView) rowView.findViewById(R.id.destination_ip);
		    addressView.setText(currentDest.getAddress());
		    Button probeButton = (Button) rowView.findViewById(R.id.destinations_page_probe_button);
		    probeButton.setOnClickListener(new View.OnClickListener() {

	            @Override
	            public void onClick(View arg0) {

	    			// Show message
	    			new AlertDialog.Builder(context)
	    		    .setTitle("Instant probe")
	    		    .setMessage("Tracebox is going to probe " + currentDest.getName() + ", you will have to accecpt the SU access in the prompt box.")
	    		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	    		        public void onClick(DialogInterface dialog, int which) {

	    		        	// Launch the prober
	    		        	TraceboxerInstantProbe prober = (TraceboxerInstantProbe) new TraceboxerInstantProbe(context);
	    		        	Destination d = prober.getDestination();
	    					prober.execute();

	    		        	progressDialog = ProgressDialog.show(
	    		        			context, "Please wait",
	    	                        "Tracebox is probing " + d.getName() + "...\nThis could take up to one minute.", true);
	    	                progressDialog.setCancelable(false);
	    		        }
	    		     })
	    		     .show();
	            }
	        });

		    return rowView;
		}
	}

	public void add_custom_destination(View view)
	{
		if (MiscUtilities.isConnected(this))
		{
			newDestinationResolver getter = (newDestinationResolver) new newDestinationResolver();
			progressDialog = ProgressDialog.show(
        			this, "Please wait",
                    "Tracebox is resolving the new given destination.", true);
			getter.execute();
		}
		else
		{
			this.showDialogBox("Error", "You are not connected to internet.");
		}
	}

	public void show_info_about_destinations(View view)
	{
		this.showDialogBox("About the destinations", "The pre-set destinations are got from the Backoffice. The list contains the TOP500 Alexa.\nYou can add a custom one, that will be used for future tests.");
	}

	public void endInstantProbe(int probeResult)
	{
		progressDialog.cancel();

		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(500); // Vibrate for 500 milliseconds

		switch (probeResult)
		{
		case 1:
			showDialogBox("Great", "Your probe has been submitted to the server and will be used for statistics, thank you.");
			break;
		case 0:
			break;
		case -1:
			showDialogBox("ERROR", "There was an error while the probing. Sorry.");
			break;
		case -2:
			showDialogBox("ERROR", "The probe could not be saved, try again later.");
			break;
		case -3:
			showDialogBox("ERROR", "There was an error while posting the data on the server. Please, try again later");
			break;
		}
	}

	private void showDialogBox(String title, String message)
	{
		new AlertDialog.Builder(this)
	    .setTitle(title)
	    .setMessage(message)
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        }
	     })
	     .show();
	}
	
	private class newDestinationResolver extends AsyncTask<URL, Integer, Long>
	{
		private EditText field;
		private String newDestination;
		private String newAddress;
		@Override
		protected Long doInBackground(URL... params) 
		{
			field = (EditText) findViewById(R.id.destinations_page_add_edittext);
			newDestination = field.getText().toString();
			newAddress = resolveDestination(newDestination);
			
			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			progressDialog.cancel();
			if (newAddress != null)
			{
				db.addDestination(new Destination(newDestination, newAddress, true));

				field.setText("");
				setNumberOfDestinationsText();
				DestinationArrayAdaptater a = ((DestinationArrayAdaptater) destinations_table.getAdapter());
				a.notifyDataSetChanged();
			}
			else
			{
				showDialogBox("Error", "There was an error while trying to add a new destination. Maybe it already exists or the resolver could not find the IP address of the enterred URL.");
			}
		}

		private String resolveDestination(String newDestination)
		{
			if (!newDestination.equals(""))
			{
				boolean exists = false;
				for (Destination d:destinations)
				{
					if (d.getAddress().equals(newDestination) || d.getName().equals(newDestination))
						exists = true;
				}

				if (!exists)
				{
					// Do the resolver online from the BO
					HttpURLConnection connection;
				    OutputStreamWriter request = null;

				    URL url = null;   
				    String response = null;         
				    String parameters = "url=" +  newDestination;

				    try
			        {
			            url = new URL(postURL);
			            connection = (HttpURLConnection) url.openConnection();
			            connection.setDoOutput(true);
			            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			            connection.setRequestMethod("POST");

			            request = new OutputStreamWriter(connection.getOutputStream());
			            request.write(parameters);
			            request.flush();
			            request.close();            
			            String line = "";

			            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
			            BufferedReader reader = new BufferedReader(isr);
			            StringBuilder sb = new StringBuilder();
			            while ((line = reader.readLine()) != null)
			                sb.append(line + "\n");

			            response = sb.toString();
			            System.out.println("Message from Server: " + response);             
			            isr.close();
			            reader.close();

			            if(response.contains("-1"))
			            	return null;
			            else
			            	return response;

			        } catch(IOException e) {
			            e.printStackTrace();
			            return null;
			        }

					/*InetAddress address = null;
					try {
						address = InetAddress.getByName(newDestination);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} 

					if (address != null)
					{
						String addressString = address.toString();
						addressString = addressString.substring(addressString.indexOf("/") + 1, addressString.length());
						return addressString;
					}
					*/
				}
			}
			return null;
		}
	}

	private class TraceboxerInstantProbe extends AsyncTask<URL, Integer, Long>
	{
		private int probeResult = 0;
		private Context context;
		private TraceboxUtility tracebox;

		public TraceboxerInstantProbe (final Context c)
		{
	        super();
	        context = c;
	        tracebox = new TraceboxUtility(context);
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			probeResult = tracebox.doTraceboxAndPost();
			return null;
		}

		public Destination getDestination()
		{
			return tracebox.getDestination();
		}

		@Override
		protected void onPostExecute(Long result)
		{
			endInstantProbe(probeResult);				
		}
	}
}
