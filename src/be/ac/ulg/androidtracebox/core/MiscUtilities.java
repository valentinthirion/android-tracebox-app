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

package be.ac.ulg.androidtracebox.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.telephony.TelephonyManager;

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

	public static String getCellularConnectionType(Context c)
	{
		if (!isConnectedWithCellular(c))
			return "";

		ConnectivityManager cm = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		switch (activeNetwork.getSubtype())
		{
		case TelephonyManager.NETWORK_TYPE_1xRTT:
            return "1xRTT"; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_CDMA:
            return "CDMA"; // ~ 14-64 kbps
        case TelephonyManager.NETWORK_TYPE_EDGE:
            return "EDGE"; // ~ 50-100 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            return "EVDO_0"; // ~ 400-1000 kbps
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            return "EVDO_A"; // ~ 600-1400 kbps
        case TelephonyManager.NETWORK_TYPE_GPRS:
            return "GPRS"; // ~ 100 kbps
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            return "HSDPA"; // ~ 2-14 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPA:
            return "HSPA"; // ~ 700-1700 kbps
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            return "HSUPA"; // ~ 1-23 Mbps
        case TelephonyManager.NETWORK_TYPE_UMTS:
            return "UMTS"; // ~ 400-7000 kbps

        case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11 
            return "EHRPD"; // ~ 1-2 Mbps
        case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
            return "EVDO_B"; // ~ 5 Mbps
        case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
            return "HSPAP"; // ~ 10-20 Mbps
        case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
            return "IDEN"; // ~25 kbps 
        case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
            return "LTE"; // ~ 10+ Mbps
        // Unknown
        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
        default:
            return "UNKNOWN";
		}
	}

	public static String getCellularCarrierName(Context c)
	{
		if (!isConnectedWithCellular(c))
			return "";

		TelephonyManager manager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
		String carrierName = manager.getNetworkOperatorName();
		return carrierName;
	}

	public static float getBatteryLevel(Context c)
	{
	    Intent batteryIntent = c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	    // Error checking that probably isn't needed but I added just in case.
	    if(level == -1 || scale == -1) {
	        return 50.0f;
	    }

	    return ((float)level / (float)scale) * 100.0f; 
	}
}
