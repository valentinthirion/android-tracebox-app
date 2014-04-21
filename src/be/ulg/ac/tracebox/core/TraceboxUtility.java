package be.ulg.ac.tracebox.core;

import be.ulg.ac.tracebox.data.*;

public class TraceboxUtility
{
	private Destination destination;
	private String rawResult;

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
			if (steps[i].contains("*"))
				continue;

			//System.out.println(steps[i]);
			String[] line = steps[i].split("\\s+");

			if (line.length < 3)
				continue;

			int ttl = 0;
			String router = "";
			String modifs = "";
			int k = 0;
			// FIND THE TTL
			for (int j = 0; j < line.length; j++)
			{
				//System.out.println("line[" + j + "]=" + line[j]);
				k = j;
				if (line[j].equals(""))
					continue;
				else
				{
					ttl = Integer.parseInt(line[j]);
					break;
				}
			}

			// FIND THE ROUTER ADDRESS
			router = line[k + 1];

			// FIND THE MODIFICATIONS
			for (int j = k + 2; j < line.length; j++)
				modifs += " " + line[j];

			Router currentRouter = new Router(ttl, router, modifs);
			newProbe.addRouter(currentRouter);
		}
		
		newProbe.endProbe();
		return newProbe;
	}
}
