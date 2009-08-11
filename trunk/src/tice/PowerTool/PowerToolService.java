package tice.PowerTool;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PowerToolService extends BroadcastReceiver {

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION)) {
				context.startService(new Intent(context, AutoStartService.class));
				Toast.makeText(context, "AutoStart service has started!", Toast.LENGTH_LONG).show();
			}
		} else {
			String text = String.format("The service is started");
			Toast.makeText(context, text.subSequence(0, text.length()), Toast.LENGTH_LONG).show();
			ExecUnixCommand("reboot -p");
		}
	}

	private void ExecUnixCommand(String cmdstr) {
		Process process = null;
		InputStream stderr = null;
		InputStream stdout = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
	        stderr = process.getErrorStream();
	        stdout = process.getInputStream();
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmdstr + "\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (IOException e) {;} catch (InterruptedException e) {}
		finally {
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
}
