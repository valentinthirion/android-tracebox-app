package be.ulg.ac.tracebox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import be.ulg.ac.tracebox.data.PacketModification;
import be.ulg.ac.tracebox.data.Probe;
import be.ulg.ac.tracebox.data.Router;

public class APIPoster {
	private Vector<Probe> probes;
	private String postData;
	private String postURL = "http://www.medineo.be/be.ac.ulg.androidtracebox/api/putProbes.php";

	public APIPoster()
	{
		probes = new Vector<Probe>();
	}

	public void addProbe(Probe p)
	{
		probes.add(p);
	}

	public boolean saveProbesAsXMLFile(String name)
	{
		// Get the XML representation
		String postData = getPostXML();

		// Write the XML in a txt file
		try {
			  
			File file = new File("/data/data/be.ulg.ac.tracebox/" + name);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(postData);
			bw.close();
 
			System.out.println("Written	");
 
		} catch (IOException e) {
			e.printStackTrace();
		}

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
		builder.append("\n");

		postData = builder.toString();
		
		return postData;
	}


	public boolean tryToPostData()
	{
		// Get the XML representation
		String postData = getPostXML();
		System.out.println(postData);

		HttpURLConnection connection;
	    OutputStreamWriter request = null;

	    URL url = null;   
	    String response = null;         
	    String parameters = "probeData=" + this.postData;

	    try
        {
            url = new URL(postURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();            
            String line = "";

            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

            response = sb.toString();
            System.out.println("Message from Server: " + response);             
            isr.close();
            reader.close();

            return response.contains("1");

        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
	}
}
