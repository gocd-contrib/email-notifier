[GoCD](https://www.gocd.org) plugin to send email notifications.

*Usage:*

## Installation

* Place the jar in `<go-server-location>/plugins/external` & restart the GoCD Server.

## Configuration

- You will see `Email Notification plugin` on the plugin listing page

![Plugins listing page][1]

- You will need to configure the plugin:

![Configure plugin pop-up][2]

- When the stage status changes, you get an email:

![Successful Notification][3]

## To customize notifications

* Clone the project

* Customize when & whom to send emails for different events (Stage - Scheduled, Passed, Failed, Cancelled)

* Run `./gradlew clean assemble` which will create plugin jar in 'build/libs' folder

[1]: images/list-plugin.png  "List Plugin"
[2]: images/configure-plugin.png  "Configure Plugin"
[3]: images/successful-notification.png  "Successful Notification"

## Configuring the plugin for GoCD on Kubernetes using Helm

### Adding the plugin
- In order to add this plugin, you have to use a local values.yaml file that will override the default [values.yaml](https://github.com/helm/charts/blob/master/stable/gocd/values.yaml) present in the official GoCD helm chart repo.
- Add the .jar file link from the releases section to the `env.extraEnvVars` section as a new environment variable.
- The environment variable name must have `GOCD_PLUGIN_INSTALL` prefixed to it.
- Example

```
env:
  extraEnvVars:
    - name: GOCD_PLUGIN_INSTALL_email-notifier
      value: https://github.com/gocd-contrib/email-notifier/releases/download/v0.3-68-exp/email-notifier-0.3-68.jar
```
- Make sure to add the link of the release you want to use.

- Then applying the local values.yaml that has these values added to it will result in a new Go Server pod being created that has the plugin installed and running.


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
