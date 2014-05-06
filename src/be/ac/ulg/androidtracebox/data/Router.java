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

package be.ac.ulg.androidtracebox.data;

import java.util.Vector;

public class Router {
	private int id;
	private String address;
	private int ttl;
	private Vector<PacketModification> modifications;

	public Router (int t, String a, String modif)
	{
		setTtl(t);
		this.setAddress(a);

		modifications = new Vector<PacketModification>();
		String[] splitted = modif.split("\\s+");
		for (int i = 0; i < splitted.length; i++)
		{
			if (!splitted[i].equals(""))
				if (splitted[i].contains("::"))
					modifications.add(new PacketModification(splitted[i]));
		}
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public Vector<PacketModification> getPacketModifications()
	{
		return modifications;
	}
}
