// src/main/java/com/eventra/service/NotificationService.java
package com.eventra.service;

import com.eventra.domain.event.Event;
import com.eventra.domain.guest.Guest;
import com.eventra.domain.user.User;
import com.eventra.integration.resend.ResendClient;
import com.eventra.integration.twilio.TwilioClient;
import com.eventra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final TwilioClient twilioClient;
    private final ResendClient resendClient;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

    public void sendInvite(Guest guest, Event event, String inviteLink, String qrCodeUrl) {
        String message = buildInviteMessage(guest, event, inviteLink);

        // Send WhatsApp with QR code media
        try {
            twilioClient.sendWhatsApp(guest.getPhone(), message, qrCodeUrl);
        } catch (Exception e) {
            log.warn("WhatsApp failed for {}: {}", guest.getPhone(), e.getMessage());
        }

        // Send Email
        if (guest.getEmail() != null) {
            try {
                String subject = "You're invited to " + event.getName();
                String html = buildInviteHtml(guest, event, inviteLink, qrCodeUrl);
                resendClient.sendEmail(guest.getEmail(), subject, message, html);
            } catch (Exception e) {
                log.warn("Email failed for {}: {}", guest.getEmail(), e.getMessage());
            }
        }
    }

    public void sendWelcomeMessage(Guest guest, Event event) {
        String message = buildWelcomeMessage(guest, event);
        try {
            twilioClient.sendWhatsApp(guest.getPhone(), message);
        } catch (Exception e) {
            log.warn("Welcome message failed: {}", e.getMessage());
        }
    }

    public void sendReminder(Guest guest, Event event, String reminderType) {
        String message = buildReminderMessage(guest, event, reminderType);
        try {
            twilioClient.sendWhatsApp(guest.getPhone(), message);
        } catch (Exception e) {
            log.warn("Reminder failed: {}", e.getMessage());
        }
    }

    public void sendBeneficiaryInvitation(UUID eventId, String email, String phone, String name) {
        String inviteLink = "https://eventra.ng/claim/" + eventId;
        String message = String.format("""
            🎉 You've been invited to claim your event on Eventra! 🎉
            
            Dear %s,
            
            An event has been created for you. Click the link below to claim your gift vault and start receiving contributions:
            
            %s
            
            This invitation expires in 7 days.
            
            Welcome to Eventra!
            """, name, inviteLink);

        if (phone != null) {
            try {
                twilioClient.sendWhatsApp(phone, message);
            } catch (Exception e) {
                log.warn("WhatsApp failed for beneficiary: {}", e.getMessage());
            }
        }

        if (email != null) {
            try {
                resendClient.sendEmail(email, "Claim Your Event on Eventra", message, message);
            } catch (Exception e) {
                log.warn("Email failed for beneficiary: {}", e.getMessage());
            }
        }
    }

    public void notifyCreatorBeneficiaryClaimed(UUID creatorId, Event event) {
        User creator = userRepository.findById(creatorId).orElseThrow();

        String message = String.format("""
            🎉 Great news! The beneficiary has claimed their event! 🎉
            
            The beneficiary has claimed the event "%s" and can now receive contributions.
            
            You can now manage the event logistics. The couple will handle the finances.
            
            View event: https://eventra.ng/events/%s
            """, event.getName(), event.getId());

        try {
            twilioClient.sendWhatsApp(creator.getPhone(), message);
        } catch (Exception e) {
            log.warn("Notification to creator failed: {}", e.getMessage());
        }
    }

    private String buildInviteMessage(Guest guest, Event event, String inviteLink) {
        return String.format("""
            🎉 YOU'RE INVITED! 🎉
            
            Dear %s,
            
            You're invited to:
            *%s*
            
            📅 Date: %s
            📍 Venue: %s
            👗 Dress Code: %s
            
            ✨ YOUR PERSONAL INVITE LINK ✨
            %s
            
            Click the link to RSVP and get your entry QR code.
            
            We can't wait to celebrate with you! 🎊
            """,
                guest.getName(),
                event.getName(),
                event.getEventDate().format(DATE_FORMATTER),
                event.getVenue(),
                event.getDressCode() != null ? event.getDressCode() : "Formal",
                inviteLink
        );
    }

    private String buildWelcomeMessage(Guest guest, Event event) {
        String vipInfo = "";
        if ("VIP".equalsIgnoreCase(guest.getTier())) {
            vipInfo = "\n\n✨ You have VIP access! ✨\n• VIP Lounge to your left\n• Personal attendant available";
        }

        return String.format("""
            🎉 Welcome %s! 🎉
            
            You're at %s at %s.%s
            
            Enjoy the celebration! 🎊
            """,
                guest.getName(),
                event.getName(),
                event.getVenue(),
                vipInfo
        );
    }

    private String buildReminderMessage(Guest guest, Event event, String reminderType) {
        return switch (reminderType) {
            case "RSVP" -> String.format("""
                ⏰ Reminder: %s is in 7 days!
                
                Have you RSVP'd? Click here to confirm:
                https://eventra.ng/invite/%s
                """, event.getName(), guest.getId());

            case "EVENT" -> String.format("""
                🎉 Tomorrow is the big day!
                
                Event: %s
                Time: %s
                Venue: %s
                
                Don't forget your QR code!
                """, event.getName(), event.getEventDate().format(DATE_FORMATTER), event.getVenue());

            default -> "";
        };
    }

    private String buildInviteHtml(Guest guest, Event event, String inviteLink, String qrCodeUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #f59e0b; color: white; padding: 20px; text-align: center; }
                    .button { display: inline-block; background: #f59e0b; color: white; padding: 12px 24px; text-decoration: none; border-radius: 8px; margin: 20px 0; }
                    .footer { background: #f5f5f5; padding: 20px; text-align: center; font-size: 12px; }
                    img { max-width: 100%; height: auto; margin: 20px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>You're Invited! 🎉</h1>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>You're invited to celebrate <strong>%s</strong>.</p>
                        
                        <h3>Event Details</h3>
                        <p>📅 <strong>Date:</strong> %s<br>
                        📍 <strong>Venue:</strong> %s<br>
                        👗 <strong>Dress Code:</strong> %s</p>
                        
                        <p style="text-align: center;">
                            <a href="%s" class="button">RSVP Now</a>
                        </p>
                        
                        <p>Your QR code is attached below. Save it to your phone for entry.</p>
                        
                        <img src="%s" alt="QR Code" style="max-width: 200px;">
                        
                        <p>We can't wait to celebrate with you! 🎊</p>
                    </div>
                    <div class="footer">
                        <p>© %d Eventra. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                guest.getName(),
                event.getName(),
                event.getEventDate().format(DATE_FORMATTER),
                event.getVenue(),
                event.getDressCode() != null ? event.getDressCode() : "Formal",
                inviteLink,
                qrCodeUrl,
                java.time.Year.now().getValue()
        );
    }
}