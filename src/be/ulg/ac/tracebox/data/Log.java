package be.ulg.ac.tracebox.data;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class Log {
	private String message;
	private Date date;

	public Log(String m)
	{
		setMessage(m);
		setDate(new Date());
	}

	public Log(String m, String d)
	{
		setMessage(m);
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy h:i"); 
	    try {
	        date = df.parse(d);
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
