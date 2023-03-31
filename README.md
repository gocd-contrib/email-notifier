[GoCD](https://www.gocd.org) plugin to send email notifications.

*Usage:*

## Installation

* Place the jar in `<go-server-location>/plugins/external` & restart the GoCD Server.

## Configuration

You will see `Email Notification plugin` on the plugin listing page

![Plugins listing page][1]

You will need to configure the plugin:

![Configure plugin pop-up][2]

Please note that
- Enabling "TLS?" enables TLS at transport level. Nothing is exchanged over plaintext and your SMTP server needs to support that.
- If you have a need for "upgrading" to TLS via the `STARTTLS` SMTP protocol command, you will need to enable the Java system property `-Dmail.smtp.starttls.enable=true` on your GoCD server, similar to GoCD's overall server settings documented [here](https://docs.gocd.org/current/configuration/admin_mailhost_info.html#smtps-and-tls). Usually this is associated with port `587`.

When the stage status changes, you get an email:

![Successful Notification][3]

## To customize notifications

* Clone the project

* Customize when & whom to send emails for different events (Stage - Scheduled, Passed, Failed, Cancelled)

* Run `./gradlew clean assemble` which will create plugin jar in 'build/libs' folder

[1]: images/list-plugin.png  "List Plugin"
[2]: images/configure-plugin.png  "Configure Plugin"
[3]: images/successful-notification.png  "Successful Notification"

## Contributing

We encourage you to contribute to GoCD. For information on contributing to this project, please see our [contributor's guide](https://www.gocd.org/contribute/).
A lot of useful information like links to user documentation, design documentation, mailing lists etc. can be found in the [resources](https://www.gocd.org/community/resources.html) section.

## License

```plain
Copyright 2019 ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
