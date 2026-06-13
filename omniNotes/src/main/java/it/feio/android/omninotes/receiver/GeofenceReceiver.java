package it.feio.android.omninotes.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import it.feio.android.omninotes.MainActivity;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.IntentHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.notifications.NotificationsHelper;
import it.feio.android.omninotes.helpers.notifications.NotificationChannels.NotificationChannelNames;
import it.feio.android.omninotes.models.Note;
import java.util.List;

public class GeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            LogDelegate.e("GeofencingEvent error " + (geofencingEvent != null ? geofencingEvent.getErrorCode() : "null"));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            if (triggeringGeofences != null) {
                for (Geofence geofence : triggeringGeofences) {
                    long noteId = Long.parseLong(geofence.getRequestId());
                    Note note = DbHelper.getInstance().getNote(noteId);
                    if (note != null) {
                        showNotification(context, note);
                    }
                }
            }
        }
    }

    private void showNotification(Context context, Note note) {
        PendingIntent contentIntent = IntentHelper.getNotePendingIntent(context, MainActivity.class, Intent.ACTION_MAIN, note);
        
        NotificationsHelper notificationsHelper = new NotificationsHelper(context);
        notificationsHelper.createStandardNotification(
                NotificationChannelNames.REMINDERS,
                R.drawable.ic_stat_notification,
                context.getString(R.string.smart_trigger_notification_title),
                contentIntent
        ).setMessage(String.format(context.getString(R.string.smart_trigger_notification_text), note.getTitle()))
        .show(note.get_id());
    }
}
