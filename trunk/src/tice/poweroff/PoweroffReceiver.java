package tice.poweroff;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.PowerManager;
import android.widget.Toast;

public class PoweroffReceiver extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	static PowerManager.WakeLock sWakeLock;
	
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
					timesCursor.close();	
					
					AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
					Shutdown st = new Shutdown(context, am);
					st.SetPoweroffSchedule(hour, minute);
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
	        if (sWakeLock != null) {
	            sWakeLock.release();
	        }

			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			sWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
	                PowerManager.ACQUIRE_CAUSES_WAKEUP |
	                PowerManager.ON_AFTER_RELEASE, "PowerToolService");
			sWakeLock.acquire();

			String text = String.format("The service is started");
			Toast.makeText(context, text.subSequence(0, text.length()), Toast.LENGTH_LONG).show();
	        Thread thr = new Thread(null, mTask, "PowerToolService");
	        thr.start();
		}
	}

    Runnable mTask = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 3*1000;
            while (System.currentTimeMillis() < endTime) {
            	try{
            		wait(endTime - System.currentTimeMillis());
            	}catch (Exception e) {;}
            }
            
	        if (sWakeLock != null) {
	        	sWakeLock.release();
	        	sWakeLock = null;
	        }
	        
            ExecUnixCommand("reboot -p");
        }
    };	
	
	private void ExecUnixCommand(String cmdstr) {
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
            while ((line = inputBr.readLine()) != null) {
            	stdstring = stdstring + line.trim();
            }
            while ((line = errBr.readLine()) != null){
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
