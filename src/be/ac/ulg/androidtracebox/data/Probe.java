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

import java.util.Date;
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
	private String carrierName;
	private String cellularCarrierType;
	private float batteryDifference;
	private String strinRepresentation;

	private Vector<Router> routers;

	public Probe(Destination dest)
	{
		this.setDestination(dest);
		setRouters(new Vector<Router>());

		startDate = new Date();
		System.out.println(startDate.toString());
	}

	public void endProbe()
	{
		endDate = new Date();
		System.out.println(endDate.toString());
	}

	public String toString()
	{
		StringBuilder text = new StringBuilder();
		// Start
		text.append("traceboxing to " + this.getDestination().getAddress() + "\n");

		// Routers
		for (Router r:this.getRouters())
		{
			text.append(r.toString() + "\n");
		}

		return text.toString();
		
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

	public String getCarrierName() {
		return carrierName;
	}

	public void setCarrierName(String carrierName) {
		this.carrierName = carrierName;
	}

	public String getCellularCarrierType() {
		return cellularCarrierType;
	}

	public void setCellularCarrierType(String cellularCarrierType) {
		this.cellularCarrierType = cellularCarrierType;
	}

	public float getBatteryDifference() {
		return batteryDifference;
	}

	public void setBatteryDifference(float batteryDifference) {
		this.batteryDifference = batteryDifference;
	}

	public String getStrinRepresentation() {
		return strinRepresentation;
	}

	public void setStrinRepresentation(String strinRepresentation) {
		this.strinRepresentation = strinRepresentation;
	}
}
