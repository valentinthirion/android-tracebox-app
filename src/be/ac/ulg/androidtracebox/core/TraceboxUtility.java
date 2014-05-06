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
