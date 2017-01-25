package com.theironyard;

import com.theironyard.services.EventRepository;
import com.theironyard.services.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CalendarSpringApplicationTests {

	@Autowired
	WebApplicationContext wap; // repo access for testing

	@Autowired
	UserRepository users; // repo access

	@Autowired
	EventRepository events; // repo access


	MockMvc mockMvc; // lets you create a testable object for passing

	@Before
	public void before() {
		mockMvc = MockMvcBuilders.webAppContextSetup(wap).build(); // build the mock object
	}

	@Test
	public void testLogin() throws Exception { // a test for the login
		mockMvc.perform( // this lets your perform a mock request
				MockMvcRequestBuilders // mock request builder
						.post("/login") // whats endpoint we are testing
						.param("name", "TestUser") // what param we are passing to the endpoint
		);

		Assert.assertTrue(users.count() == 1); // return true if a user was created
	}

	@Test
	public void testAddEvent() throws Exception {
		testLogin(); // to make sure a user exists

		mockMvc.perform(
				MockMvcRequestBuilders
						.post("/create-event") //endpoint being tested
						.param("description", "Test event")
						.param("dateTime", LocalDateTime.now().toString()) // params being tested
						.sessionAttr("userName", "TestUser") // looks up the user in database
		);

		Assert.assertTrue(events.count() == 1); // return true if event was created
	}
}
