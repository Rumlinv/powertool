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
	
	public Shutdown(Context c, AlarmManager am){
		mCtx = c;
		mArm = am;
	}
	
    public void SetPoweroffSchedule(int hour, int minute){
    	
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
        
        Intent intent = new Intent(mCtx, PowerToolService.class);
        PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent, 0);
        
        //AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, scheduledate.getTime() , 6000 , sender);
        mArm.cancel(sender);
        mArm.set(AlarmManager.RTC_WAKEUP, scheduledate.getTime() , sender);
        
    	String text = String.format("The service will be started in %.1f minutes", (scheduledate.getTime() - now.getTime()) / 60000.0);
		Toast.makeText(mCtx, text.subSequence(0, text.length()), Toast.LENGTH_LONG).show();
    }  
}
