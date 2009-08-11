package tice.PowerTool;

import android.app.Activity;
import android.app.AlarmManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class PowerTool extends Activity {

	private TextView mTimeDisplay;
	private DBAdapter mDbHelper;
	private int mRowId;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mDbHelper = new DBAdapter(this);
		mDbHelper.open();
		mRowId = -1;

		int hour = 02;
		int minute = 00;

		Cursor timesCursor = mDbHelper.fetchAllTimes();
		// startManagingCursor(timesCursor);
		if (timesCursor.getCount() != 0) {
			timesCursor.moveToFirst();
			mRowId = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWID));
			hour = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_HOUR));
			minute = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_MINUTE));

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			Shutdown st = new Shutdown(this, am);
			st.SetPoweroffSchedule(hour, minute);
		}

		mTimeDisplay = (TextView) findViewById(R.id.TextView);

		TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);

		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				updateDisplay(hourOfDay, minute);
			}
		});

		Button btnOK = (Button) findViewById(R.id.btnOK);
		btnOK.setOnClickListener(mSetSchedule);
	}

	private OnClickListener mSetSchedule = new OnClickListener() {
		public void onClick(View v) {
			TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);

			int hour = timePicker.getCurrentHour();
			int minute = timePicker.getCurrentMinute();

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			Shutdown st = new Shutdown(PowerTool.this, am);
			st.SetPoweroffSchedule(hour, minute);

			if (mRowId == -1) {
				mDbHelper.createTime(hour, minute);
			} else {
				mDbHelper.updateTime(mRowId, hour, minute);
			}
		}
	};

	private void updateDisplay(int hourOfDay, int minute) {
		mTimeDisplay.setText(new StringBuilder().append(pad(hourOfDay)).append(":").append(pad(minute)));
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
}