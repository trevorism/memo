package com.trevorism.service

import com.trevorism.EmailClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.Email

class ForgotPasswordEmailer {

    private EmailClient emailClient

    ForgotPasswordEmailer(SecureHttpClient secureHttpClient) {
        this.emailClient = new EmailClient(secureHttpClient)
    }

    Email sendForgotPasswordEmail(String emailAddress, String username, String link) {
        Email email = new Email(recipients: [emailAddress], subject: "Memowand: Forgot Password", body: buildBody(username, link))
        emailClient.sendEmail(email)
    }

    private static String buildBody(String username, String link) {
        StringBuilder sb = new StringBuilder()
        sb << "You recently submitted a forgot password request for memowand.com.\n"
        sb << "Your username is: ${username}\n\n"
        sb << "Click here to reset your password: ${link}\n"
        sb << "The link will expire in 1 hour."
        return sb.toString()
    }
}

