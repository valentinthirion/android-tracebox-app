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

	public Log(String m, long d)
	{
		setMessage(m);
		setDate(new Date(d));
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
