/*
 * Copyright (C) 2013-2025 Federico Iosue (developer@omninotes.app)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.async.notes;

import android.content.Context;
import android.os.AsyncTask;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.OmniNotes;
import it.feio.android.omninotes.async.bus.NotesUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.location.GeofenceHelper;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.utils.ReminderHelper;
import it.feio.android.omninotes.utils.StorageHelper;
import it.feio.android.omninotes.utils.date.DateUtils;
import java.util.List;


public class SaveNoteTask extends AsyncTask<Note, Void, Note> {

  private Context context;
  private boolean updateLastModification = true;

  public SaveNoteTask(boolean updateLastModification) {
    super();
    this.context = OmniNotes.getAppContext();
    this.updateLastModification = updateLastModification;
  }

  @Override
  protected Note doInBackground(Note... params) {
    Note note = params[0];
    purgeRemovedAttachments(note);
    boolean reminderMustBeSet = DateUtils.isFuture(note.getAlarm());
    if (reminderMustBeSet) {
      note.setReminderFired(false);
    }
    note = DbHelper.getInstance().updateNote(note, updateLastModification);

    GeofenceHelper geofenceHelper = new GeofenceHelper(context);
    geofenceHelper.removeGeofence(note);
    if (note.getTriggerType() == Note.TRIGGER_TYPE_LOCATION) {
      geofenceHelper.addGeofence(note);
    }

    if (reminderMustBeSet) {
      ReminderHelper.addReminder(context, note);
    }
    return note;
  }

  private void purgeRemovedAttachments(Note note) {
    List<Attachment> deletedAttachments = note.getAttachmentsListOld();
    for (Attachment attachment : note.getAttachmentsList()) {
      if (attachment.getId() != null) {
        deletedAttachments.remove(attachment);
      }
    }
    for (Attachment attachmentDeleted : deletedAttachments) {
      StorageHelper.delete(context, attachmentDeleted.getUri().getPath());
    }
  }

  @Override
  protected void onPostExecute(Note note) {
    super.onPostExecute(note);
    EventBus.getDefault().post(new NotesUpdatedEvent(List.of(note)));
  }
}
