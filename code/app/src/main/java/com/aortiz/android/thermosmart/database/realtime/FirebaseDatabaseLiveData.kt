package com.aortiz.android.thermosmart.database.realtime

import androidx.lifecycle.LiveData
import com.google.firebase.database.*
import timber.log.Timber

class FirebaseDatabaseLiveData<T> private constructor(
    private val reference: DatabaseReference,
    private val type: Class<T>?,
    private val typeIndicator: GenericTypeIndicator<T>?,
) : LiveData<T>() {
    constructor(reference: DatabaseReference, type: Class<T>) : this(reference, type, null) {
        Timber.d("reference $reference")
    }

    constructor(reference: DatabaseReference, type: GenericTypeIndicator<T>) : this(
        reference,
        null,
        type
    )

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
        reference.addValueEventListener(listener)
    }

    override fun onInactive() {
        super.onInactive()
        reference.removeEventListener(listener)
    }
}

