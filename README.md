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