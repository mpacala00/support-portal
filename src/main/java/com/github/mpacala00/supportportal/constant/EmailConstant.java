package com.github.mpacala00.supportportal.constant;

public class EmailConstant {

    //gmail credentials
    //remember to enable less secure apps in your gmail acc: https://hotter.io/docs/email-accounts/secure-app-gmail/
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GMAIL_SMTP_SERVER = "smtp.gmail.com";

    //smtp
    public static final String MAIL_TRANSER_PROTOCOL = "smtp";
    public static final String SMTP_HOST = "mail.smtp.host";
    public static final String SMTP_AUTH = "mail.smtp.auth";
    public static final String SMTP_PORT = "mail.smtp.port";

    //tls - transfer layer security
    public static final String SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";
    public static final String SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";

    public static final String FROM_EMAIL = "support@getarrays.com";
    public static final String CC_EMAIL = ""; //carbon copy (do wiadomosci)
    public static final String EMAIL_SUBJECT = "Get Arrays, LLC - new password";
    public static final int DEFAULT_PORT = 465;
}
