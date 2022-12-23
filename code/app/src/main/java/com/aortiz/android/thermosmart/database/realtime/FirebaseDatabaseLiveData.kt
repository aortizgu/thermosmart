package com.aortiz.android.thermosmart.database.realtime

import androidx.lifecycle.LiveData
import com.google.firebase.database.*
import timber.log.Timber

class FirebaseDatabaseLiveData<T> private constructor(
    private val reference: DatabaseReference,
    private val type: Class<T>?,
    private val typeIndicator: GenericTypeIndicator<T>?,
    private val sync: Boolean = true
) : LiveData<T>() {
    constructor(reference: DatabaseReference, type: Class<T>, sync: Boolean = true) : this(reference, type, null, sync) {
        Timber.d("reference $reference")
    }

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                value = if (type != null)
                    snapshot.getValue(type)
                else
                    snapshot.getValue(typeIndicator!!)
            } catch (e: DatabaseException) {
                Timber.e("onDataChange: $e")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            value = null
        }
    }

    override fun onActive() {
        super.onActive()
        Timber.d("onActive $reference")
        if (sync) reference.keepSynced(true)
        reference.addValueEventListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        Timber.d("onInactive $reference")
        if (sync) reference.keepSynced(false)
        reference.removeEventListener(listener)
    }
}

