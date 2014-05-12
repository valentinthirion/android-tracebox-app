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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommandManager {

	public static boolean isDeviceRooted()
	{
		Process process = null;            
	    try {
	        process = Runtime.getRuntime().exec("su");
	        return true;
	    } catch (Exception e) {
	        return false;
	    } finally {
	        if (process != null) {
	            try {
	                process.destroy();
	            } catch (Exception e) {
	            }
	        }
	    }
	}

	public static String executeCommandAsRoot(String cmd) throws Exception
	{
		int BUFFER_SIZE = 32768;

		StringBuilder builder = new StringBuilder();
		try {
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream stdin = new DataOutputStream(p.getOutputStream());

			// Execute the command
			System.out.println("Cmd: " + cmd);
			stdin.writeBytes(cmd + " \n");

			InputStream stdout = p.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			int read;
			
			while (true)
			{
				System.out.println("wait");
				Thread.sleep(5000); // WAIT

				if (stdout.available() <= 0)
					break;

			    read = stdout.read(buffer);

			    if (read == -1)
			    	break;

			    String seg = new String(buffer, 0, read);
			    builder.append(seg);
			    System.out.println("ret : " + seg);

			    if(read < BUFFER_SIZE)
			    	continue;
			}

			stdin.writeBytes("exit\n");
			stdin.flush();

			return builder.toString();

		} catch (IOException e) {
	
		    throw new RuntimeException(e);
		}
    }
}
