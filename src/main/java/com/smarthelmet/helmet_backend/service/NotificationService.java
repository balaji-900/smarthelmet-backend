package com.smarthelmet.helmet_backend.service;

import com.smarthelmet.helmet_backend.model.Alert;
import com.smarthelmet.helmet_backend.model.Worker;
import com.smarthelmet.helmet_backend.config.TwilioConfig;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class NotificationService {

    private final TwilioConfig twilioConfig;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Value("${voice.call.delay-seconds}")
    private int voiceCallDelaySeconds;

    // Store scheduled tasks so we can cancel them on ack
    private final Map<String, ScheduledFuture<?>> scheduledCalls = new ConcurrentHashMap<>();

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

        // Also schedule a voice call to family after delay
        scheduleVoiceCall(worker, alert);
    }

    // 🚨 Alert SMS to worker (not family)
    public void sendAlertToWorker(Worker worker, Alert alert) {
        String sms = "🚨 ALERT!\nWorker: " + worker.getName() +
                "\nHelmet: " + worker.getHelmetId() +
                "\nMessage: " + alert.getMessage() +
                "\nLocation: " + alert.getLat() + "," + alert.getLng();

        sendSms(worker.getPhoneNumber(), sms);
    }

    // ✅ Safe SMS
    public void sendSafeSms(Worker worker, Alert alert) {
        String sms = "✅ SAFE\nWorker: " + worker.getName() +
                "\nHelmet: " + worker.getHelmetId() +
                "\nAcknowledged at: " + alert.getAcknowledgedAt();

        sendSms(worker.getPhoneNumber(), sms);

        // Cancel pending voice call if acked
        cancelScheduledVoiceCall(worker.getHelmetId());
    }

    // ✅ Safe SMS to family
    public void sendFamilySms(Worker worker, Alert alert) {
        String familyNumber = worker.getFamilyPhoneNumber();
        if (familyNumber != null && !familyNumber.isEmpty()) {
            String message = "✅ Worker " + worker.getName() + " (Helmet: " + worker.getHelmetId() + ") is SAFE now.";
            sendSms(familyNumber, message);
        }

        // Cancel pending voice call if acked
        cancelScheduledVoiceCall(worker.getHelmetId());
    }

    // Core method to send SMS
    private void sendSms(String toPhone, String body) {
        if (toPhone != null && !toPhone.startsWith("+")) {
            toPhone = "+91" + toPhone;
        }
        Message.creator(
                new PhoneNumber(toPhone),
                new PhoneNumber(twilioConfig.getTrialNumber()),
                body
        ).create();
    }

    // 🔔 Schedule a voice call to family after delay
    public void scheduleVoiceCall(Worker worker, Alert alert) {
        if (worker.getFamilyPhoneNumber() == null) return;

        Runnable callTask = () -> makeVoiceCall(worker, alert);

        ScheduledFuture<?> future = scheduler.schedule(
                callTask,
                voiceCallDelaySeconds,
                TimeUnit.SECONDS
        );

        // Save so we can cancel later if ack arrives
        scheduledCalls.put(worker.getHelmetId(), future);
    }

    // 🔔 Actually make Twilio voice call
    private void makeVoiceCall(Worker worker, Alert alert) {
        try {
            String familyNumber = worker.getFamilyPhoneNumber();
            if (familyNumber != null && !familyNumber.startsWith("+")) {
                familyNumber = "+91" + familyNumber;
            }

            Call.creator(
                    new PhoneNumber(familyNumber),
                    new PhoneNumber(twilioConfig.getTrialNumber()),
                    new URI("https://helmet-backend-production.up.railway.app/voice/alert")
            ).create();

            System.out.println("📞 Voice call placed to " + familyNumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ❌ Cancel a scheduled voice call if alert is acknowledged
    public void cancelScheduledVoiceCall(String helmetId) {
        ScheduledFuture<?> future = scheduledCalls.remove(helmetId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
            System.out.println("⏹️ Scheduled voice call cancelled for helmetId: " + helmetId);
        }
    }
}
