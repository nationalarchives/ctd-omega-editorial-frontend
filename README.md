# ctd-omega-editorial-frontend

This project is an editorial frontend for managing The National Archives Pan-Archival Catalogue (PAC).

The project is developed in Scala Play framework with govuk-frontend toolkit using  Scala Twirl implementation of govuk-frontend and hmrc-frontend for building the frontend web application.

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