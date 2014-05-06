package be.ac.ulg.androidtracebox.core;

import be.ac.ulg.androidtracebox.data.*;

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
				k = j;
				if (line[j].equals(""))
					continue;
				else
				{
					try {
						ttl = Integer.parseInt(line[j]);
						break;
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
						break;
					}
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
