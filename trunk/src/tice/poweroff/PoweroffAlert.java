package tice.poweroff;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PoweroffAlert extends Activity {
    
	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.poweroffalert);
        
		Button btnPoweroff = (Button) findViewById(R.id.btnPoweroff);
		btnPoweroff.setOnClickListener(mPoweroff);
		
		Button btnReboot = (Button) findViewById(R.id.btnReboot);
		btnReboot.setOnClickListener(mReboot);
    }
	
	private OnClickListener mPoweroff = new OnClickListener() {
		public void onClick(View v) {
	        Thread thr1 = new Thread(null, mTaskPoweroff, "PowerToolService");
	        thr1.start();
		}
	};	
	
	private OnClickListener mReboot = new OnClickListener() {
		public void onClick(View v) {
	        Thread thr1 = new Thread(null, mTaskReboot, "PowerToolService");
	        thr1.start();
		}
	};	
	
	Runnable mTaskPoweroff = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 500;
            while (System.currentTimeMillis() < endTime) {
            	try{
            		wait(endTime - System.currentTimeMillis());
            	}catch (Exception e) {;}
            }
            Shutdown.ExecUnixCommand("/system/bin/toolbox reboot -p");
        }
    };	
    
    Runnable mTaskReboot = new Runnable() {
        public void run() {
            long endTime = System.currentTimeMillis() + 500;
            while (System.currentTimeMillis() < endTime) {
            	try{
            		wait(endTime - System.currentTimeMillis());
            	}catch (Exception e) {;}
            }
            Shutdown.ExecUnixCommand("/system/bin/toolbox reboot");
        }
    };
}
