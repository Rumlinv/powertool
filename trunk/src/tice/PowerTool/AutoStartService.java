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
        if (timesCursor.getCount() != 0){
        	timesCursor.moveToFirst();
        	int hour = timesCursor.getInt( timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_HOUR));
        	int minute = timesCursor.getInt( timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_MINUTE));
        	
            AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
            Shutdown st = new Shutdown(this,am);
        	st.SetPoweroffSchedule(hour,minute);
        }        
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
/*
    private void SetPoweroffSchedule(int hour, int minute){
    	
        Date now = new Date();
        Date scheduledate = new Date();
   
        scheduledate.setHours(hour);
        scheduledate.setMinutes(minute);

        if( scheduledate.getHours() - now.getHours() < 0 ){
        	scheduledate.setDate(now.getDate() + 1);
        } else if ( scheduledate.getHours() - now.getHours() == 0){
        	if( scheduledate.getMinutes() - now.getMinutes() <= 0 ){
        		scheduledate.setDate(now.getDate() + 1);
        	}
        } 	
        
        Intent intent = new Intent(this, PowerToolService.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, scheduledate.getTime() , 6000 , sender);
        am.cancel(sender);
        am.set(AlarmManager.RTC_WAKEUP, scheduledate.getTime() , sender);
        
    	String text = String.format("The service will be started in %.1f minutes", (scheduledate.getTime() - now.getTime()) / 60000.0);
		Toast.makeText(this, text.subSequence(0, text.length()), Toast.LENGTH_LONG).show();
    }
*/     
}
