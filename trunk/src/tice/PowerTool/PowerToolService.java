package tice.PowerTool;

import java.io.DataOutputStream;
import java.io.IOException;

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
				context
						.startService(new Intent(context,
								AutoStartService.class));
				Toast.makeText(context, "AutoStart service has started!",
						Toast.LENGTH_LONG).show();
			}
		} else {
			String text = String.format("The service is started");
			Toast.makeText(context, text.subSequence(0, text.length()),
					Toast.LENGTH_LONG).show();
			ExecUnixCommand("reboot -p\n");
		}
	}

	private void ExecUnixCommand(String cmdstr) {
		try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process
					.getOutputStream());
			os.writeBytes(cmdstr);
			os.writeBytes("exit\n");
			os.flush();
		} catch (IOException e) {
		}
	}
}
