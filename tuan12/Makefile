JAVAC = javac
JAVAR = java

CLIENT_SRC = Client/Client.java
SERVER_SRC = Server/Server.java

client: 
	$(JAVAC) $(CLIENT_SRC)
	$(JAVAR) -cp Client Client
	rm -f Client/client.class

server:
	$(JAVAC) $(SERVER_SRC)
	$(JAVAR) -cp Server Server
	rm -f Server/server.class

clean:
	rm -f ./Client/*.class
	rm -f ./Server/*.class