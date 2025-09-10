package com.smarthelmet.helmet_backend.service;

import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final TwilioConfig twilioConfig;
    private final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
    }

    // Send alert SMS to family AND return the text (so controller can also broadcast to coworkers)
    public void sendAlertSmsToFamily(Worker worker, Alert alert) {
        String familyPhone = worker.getFamilyPhoneNumber();
        String sms = "🚨 ALERT!\nWorker: " + worker.getName() +
                "\nHelmet: " + worker.getHelmetId() +
                "\nMessage: " + alert.getMessage() +
                "\nLocation: " + alert.getLat() + "," + alert.getLng() +
                "\nAlertId: " + (alert.getId() != null ? alert.getId() : "n/a");

        sendSmsSafely(familyPhone, sms);
    }

    // Notify a single worker (co-worker) that someone is in alert (used to notify all coworkers)
    public void sendAlertSmsToCoworker(Worker coworker, Alert alert) {
        String sms = "⚠️ ALERT: Worker " + alert.getHelmetId() +
                " triggered an alert.\nMessage: " + alert.getMessage() +
                "\nLocation: " + alert.getLat() + "," + alert.getLng() +
                "\nAlertId: " + (alert.getId() != null ? alert.getId() : "n/a");
        sendSmsSafely(coworker.getPhoneNumber(), sms);
    }

    // Notify co-worker that worker is SAFE (used after acknowledgment)
    public void sendSafeSms(Worker worker, Alert alert) {
        String sms = "✅ SAFE\nWorker: " + worker.getName() +
                "\nHelmet: " + worker.getHelmetId() +
                "\nAcknowledged at: " + (alert.getAcknowledgedAt() != null ? alert.getAcknowledgedAt().toString() : "n/a");
        sendSmsSafely(worker.getPhoneNumber(), sms);
    }

    // Notify family that worker is safe
    public void sendFamilySms(Worker worker, Alert alert) {
        String familyNumber = worker.getFamilyPhoneNumber();
        String message = "✅ Worker " + worker.getName() + " (Helmet: " + worker.getHelmetId() + ") is SAFE now.";
        sendSmsSafely(familyNumber, message);
    }

    // centralised safe sender that handles nulls and logs Twilio API errors
    private void sendSmsSafely(String toPhone, String body) {
        if (toPhone == null || toPhone.trim().isEmpty()) {
            logger.warn("Attempt to send SMS but destination phone is null/empty. message: {}", body);
            return;
        }

        String normalized = toPhone.trim();
        if (!normalized.startsWith("+")) {
            // Prepend +91 only if number seems local; you can make country dynamic later
            normalized = "+91" + normalized;
        }

        try {
            Message.creator(
                    new com.twilio.type.PhoneNumber(normalized),
                    new com.twilio.type.PhoneNumber(twilioConfig.getTrialNumber()),
                    body
            ).create();
            logger.info("SMS queued to {}.", normalized);
        } catch (ApiException e) {
            logger.error("Twilio ApiException when sending SMS to {}: {} - {}", normalized, e.getCode(), e.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected exception while sending SMS to {}: {}", normalized, ex.getMessage());
        }
    }
}
