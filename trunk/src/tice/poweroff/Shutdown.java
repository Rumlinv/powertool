package tice.poweroff;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

public class Shutdown {

	static private Context mCtx;
	private AlarmManager mArm;

	public Shutdown(Context c, AlarmManager am) {
		mCtx = c;
		mArm = am;
	}
	
	public void SetPoweroffSchedule(int hour, int minute) {

		Date now = new Date();
		Date scheduledate = new Date();

		scheduledate.setHours(hour);
		scheduledate.setMinutes(minute);
		scheduledate.setSeconds(0);

		if (scheduledate.getHours() - now.getHours() < 0) {
			scheduledate.setDate(now.getDate() + 1);
		} else if (scheduledate.getHours() - now.getHours() == 0) {
			if (scheduledate.getMinutes() - now.getMinutes() <= 0) {
				scheduledate.setDate(now.getDate() + 1);
			}
		}

		Intent intent = new Intent(mCtx, PoweroffReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(mCtx, 0, intent, 0);

		mArm.cancel(sender);
		mArm.set(AlarmManager.RTC_WAKEUP, scheduledate.getTime(), sender);
		//mArm.setRepeating(AlarmManager.RTC_WAKEUP, scheduledate.getTime() ,  6000 , sender);
		
		String text;
		double time = (scheduledate.getTime() - now.getTime()) / 1000.0;
		if( time < 60){
			text = String.format("Power Off will start in %.1f seconds", time);
		} else if ( time < 3600 ){
			text = String.format("Power Off will start in %.1f minutes", time / 60);
		} else {
			text = String.format("Power Off will start in %.1f hours", time / 3600);
		}
		
		Toast.makeText(mCtx, text.subSequence(0, text.length()),Toast.LENGTH_LONG).show();
	}
	
	static public boolean Getsu(){
		return ExecUnixCommand("ls");
	}

/*
	static private String ExecUnixCommand(String cmdstr, String arg) {
		try {
		    // android.os.Exec is not included in android.jar so we need to use reflection.
		    Class<?> execClass = Class.forName("android.os.Exec");
		    Method createSubprocess = execClass.getMethod("createSubprocess",
		            String.class, String.class, String.class, int[].class);
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
		        while (reader.ready() && (line = reader.readLine()) != null) {
		            output += line + "\n";
		        }
		    } catch (IOException e) {
		        // It seems IOException is thrown when it reaches EOF.
		    }
		    
		    // Waits for the command to finish.
		    //waitFor.invoke(null, pid[0]);
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
*/	
	
	static private boolean ExecUnixCommand(String cmdstr) {
		boolean bret = false;
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
			os.flush();
			os.writeBytes("exit\n");
			os.flush();
            while (inputBr.ready() && (line = inputBr.readLine()) != null) {
            	stdstring = stdstring + line.trim();
            }
            while (errBr.ready() && (line = errBr.readLine()) != null){
            	errstring = errstring + line.trim();;
            }
            //doWaitFor(process);
            process.waitFor();
            bret = true;
		} catch (IOException e) {;} catch (InterruptedException e) {;} 
            try {
                if (os != null) os.close();
                if (stderr != null) stderr.close();
                if (stdout != null) stdout.close();
            } catch (Exception ex) { ;}
            try {
            	process.destroy();
            } catch (Exception e) {;}
        return bret;
	}
	
	public static int doWaitFor(Process p) {
	    int exitValue = -1; // returned to caller when p is finished
	    try {
	        InputStream in = p.getInputStream();;
	        InputStream err = p.getErrorStream();;
	        boolean finished = false; // Set to true when p is finished

	        while(!finished){
	            try {
	                while( in.available() > 0) {
	                    ;
	                }
	                while( err.available() > 0) {
	                    ;
	                }

	                exitValue = p.exitValue();
	                finished = true;
	            }
	            catch (IllegalThreadStateException e) {
	                Thread.currentThread();
	                Thread.sleep(1000);
	                finished = true;
	            }
	        }
	    }
	    catch (Exception e) {
	        ;
	    }

	    // return completion status to caller
	    return exitValue;
	}
	
    static final Runnable mTaskReboot = new Runnable() {
        public void run() {
        	
			PowerManager pm = (PowerManager) mCtx.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock sWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
	                PowerManager.ACQUIRE_CAUSES_WAKEUP |
	                PowerManager.ON_AFTER_RELEASE, "PowerToolService");
			sWakeLock.acquire();
        	
            long endTime = System.currentTimeMillis() + 3*1000;
            while (System.currentTimeMillis() < endTime) {
            	try{
            		wait(endTime - System.currentTimeMillis());
            	}catch (Exception e) {;}
            }
            
            sWakeLock.release();
            ExecUnixCommand("reboot");
        }
    };
    
    static final Runnable mTaskPoweroff = new Runnable() {
        public void run() {
        	
			PowerManager pm = (PowerManager) mCtx.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock sWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
	                PowerManager.ACQUIRE_CAUSES_WAKEUP |
	                PowerManager.ON_AFTER_RELEASE, "PowerToolService");
			sWakeLock.acquire();
			
        	long endTime = System.currentTimeMillis() + 3*1000;
            while (System.currentTimeMillis() < endTime) {
            	try{
            		wait(endTime - System.currentTimeMillis());
            	}catch (Exception e) {;}
            }
            
            sWakeLock.release();
            ExecUnixCommand("reboot -p");
        }
    };	
    
    static void Reboot(Context context){
    	mCtx = context;
		Toast.makeText(context, "Reboot will start in 3 secondes", Toast.LENGTH_LONG).show();
        Thread thr = new Thread(null, mTaskReboot, "PowerToolThread");
        thr.start();
    }
    
    static void Poweroff(Context context){
    	mCtx = context;
		Toast.makeText(context, "Poweroff will start in 3 seconds", Toast.LENGTH_LONG).show();
        Thread thr = new Thread(null, mTaskPoweroff, "PowerToolThread");
        thr.start();
    }   
}
