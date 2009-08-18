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
			Shutdown.Poweroff(PoweroffAlert.this);
		}
	};	
	
	private OnClickListener mReboot = new OnClickListener() {
		public void onClick(View v) {
			Shutdown.Reboot(PoweroffAlert.this);
		}
	};	
}
