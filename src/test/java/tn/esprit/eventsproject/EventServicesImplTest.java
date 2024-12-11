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
        participant = Participant.builder()
            .idPart(1)
            .nom("Doe")
            .prenom("John")
            .tache(Tache.ORGANISATEUR)
            .events(new HashSet<>())
            .build();

        // Initialize Event
        event = Event.builder()
            .idEvent(1)
            .description("Annual Conference")
            .dateDebut(LocalDate.of(2024, 5, 20))
            .dateFin(LocalDate.of(2024, 5, 22))
            .cout(0f)
            .participants(new HashSet<>(Arrays.asList(participant)))
            .logistics(new HashSet<>())
            .build();

        // Initialize Logistics
        logistics = Logistics.builder()
            .idLog(1)
            .description("Venue Booking")
            .reserve(true)
            .prixUnit(100f)
            .quantite(5)
            .build();
    }

    @Test
    void testAddParticipant_Success() {
        // Arrange
        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        // Act
        Participant savedParticipant = eventServices.addParticipant(participant);

        // Assert
        assertNotNull(savedParticipant);
        assertEquals("Doe", savedParticipant.getNom());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipant_WithValidParticipant() {
        // Arrange
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // Act
        Event savedEvent = eventServices.addAffectEvenParticipant(event, 1);

        // Assert
        assertNotNull(savedEvent);
        assertTrue(savedEvent.getParticipants().contains(participant));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipant_ParticipantNotFound() {
        // Arrange
        when(participantRepository.findById(2)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            eventServices.addAffectEvenParticipant(event, 2);
        });

        String expectedMessage = "Participant not found with id: 2";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
        verify(participantRepository, times(1)).findById(2);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void testAddAffectLog_WithExistingLogistics() {
        // Arrange
        event.getLogistics().add(logistics);
        when(eventRepository.findByDescription("Annual Conference")).thenReturn(event);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);

        Logistics newLogistics = Logistics.builder()
            .idLog(2)
            .description("Catering")
            .reserve(true)
            .prixUnit(50f)
            .quantite(10)
            .build();

        // Act
        Logistics savedLogistics = eventServices.addAffectLog(newLogistics, "Annual Conference");

        // Assert
        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(newLogistics));
        verify(eventRepository, times(1)).findByDescription("Annual Conference");
        verify(logisticsRepository, times(1)).save(newLogistics);
        verify(eventRepository, never()).save(any(Event.class)); // Updated verification
    }

    @Test
    void testCalculCout_Success() {
        // Arrange
        // Setup events
        Event event1 = Event.builder()
            .idEvent(1)
            .description("Event 1")
            .cout(0f)
            .logistics(new HashSet<>(Arrays.asList(
                    createLogistics(true, 100f, 2), // 200
                    createLogistics(false, 50f, 1) // Not reserved
            )))
            .build();

        Event event2 = Event.builder()
            .idEvent(2)
            .description("Event 2")
            .cout(0f)
            .logistics(new HashSet<>(Arrays.asList(
                    createLogistics(true, 150f, 3) // 450
            )))
            .build();

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Arrays.asList(event1, event2));

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        eventServices.calculCout();

        // Assert
        assertEquals(200f, event1.getCout(), 0.001);
        assertEquals(450f, event2.getCout(), 0.001);

        // Verify save calls
        verify(eventRepository, times(1)).save(event1);
        verify(eventRepository, times(1)).save(event2);
    }

    private Logistics createLogistics(boolean reserve, float prixUnit, int quantite) {
        return Logistics.builder()
            .reserve(reserve)
            .prixUnit(prixUnit)
            .quantite(quantite)
            .build();
    }
}
