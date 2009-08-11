package tice.PowerTool;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class Shutdown {

	private Context mCtx;
	private AlarmManager mArm;

	public Shutdown(Context c, AlarmManager am) {
		mCtx = c;
		mArm = am;
	}

	public void SetPoweroffSchedule(int hour, int minute) {

		Date now = new Date();
		Date scheduledate = new Date();

		scheduledate.setHours(hour);
		scheduledate.setMinutes(minute);
		scheduledate.setSeconds(0);

		if (scheduledate.getHours() - now.getHours() < 0) {
			scheduledate.setDate(now.getDate() + 1);
		} else if (scheduledate.getHours() - now.getHours() == 0) {
			if (scheduledate.getMinutes() - now.getMinutes() <= 0) {
				scheduledate.setDate(now.getDate() + 1);
			}
		}

		Intent intent = new Intent(mCtx, PowerToolService.class);
		PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent, 0);

		mArm.cancel(sender);
		mArm.set(AlarmManager.RTC_WAKEUP, scheduledate.getTime(), sender);
		//mArm.setRepeating(AlarmManager.RTC_WAKEUP, scheduledate.getTime() ,  6000 , sender);
		
		String text;
		double time = (scheduledate.getTime() - now.getTime()) / 1000.0;
		if( time < 60){
			text = String.format("PowerOff will start in %.1f seconds", time);
		} else if ( time < 3600 ){
			text = String.format("PowerOff will start in %.1f minutes", time / 60);
		} else {
			text = String.format("PowerOff will start in %.1f hours", time / 3600);
		}
		
		Toast.makeText(mCtx, text.subSequence(0, text.length()),Toast.LENGTH_LONG).show();
	}
}
