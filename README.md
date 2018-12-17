[GoCD](https://www.gocd.org) plugin to send email notifications.

*Usage:*

## Installation

* Place the jar in `<go-server-location>/plugins/external` & restart the GoCD Server.

## Configuration

- You will see `Email Notification plugin` on the plugin listing page
![Plugins listing page][1]

- You will need to configure the plugin:
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

## Contributing

We encourage you to contribute to GoCD. For information on contributing to this project, please see our [contributor's guide](http://www.go.cd/contribute).
A lot of useful information like links to user documentation, design documentation, mailing lists etc. can be found in the [resources](http://www.go.cd/community/resources.html) section.

## License

```plain
Copyright 2018 ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
