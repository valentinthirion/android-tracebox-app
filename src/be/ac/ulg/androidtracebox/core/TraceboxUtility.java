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
		Probe probe = this.doTracebox();

		// Error while probing
		if (probe == null)
			return -1;

		// Set Other data to the probe
		probe = setExternalDataToProbe(probe);
		probe.setBatteryDifference(batteryBefore - MiscUtilities.getBatteryLevel(context));

		// Save it
		db.addProbe(probe);

		// Prepare the probe to be sent
		APIPoster poster = new APIPoster(context);
		poster.addProbe(probe);

		// Save AS XML file
		if (!poster.saveProbesAsXMLFile("out.xml"))
		{
			db.addLog(new Log("Error while saving probe to " + probe.getDestination().getName()));
			return -2;
		}

		if (!poster.tryToPostData())
			return -3;
		
		// Save log
		db.addLog(new Log("Probed " + probe.getDestination().getName()));

		return 1;
	}

	private Probe setExternalDataToProbe(final Probe probe)
	{
		// Save the probe and post it
		probe.setConnectivityMode(MiscUtilities.getConnectivityType(context)); // Save the connectivityMode
		probe.setCarrierName(MiscUtilities.getCellularCarrierName(context));
		probe.setCellularCarrierType(MiscUtilities.getCellularConnectionType(context));
		LocationResult locationResult = new LocationResult(){
		    @Override
		    public void gotLocation(Location location){
		        probe.setLocation(location); // Save the location
		    }
		};
		//MyLocation myLocation = new MyLocation();
		//myLocation.getLocation(context, locationResult);

		return probe;
	}
	

	public TraceboxUtility(Destination d)
	{
		super();
		destination = d;
	}

	public Probe doTracebox()
	{
		// Create the probe
		Probe newProbe = new Probe(this.destination);

		// Create the request
		String request = "system/xbin/busybox tracebox " + destination.getAddress(); 
		System.out.println("Request : " + request);

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
				// SPACES
				if (line[j].equals(""))
					continue;

				// TTL
				try {
					ttl = Integer.parseInt(line[j]);
					ttlFound = true;
					continue;
				}
				catch (NumberFormatException e) {
					//System.out.println(line[j]);
				}

				// Star
				if (line[j].equals("*"))
				{
					starsCounter++;
				}

				// Stars counter
				if (starsCounter == 3)
				{
					Router currentRouter = new Router(ttl, "* * *", "");
					newProbe.addRouter(currentRouter);
					break;
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

						Router currentRouter = new Router(ttl, router, modifs);
						newProbe.addRouter(currentRouter);

						break;
					}
				}
			}

			
		}
		
		newProbe.endProbe();
		return newProbe;
	}
}
