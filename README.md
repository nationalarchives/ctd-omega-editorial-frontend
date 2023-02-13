# Pan-Archival Editorial Frontend Catalogue

[![Build Status](https://github.com/nationalarchives/ctd-omega-editorial-frontend/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/nationalarchives/ctd-omega-editorial-frontend/actions/workflows/ci.yml)
[![Scala 2.13](https://img.shields.io/badge/scala-2.13-red.svg)](http://scala-lang.org)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://opensource.org/licenses/MIT)

This project is an editorial frontend for managing The National Archives Pan-Archival Catalogue (PAC).

The project is developed in Scala Play framework with [GOV.UK]( https://design-system.service.gov.uk/) Design System toolkit using Twirl implementation of the design system provided by [HMRC](https://github.com/hmrc/play-frontend-hmrc) for building the frontend web application.

## Building from Source Code

### Pre-requisites for building and running the project:
* [Git](https://git-scm.com)
* [sbt](https://www.scala-sbt.org/) >= 1.6.2
* [Scala](https://www.scala-lang.org/) >= 2.13.8
* [Java JDK](https://adoptopenjdk.net/) >= 1.8

## Steps:

1. Clone the Git repository:
```
git clone https://github.com/nationalarchives/ctd-omega-editorial-frontend.git
```
2. Switch to the newly created directory:
```
cd ./ctd-omega-editorial-frontend
```
3. To build the project run the following command:
```
sbt clean compile
```
4. Now run the application
```
sbt run
```
5. Go to a web browser and type the following url to launch the application
```
http://localhost:9000/login
```
6. Enter a valid username and password to access the application

## Publishing a Release to Maven Central

1. Run `sbt clean release`
2. Answer the questions
3. Login to https://oss.sonatype.org/ then Close, and Release the Staging Repository
   25 

## Creating a deployment and running it as a Linux service

1. To create a deployable zip package of the application run:
```
sbt dist
```
2. Copy the zip file create in #1 to the deployment directory, e.g.
```
sudo unzip ./target/universal/ctd-omega-editorial-frontend-0.1.0-SNAPSHOT.zip -d /opt
```
3. Create a ctd-web user which is specifically used to run the service:
```
sudo adduser --system --shell /sbin/nologin ctd-web
```
4. Change the owner of the deployment directory to be the newly created ctd-web user:
```
sudo chown -R ctd-web:ctd-web /opt/ctd-omega-editorial-frontend-0.1.0-SNAPSHOT
```
5. It is recommended to create a symbolic link to the current version as follows:
```
sudo ln -s /opt/ctd-omega-editorial-frontend-0.1.0-SNAPSHOT /opt/ctd-omega-editorial-frontend
```
6. Edit the Logback configuration at `/opt/ctd-omega-editorial-frontend/conf/logback.xml` and replace the line mentioning `application.home` as shown below. This is necessary due to a bug using `application.home` with `sbt dist` which is described [here](https://github.com/playframework/playframework/issues/8759)
```
<!-- <file>${application.home:-.}/logs/application.log</file> -->
<file>/opt/ctd-omega-editorial-frontend/logs/application.log</file>
```
7. Create a systemd config file as follows, replacing the text `some_application_secret` with a specially generated secret (as described [here](https://www.playframework.com/documentation/2.8.x/ApplicationSecret#Generating-an-application-secret)) and `/path/to/java-1.8.0` with the Java 8 installation directory (e.g. `/usr/lib/jvm/java-1.8.0-amazon-corretto.x86_64`) 
```
[Unit]
Description=CTD Omega Editorial Frontend
Documentation=https://github.com/nationalarchives/ctd-omega-editorial-frontend
After=syslog.target

[Service]
Type=simple
User=ctd-web
Group=ctd-web
Environment="JAVA_HOME=/path/to/java-1.8.0"
Environment="APPLICATION_SECRET=some_application_secret"
ExecStart=/opt/ctd-omega-editorial-frontend/bin/ctd-omega-editorial-frontend
Restart=always

[Install]
WantedBy=multi-user.target
```
7. Save the config file as `ctd-omega-editorial-frontend.service` in the `/etc/systemd/system` directory and ensure that the file is executable by the `ctd-web` user, e.g.
```
sudo chmod 755 /etc/systemd/system/ctd-omega-editorial-frontend.service
```
8. Reload the systemd manager configuration:
```
sudo systemctl daemon-reload
```
9. Enable the `ctd-omega-editorial-frontend` service:
```
sudo systemctl enable ctd-omega-editorial-frontend
```
10. Start the `ctd-omega-editorial-frontend` service:
```
sudo systemctl start ctd-omega-editorial-frontend
```
11. Check the application is running by going to http://localhost:9000. You can also check the status using:
```
sudo systemctl status ctd-omega-editorial-frontend
```
12. To view the service log you can use:
```
sudo journalctl -e -u ctd-omega-editorial-frontend
```
13. To check that the application secret and other environment variables are correctly set you can use:
```
sudo strings /proc/<PID>/environ
```
14. To stop the service you can use:
```
sudo systemctl stop ctd-omega-editorial-frontend
```

## Dev

### Running Tests

To run the unit tests only:

```
sbt test
```

To run just the integration tests:

```
sbt IntegrationTest/test
```

And to run all:

```
sbt test IntegrationTest/test
```

Note that we run [sbt-coverage](https://github.com/scoverage/sbt-scoverage) automatically for both unit and integration tests.

However, in order to generate an html report, run:

``` 
sbt coverageReport
```

You'll find it in `/target/scala-2.13/scoverage-report/`.
 
### Before Pushing

Remember to run `sbt fmtCheck`.

For convenience, you can run [runBeforePushing.sh](./runBeforePushing.sh), which runs it after all of the tests; note that a coverage report is also generated.

## JMS EchoServer

This is just a proof-of-concept of how we can embed the `EchoServer` from [jms4s-request-reply-stub](https://github.com/nationalarchives/jms4s-request-reply-stub).

Before starting the application, you'll need to boot the [ElasticMQ](https://github.com/softwaremill/elasticmq) container.

```
docker-compose up -d
```

When you start this application (with `sbt run`, for example), it will automatically listen for JMS messages on the `request-general` queue;
whenever you send a message to that queue, an echo message will be posted to the response queue, called `omega-editorial-web-application-instance-1`.

For example:

```
curl "http://localhost:9324/request-general?Action=SendMessage&MessageBody=HelloWorld"
```

``` 
<SendMessageResponse xmlns="http://queue.amazonaws.com/doc/2012-11-05/">
  <SendMessageResult>
    <MD5OfMessageBody>68e109f0f40ca72a15e05cc22786f8e6</MD5OfMessageBody>
    <MessageId>e33514d3-e126-4f5a-b15c-157e2e3edbfb</MessageId>
  </SendMessageResult>
  <ResponseMetadata>
    <RequestId>00000000-0000-0000-0000-000000000000</RequestId>
  </ResponseMetadata>
</SendMessageResponse>
```

You can check the [Elastic MQ] console at `http://localhost:9325/` where you should see that there's one more message in `omega-editorial-web-application-instance-1`.

In order to retrieve that message, run:

``` 
curl "http://localhost:9324/omega-editorial-web-application-instance-1?Action=ReceiveMessage"
```

``` 
<ReceiveMessageResponse xmlns="http://queue.amazonaws.com/doc/2012-11-05/">
  <ReceiveMessageResult>
    <Message>
      <MessageId>93d62818-d34f-4088-8fd9-a2de5377df8a</MessageId>
      <ReceiptHandle>93d62818-d34f-4088-8fd9-a2de5377df8a#7b4f7f8e-6bc1-46a6-b6ca-dfcd0ad946f9</ReceiptHandle>
      <MD5OfBody>edd5290abc2df3bf6d034867b69369c2</MD5OfBody>
      <Body>Echo Server: HelloWorld</Body>
    </Message>
  </ReceiveMessageResult>
  <ResponseMetadata>
    <RequestId>00000000-0000-0000-0000-000000000000</RequestId>
  </ResponseMetadata>
</ReceiveMessageResponse>
```