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

    public static boolean eventTimeConflict (LocalDateTime start, LocalDateTime end) {

       if(end.isBefore(start)){
           return true; // conflict exists
       }
       return false; // conflict doesn't exist
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home(Model model, HttpSession session) {
        String userName = (String) session.getAttribute("userName"); // Getting userName from session
        List<Event> eventEntities = new ArrayList<>();
        if (userName != null) { // if we have a current user in session
            User user = users.findFirstByName(userName); //find the user object by session name
            if (user != null) {
                eventEntities = events.findAllByUserOrderByDateTimeStartDesc(user);
            }
            model.addAttribute("user", user); // add this user to our model
            model.addAttribute("now", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));//gives a local date/time now
            model.addAttribute("oneAhead", LocalDateTime.now().plusSeconds(1).truncatedTo(ChronoUnit.SECONDS)); // gives a time one second ahead
        }
        model.addAttribute("events", eventEntities); //store the events
        return "home"; // go to home.html
    }

    @RequestMapping(path = "/create-event", method = RequestMethod.POST)
    public String createEvent(HttpSession session, String description, String dateTimeStart, String dateTimeEnd) throws Exception { // expects a session , and two strings
        String userName = (String) session.getAttribute("userName"); // gets userName from session
        if (userName != null) { // if we have a user in session
            Event event = new Event(description, LocalDateTime.parse(dateTimeStart), LocalDateTime.parse(dateTimeEnd), users.findFirstByName(userName)); // creates event object with the parameters that were passed
            // also we find the user that with the session userName
           boolean conflict = eventTimeConflict(event.getDateTimeStart(), event.getDateTimeEnd());
            if(conflict == false) {
                int count = 0;
                List<Event> eventCheck;
                eventCheck = events.findAllByUserOrderByDateTimeStartDesc(users.findFirstByName(userName));
                for (int i = 0; i < eventCheck.size(); i++) {
                    if(event.getDateTimeStart().isEqual(eventCheck.get(i).getDateTimeStart())
                            || event.getDateTimeEnd().isEqual(eventCheck.get(i).getDateTimeEnd())
                            || event.getDateTimeStart().isEqual(eventCheck.get(i).getDateTimeEnd())
                            || event.getDateTimeEnd().isEqual(eventCheck.get(i).getDateTimeStart())
                            || event.getDateTimeStart().isBefore(eventCheck.get(i).getDateTimeEnd())
                            || event.getDateTimeEnd().isBefore(eventCheck.get(i).getDateTimeEnd())) {
                        count = 1;
                        break;
                    } else if(event.getDateTimeStart().isBefore(eventCheck.get(i).getDateTimeStart())
                    && event.getDateTimeEnd().isBefore(eventCheck.get(i).getDateTimeStart())) {
                        count = 0;
                    } else if(event.getDateTimeStart().isAfter(eventCheck.get(i).getDateTimeEnd())
                            && event.getDateTimeEnd().isAfter(eventCheck.get(i).getDateTimeEnd())) {
                        count = 0;
                    }
                }
                if(count == 0) {
                    events.save(event);
                }
            }
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
