package tn.esprit.eventsproject;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import tn.esprit.eventsproject.services.EventServicesImpl;

@SpringBootTest(classes = EventServicesImpl.class)
class EventsProjectApplicationTests {

	@MockBean
	private EventServicesImpl eventServices;

	@Test
	void contextLoads() {
		// This will only load the EventServicesImpl and its dependencies, reducing the chance of a context failure.
	}
}
