package tice.poweroff;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class PoweroffWidgetProvider extends AppWidgetProvider {

	static final String CUSTOM_ACTION_POWEROFF = "tice.poweroff.intent.action.CUSTOM_ACTION_POWEROFF"; 
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
	    
	    
	    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.poweroffwidget);
	    	
	    Intent intent = new Intent();
	    intent.setAction(CUSTOM_ACTION_POWEROFF);
	    intent.putExtra("ACTION","REBOOT");
	    intent.setClassName(context,PoweroffAlert.class.getName());
	    	
	    PendingIntent pendingintent =  PendingIntent.getActivity(context, 0, intent, 0);
	    views.setOnClickPendingIntent(R.id.ImageButton01, pendingintent);
	    	
	    appWidgetManager.updateAppWidget(appWidgetIds, views);

	    super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
