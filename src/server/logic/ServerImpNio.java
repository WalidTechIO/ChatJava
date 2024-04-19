package server.logic;

import server.manager.ConnectionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Serveur implemente avec une {@link ServerSocketChannel} et un {@link Selector}.
 */
class ServerImpNio extends Server {

    /**
     * Selecteur du serveur.
     */
    private final Selector sel;

    /**
     * Socket RV.
     */
    private final ServerSocketChannel serverSocket;

    /**
     * Map des clients connectes et de leur manager.
     */
    private final Map<SocketChannel, ConnectionManager> clients = new HashMap<>();

    /**
     * Constructeur.
     *
     * @param rvSocketport Port de la socket RV.
     * @param multiCastAddress Adresse du groupe mutlicast.
     * @param upDelay Temps avant fin du serveur.
     * @throws IOException Si probleme lors de l'ouverture du service.
     */
    ServerImpNio(int rvSocketport, InetSocketAddress multiCastAddress, int upDelay) throws IOException {
        super(multiCastAddress, upDelay);
        sel = Selector.open();

        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(rvSocketport));
        serverSocket.configureBlocking(false);
        serverSocket.register(sel, SelectionKey.OP_ACCEPT);

        port = serverSocket.socket().getLocalPort();
    }

    @Override
    public void disconnect(Object obj, boolean rm) {
        if(!(obj instanceof SocketChannel client)) throw new IllegalArgumentException();
        try {
            client.keyFor(sel).cancel();
            System.out.println("TCP Server: " + clients.get(client).getName() + " has left the chat !");
            if(rm) clients.remove(client);
            client.close();
        } catch (IOException ignored) {
            //On ignore car il y a peu de chance que l'exception soit levee et le client sera deconnecte dans tout les cas
        }
    }

    @Override
    public void broadcast(Object obj) {
        if(!(obj instanceof ByteBuffer)) throw new IllegalArgumentException();
        for(ConnectionManager cm : clients.values()) {
            cm.sendObject(obj);
        }
    }

    @Override
    void end() {
        for(SocketChannel c : clients.keySet()) disconnect(c, false);
        clients.clear();
        serverSocket.keyFor(sel).cancel();
        try {
            serverSocket.close();
            sel.close();
        } catch (IOException ignored) {}
    }

    @Override
    void main() {
        try {
            sel.select();
            Iterator<SelectionKey> it = sel.selectedKeys().iterator();

            while(it.hasNext()) {
                SelectionKey key = it.next(); it.remove();

                if(key.isAcceptable()) {
                    //Un client souhaite se connecter.
                    SocketChannel client = serverSocket.accept();
                    try {
                        client.configureBlocking(false);
                        client.register(sel, SelectionKey.OP_READ);
                        clients.put(client, invoke(client.socket()));
                        System.out.println("TCP Server: " + clients.get(client).getName() + " has joined the chat !");
                    } catch (IOException e) {
                        disconnect(client,true);
                    }
                }

                if(key.isReadable()) {
                    //Un client a envoyé un message.
                    ConnectionManager cm = clients.get((SocketChannel) key.channel());
                    cm.broadcastReception();
                }

            }
        } catch(Exception ignored) {
            //Survenue au select ou sur l'accept, on reesaye
        }
    }
}
