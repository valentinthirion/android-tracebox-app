package be.ulg.ac.tracebox;

import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import be.ulg.ac.tracebox.MyLocation.LocationResult;
import be.ulg.ac.tracebox.core.TraceboxUtility;
import be.ulg.ac.tracebox.data.DatabaseHandler;
import be.ulg.ac.tracebox.data.Destination;
import be.ulg.ac.tracebox.data.Probe;

public class MainActivity extends Activity {

	private boolean installed = false;
	private boolean probing = false;
	
	private SharedPreferences sharedpreferences;
	private Editor editor;

	private Button instantProbeButton;
	private TextView nextProbeTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// SET PREFERENCES STUFF
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedpreferences.edit();

        this.drawView();

		// Launch installer if not installed
		if (!installed == true)
		{
			Intent installationItent = new Intent(this, InstallationActivity.class);
			startActivity(installationItent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onResume()
	{
		this.drawView();
	    super.onResume();
	}

	private void drawView()
	{
		// GET VALUES
		installed = sharedpreferences.getBoolean("systemInstalled", false);
		probing = sharedpreferences.getBoolean("probingEnabled", false);

		Button statusButton = (Button) this.findViewById(R.id.status_button);
		Button installationButton = (Button) this.findViewById(R.id.installation_button);
		instantProbeButton = (Button) this.findViewById(R.id.instant_probe_button);
		nextProbeTitle = (TextView) this.findViewById(R.id.next_probe_title);

		// INSTALLATION
		if (this.installed)
		{
			installationButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			installationButton.setText(R.string.main_installation_installed_button);
			statusButton.setEnabled(true);
			instantProbeButton.setEnabled(true);
			if (Utilities.isConnected(this))
				instantProbeButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			else
				instantProbeButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
		}
		else
		{
			statusButton.setEnabled(false);
			instantProbeButton.setEnabled(false);
			instantProbeButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
		}

		if (this.probing)
		{
			statusButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			statusButton.setText(R.string.main_status_online_button);
			nextProbeTitle.setVisibility(View.VISIBLE);
		}
		else
		{
			statusButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
			statusButton.setText(R.string.main_status_offline_button);
			nextProbeTitle.setVisibility(View.INVISIBLE);
		}	
	}

	/*
	 * BUTTONS ACTIONS
	 */
	public void open_settings_page(View view)
	{
		Intent installationItent = new Intent(this, SettingsActivity.class);
		startActivity(installationItent);
	}

	public void open_destinations_page(View view)
	{
		Intent installationItent = new Intent(this, DestinationsActivity.class);
		startActivity(installationItent);
	}

	public void open_results_page(View view)
	{
		Intent installationItent = new Intent(this, ResultsActivity.class);
		startActivity(installationItent);
	}

	public void open_installation_page(View view)
	{
		if (Utilities.isConnected(this))
		{
			Intent installationItent = new Intent(this, InstallationActivity.class);
			startActivity(installationItent);
		}
	}

	public void turn_probing_on_off(View view)
	{
		if (probing)
		{
			System.out.println("TURN PROBING OFF");
			editor.putBoolean("probingEnabled", false);
		}
		else
		{
			System.out.println("TURN PROBING ON");
			editor.putBoolean("probingEnabled", true);
		}

		editor.commit();
		this.drawView();
	}

	public void show_about_page(View view)
	{
		Intent installationItent = new Intent(this, AboutActivity.class);
		startActivity(installationItent);
	}

	public void send_instant_probe(View view)
	{
		if (Utilities.isConnected(this))
		{
			instantProbeButton.setEnabled(false);
			instantProbeButton.setBackgroundColor(Color.argb(255, 230, 126, 34)); // FLAT ORANGE
			try {
				DatabaseHandler db = new DatabaseHandler(this);

				Destination randomDestination = db.getRandomDestination();

				// Show message
				new AlertDialog.Builder(this)
			    .setTitle("Instant probe")
			    .setMessage("Tracebox is going to probe " + randomDestination.getName() + ", you will have to accecpt the SU access in the prompt box.")
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // continue with delete
			        }
			     })
			     .show();

				TraceboxerInstantProbe prober = (TraceboxerInstantProbe) new TraceboxerInstantProbe(randomDestination);
				prober.execute();
			}
			catch (Exception e) {
				System.out.println("ERROR in busybox: " + e.getMessage());
			}
		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("Error")
		    .setMessage("You are not connected to internet by Wifi/Cellular. Tracebox can not send probes without a connection. Try later.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();
		}
	}

	public void endInstantProbe(final Probe p)
	{
		Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(500); // Vibrate for 500 milliseconds
		instantProbeButton.setEnabled(true);
		instantProbeButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN

		if (p != null)
		{
			// SAVE THIS PROBE AND SEND IT TO THE SERVER
			p.setConnectivityMode(Utilities.getConnectivityType(this)); // Save the connectivityMode
			
			LocationResult locationResult = new LocationResult(){
			    @Override
			    public void gotLocation(Location location){
			        p.setLocation(location); // Save the location
			    }
			};
			MyLocation myLocation = new MyLocation();
			myLocation.getLocation(this, locationResult);

			// Prepare the probe to be sended
			APIPoster poster = new APIPoster();
			poster.addProbe(p);
			if (poster.postProbes())
			{
				new AlertDialog.Builder(this)
			    .setTitle("Great")
			    .setMessage("You just probed " + p.getDestination().getName() + ", the result has been stored and will be used for statistics. Thanks.")
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // continue with delete
			        }
			     })
			     .show();
			}
			else
				showErrorAfterInstandProbe();

			
		}
		else
			showErrorAfterInstandProbe();
	}

	private void showErrorAfterInstandProbe()
	{
		new AlertDialog.Builder(this)
	    .setTitle("Error")
	    .setMessage("There was an error while the probing.")
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // continue with delete
	        }
	     })
	     .show();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
			return false;
		if (keyCode == KeyEvent.KEYCODE_SEARCH)
			return false;
		if (keyCode == KeyEvent.KEYCODE_SETTINGS) 
			return false;
		return super.onKeyDown(keyCode, event);
	}

	private class TraceboxerInstantProbe extends AsyncTask<URL, Integer, Long>
	{
		private Destination dest;
		private Probe newProbe;

		public TraceboxerInstantProbe (Destination d)
		{
	        super();
	        dest = d;
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			TraceboxUtility tracebox = new TraceboxUtility(dest);
			newProbe = tracebox.doTracebox();
			//newProbe = new Probe(dest);
			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			endInstantProbe(newProbe);				
		}
	}
}
