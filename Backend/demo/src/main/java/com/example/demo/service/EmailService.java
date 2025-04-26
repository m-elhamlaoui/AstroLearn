package com.example.demo.service;

import com.example.demo.model.User;

public interface EmailService {

    void sendVerificationRequestNotificationToAdmins(User requestingUser);

    void sendVerificationApprovedNotification(User targetUser);

    void sendVerificationRejectedNotification(User targetUser, String reason); // Optional reason
}
