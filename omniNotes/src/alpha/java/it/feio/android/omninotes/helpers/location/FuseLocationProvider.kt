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
package it.feio.android.omninotes.helpers.location

import android.content.Context
import java.lang.SecurityException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import it.feio.android.omninotes.OmniNotes
import it.feio.android.omninotes.models.listeners.OnGeoUtilResultListener

class FuseLocationProvider : LocationProvider {

    companion object {
        private val lock = Any()
        @Volatile
        private var fusedLocationProviderClient: FusedLocationProviderClient? = null

        fun getFusedLocationProviderClient(context: Context): FusedLocationProviderClient {
            return fusedLocationProviderClient ?: synchronized(lock) {
                fusedLocationProviderClient ?: LocationServices.getFusedLocationProviderClient(context).also {
                    fusedLocationProviderClient = it
                }
            }
        }
    }

    override fun instantiate() {
        getFusedLocationProviderClient(OmniNotes.getAppContext())
    }

    @Throws(SecurityException::class)
    override fun getLocation(onGeoUtilResultListener: OnGeoUtilResultListener?) {
        val client = getFusedLocationProviderClient(OmniNotes.getAppContext())
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    onGeoUtilResultListener?.onLocationRetrieved(location)
                } else {
                    client.lastLocation.addOnSuccessListener { lastLoc ->
                        onGeoUtilResultListener?.onLocationRetrieved(lastLoc)
                    }.addOnFailureListener { e -> onGeoUtilResultListener?.onLocationUnavailable(e) }
                }
            }
            .addOnFailureListener { e -> onGeoUtilResultListener?.onLocationUnavailable(e) }
    }

}
