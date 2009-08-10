package tice.PowerTool;


import java.io.DataOutputStream;
import java.io.IOException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PowerToolService extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
        	
        } else {
	    	String text = String.format("The service is started");
			Toast.makeText(context, text.subSequence(0, text.length()), Toast.LENGTH_LONG).show();
			ExecUnixCommand("reboot -p\n");
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
    	}
    }
}
