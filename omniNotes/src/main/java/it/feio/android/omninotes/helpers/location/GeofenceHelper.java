package it.feio.android.omninotes.helpers.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.receiver.GeofenceReceiver;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.IntentHelper;
import java.util.Collections;

public class GeofenceHelper {

    private final GeofencingClient geofencingClient;
    private final Context context;
    private PendingIntent geofencePendingIntent;

    public GeofenceHelper(Context context) {
        this.context = context;
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    public void addGeofence(Note note) {
        if (note.getTriggerType() != Note.TRIGGER_TYPE_LOCATION || note.getLatitude() == null || note.getLongitude() == null) {
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(String.valueOf(note.get_id()))
                .setCircularRegion(
                        note.getLatitude(),
                        note.getLongitude(),
                        note.getTriggerLocationRadius().floatValue()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        try {
            geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                    .addOnSuccessListener(aVoid -> LogDelegate.d("Geofence added for note " + note.get_id()))
                    .addOnFailureListener(e -> LogDelegate.e("Failed to add geofence", e));
        } catch (SecurityException securityException) {
            LogDelegate.e("Missing location permission for geofencing", securityException);
        }
    }

    public void removeGeofence(Note note) {
        geofencingClient.removeGeofences(Collections.singletonList(String.valueOf(note.get_id())))
                .addOnSuccessListener(aVoid -> LogDelegate.d("Geofence removed for note " + note.get_id()))
                .addOnFailureListener(e -> LogDelegate.e("Failed to remove geofence", e));
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(context, GeofenceReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(context, 0, intent, IntentHelper.mutablePendingIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT));
        return geofencePendingIntent;
    }
}
