package tice.PowerTool;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

class PowerToolState implements Serializable{
	private static final long serialVersionUID = 1L;
	public ScheduledFuture<?> mServicesHandle;

    public PowerToolState(ScheduledFuture<?> handle){
        this.mServicesHandle = handle;
    }
} 

public class PowerTool extends Activity {
	
	private TextView mTimeDisplay;
	NotificationManager mNM;
	ScheduledFuture<?> mServicesHandle;
	ScheduledExecutorService mService;
	private static final String TAG = "PowerTool";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
      
        mTimeDisplay = (TextView) findViewById(R.id.TextView);
        mNM  = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        if (savedInstanceState == null){
        	mServicesHandle = null;
        } else {
        	PowerToolState s = (PowerToolState)savedInstanceState.getSerializable(TAG);
        	mServicesHandle = s.mServicesHandle;
        }

        TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateDisplay(hourOfDay, minute);
            }
        });
        
        Button btnOK = (Button) findViewById(R.id.btnOK); 
        btnOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	StartServices();
            }
        }); 
    }
 
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void updateDisplay(int hourOfDay, int minute) {
        mTimeDisplay.setText(
                    new StringBuilder()
                    .append(pad(hourOfDay)).append(":")
                    .append(pad(minute)));
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
    private void StartServices(){

    	if (mServicesHandle != null){
    		if (mServicesHandle.cancel(true) == false){
    			Toast.makeText(PowerTool.this, R.string.set_time_failed, Toast.LENGTH_LONG).show();
    		}
    	}
    	
    	mService = Executors.newScheduledThreadPool(1);
    	
    	Runnable task = new Runnable(){
             public void run() {
            	 ExecUnixCommand("reboot -p\n");
             }
        };
        
        Date now = new Date();
        Date scheduledate = new Date();
        TimePicker timePicker = (TimePicker) findViewById(R.id.TimePicker);
   
        scheduledate.setHours(timePicker.getCurrentHour());
        scheduledate.setMinutes(timePicker.getCurrentMinute());

        if( scheduledate.getHours() - now.getHours() < 0 ){
        	scheduledate.setDate(now.getDate() + 1);
        } else if ( scheduledate.getHours() - now.getHours() == 0){
        	if( scheduledate.getMinutes() - now.getMinutes() <= 0 ){
        		scheduledate.setDate(now.getDate() + 1);
        	}
        }

        long time = (scheduledate.getTime() - System.currentTimeMillis()) / 1000;
        
        mServicesHandle = mService.schedule(task, time,  TimeUnit.SECONDS); 
        
        if(mServicesHandle == null){
			Toast.makeText(PowerTool.this, R.string.set_time_failed, Toast.LENGTH_LONG).show();
        } else {
        	String text = String.format("The service will be started in %.1f minutes", time / 60.0);
			Toast.makeText(PowerTool.this, text.subSequence(0, text.length()), Toast.LENGTH_LONG).show();
        }
    }
    
    private void ExecUnixCommand(String cmdstr){
    	try{
    		Process process = Runtime.getRuntime().exec("su");
    		DataOutputStream os = new DataOutputStream(process.getOutputStream());
    		os.writeBytes(cmdstr);
    		os.writeBytes("exit\n");
    		os.flush();
    	} catch (IOException e) {
    	// TODO Auto-generated catch block
    	}
    }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
//		outState.putSerializable(TAG, new PowerToolState(mServicesHandle));
	}
}