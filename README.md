## Introduction

Go CD plugin to send Email notifications.
This is a sample plugin.  You may extend it to your own use.  It is **not** a requirement to send email from a default installation of Go CD. 

*Usage:*

## Installation

* Place the jar in `<go-server-location>/plugins/external` & restart Go Server

## Configuration

- You will see `Email Notification plugin` on plugin listing page
![Plugins listing page][1]

- You will need to configure the plugin (this feature requires GoCD version >= v15.2, use system properties to configure the plugin) 
![Configure plugin pop-up][2]

- When the stage status changes...
![Successful Notification][3]

## To customize notifications

* Clone the project
* Customize when & whom to send emails for different events (Stage - Scheduled, Passed, Failed, Cancelled)
* Run `mvn clean package -DskipTests` which will create plugin jar in 'dist' folder

[1]: images/list-plugin.png  "List Plugin"
[2]: images/configure-plugin.png  "Configure Plugin"
[3]: images/successful-notification.png  "Successful Notification"
