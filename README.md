# Music Streamer v2.0
## _The Local music streamer_

This is a Java-based client-server application that allows you to stream and control music stored on a central server. 
The server manages the music files and provides client services such as track listing, audio data streaming, metadata retrieval, track search, and track rating.
The client portion, built with JavaFX, connects to the server via Java RMI, displays the available music library in a GUI, and provides playback controls such as a Play/Pause button and next and previous buttons. A responsive progress bar is also included, which accurately displays the current playback position and total duration of the selected track. The application ensures a smooth user experience by efficiently handling temporary files, allowing users' music libraries to stream seamlessly.

## Installation

Music Streamer requires Java JDK (can be downloaded here: https://www.oracle.com/ca-en/java/technologies/downloads/)

First Clone This Repo:
```sh
git clone link
```

Next cd into the Server folder on the machine that will be your server and run "build_server.bat" then "run_server.bat":
```sh
cd Directory/Server
java server
build_server.bat
run_server.bat
```

Now on your Client Device cd into the Client folder and run "build_client.bat" then "run_client.bat":
```sh
cd Directory/Client
build_client.bat
run_client.bat
```

GUI should launch, That is all enjoy your music!
