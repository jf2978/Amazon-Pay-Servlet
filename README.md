# Amazon Pay Servlet

As a part of my internship, I was matched with [Convessa](http://convessa.com/) - an enterprise-grade voice platform that enables the development of extensive, robust and scalable voice applications. One of my assigned tasks was to research, evaluate and implement a payment service and checkout flow for the company's showcase voice app [Mastermind](http://mastermindbot.com). This project creates this flow (front-end & back-end) according to Amazon Payment's Express Integration (hosted payment).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Files
1. **button.html**

HTML containing a simple webpage that holds the JavaScript code for the custom Amazon Pay button - uses jQuery to hit the ButtonServlet to retrieve JSON parameters

2. **ButtonServlet.java**

Servlet used to send JSON parameters to button.html

3. **AmazonParameters.java**

Provides Amazon credentials + parameters

4. **AmazonSignature.java**

Calculates + encrypts signature with secret key to verify requests

5. **AmazonPayAPI.java**

Configures + wraps the Amazon Pay SDK for use with the Mastermind account

6. **ChaChing.java**

Servlet used to handle the return URL (GET) and Amazon notifications (POST)

7. **PaymentProcessor.java**

Object class used to process results from both the GET and POST

8. **MMPaymentServer.java**
Main method that instantiates a SSL Jetty Server with the servlets intact

### Prerequisites

Even though this integration was tested locally (Amazon Pay Sandbox Mode), there are a few things that have to be set up in order to effectively test each aspect of the checkout flow.

### Installing

A step by step series of examples that tell you have to get a development env running

Say what the step will be

```
Give the example
```

And repeat

```
until finished
```

End with an example of getting some data out of the system or using it for a little demo

## Running the tests

Explain how to run the automated tests for this system

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on a live system

## Built With

* [Dropwizard](http://www.dropwizard.io/1.0.2/docs/) - The web framework used
* [Maven](https://maven.apache.org/) - Dependency Management
* [ROME](https://rometools.github.io/rome/) - Used to generate RSS Feeds

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Billie Thompson** - *Initial work* - [PurpleBooth](https://github.com/PurpleBooth)

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Hat tip to anyone who's code was used
* Inspiration
* etc
