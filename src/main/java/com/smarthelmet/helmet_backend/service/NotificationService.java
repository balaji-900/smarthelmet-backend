package com.smarthelmet.helmet_backend.service;

import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final TwilioConfig twilioConfig;

    public NotificationService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    // 🚨 Alert SMS
    public void sendAlertSms(Worker worker, Alert alert) {
        String sms = "🚨 ALERT!\nWorker: " + worker.getName() +
                "\nHelmet: " + worker.getHelmetId() +
                "\nMessage: " + alert.getMessage() +
                "\nLocation: " + alert.getLat() + "," + alert.getLng();

        sendSms(worker.getFamilyPhoneNumber(), sms);
    }
    // 🚨 Alert SMS to worker (not family)
    public void sendAlertToWorker(Worker worker, Alert alert) {
        String sms = "🚨 ALERT!\nWorker: " + worker.getName() +
            "\nHelmet: " + worker.getHelmetId() +
            "\nMessage: " + alert.getMessage() +
            "\nLocation: " + alert.getLat() + "," + alert.getLng();

        sendSms(worker.getPhoneNumber(), sms); // 👈 send to worker’s phone
    }


    // ✅ Safe SMS
    public void sendSafeSms(Worker worker, Alert alert) {
        String sms = "✅ SAFE\nWorker: " + worker.getName() +
                "\nHelmet: " + worker.getHelmetId() +
                "\nAcknowledged at: " + alert.getAcknowledgedAt();

        sendSms(worker.getPhoneNumber(), sms);
    }

    // Core method to send SMS
    private void sendSms(String toPhone, String body) {
        if(toPhone != null && !toPhone.startsWith("+")) {
            toPhone = "+91" + toPhone; // only add +91 if it doesn't start with +
        }
        Message.creator(
                new com.twilio.type.PhoneNumber(toPhone),   // Destination number
                new com.twilio.type.PhoneNumber(twilioConfig.getTrialNumber()), // From Twilio number
                body
        ).create();
    }
    public void sendFamilySms(Worker worker, Alert alert) {
        String familyNumber = worker.getFamilyPhoneNumber();
        if(familyNumber != null && !familyNumber.startsWith("+")) {
            familyNumber = "+91" + familyNumber; // only add +91 if it doesn't start with +
        }
        if(familyNumber != null && !familyNumber.isEmpty()) {
            String message = "✅ Worker " + worker.getName() + " (Helmet: " + worker.getHelmetId() + ") is SAFE now.";
            sendSms(familyNumber, message);
        }
    }

}
