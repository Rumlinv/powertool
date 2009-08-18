package tice.poweroff;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class PoweroffReceiver extends BroadcastReceiver {

	static final String CUSTOM_ACTION_POWEROFF = "tice.poweroff.intent.action.CUSTOM_ACTION_POWEROFF"; 
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION)) {
//				context.startService(new Intent(context, AutoStartService.class));
				DBAdapter mDbHelper = new DBAdapter(context);
				mDbHelper.open();

				Cursor timesCursor = mDbHelper.fetchAllTimes();
				if (timesCursor.getCount() != 0) {
					timesCursor.moveToFirst();
					int hour = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_HOUR));
					int minute = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_MINUTE));
					int enable = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ENABLE));
					timesCursor.close();	

					if (enable != 0){	
						AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
						Shutdown st = new Shutdown(context, am);
						st.SetPoweroffSchedule(hour, minute);
					}
				}
				
				Cursor appsCursor = mDbHelper.fetchAllAppNames();
				if (appsCursor.getCount() != 0) {
					appsCursor.moveToFirst();
					do{
						String packagename = appsCursor.getString(appsCursor.getColumnIndexOrThrow(DBAdapter.KEY_PACKAGENAME));
						String name = appsCursor.getString(appsCursor.getColumnIndexOrThrow(DBAdapter.KEY_NAME));
						Intent newintent = new Intent(Intent.ACTION_MAIN);
						newintent.addCategory(Intent.CATEGORY_LAUNCHER);
						newintent.setComponent(new ComponentName( packagename, name));
						newintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
						context.startActivity(newintent);
				        
					}while (appsCursor.moveToNext());
				}
				appsCursor.close();
			}
		} else {
			Shutdown.Poweroff(context);
		}
	}
}
