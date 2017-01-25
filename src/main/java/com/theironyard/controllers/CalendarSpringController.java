package com.theironyard.controllers;

import com.theironyard.entities.Event;
import com.theironyard.entities.User;
import com.theironyard.services.EventRepository;
import com.theironyard.services.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 1/25/17.
 */
@Controller
public class CalendarSpringController {
    @Autowired
    EventRepository events;

    @Autowired
    UserRepository users;

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model, HttpSession session) {
        String userName = (String) session.getAttribute("userName"); // Getting userName from session
        List<Event> eventEntities = new ArrayList<>();
        if (userName != null) { // if we have a current user in session
            User user = users.findFirstByName(userName); //find the user object by session name
            if (user != null) {
                eventEntities = events.findAllByUserOrderByDateTimeDesc(user);
            }
            model.addAttribute("user", user); // add this user to our model
            model.addAttribute("now", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)); //gives a local date/time
        }
        model.addAttribute("events", eventEntities); //store the events
        return "home"; // go to home.html
    }

    @RequestMapping(path = "/create-event", method = RequestMethod.POST)
    public String createEvent(HttpSession session, String description, String dateTime) { // expects a session , and two strings
        String userName = (String) session.getAttribute("userName"); // gets userName from session
        if (userName != null) { // if we have a user in session
            Event event = new Event(description, LocalDateTime.parse(dateTime), users.findFirstByName(userName)); // creates even object with the parameters that were passed
            // also we find the user that with the session userName
            events.save(event); // saves the even object
        }
        return "redirect:/"; // redirects back to "/" endpoint which sends back to home.html
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(HttpSession session, String name) { // needs session and a String that is the name of the user
        User user = users.findFirstByName(name); // if the user exists this method will find it and store it in the object
        if (user == null) { // if the user does not already exist
            user = new User(name); //creates the new user
            users.save(user); // saves the new user to the repo
        }
        session.setAttribute("userName", name); //sets that name to userName session for accessing purposes
        return "redirect:/"; // redirects to "/" endpoint so that it hits the home.html
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public String logout(HttpSession session) { // needs session
        session.invalidate(); // ends the session
        return "redirect:/"; // redirects to "/" endpoint so that it his the home.html with no use logged in now
    }
}
