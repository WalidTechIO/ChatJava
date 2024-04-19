SRC_DIR=../src/
OBJ_DIR=../bin/
BUILD_DIR=../build/

SRC_SERVER=$(wildcard $(SRC_DIR)server/**/*.java)
SRC_SERVER+=$(wildcard $(SRC_DIR)server/*.java)
SRC_IO=$(wildcard $(SRC_DIR)io/**/*.java)
SRC_CLIENT=$(wildcard $(SRC_DIR)client/**/*.java)
SRC_CLIENT+=$(wildcard $(SRC_DIR)client/*.java)

OBJSRV=server/ io/
OBJCLNT=client io

EXE_CLNT=client.jar
EXE_SRV=server.jar

SRV_MAIN=server.MainServer
CLNT_MAIN=client.MainClient

JAR_CMD=jar
COMPILER=javac

CLNT_FLAGS=--module-path ./javafx-linux/lib --add-modules javafx.controls,javafx.fxml

.PHONY: clean

all: $(EXE_CLNT) $(EXE_SRV)

clientclass: $(SRC_CLIENT) $(SRC_IO)
	$(COMPILER) $(CLNT_FLAGS) -d $(OBJ_DIR) $^
	cp $(SRC_DIR)client/ui/chat.fxml $(OBJ_DIR)client/ui/

serverclass: $(SRC_SERVER) $(SRC_IO)
	$(COMPILER) -d $(OBJ_DIR) $^

$(EXE_SRV): serverclass
	cd ../bin && $(JAR_CMD) --create --file=$(BUILD_DIR)$@ -e $(SRV_MAIN) $(OBJSRV)

$(EXE_CLNT): clientclass
	cd ../bin && $(JAR_CMD) --create --file=$(BUILD_DIR)$@ -e $(CLNT_MAIN) $(OBJCLNT)

clean:
	rm -rf $(EXE_CLNT) $(EXE_SRV) $(OBJ_DIR)*