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
