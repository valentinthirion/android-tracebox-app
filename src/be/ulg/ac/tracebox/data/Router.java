package be.ulg.ac.tracebox.data;

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
