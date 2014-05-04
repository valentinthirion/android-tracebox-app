package be.ulg.ac.tracebox.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MiscUtilities {

	public static boolean isConnected(Context c)
	{
		ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
		return isConnected;
	}

	public static boolean isConnectedWithWifi(Context c)
	{
		return getConnectivityType(c) == ConnectivityManager.TYPE_WIFI;
	}

	public static boolean isConnectedWithCellular(Context c)
	{
		return getConnectivityType(c) == ConnectivityManager.TYPE_MOBILE;
	}

	public static int getConnectivityType(Context c)
	{
		ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork.getType();
	}
}
