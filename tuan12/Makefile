JAVAC = javac
JAVAR = java

CLIENT_SRC = Client/Client.java
SERVER_SRC = Server/Server.java

client: 
	$(JAVAC) $(CLIENT_SRC)
	$(JAVAR) Client/Client

server:
	$(JAVAC) $(SERVER_SRC)
	$(JAVAR) Server/Server

clean:
	rm -f ./Client/*.class
	rm -f ./Server/*.class