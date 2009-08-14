package tice.PowerTool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

public class PowerTool extends Activity {

	private TextView mTimeDisplay;
	private DBAdapter mDbHelper;
	private int mRowId;
	private ApplicationInfo mApplist;

    private static final int APPLIST_ID = Menu.FIRST;
	
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
			timesCursor.close();
			
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
/*	
	        Thread thr = new Thread(null, mTask, "PowerToolService");
	        thr.start();
*/
		
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

	
    Runnable mTask = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 15*1000;
            while (System.currentTimeMillis() < endTime) {
            	try{
            		wait(endTime - System.currentTimeMillis());
            	}catch (Exception e) {;}
            }
            ExecUnixCommand("/system/bin/toolbox reboot -p");
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
			os.writeBytes("exit\n");
			os.flush();
            while ((line = inputBr.readLine()) != null) {
            	stdstring = stdstring + line.trim();
            }
            while ((line = errBr.readLine()) != null){
            	errstring = errstring + line.trim();;
            }
			process.waitFor();
		} catch (IOException e) {
			;
		} catch (InterruptedException e) {
			;
		} 
            try {
                if (os != null) os.close();
                if (stderr != null) stderr.close();
                if (stdout != null) stdout.close();
            } catch (Exception ex) {;}

            try {
            	process.destroy();
            } catch (Exception e) {;}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, APPLIST_ID, 0, R.string.app_list);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case APPLIST_ID:
        	showDialog(APPLIST_ID);
            return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }	

    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case APPLIST_ID:
        	GetApplications();
            return new AlertDialog.Builder(PowerTool.this)
            //.setIcon(R.drawable.ic_popup_reminder)
            .setTitle(R.string.app_list)
            .setMultiChoiceItems(mApplist.mTitlelist,
            		mApplist.mChecklist,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                        	mApplist.mChecklist[whichButton] = isChecked;
                        }
                    })
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                	SaveApplist();
    				Cursor appsCursor = mDbHelper.fetchAllAppNames();
    				if (appsCursor.getCount() != 0) {
    					appsCursor.moveToFirst();
    					do{
    						String packagename = appsCursor.getString(appsCursor.getColumnIndexOrThrow(DBAdapter.KEY_PACKAGENAME));
    						String name = appsCursor.getString(appsCursor.getColumnIndexOrThrow(DBAdapter.KEY_NAME));

    						Intent intent = new Intent(Intent.ACTION_MAIN);
    				        intent.addCategory(Intent.CATEGORY_LAUNCHER);
    				        intent.setComponent(new ComponentName( packagename, name));
    				        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    						
    				        startActivity(intent);
    				        
    					}while (appsCursor.moveToNext());
    				}
    				appsCursor.close();
                }
            })
            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    /* User clicked No so do some stuff */
                }
            })
           .create(); 
        }
        return null;
    }
    
	private String[] GetApplications(){
    	
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

    	
        if (apps != null) {
            final int count = apps.size();
            
            mApplist = new ApplicationInfo(count);

            for (int i = 0; i < count; i++) {
                ResolveInfo info = apps.get(i);
                mApplist.mTitlelist[i] = (String)info.loadLabel(manager);
                mApplist.mPackagelist[i] = info.activityInfo.applicationInfo.packageName;
                mApplist.mNamelist[i] = info.activityInfo.name;
                mApplist.mChecklist[i] = mDbHelper.findAppName(info.activityInfo.name);                           
            }
        }       
    	
		return mApplist.mTitlelist;
    }
	
	private void SaveApplist(){

		int count = mApplist.mTitlelist.length;
		
		if (count > 0){
			mDbHelper.DeleteAppNames();
		}
			
        for (int i = 0; i < count; i++) {
        	if (mApplist.mChecklist[i] == true){
        		mDbHelper.createAppName(mApplist.mTitlelist[i], mApplist.mPackagelist[i],mApplist.mNamelist[i]);
        	}
        }
	}
}