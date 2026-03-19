package com.example.opcodeapp;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.opcodeapp.db.DBManager;
import com.example.opcodeapp.db.FirestoreCallbackSend;
import com.example.opcodeapp.model.Event;
import com.example.opcodeapp.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventCreationTest {

    @Mock private FirebaseFirestore mockDb;
    @Mock private CollectionReference mockCollection;
    @Mock private DocumentReference mockDocRef;
    @Mock private Task<Void> mockTask;
    @Mock private FirestoreCallbackSend mockListener;

    private DBManager dbManager;
    private User organizer;
    private Event event;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        organizer = new User("Jane Organizer", "jane@ualberta.ca", "7801234567");
        organizer.setId("organizer_id_001");

        event = new Event(
                "Trivia Night", "Library", "",
                LocalDate.of(2026, 12, 25), LocalDateTime.now(),
                LocalDate.of(2026, 12, 25), LocalDateTime.of(2026, 12, 20, 0, 0),
                organizer, 0f
        );

        when(mockDb.collection("Events")).thenReturn(mockCollection);
        when(mockCollection.document()).thenReturn(mockDocRef);
        when(mockDocRef.getId()).thenReturn("generated_event_id");
        when(mockDocRef.set(any(Event.class))).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        dbManager = new DBManager(mockDb);
    }

    // 1. Event constructor stores fields correctly

    @Test
    public void testConstructor_storesName() {
        assertEquals("Trivia Night", event.getName());
    }

    @Test
    public void testConstructor_storesLocation() {
        assertEquals("Library", event.getLocation());
    }

    @Test
    public void testConstructor_storesStartDate() {
        assertEquals(LocalDate.of(2026, 12, 25), event.getStartDate());
    }

    @Test
    public void testConstructor_storesOrganizer() {
        assertEquals(organizer, event.getOrganizer());
    }

    // 2. DBManager.addEvent interaction

    @Test
    public void testAddEvent_setsGeneratedId() {
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            ((OnSuccessListener<Void>) invocation.getArgument(0)).onSuccess(null);
            return mockTask;
        });

        dbManager.addEvent(event, mockListener);

        assertEquals("generated_event_id", event.getId());
    }

    @Test
    public void testAddEvent_callsOnSendSuccess() {
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            ((OnSuccessListener<Void>) invocation.getArgument(0)).onSuccess(null);
            return mockTask;
        });

        dbManager.addEvent(event, mockListener);

        verify(mockListener).onSendSuccess();
        verify(mockListener, never()).onSendFailure(any());
    }

    @Test
    public void testAddEvent_callsOnSendFailure() {
        Exception fakeError = new Exception("Network error");

        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenAnswer(invocation -> {
            ((OnFailureListener) invocation.getArgument(0)).onFailure(fakeError);
            return mockTask;
        });

        dbManager.addEvent(event, mockListener);

        verify(mockListener).onSendFailure(fakeError);
        verify(mockListener, never()).onSendSuccess();
    }

    // 3. Organizer is set correctly on the Event

    @Test
    public void testOrganizer_preservedOnEvent() {
        assertEquals("organizer_id_001", event.getOrganizer().getId());
        assertEquals("jane@ualberta.ca", event.getOrganizer().getEmail());
    }
}
