package server.manager;

import server.logic.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Thread gerant un client a partir de sa {@link Socket}.
 */
class ConnectionManagerImp extends ConnectionManager implements Runnable {

    /**
     * Socket cliente.
     */
    private final Socket socket;

    /**
     * ObjectInputStream client.
     */
    private final ObjectInputStream in;

    /**
     * ObjectOutputStream client.
     */
    private final ObjectOutputStream out;

    /**
     * Statut de travail du thread.
     */
    boolean working = true;

    /**
     * Thread de gestion.
     */
    private final Thread t;

    /**
     * Constructeur.
     *
     * @param client Socket cliente.
     * @param server Serveur a l'origine du manager.
     * @throws IOException Si erreur lors de l'instantiation du Thread.
     */
    ConnectionManagerImp(Socket client, Server server) throws IOException {
        super(client.getInetAddress().getHostAddress() + ":" + client.getPort(), server);
        if(client.getChannel() != null || server.useNio()) throw new IllegalArgumentException();
        socket = client;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        t = new Thread(this);
        t.start();
    }

    @Override
    public void sendObject(Object obj) {
        try {
            out.writeObject(obj);
        } catch (IOException e) {
            server.disconnect(this, true);
        }
    }

    @Override
    public boolean broadcastReception() {
        try {
            server.broadcast(in.readObject());
            return true;
        } catch (IOException|ClassNotFoundException e) {
            server.disconnect(this, true);
            return false;
        }
    }

    @Override
    public void stopConnection() {
        working = false;
        try {
            socket.close();
        } catch (IOException ignored) {}
        t.interrupt();
    }

    @Override
    public void run() {
        System.out.println("TCP Server: " + getName() + " has joined the chat !");
        while(working) {
            if(broadcastReception())
                System.out.println("TCP Server: " + getName() + " has sent a message.");
        }
        System.out.println("TCP Server: " + getName() + " has left the chat !");
    }

}
