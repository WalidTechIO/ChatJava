package server.logic;

import server.manager.ConnectionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;

/**
 * Serveur implemente avec une {@link ServerSocket}.
 */
class ServerImp extends Server {

    /**
     * Socket RV.
     */
    private final ServerSocket serverSocket;

    /**
     * Set des Thread clients connectes.
     */
    private final Set<ConnectionManager> clients = new HashSet<>();

    /**
     * Constructeur.
     *
     * @param rvSocketport Port de la socket RV.
     * @param mutliCastAddress Adresse du groupe mutlicast.
     * @param upDelay Temps avant fin du serveur.
     * @throws IOException Si probleme lors de l'ouverture du service.
     */
    ServerImp(int rvSocketport, InetSocketAddress mutliCastAddress, int upDelay) throws IOException {
        super(mutliCastAddress, upDelay);
        serverSocket = new ServerSocket(rvSocketport);
        port = serverSocket.getLocalPort(); //Si rvSocketport etait a 0, on aura bien dans port le port associée par le systeme.
    }

    @Override
    public void disconnect(Object obj, boolean rm) {
        if(!(obj instanceof ConnectionManager client)) throw new IllegalArgumentException();
        client.stopConnection();
        if(rm) clients.remove(client);
    }

    @Override
    public synchronized void broadcast(Object obj) {
        for(ConnectionManager c : clients) c.sendObject(obj);
    }

    @Override
    void end() {
        for(ConnectionManager c : clients) disconnect(c, false);
        clients.clear();
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
    }

    @Override
    void main() {
        try {
            clients.add(invoke(serverSocket.accept()));
        } catch(IOException ignored) {}
    }

}