package be.ulg.ac.tracebox;

import java.util.Vector;

import be.ulg.ac.tracebox.data.*;

public class APIPoster {
	private Vector<Probe> probes;

	public APIPoster()
	{
		probes = new Vector<Probe>();
	}

	public void addProbe(Probe p)
	{
		probes.add(p);
	}

	public boolean postProbes()
	{
		String postData = getPostXML();
		System.out.println(postData);

		return true;
	}

	private String getPostXML()
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<xml version=\"1.0\" encoding=\"UTF-8\">");
		builder.append("\n");
		for (Probe p:probes)
		{
			// Probe information
			builder.append("<probe address=\"" + p.getDestination().getAddress() + "\" starttime=\"" + p.getStartDate().toString() + "\" endtime=\"" + p.getEndDate().toString() + "\" connectivityType=\"" + p.getConnectivityMode() + "\" location=\"" + p.getLocationAsString() + "\">");
			builder.append("\n");

			// Routers
			for (Router r:p.getRouters())
			{
				builder.append("<router address=\"" + r.getAddress() + "\" ttl=\"" + r.getTtl() + "\">");
				builder.append("\n");

				// Packet modifications
				for (PacketModification m:r.getPacketModifications())
				{
					builder.append("<packetmodification layer=\"" + m.getLayer() + "\" field=\"" + m.getField() + "\" />");
					builder.append("\n");
				}

				builder.append("</router>");
				builder.append("\n");
			}
			builder.append("</probe>");
			builder.append("\n");
		}

		builder.append("</xml>");
		
		return builder.toString();
	}

}
