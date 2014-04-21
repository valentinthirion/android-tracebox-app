package be.ulg.ac.tracebox;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class ResultsActivity extends Activity {
	@SuppressWarnings("unused")
	private boolean logsMode;
	private Button middleBoxesButton;
	private Button logsBoxesButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);
	
		logsMode = false;

		middleBoxesButton = (Button) this.findViewById(R.id.view_middleboxes_mode_button);
		logsBoxesButton = (Button) this.findViewById(R.id.view_logs_mode_button);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
		return true;
	}

	/*
	 * Buttons
	 */
	public void view_middleboxes_mode(View view)
	{
		logsMode = false;
		middleBoxesButton.setBackgroundColor(Color.argb(255, 44, 62, 80)); // FLAT DARK
		logsBoxesButton.setBackgroundColor(Color.argb(255, 41, 128, 185)); // FLAT BLUE
	}

	public void view_logs_mode(View view)
	{
		logsMode = true;
		middleBoxesButton.setBackgroundColor(Color.argb(255, 41, 128, 185)); // FLAT BLUE
		logsBoxesButton.setBackgroundColor(Color.argb(255, 44, 62, 80)); // FLAT DARK
	}

}
