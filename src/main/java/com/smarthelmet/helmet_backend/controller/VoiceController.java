package com.smarthelmet.helmet_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;

@RestController
public class VoiceController {

    @PostMapping(value = "/voice/alert", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoiceAlert() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<Response>" +
               "<Say voice=\"alice\">Emergency Alert! A worker has not acknowledged their helmet alert. Please take immediate action.</Say>" +
               "</Response>";
    }

    // (Optional) keep GET for testing in browser
    @GetMapping(value = "/voice/alert", produces = MediaType.APPLICATION_XML_VALUE)
    public String testVoiceAlert() {
        return handleVoiceAlert();
    }
}
