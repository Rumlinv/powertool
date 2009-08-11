package tice.PowerTool;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;

public class AutoStartService extends Service {

	public class LocalBinder extends Binder {
		AutoStartService getService() {
			return AutoStartService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onCreate() {
		super.onCreate();

		DBAdapter mDbHelper = new DBAdapter(this);
		mDbHelper.open();

		Cursor timesCursor = mDbHelper.fetchAllTimes();
		if (timesCursor.getCount() != 0) {
			timesCursor.moveToFirst();
			int hour = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_HOUR));
			int minute = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_MINUTE));

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			Shutdown st = new Shutdown(this, am);
			st.SetPoweroffSchedule(hour, minute);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
