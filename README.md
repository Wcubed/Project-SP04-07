This an implementation of the game Spectrangle.

It was built during the programming project of the the SoftwareSystems course of the University of Twente.

# Overview

The game consists of a server and a client. Clients can connect to a server to play a game of Spectrangle.
A server can handle multiple games at once.

The client and server communicate over port 4000, according to the protocol specified in `Network protocol v1_4.pdf`.

Pre-compiled jars of both client and server can be found in the `artifacts` folder.
They can be started using `java -jar Client.jar` or `java -jar Server.jar`.

# Compiling

All the source files are included in the folder `src/main`.

The main class for the client is `ss.spec.client.Client` and the main class for the server is `ss.spec.server.Server`.
The build manifests can be found in `src/main/client.META-INF` and `src/main/server.META-INF` respectively.

The necessary libraries are included in the `lib` folder.

The JavaDoc is included in the `doc` folder.

# Testing

All unit tests are located under `src/test` and are implemented using JUnit 5.

They can be run either individually or all at once using your IDE of choice.