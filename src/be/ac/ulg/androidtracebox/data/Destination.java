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

public final class Destination {
	private int id;
	private String name;
	private String address;
	private boolean custom = false;

	public Destination ()
	{
	}

	// Normal destination
	public Destination (String n, String a)
	{
		setName(n);
		setAddress(a);
	}

	// Custom destination
	public Destination (String n, String a, boolean c)
	{
		setName(n);
		setAddress(a);
		if (c)
			custom = true;
	}

	public boolean isDestinationCustom()
	{
		return custom;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

}
