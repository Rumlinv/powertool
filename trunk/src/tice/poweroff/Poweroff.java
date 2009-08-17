package tice.poweroff;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class Poweroff extends Activity {

	private TextView mTimeDisplay;
	private DBAdapter mDbHelper;
	private int mRowId;
	private ApplicationInfo mApplist;

    private static final int APPLIST_ID = Menu.FIRST;
	
    /* called by system on minute ticks */
    private final Handler mHandler = new Handler();
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTime();
            }
        };
    
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
				//updateDisplay(hourOfDay, minute);
			}
		});

		Button btnOK = (Button) findViewById(R.id.btnOK);
		btnOK.setOnClickListener(mSetSchedule);
		
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(mUnSchedule);
		
		ExecUnixCommand("su");
		
		updateTime();
	}

	private OnClickListener mSetSchedule = new OnClickListener() {
		public void onClick(View v) {
		
			TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);

			int hour = timePicker.getCurrentHour();
			int minute = timePicker.getCurrentMinute();

			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			Shutdown st = new Shutdown(Poweroff.this, am);
			st.SetPoweroffSchedule(hour, minute);

			if (mRowId == -1) {
				mDbHelper.createTime(hour, minute);
			} else {
				mDbHelper.updateTime(mRowId, hour, minute);
			}
		}
	};
	
	private OnClickListener mUnSchedule = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(Poweroff.this, PoweroffReceiver.class);
			PendingIntent sender = PendingIntent.getBroadcast(Poweroff.this, 0, intent, 0);
			
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.cancel(sender);

			Toast.makeText(Poweroff.this, "The Poweroff is unscheduled", Toast.LENGTH_LONG).show();
		}
	};
	
	private void updateTime(){
		Date now = new Date();
		int hourOfDay = now.getHours();
		int minute = now.getMinutes();
		
		mTimeDisplay.setTextSize((float) 68);
		mTimeDisplay.setText(new StringBuilder().append(pad(hourOfDay)).append(":").append(pad(minute)));
	}

	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
	
	private void ExecUnixCommand(String cmdstr) {
		Process process = null;
		InputStream stderr = null;
		InputStream stdout = null;
		DataOutputStream os = null;
		//String line, stdstring="", errstring="";
		try {
			process = Runtime.getRuntime().exec("su");
	        stderr = process.getErrorStream();
	        stdout = process.getInputStream();
            //BufferedReader errBr = new BufferedReader(new InputStreamReader(stderr), 8192);
            //BufferedReader inputBr = new BufferedReader(new InputStreamReader(stdout), 8192);
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmdstr + "\n");
			os.writeBytes("exit\n");
			os.flush();
            /*
			while ((line = inputBr.readLine()) != null) {
            	stdstring = stdstring + line.trim();
            }
            while ((line = errBr.readLine()) != null){
            	errstring = errstring + line.trim();;
            }
            
			process.waitFor();
			*/
		} catch (IOException e) {
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
            return new AlertDialog.Builder(Poweroff.this)
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

	@Override
	protected void onPause() {
		super.onPause();
		
		unregisterReceiver(mIntentReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter, null, mHandler);
	}
}