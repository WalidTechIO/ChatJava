package client.logic;

import io.data.Message;
import client.ui.ChatController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

/**
 * Implementation d'un client avec une {@link Socket}.
 */
class ClientImp extends Client {

    /**
     * Socket de connexion.
     */
    private final Socket s;

    /**
     * ObjectOutputStream basee sur la socket.
     */
    private final ObjectOutputStream out;

    /**
     * ObjectInputStream basee sur la socket.
     */
    private final ObjectInputStream in;

    /**
     * Constructeur
     * @param address Adresse vers laquelle se connecter.
     * @param controller Controller de l'UI.
     * @throws IOException si il y'a un probleme de connexion.
     */
    ClientImp(InetSocketAddress address, ChatController controller) throws IOException {
        super(controller);
        s = new Socket();
        s.setSoTimeout(500); //Tentative de co classic sur NIO va resulter en IOException alors que sans timeout -> bloquage
        try {
            s.connect(address);
            in = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(s.getOutputStream());
            s.setSoTimeout(0);
            startReceiver();
        } catch (IOException e) {
            //Ceci evitera que le serveur ait un client connecte pour rien si on tente d'acceder en classic sur un serveur NIO
            closeSocket();
            throw e;
        }
    }

    @Override
    void closeSocket() {
        try {
            s.close();
        } catch (IOException ignored) {}
    }

    @Override
    public Message receiveMessage() throws IOException, ClassNotFoundException {
        Object obj = in.readObject();
        if(obj instanceof Message msg) return msg;
        throw new IOException("Receipt incorrect object");
    }

    @Override
    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
        } catch (IOException e) {
            stopConnection(true);
        }
    }

    @Override
    public boolean isConnected() {
        return working || s.isConnected();
    }


}