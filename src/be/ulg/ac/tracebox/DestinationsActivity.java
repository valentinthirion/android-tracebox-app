package be.ulg.ac.tracebox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import be.ulg.ac.tracebox.core.MiscUtilities;
import be.ulg.ac.tracebox.data.DatabaseHandler;
import be.ulg.ac.tracebox.data.Destination;

public class DestinationsActivity extends Activity {
	private Vector<Destination> destinations;

	private EditText newDestinationEditText;
	private TableLayout destinations_table;
	private DatabaseHandler db;

	private String postURL = "http://www.medineo.be/be.ac.ulg.androidtracebox/api/resolveURL.php";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_destinations);

		setTitle("Tracebox for Android - Destinations");

		newDestinationEditText = (EditText) findViewById(R.id.destinations_page_add_edittext);
		destinations_table = (TableLayout) findViewById(R.id.destinations_table);

		db = new DatabaseHandler(this);
		newDestinationEditText.clearFocus();

		fillTable();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.destinations, menu);
		return true;
	}

	public void fillTable()
	{
		destinations = db.getAllDestinations();

		for (Destination d:destinations)
		{
			TableRow tableRow = new TableRow(this);

			TextView nameText = new TextView(this);
			nameText.setText(d.getName());
	        tableRow.addView(nameText);

	        TextView addressText = new TextView(this);
	        addressText.setText(d.getAddress());
	        tableRow.addView(addressText);

	        destinations_table.addView(tableRow);
		}

		TextView number = (TextView) findViewById(R.id.number_of_destinations_label);
		number.setText("# of destinations: " + destinations.size());
	}

	public void showErrorDialogBox()
	{
		new AlertDialog.Builder(this)
	    .setTitle("Error")
	    .setMessage("There was an error while trying to add a new destination. Maybe it already exists or the resolver could not find the IP address of the enterred URL.")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        }
	     })
	     .show();
	}
	public void add_custom_destination(View view)
	{
		if (MiscUtilities.isConnected(this))
		{
			newDestinationResolver getter = (newDestinationResolver) new newDestinationResolver();
			getter.execute();
		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("Error")
		    .setMessage("You are not connected to internet.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();
		}
	}

	public void show_info_about_destinations(View view)
	{
		new AlertDialog.Builder(this)
	    .setTitle("About the destinations")
	    .setMessage("The pre-set destinations are got from the Backoffice. The list contains the TOP500 Alexa.\nYou can add a custom one, that will be used for future tests.")
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
			if (newAddress != null)
			{
				db.addDestination(new Destination(newDestination, newAddress, true));
				field.setText("");
				fillTable();
			}
			else
			{
				showErrorDialogBox();
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

}
