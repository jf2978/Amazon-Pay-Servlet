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

- Amazon Pay Account (with valid credentials)
- SSL Certificate (optional for Sandbox)
- [Amazon Pay Java SDK](https://github.com/amzn/amazon-pay-sdk-java)
- [Jetty](http://www.eclipse.org/jetty/download.html)

[//]: # (## Flow Diagram)

## Deployment

Some things to remember to change once moving onto Production

1. Change the script source in button.html to render the button w/o the sandbox watermark and enable access to real Amazon accounts (just remove '/sandbox')
2. In [AmazonPayAPI.java](https://github.com/jf2978/amazon-pay-servlet/blob/master/FinalConvessaServlet/src/amazonPay/AmazonPayAPI.java), the SDK Client object should not include the ".withSandboxMode(true)" parameter

## Built With
* [Eclipse](https://www.eclipse.org/downloads/) - Java IDE for embedded Jetty
* [Maven](https://maven.apache.org/) - Dependency Management

## Authors

* **Jeffrey Fabian** - [jf2978](https://github.com/jf2978)
* **Dan McCafferty** - _SSL Cert Configuration_ - [Convessa](convessa.com) Co-Founder & CTO
