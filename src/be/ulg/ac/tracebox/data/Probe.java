package be.ulg.ac.tracebox.data;

import java.sql.Date;
import java.util.Vector;

import android.location.Location;

public class Probe {
	private int id;
	private int connectivityMode;
	private double latitude;
	private double longitude;
	private Destination destination;
	private Date startDate;
	private Date endDate;

	private Vector<Router> routers;

	public Probe(Destination dest)
	{
		this.setDestination(dest);
		setRouters(new Vector<Router>());

		startDate = new Date(0);
	}

	public void endProbe()
	{
		endDate = new Date(0);
	}

	public void addRouter(Router r)
	{
		routers.add(r);
	}

	public Vector<Router> getRouters() {
		return routers;
	}

	public void setRouters(Vector<Router> routers) {
		this.routers = routers;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getConnectivityMode() {
		return connectivityMode;
	}

	public void setConnectivityMode(int connectivityMode) {
		this.connectivityMode = connectivityMode;
	}

	public void setLocation(Location loc)
	{
		setLatitude(loc.getLatitude());
		setLongitude(loc.getLongitude());
	}
	public String getLocationAsString()
	{
		return new String(latitude + "/" + longitude);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
}
