package be.ac.ulg.androidtracebox;

import java.util.Vector;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import be.ac.ulg.androidtracebox.data.DatabaseHandler;
import be.ac.ulg.androidtracebox.data.Log;
import be.ac.ulg.androidtracebox.R;

public class ResultsActivity extends Activity {
	private boolean logsMode;
	private Button middleBoxesButton;
	private Button logsBoxesButton;
	private TableLayout resultTable;
	private DatabaseHandler db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_results);

		setTitle("Tracebox for Android - Results");
	
		logsMode = false;

		middleBoxesButton = (Button) this.findViewById(R.id.view_middleboxes_mode_button);
		logsBoxesButton = (Button) this.findViewById(R.id.view_logs_mode_button);
		resultTable = (TableLayout) findViewById(R.id.results_table);

		db = new DatabaseHandler(this);

		this.fillTable();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.results, menu);
		return true;
	}

	public void fillTable()
	{
		resultTable.removeAllViews();

		if (logsMode)
		{
			Vector<Log> logs = db.getAllLogs();
			for (Log l:logs)
			{
				TableRow tableRow = getRow();

				TextView dateText = new TextView(this);
				dateText.setText(l.getDate().toString() + "\n  " + l.getMessage());
		        tableRow.addView(dateText);


		        resultTable.addView(tableRow);
			}
		}
		else
		{
			Vector<String> probes = db.getAllProbesInString();
			for (String s:probes)
			{
				TableRow tableRow = getRow();

				TextView probeText = new TextView(this);
				probeText.setText(s);
		        tableRow.addView(probeText);

		        resultTable.addView(tableRow);
			}
		}
	}

	private TableRow getRow()
	{
		TableRow tableRow = new TableRow(this);

		TableLayout.LayoutParams tableRowParams
		= new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT,TableLayout.LayoutParams.WRAP_CONTENT);

		// Margin
		int leftMargin=10;
		int topMargin=5;
		int rightMargin=10;
		int bottomMargin=5;
		tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);
		tableRow.setLayoutParams(tableRowParams);

		// Padding
		tableRow.setPadding(5, 5, 5, 5);

		// Color
		tableRow.setBackgroundColor(Color.WHITE);

		return tableRow;
	}

	/*
	 * Buttons
	 */
	public void view_middleboxes_mode(View view)
	{
		logsMode = false;
		middleBoxesButton.setBackgroundColor(Color.argb(255, 44, 62, 80)); // FLAT DARK
		logsBoxesButton.setBackgroundColor(Color.argb(255, 41, 128, 185)); // FLAT BLUE
		this.fillTable();
	}

	public void view_logs_mode(View view)
	{
		logsMode = true;
		middleBoxesButton.setBackgroundColor(Color.argb(255, 41, 128, 185)); // FLAT BLUE
		logsBoxesButton.setBackgroundColor(Color.argb(255, 44, 62, 80)); // FLAT DARK
		this.fillTable();
	}

}
