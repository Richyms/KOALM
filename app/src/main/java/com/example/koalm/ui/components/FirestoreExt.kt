package com.example.koalm.ui.components

import androidx.compose.runtime.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun <T> DocumentReference.snapshotsAsState(
    mapper: (DocumentSnapshot?) -> T
): State<T> {
    val state = remember { mutableStateOf(mapper(null)) }
    DisposableEffect(this) {
        val reg = addSnapshotListener { snap, _ -> state.value = mapper(snap) }
        onDispose { reg.remove() }
    }
    return state
}
