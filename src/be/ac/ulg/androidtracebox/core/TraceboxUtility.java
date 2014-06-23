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

package be.ac.ulg.androidtracebox.core;

import java.util.Vector;

import android.content.Context;
import android.location.Location;
import be.ac.ulg.androidtracebox.core.MyLocation.LocationResult;
import be.ac.ulg.androidtracebox.data.*;

public class TraceboxUtility
{
	private Destination destination;
	private String rawResult;
	private Context context;
	private DatabaseHandler db;
	private float batteryBefore;
	private Probe probeResult;
	private int mode;

	public TraceboxUtility(Context c)
	{
		context = c;

		// Get the destination
		db = new DatabaseHandler(context);
		batteryBefore = MiscUtilities.getBatteryLevel(context);
		destination = db.getRandomDestination();
	}

	public TraceboxUtility(Context c, Destination d)
	{
		context = c;
		destination = d;

		db = new DatabaseHandler(context);
		batteryBefore = MiscUtilities.getBatteryLevel(context);
	}

	public Probe getProbe()
	{
		return probeResult;
	}

	public Destination getDestination()
	{
		return this.destination;
	}

	/*
	 * Returns :
	 *	1	: all went fine
	 * -1	: error when probing
	 * -2	: error when saving
	 * -3	: error when posting
	 * 
	 */
	public int doTraceboxAndPost()
	{
		probeResult = this.doTracebox();

		// Error while probing
		if (probeResult == null)
			return -1;

		// Error, no router found
		if (probeResult.getRouters().size() == 0)
			return -1;

		// Set Other data to the probe
		probeResult = setExternalDataToProbe(probeResult);
		probeResult.setBatteryDifference(batteryBefore - MiscUtilities.getBatteryLevel(context));

		// Save it
		db.addProbe(probeResult);

		// Prepare the probe to be sent
		APIPoster poster = new APIPoster(context);
		poster.addProbe(probeResult);

		// Save AS XML file
		if (!poster.saveProbesAsXMLFile("out.xml"))
		{
			db.addLog(new Log("Error while saving probe to " + probeResult.getDestination().getName()));
			return -2;
		}

		if (!poster.tryToPostData())
			return -3;
		
		// Save log
		db.addLog(new Log("Probed " + probeResult.getDestination().getName()));

		return 1;
	}

	private Probe setExternalDataToProbe(final Probe probe)
	{
		// Save the probe and post it
		probe.setConnectivityMode(MiscUtilities.getConnectivityType(context)); // Save the connectivityMode
		probe.setCarrierName(MiscUtilities.getCellularCarrierName(context));
		probe.setCellularCarrierType(MiscUtilities.getCellularConnectionType(context));
		new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        probe.setLocation(location); // Save the location
		    }
		};
		//MyLocation myLocation = new MyLocation();
		//myLocation.getLocation(context, locationResult);

		return probe;
	}

	public Probe doTracebox()
	{
		// Create the probe
		Probe newProbe = new Probe(this.destination);

		// Create the request
		String request = "system/xbin/busybox tracebox " + destination.getAddress(); 
		//System.out.println("Request : " + request);

		try {
			// Execute the request
			rawResult = CommandManager.executeCommandAsRoot(request);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		// Work on the result
		String[] steps = rawResult.split("\n");
		for (int i = 0; i < steps.length; i++)
		{
			//System.out.println("Line : " + steps[i]);

			String[] line = steps[i].split("\\s+");
			int ttl = 0;
			String router = "";
			String modifs = "";
			boolean ttlFound = false;
			boolean routerFound = false;
			int starsCounter = 0;

			for (int j = 0; j < line.length; j++)
			{
				// TTL
				try {
					ttl = Integer.parseInt(line[j]);
					ttlFound = true;
					continue;
				}
				catch (NumberFormatException e) {
					//System.out.println(line[j]);
				}

				// SPACES
				if (line[j].equals(""))
					continue;

				// Star
				if (line[j].equals("*"))
				{
					starsCounter++;

					// Stars counter
					if (starsCounter == 3)
					{
						newProbe.addRouter(new Router(ttl, "* * *", ""));
						break;
					}
					continue;
				}

				// NOT STAR AND HAS TTL
				if (ttlFound)
				{
					// FOUND ROUTER
					if (!line[j].startsWith("IP") && !line[j].startsWith("TCP") && !routerFound)
					{
						router = line[j];
						routerFound = true;

						// FIND THE MODIFICATIONS
						for (int k = j + 1; k < line.length; k++)
							modifs += " " + line[k];

						//System.out.println("Modifs : " + modifs);
						newProbe.addRouter(new Router(ttl, router, modifs));

						break;
					}
				}
			}
		}
		
		newProbe.endProbe();
		return newProbe;
	}

	public boolean doTraceboxToDetectProxies()
	{
		// Create the request
		String request = "system/xbin/busybox tracebox " + destination.getAddress() + " -sc proxy"; 
		System.out.println("Request : " + request);

		try {
			// Execute the request
			rawResult = CommandManager.executeCommandAsRoot(request);

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		// Work on the result and detect if found proxy
		String[] steps = rawResult.split("\n");
		for (int i = 0; i < steps.length; i++)
		{
			System.out.println(steps[i]);
			if (steps[i].contains("PROXY DETECTED"))
				return true;
			if (steps[i].contains("PROBABLY NO PROXY DETECTED"))
				return false;
			else
				continue;
		}
		return false;
	}

	public Vector<Integer> doTraceboxToDetectFullICMPRouters()
	{
		// Create the request
		String request = "system/xbin/busybox tracebox " + destination.getAddress() + " -sc full_icmp"; 
		System.out.println("Request : " + request);

		try {
			// Execute the request
			rawResult = CommandManager.executeCommandAsRoot(request);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		int routersCount = 0;
		int fullICMCount = 0;

		// Work on the result and detect if found proxy
		String[] steps = rawResult.split("\n");
		for (int i = 0; i < steps.length; i++)
		{
			System.out.println(steps[i]);
			String[] line = steps[i].split("\\s+");
			int ttl = 0;
			boolean ttlFound = false;
			boolean routerFound = false;
			
			for (int j = 0; j < line.length; j++)
			{
				// TTL
				try {
					ttl = Integer.parseInt(line[j]);
					ttlFound = true;
					routersCount++;
					continue;
				}
				catch (NumberFormatException e) {
					//System.out.println(line[j]);
				}

				// SPACES
				if (line[j].equals(""))
					continue;

				// STARS
				if (line[j].equals("*"))
					continue;

				// NOT STAR AND HAS TTL
				if (ttlFound)
				{
					// FOUND ROUTER
					if (!line[j].startsWith("IP") && !line[j].startsWith("TCP") && !routerFound)
					{
						routerFound = true;
						fullICMCount++;
						break;
					}
				}
			}
		}

		Vector<Integer> r = new Vector<Integer>();
		r.add(routersCount);
		r.add(fullICMCount);

		return r;
	}
}
