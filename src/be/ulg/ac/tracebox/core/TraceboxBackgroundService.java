package be.ulg.ac.tracebox.core;

import java.net.URL;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;
import be.ulg.ac.tracebox.core.MyLocation.LocationResult;
import be.ulg.ac.tracebox.data.*;

public class TraceboxBackgroundService extends Service
{
	private SharedPreferences sharedpreferences;
	private int numberOfDestinations;
	private int destinationsProbed;
	//private int maxDuration;
	private Vector<Destination> destinations;
	private Vector<Probe> probes;
	private DatabaseHandler db;

	public void onCreate()
	{
		Toast.makeText(this, "Tracebox scheduled created!", Toast.LENGTH_LONG).show();
	}

	private void getRandomDestinations()
	{
		// Need the DB access and get destinations
		destinations = new Vector<Destination>();
		db = new DatabaseHandler(this);
		for (int i = 0; i < numberOfDestinations; i++)
		{
			Destination randomDestination = db.getRandomDestination();
			destinations.add(randomDestination);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

		boolean probing = sharedpreferences.getBoolean("probingEnabled", false);
		if (probing)
		{
			// Need the max number of destinations
			numberOfDestinations = sharedpreferences.getInt("numberOfDestinations", 5);
			destinationsProbed = 0;

			// Need the max duration
			//maxDuration = sharedpreferences.getInt("maxDuration", 120);

			getRandomDestinations();
			probes = new Vector<Probe>();

			Toast.makeText(this, "Tracebox probing started!", Toast.LENGTH_LONG).show();
			getRandomDestinations();
			doTheTraceboxJob();
		}
		else
			System.out.println("Tried to start probe but disabled");
			
		return Service.START_STICKY;
	}

	private void doTheTraceboxJob()
	{
		startProbe(destinations.elementAt(0));
	}

	private void startProbe(Destination d)
	{
		// Do the tracebox
		Toast.makeText(this, "Probing:" + d.getName(), Toast.LENGTH_LONG).show();
		TraceboxerProber prober = (TraceboxerProber) new TraceboxerProber(d);
		destinationsProbed++;
		prober.execute();	
	}

	private void endProbe(final Probe p)
	{
		System.out.println("Probed: " + p.getDestination().getName());
		p.setConnectivityMode(MiscUtilities.getConnectivityType(this));

		// Save the location
		LocationResult locationResult = new LocationResult() {
		    @Override
		    public void gotLocation(Location location) {
		       p.setLocation(location);
		    }
		};
		MyLocation myLocation = new MyLocation();
		myLocation.getLocation(this, locationResult);

		probes.add(p);

		if (destinationsProbed < numberOfDestinations)
		{
			Destination d = destinations.elementAt(destinationsProbed);
			startProbe(d);			
		}
		else
		{
			// Post the probes
			APIPoster poster = new APIPoster();

			// Send X probes
			for (Probe currentProbe:probes)
			{
				// Save the connectivityMode
				poster.addProbe(currentProbe);
				
			}

			// Save Probe
			db.addProbe(p);

			// Save AS XML file
			if (poster.saveProbesAsXMLFile("out.xml"))
			{
				ProbePoster instantPoster = new ProbePoster(poster);
				instantPoster.execute();
			}

			// Save log
			db.addLog(new Log("Probed " + p.getDestination().getName()));
		}		
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	public void onDestroy()
	{
		Toast.makeText(this, "Tracebox scheduled stopped", Toast.LENGTH_LONG).show();
	}

	private class TraceboxerProber extends AsyncTask<URL, Integer, Long>
	{
		private Destination dest;
		private Probe newProbe;

		public TraceboxerProber (Destination d)
		{
	        super();
	        dest = d;
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			TraceboxUtility tracebox = new TraceboxUtility(dest);
			newProbe = tracebox.doTracebox();
			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			endProbe(newProbe);				
		}
	}

	private class ProbePoster extends AsyncTask<URL, Integer, Long>
	{
		private APIPoster poster;
		private boolean postOK = false;

		public ProbePoster (APIPoster p)
		{
	        super();
	        poster = p;
	    }

		@Override
		protected Long doInBackground(URL... params)
		{
			postOK = poster.tryToPostData();
			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			if (postOK)
				System.out.println("Great\nYour probe has been submitted to the server and will be used for statistics, thank you.");
			else
				System.out.println("ERROR\nThere was an error while posting the data on the server. Please, try again later");
		}
	}

}
