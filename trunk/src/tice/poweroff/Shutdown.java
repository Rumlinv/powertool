package tice.poweroff;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

		Intent intent = new Intent(mCtx, PoweroffReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent, 0);

		mArm.cancel(sender);
		mArm.set(AlarmManager.RTC_WAKEUP, scheduledate.getTime(), sender);
		//mArm.setRepeating(AlarmManager.RTC_WAKEUP, scheduledate.getTime() ,  6000 , sender);
		
		String text;
		double time = (scheduledate.getTime() - now.getTime()) / 1000.0;
		if( time < 60){
			text = String.format("Power Off will start in %.1f seconds", time);
		} else if ( time < 3600 ){
			text = String.format("Power Off will start in %.1f minutes", time / 60);
		} else {
			text = String.format("Power Off will start in %.1f hours", time / 3600);
		}
		
		Toast.makeText(mCtx, text.subSequence(0, text.length()),Toast.LENGTH_LONG).show();
	}
	
	static public void ExecUnixCommand(String cmdstr) {
		Process process = null;
		InputStream stderr = null;
		InputStream stdout = null;
		DataOutputStream os = null;
		String line, stdstring="", errstring="";
		try {
			process = Runtime.getRuntime().exec("su");
	        stderr = process.getErrorStream();
	        stdout = process.getInputStream();
            BufferedReader errBr = new BufferedReader(new InputStreamReader(stderr), 8192);
            BufferedReader inputBr = new BufferedReader(new InputStreamReader(stdout), 8192);
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmdstr + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
            while (inputBr.ready() && (line = inputBr.readLine()) != null) {
            	stdstring = stdstring + line.trim();
            }
            while (errBr.ready() && (line = errBr.readLine()) != null){
            	errstring = errstring + line.trim();;
            }
			//process.waitFor();
		} catch (IOException e) {;} 
            try {
                if (os != null) os.close();
                if (stderr != null) stderr.close();
                if (stdout != null) stdout.close();
            } catch (Exception ex) {;}

            try {
            	process.destroy();
            } catch (Exception e) {;}
	}
}
