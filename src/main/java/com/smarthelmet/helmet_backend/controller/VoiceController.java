package com.smarthelmet.helmet_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;

@RestController
public class VoiceController {

    @GetMapping(value = "/voice/alert", produces = MediaType.APPLICATION_XML_VALUE)
    public String handleVoiceAlert() {
        // TwiML response (what Twilio says on the call)
        VoiceResponse response = new VoiceResponse.Builder()
                .say(new Say.Builder(" Emergency Alert! A worker has not acknowledged their helmet alert. Please take immediate action.").voice(Say.Voice.ALICE).build())
                .build();

        return response.toXml();
    }
}
