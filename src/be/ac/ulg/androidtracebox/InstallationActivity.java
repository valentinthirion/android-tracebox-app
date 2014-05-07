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

package be.ac.ulg.androidtracebox;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.apache.http.util.ByteArrayBuffer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import be.ac.ulg.androidtracebox.core.CommandManager;
import be.ac.ulg.androidtracebox.data.*;
import be.ac.ulg.androidtracebox.R;

public class InstallationActivity extends Activity {

	boolean deviceIsRooted = false;
	boolean busyboxIsInstalled = false;
	boolean installed = false;
	private Button rootButton;
	private Button busyboxButton;
	private Button endInstallationButton;
	private TextView display;
	private SharedPreferences sharedpreferences;
	private Editor editor;
	boolean downloading = false;
	private ProgressDialog progressDialog;
	private DatabaseHandler db;

	public Vector<Destination> destinations = new Vector<Destination>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);    
        setContentView(R.layout.installation_activity);

        setTitle("Tracebox for Android - Installation");

        db = new DatabaseHandler(this);

        // SET PREFERENCES STUFF
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedpreferences.edit();

        // GET VALUES
        deviceIsRooted = sharedpreferences.getBoolean("systemInstalled", false);
        busyboxIsInstalled = sharedpreferences.getBoolean("probingEnabled", false);
        if (deviceIsRooted && busyboxIsInstalled)
        {
        	installed = true;
        	editor.putBoolean("systemInstalled", true);
			editor.commit();
        }

        // Find elements
        rootButton = (Button) findViewById(R.id.root_button);
        busyboxButton = (Button) findViewById(R.id.busybox_button);
        endInstallationButton = (Button) findViewById(R.id.endInstallation);
        display = (TextView) findViewById(R.id.status_text);

        busyboxButton.setEnabled(false);
        endInstallationButton.setEnabled(false);

        this.showText("Installation waiting ...");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// Checks if the device is rooted and show a message to the user
	public void checkIfRooted(View view)
	{
		if (CommandManager.isDeviceRooted())
		{
			new AlertDialog.Builder(this)
		    .setTitle("Great")
		    .setMessage("Your device is rooted, You now need to install the special busybox.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();

			deviceIsRooted = true;
			rootButton.setText("YES");
			rootButton.setEnabled(false);
			rootButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			busyboxButton.setEnabled(true);
			busyboxButton.setBackgroundColor(Color.argb(255, 230, 126, 34)); // FLAT ORANGE

			// Set settings
			editor.putBoolean("rootedDevice", true);
			editor.commit();

			this.showText("Device is rooted");

			// Save log
			db.addLog(new Log("Checked if device was rooted, result OK"));
		}
		else
		{
			new AlertDialog.Builder(this)
		    .setTitle("Error")
		    .setMessage("Your device is not rooted, tracebox could only be used on rooted device. Go root it and then retry.")
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        }
		     })
		     .show();

			rootButton.setText("NO");
			rootButton.setEnabled(false);
			rootButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED
			busyboxButton.setBackgroundColor(Color.argb(255, 231, 76, 60)); // FLAT RED

			editor.putBoolean("rootedDevice", false);
			editor.commit();

			this.showText("Device is not rooted rooted");

			// Save log
			db.addLog(new Log("Checked if device was rooted, result NOK"));
		}
	}

	// Install busybox
	public void installBusybox(View view)
	{		
		if (!deviceIsRooted)
			return;

		// Waiting box
		progressDialog = ProgressDialog.show(
                this, "Please wait",
                "The busybox containing tracebox is being dowloaded and installed...\nThis could be long.", true);
        progressDialog.setCancelable(false);
		
		// FIND BUSYBOX AND INSTALL IT
		downloading = true;
		busyboxButton.setEnabled(false);

		BusyboxGetter getter = (BusyboxGetter) new BusyboxGetter();
		getter.execute();
	}

	private void endBusyboxInstallation(boolean ok)
	{
		if (ok)
		{
			editor.putBoolean("busyboxInstalled", true);
			busyboxIsInstalled = true;

			this.showText("Busybox is installed.");

			// Save log
			db.addLog(new Log("Installed Busybox"));
			
			getDestinations();
			return;
		}

		this.showText("Error while downloading busybox, try again.");
		return;
	}

	// Get destination from XML
	private void getDestinations()
	{
		downloading = true;

		// GET DESTINATIONS
		this.showText("Getting destinations...");
		DestinationsGetter getter = (DestinationsGetter) new DestinationsGetter();
		getter.execute();
		this.showText("Please wait, this process could be long...\n..\n..");
	}

	// Show status in the display
	public void showText(String text)
	{
		display.append("\n" + text);
	}

	// Save destinations to SQLite
	private boolean saveDestinations()
	{
		progressDialog.cancel();

		if (destinations.size() > 0)
		{
			// Save the destinations id DB
			db.deleteNonCustomDestinations();
					
			for (Destination d: destinations)
				db.addDestination(d);

			downloading = false;
			this.showText("Destinations got!");

			this.showText("Installation completed.");
			editor.putBoolean("destinationsGot", true);
			editor.commit();

			busyboxButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			endInstallationButton.setBackgroundColor(Color.argb(255, 46, 204, 113)); // FLAT GREEN
			endInstallationButton.setEnabled(true);

			// Save log
			db.addLog(new Log("Destinations got"));
			return false;
		}

		this.showText("Error while getting destinations, try again.");

		return true;
	}

	// Finish the installation and go to the main menu
	public void endInstallation(View view)
	{
		if (!this.deviceIsRooted || !this.busyboxIsInstalled)
		{
			System.out.println("GET OUT WARRIORS!");
		}
		else
		{
			// Save log
			db.addLog(new Log("Installation correctly ended"));

			String lastBusyboxVersion = getResources().getString(R.string.busybox_version);
			int lastBusyboxVersionInt;
			try {
				lastBusyboxVersionInt = Integer.parseInt(lastBusyboxVersion);
				editor.putInt("installedBusyboxVersion", lastBusyboxVersionInt);
			}
			catch (Exception e)
			{
				editor.putInt("installedBusyboxVersion", 0);
			}
			
			System.out.println("GO GO WARRIORS!");
			editor.putBoolean("systemInstalled", true);
			editor.commit();
			this.finish();
		}
	}

	// Busybox getter
	@SuppressLint("SdCardPath")
	private class BusyboxGetter extends AsyncTask<URL, Integer, Long>
	{
		private static final int TIMEOUT_CONNECTION = 5000;
		private static final int TIMEOUT_SOCKET = 5000;
		private boolean installationOK = false;
		@Override
		protected Long doInBackground(URL... params)
		{
			String address = getResources().getString(R.string.busybox_url);
			String fileName = "busybox";
			String internalDestination = "/data/data/be.ac.ulg.androidtracebox/";

			try {
				URL url = new URL(address);

		        //File root = android.os.Environment.getExternalStorageDirectory();
		        File dir = new File(internalDestination);
		        if (dir.exists() == false)
		             dir.mkdirs(); 

		        File file = new File(dir, fileName);

		        long startTime = System.currentTimeMillis();
		        System.out.println("DownloadManager : download url:" + url);
		        System.out.println("DownloadManager  download file name:" + fileName);

		        URLConnection uconn = url.openConnection();
		        uconn.setReadTimeout(TIMEOUT_CONNECTION);
		        uconn.setConnectTimeout(TIMEOUT_SOCKET);

		        InputStream is = uconn.getInputStream();
		        BufferedInputStream bufferinstream = new BufferedInputStream(is);

		        ByteArrayBuffer baf = new ByteArrayBuffer(5000);
		        int current = 0;
		        while ((current = bufferinstream.read()) != -1)
		        {
		            baf.append((byte) current);
		        }

		        FileOutputStream fos = new FileOutputStream(file);
		        fos.write(baf.toByteArray());
		        fos.flush();
		        fos.close();
		        System.out.println("DownloadManager: download ready in" + ((System.currentTimeMillis() - startTime)/1000) + "sec");
		        int dotindex = fileName.lastIndexOf('.');
		        if (dotindex >= 0)
		            fileName = fileName.substring(0, dotindex);

		        System.out.println("Busybox downloaded");

		        try {
		        	System.out.println("INSTALLATION BEGINS");
		        	System.out.println(CommandManager.executeCommandAsRoot("mount -o rw,remount -t yaffs2 /dev/block/mtdblock4 /system"));
		        	System.out.println(CommandManager.executeCommandAsRoot("dd if=/data/data/be.ulg.ac.tracebox/busybox of=/system/xbin/busybox"));
		        	System.out.println(CommandManager.executeCommandAsRoot("chmod 4755 /system/xbin/busybox"));
		        	System.out.println(CommandManager.executeCommandAsRoot("mount -o ro,remount -t yaffs2 /dev/block/mtdblock4 /system"));
		        	System.out.println("INSTALLATION DONE");
					
					installationOK = true;
				} catch (Exception e) {
					System.out.println("Error while installing busybox");
					e.printStackTrace();
				}
		        
		    }
		    catch (IOException e) {
		    	System.out.println("DownloadManager: Error:" + e);
		    }

			return null;
		}

		@Override
		protected void onPostExecute(Long result)
		{
			endBusyboxInstallation(installationOK);
		}
	}
	// Destinations getter
	private class DestinationsGetter extends AsyncTask<URL, Integer, Long>
	{
		Vector<Destination> getterDestinations = new Vector<Destination>();
			/*
			 * 	This function is used to get the destinations from the BO
			 */
		@Override
		protected Long doInBackground(URL... params) 
		{
			this.parseDestinations();
			return null;
		}

		private void parseDestinations()
		{
			String url = getResources().getString(R.string.api_prefix) + "getDestinations.php";
			XmlPullParserFactory xmlFactoryObject = null;
			XmlPullParser parser = null;

			// Create parser
			try {
				xmlFactoryObject = XmlPullParserFactory.newInstance();
				parser = xmlFactoryObject.newPullParser();

			} catch (XmlPullParserException e) {
				e.printStackTrace();
			}
				
			// Set stream to parser
			try {
				InputStream in_s = new URL(url).openStream();
			    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		        parser.setInput(in_s, null);

		     // Parse
				try
				{
					System.out.println("Trying to parse: " + url);
					int event = parser.getEventType();
			        while (event != XmlPullParser.END_DOCUMENT) 
			        {
			        	String name = parser.getName();
			        	switch (event)
			        	{
			        		case XmlPullParser.START_TAG:
			        			if(name.equals("destination"))
			        			{
			        				Destination currentDestination = new Destination(parser.getAttributeValue(null,"name"), parser.getAttributeValue(null,"address"));
			        				getterDestinations.add(currentDestination);
			        			}
			        			break;
			        		case XmlPullParser.END_TAG:
			        			break;
			        	} 
			        	event = parser.next(); 					
			        }
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			destinations = getterDestinations;
		}
		@Override
		protected void onPostExecute(Long result)
		{
			saveDestinations();
		}
	}
}
