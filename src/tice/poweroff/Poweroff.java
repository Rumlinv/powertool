package tice.poweroff;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class Poweroff extends Activity {

	//private AnalogClock mTimeDisplay;
	private DBAdapter mDbHelper;
	private int mRowId;
	private ApplicationInfo mApplist;

    private static final int APPLIST_ID = Menu.FIRST;
    private static final int ABOUT_ID = Menu.FIRST + 1;
	
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
		int enable = 0;

		Cursor timesCursor = mDbHelper.fetchAllTimes();
		// startManagingCursor(timesCursor);
		if (timesCursor.getCount() != 0) {
			timesCursor.moveToFirst();
			mRowId = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWID));
			hour = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_HOUR));
			minute = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_MINUTE));
			enable = timesCursor.getInt(timesCursor.getColumnIndexOrThrow(DBAdapter.KEY_ENABLE));
			timesCursor.close();
			
			if (enable != 0){
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				Shutdown st = new Shutdown(this, am);
				st.SetPoweroffSchedule(hour, minute);
			}
		}

		//mTimeDisplay = (AnalogClock) findViewById(R.id.AnalogClock);

		TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);


		timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				CheckBox chkPoweroff = (CheckBox) findViewById(R.id.CheckBox);
				chkPoweroff.setChecked(false);
			}
		});
		
/*
		Button btnOK = (Button) findViewById(R.id.btnOK);
		btnOK.setOnClickListener(mSetSchedule);
		
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(mUnSchedule);
*/
		
		CheckBox chkPoweroff = (CheckBox) findViewById(R.id.CheckBox);
		chkPoweroff.setChecked(enable == 0? false : true);
		chkPoweroff.setOnCheckedChangeListener(mCheckPoweroff);
		
		Shutdown.Getsu();
	}

	private OnCheckedChangeListener mCheckPoweroff = new OnCheckedChangeListener(){
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

			TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);
			int hour = timePicker.getCurrentHour();
			int minute = timePicker.getCurrentMinute();
			int enable = 0;

			if (isChecked == true){
				enable = 1;
				
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				Shutdown st = new Shutdown(Poweroff.this, am);
				st.SetPoweroffSchedule(hour, minute);

			}else{
				enable = 0;
				
				Intent intent = new Intent(Poweroff.this, PoweroffReceiver.class);
				PendingIntent sender = PendingIntent.getBroadcast(Poweroff.this, 0, intent, 0);
				
				AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
				am.cancel(sender);

				Toast.makeText(Poweroff.this, "Power Off is unscheduled", Toast.LENGTH_LONG).show();
			}
			
			if (mRowId == -1) {
				mDbHelper.createTime(hour, minute,enable);
			} else {
				mDbHelper.updateTime(mRowId, hour, minute,enable);
			}
		}
	};
	
/*
	
	private void updateTime(){
		Date now = new Date();
		int hourOfDay = now.getHours();
		int minute = now.getMinutes();
		
		//mTimeDisplay.setTextSize((float) 68);
		//mTimeDisplay.setText(new StringBuilder().append(pad(hourOfDay)).append(":").append(pad(minute)));
	}
	
	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
*/
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case R.id.app_list:
        	showDialog(APPLIST_ID);
        	break;
        case R.id.about:
        	showDialog(ABOUT_ID);
        	break;
        case R.id.poweroff_now:
        	Shutdown.Poweroff(Poweroff.this);
        	break; 
        case R.id.reboot_now:
        	Shutdown.Reboot(Poweroff.this);
        	break; 
        }
       
        return super.onMenuItemSelected(featureId, item);
    }	

    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case ABOUT_ID:
        	return new AlertDialog.Builder(Poweroff.this)
        	.setTitle(R.string.app_name)
        	.setMessage("Version 1.1 -- Created by Tice")
        	.create();
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
}