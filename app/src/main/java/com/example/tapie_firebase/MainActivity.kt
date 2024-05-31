package com.example.tapie_firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tapie_firebase.ui.theme.TapiefirebaseTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TapiefirebaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NoteApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NoteApp(modifier: Modifier = Modifier) {
    var noteText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(listOf<String>()) }

    val db = FirebaseFirestore.getInstance()

    // Firestore listener 등록
    DisposableEffect(Unit) {
        val listenerRegistration = db.collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // 오류 처리
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val fetchedNotes = snapshot.documents.mapNotNull { it.getString("text") }
                    notes = fetchedNotes
                }
            }

        // Composable이 해제될 때 리스너 제거
        onDispose {
            listenerRegistration.remove()
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        BasicTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )
        Button(onClick = {
            if (noteText.isNotEmpty()) {
                addNoteToFirebase(noteText, db) {
                    noteText = ""
                }
            }
        }) {
            Text(text = "Add Note")
        }
        Spacer(modifier = Modifier.height(16.dp))
        notes.forEach { note ->
            Text(text = note, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

fun addNoteToFirebase(note: String, db: FirebaseFirestore, onSuccess: () -> Unit) {
    val newNote = hashMapOf("text" to note)
    db.collection("notes").add(newNote)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { /* handle error */ }
}

@Preview(showBackground = true)
@Composable
fun NoteAppPreview() {
    TapiefirebaseTheme {
        NoteApp()
    }
}
