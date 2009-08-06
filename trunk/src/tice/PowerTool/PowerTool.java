package tice.PowerTool;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class PowerTool extends Activity {
	
	private TextView mTimeDisplay;
	NotificationManager mNM;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
      
        mTimeDisplay = (TextView) findViewById(R.id.TextView);
        mNM  = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
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
         
        ExecUnixCommand("/system/bin/su","");
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
    	
    	//Toast.makeText(PowerTool.this, R.string.test, Toast.LENGTH_LONG).show();
    	
    	String strret = ExecUnixCommand("/system/bin/reboot","");
    	Toast.makeText(PowerTool.this, strret, Toast.LENGTH_LONG).show();
/*    	
    	ScheduledExecutorService service=Executors.newScheduledThreadPool(1);
    	
    	Runnable task = new Runnable(){
             public void run() {
            	//showNotification();
             }
        };
        
        service.schedule(task, 10,  TimeUnit.SECONDS); 
*/           	
    }
    
    private String ExecUnixCommand(String cmdstr, String arg){
    	try {
    	    // android.os.Exec is not included in android.jar so we need to use reflection.
    	    Class<?> execClass = Class.forName("android.os.Exec");
    	    Method createSubprocess = execClass.getMethod("createSubprocess", String.class, String.class, String.class, int[].class);
    	    Method waitFor = execClass.getMethod("waitFor", int.class);
    	    
    	    // Executes the command.
    	    // NOTE: createSubprocess() is asynchronous.
    	    int[] pid = new int[1];
    	    FileDescriptor fd = (FileDescriptor)createSubprocess.invoke(
    	            null, cmdstr, arg, null, pid);
    	    
    	    // Reads stdout.
    	    // NOTE: You can write to stdin of the command using new FileOutputStream(fd).
    	    FileInputStream in = new FileInputStream(fd);
    	    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    	    String output = "";
    	    try {
    	        String line;
    	        while ((line = reader.readLine()) != null) {
    	            output += line + "\n";
    	        }
    	    } catch (IOException e) {
    	        // It seems IOException is thrown when it reaches EOF.
    	    }
    	    
    	    // Waits for the command to finish.
    	    waitFor.invoke(null, pid[0]);
    	    
    	    return output;
    	} catch (ClassNotFoundException e) {
    	    throw new RuntimeException(e.getMessage());
    	} catch (SecurityException e) {
    	    throw new RuntimeException(e.getMessage());
    	} catch (NoSuchMethodException e) {
    	    throw new RuntimeException(e.getMessage());
    	} catch (IllegalArgumentException e) {
    	    throw new RuntimeException(e.getMessage());
    	} catch (IllegalAccessException e) {
    	    throw new RuntimeException(e.getMessage());
    	} catch (InvocationTargetException e) {
    	    throw new RuntimeException(e.getMessage());
    	}
    }
    
    private void showNotification(){
    	Looper.prepare();
/*    	
    	CharSequence text = getText(R.string.test);
        Notification notification = new Notification(R.drawable.icon, text, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(PowerTool.this, 0, new Intent(PowerTool.this, PowerTool.class), 0);
        notification.setLatestEventInfo(PowerTool.this, getText(R.string.test), text, contentIntent);   
    	mNM.notify(R.string.test, notification);
*/    	
    	String strret = ExecUnixCommand("/system/bin/su","");
    	//strret = ExecUnixCommand("/system/bin/reboot","-p");
    	
    	Toast.makeText(PowerTool.this, strret, Toast.LENGTH_LONG).show();
    	Looper.loop();
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
	} 
}