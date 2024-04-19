package server.manager;

import server.logic.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Classe gerant les Message entrants à partir de sa {@link SocketChannel}.
 */
class ConnectionManagerImpNio extends ConnectionManager {

    /**
     * Socket cliente.
     */
    private final SocketChannel socket;

    /**
     * Constructeur.
     *
     * @param client Socket cliente.
     * @param server Serveur a l'origine du manager.
     */
    ConnectionManagerImpNio(Socket client, Server server) {
        super(client.getInetAddress().getHostAddress() + ":" + client.getPort(), server);
        if(!server.useNio() || client.getChannel() == null) throw new IllegalArgumentException();
        socket = client.getChannel();
    }

    @Override
    public void sendObject(Object obj) {
        if(!(obj instanceof ByteBuffer buf)) throw new IllegalArgumentException();
        try {
            socket.write(buf.flip());
        } catch (IOException e) {
            stopConnection();
        }
    }

    @Override
    public boolean broadcastReception() {
        try {
            ByteBuffer buf = ByteBuffer.allocate(5000);
            if(socket.read(buf) != -1) {
                System.out.println("TCP Server: " + getName() + " has sent a message.");
                server.broadcast(buf);
                return true;
            }
        } catch (IOException ignored) {}
        stopConnection();
        return false;
    }

    @Override
    public void stopConnection() {
        server.disconnect(socket,true);
    }

}
