# wordle

Guess the Wordle in 12 tries. Each guess must be a valid 10-letter word. The color of the tiles will change to show how close your guess was to the word.

## Usage
To run Server/Client you can use **java cli tools**, run one of **shell** scripts in the `/scripts` folder, or create and execute .jar archive.
### Wordle Server
#### CLI
To start server compile all files and run with
```
javac -g -cp "lib/*" -d bin/ src/wordle/*/*.java
java -cp "lib/*":bin/ wordle.server.WordleServerMain
```
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
#### CLI
To start client compile all files and run with
```
javac -g -cp "lib/*" -d bin/ src/wordle/*/*.java
java -cp "lib/*":bin/ wordle.client.WordleClientMain
```
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

