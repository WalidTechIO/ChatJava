package client.logic;

import client.ui.ChatController;
import io.data.Message;
import io.utils.Serialisation;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Implementation d'un client avec une {@link SocketChannel}.
 */
class ClientImpNio extends Client {

    /**
     * Socket de connexion.
     */
    private final SocketChannel s;

    /**
     * Constructeur.
     *
     * @param address Adresse vers laquelle se connecter.
     * @param controller Controller de l'UI.
     * @throws IOException si il y'a un probleme de connexion.
     */
    ClientImpNio(InetSocketAddress address, ChatController controller) throws IOException {
        super(controller);
        s = SocketChannel.open(address);
        s.configureBlocking(true);
        startReceiver();
    }

    @Override
    public void sendMessage(Message msg) {
        try {
            byte[] serializedMsg = Serialisation.serialiser(msg);
            s.write(ByteBuffer.wrap(serializedMsg));
        } catch (IOException e) {
            stopConnection(true);
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
        ByteBuffer buf = ByteBuffer.allocate(5000);
        s.read(buf);
        Object obj = Serialisation.deserialiser(buf.flip().array());
        if(obj instanceof Message msg) return msg;
        throw new IOException("Receipt incorrect object");
    }

    @Override
    public boolean isConnected() {
        return working || s.isConnected();
    }
}
