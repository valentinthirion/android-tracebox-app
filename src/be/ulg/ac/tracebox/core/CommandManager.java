package be.ulg.ac.tracebox.core;

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
			stdin.writeBytes(cmd + " \n");
			//System.out.println("Cmd: " + cmd);
			
			InputStream stdout = p.getInputStream();

			byte[] buffer = new byte[BUFFER_SIZE];
			int read;
			
			while(true)
			{
				Thread.sleep(5000); // WAIT FOR JOBS
				if (stdout.available() <= 0)
					break;

			    read = stdout.read(buffer);

			    if (read == -1)
			    	break;

			    String seg = new String(buffer, 0, read);
			    builder.append(seg);

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
