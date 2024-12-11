package tn.esprit.eventsproject.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @InjectMocks
    private EventServicesImpl eventServices;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    private Participant participant;
    private Event event;
    private Logistics logistics;

    @BeforeEach
    void setUp() {
        // Initialize Participant
        participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Doe");
        participant.setPrenom("John");
        participant.setTache(Tache.ORGANISATEUR);
        participant.setEvents(new HashSet<>());

        // Initialize Event
        event = new Event();
        event.setIdEvent(1);
        event.setDescription("Annual Conference");
        event.setDateDebut(LocalDate.of(2024, 5, 20));
        event.setDateFin(LocalDate.of(2024, 5, 22));
        event.setCout(0f);
        event.setParticipants(new HashSet<>(Arrays.asList(participant)));
        event.setLogistics(new HashSet<>());

        // Initialize Logistics
        logistics = new Logistics();
        logistics.setIdLog(1);
        logistics.setDescription("Venue Booking");
        logistics.setReserve(true);
        logistics.setPrixUnit(100f);
        logistics.setQuantite(5);
    }

    @Test
    void testAddParticipant_Success() {
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant savedParticipant = eventServices.addParticipant(participant);

        assertNotNull(savedParticipant);
        assertEquals("Doe", savedParticipant.getNom());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipant_WithValidParticipant() {
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(savedEvent);
        assertTrue(savedEvent.getParticipants().contains(participant));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipant_ParticipantNotFound() {
        when(participantRepository.findById(2)).thenReturn(Optional.empty());

        Exception exception = assertThrows(NullPointerException.class, () -> {
            eventServices.addAffectEvenParticipant(event, 2);
        });

        String expectedMessage = "Cannot invoke \"tn.esprit.eventsproject.entities.Participant.getEvents()\" because \"participant\" is null";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains("Cannot invoke"));
        verify(participantRepository, times(1)).findById(2);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testAddAffectLog_WithExistingLogistics() {
        event.getLogistics().add(logistics);
        when(eventRepository.findByDescription("Annual Conference")).thenReturn(event);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Logistics newLogistics = new Logistics();
        newLogistics.setIdLog(2);
        newLogistics.setDescription("Catering");
        newLogistics.setReserve(true);
        newLogistics.setPrixUnit(50f);
        newLogistics.setQuantite(10);

        Logistics savedLogistics = eventServices.addAffectLog(newLogistics, "Annual Conference");

        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(newLogistics));
        verify(eventRepository, times(1)).findByDescription("Annual Conference");
        verify(logisticsRepository, times(1)).save(newLogistics);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testCalculCout_Success() {
        // Setup events
        Event event1 = new Event();
        event1.setIdEvent(1);
        event1.setDescription("Event 1");
        event1.setCout(0f);
        event1.setLogistics(new HashSet<>(Arrays.asList(
                createLogistics(true, 100f, 2), // 200
                createLogistics(false, 50f, 1) // Not reserved
        )));

        Event event2 = new Event();
        event2.setIdEvent(2);
        event2.setDescription("Event 2");
        event2.setCout(0f);
        event2.setLogistics(new HashSet<>(Arrays.asList(
                createLogistics(true, 150f, 3) // 450
        )));

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Arrays.asList(event1, event2));

        when(eventRepository.save(any(Event.class))).thenReturn(null); // Mock save

        eventServices.calculCout();

        // Verify calculations
        assertEquals(200f, event1.getCout());
        assertEquals(450f, event2.getCout());

        // Verify save calls
        verify(eventRepository, times(1)).save(event1);
        verify(eventRepository, times(1)).save(event2);
    }

    private Logistics createLogistics(boolean reserve, float prixUnit, int quantite) {
        Logistics log = new Logistics();
        log.setReserve(reserve);
        log.setPrixUnit(prixUnit);
        log.setQuantite(quantite);
        return log;
    }
}
