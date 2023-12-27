# Wordle 🔡

Guess the Wordle in 12 tries. Each guess must be a valid 10-letter word. The color of the tiles will change to show how close your guess was to the word.
Mine version of wordle is a TUI game that was written & presented as the network course project.

## Overview
The main challenge of the project was the choice of server implementation and its structural model.
I chose to implement the server using both non-blocking I/O, exploiting the selector, and multi-threaded approach, exploiting a pool of thread Workers that acted as dispatchers of requests from the various clients. In fact, the thread pool processed individual events from the main event loop in the ServerController, thus eliminating the problem of CPU and I/O bounded tasks by making the best use of the multi-core CPU architecture of today's machines.

### There are multiple advantages of this implementation, such as:

- No threads will ever be blocked as long as there are events ( Requests ) to process
- Low memory utilisation
- Low context switching overhead
- Multi-core CPU utilisation
  
### But with such a model, other issues arise such as:
- Main event loop can become a server bottleneck
- Responses to clients may be delayed in time (because the Worker thread has not finished its execution)
- A minimum of synchronisation is required to access and use data structures

## Main data structures and classes
#### **ConnectedClients**:

A `HashMap<SocketChannel, Integer>` that mapped connected SocketChannels to their unique ClientID relative to the current session. It did not require any special synchronisation because it is only used by the ServerController to retrieve IDs that would soon be used by the Workers to retrieve the User associated with the connected socket

#### **LoggedUsers**:

A `ConcurrentHashMap<Integer, User>` used by the Workers to retrieve and save the state of the user, in the form of the User class object, and execute the various functions

#### **WordSession**

A `WordManager` class object that handled the random retrieval of the next Secret Word and loaded the entire word dictionary into memory.
I chose to load the dictionary into two data structures:
- A List<String> wordsCollection from which I fished the next secret word, exploiting a pseudo-random stream of integers, in O(1) time because access is directly to the next index
- A HashSet<String> wordsDict which was used to check whether the Guess Word proposed by a user exists in the dictionary. This search, thanks to the mapping of the set by exploiting hashing, also took place in O(1)

Such an implementation places a burden on the use of the server's heap memory (we are talking about 4MB of memory given the 30,000 words), but it certainly facilitates and speeds up access to the words in the dictionary and above all eliminates the many continuous I/O operations on the words.txt file.

#### **ResourceController**
The class through which JSON files are managed, written and updated. The JSON files in question are the serialised `User` objects, via TypeAdapters, which allow information, statistics and the game status of each user to be saved.

#### **ShareWatcher**
Rappresented by a thread, on a client-side, responsible for creating the multicast socket and connecting to the multicast group and port specified in the client.config file
It uses a `List<String> sharedList` to store all user game shares coming from the server. This list is accessed, from the client class, using the `printShare()` method. I therefore made the list thread-safe by exploiting `Collections.synchronisedList()`. The run loop instead listens and receives **Datagram** packets from the server

#### **ServerController**
The main server class, where the main event loop takes place, using the selector to retrieve the selected-key set ready for writing, reading and accepting

## References
- [Performance and scalability analysis of Java IO and NIO based server models, their implementation and comparison](https://s3-eu-central-1.amazonaws.com/ucu.edu.ua/wp-content/uploads/sites/8/2019/12/Petro-Karabyn.pdf "Performance and scalability analysis of Java IO and NIO based server models, their implementation and comparison")

## Usage
To run Server/Client you can use **java cli tools**, run one of **shell** scripts in the `/scripts` folder, or create and execute .jar archive.
### Wordle Server

#### Scripts
To start server compile all files and run with
```
./scripts/compile.sh
./scripts/runServer.sh
```
#### JAR
To start server create .jar and run with
```
jar cvfm wordleServer.jar SERVER.MF -C bin/ wordle/server/ -C bin/ wordle/utils/
java -jar wordleServer.jar
```

### Wordle Client
#### Scripts
To start client compile all files and run with
```
./scripts/compile.sh
./scripts/runClient.sh
```
#### JAR
To start server create .jar and run with
```
jar cvfm wordleClient.jar CLIENT.MF -C bin/ wordle/client/ -C bin/ wordle/utils/
java -jar wordleClient.jar
```

