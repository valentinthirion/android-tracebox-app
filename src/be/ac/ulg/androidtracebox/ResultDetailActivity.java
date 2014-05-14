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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ResultDetailActivity extends Activity {
	private String destinationString;
	private String probeString;
	private TextView display;
	private TextView pageTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result_detail);

		setTitle("Tracebox for Android - Probe detail");

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			destinationString = extras.getString("destinationString");
			probeString = extras.getString("probeString");
		}

		pageTitle = (TextView) this.findViewById(R.id.results_detail_page_label);
		pageTitle.setText("Detail for probe to " + destinationString);
		
		display = (TextView) this.findViewById(R.id.results_detail_display);
		display.setText(probeString);
	}

	public void exportText(View view)
	{
		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, probeString);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.results_detail_page_export_title)));
	}
}
