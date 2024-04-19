package server.manager;

import server.logic.Server;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

/**
 * Classe abstraite representant un ConnectionManager.
 */
public abstract class ConnectionManager {

    /**
     * Serveur a l'origine du manager.
     */
    final Server server;

    /**
     * Nom client.
     */
    final String name;

    /**
     * Constructeur abstrait d'un COnnectionManager
     * @param name Nom du client.
     * @param server Serveur a l'origine du manager.
     */
    ConnectionManager(String name, Server server) {
        this.name = name;
        this.server = server;
    }

    /**
     *
     * @param client Client.
     * @param server Serveur.
     * @return ConnectionManager correspondant.
     * @throws IllegalArgumentException Si les arguments d'appels sont invalides.
     * @throws IOException Si un probleme a lieu lors de l'instantiation du manager.
     */
    public static ConnectionManager invoke(Socket client, Server server) throws IOException {
        if(client.getChannel() == null && !server.useNio()) return new ConnectionManagerImp(client, server);
        if(client.getChannel() != null && server.useNio()) return new ConnectionManagerImpNio(client, server);
        throw new IllegalArgumentException();
    }

    /**
     * Renvoie le nom du client.
     * @return Nom du client.
     */
    public String getName() {
        return name;
    }

    /**
     * Envoie l'object au client.
     * @param obj Object a envoyé.
     */
    abstract public void sendObject(Object obj);

    /**
     * Broadcast l'objet reçu.
     */
    abstract public boolean broadcastReception();

    /**
     * Stop le manager.
     */
    abstract public void stopConnection();

}
