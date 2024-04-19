package server.logic;

import io.utils.Serialisation;
import server.manager.ConnectionManager;

import java.io.IOException;
import java.net.*;
import java.util.Objects;

/**
 * Classe abstraite du serveur.
 */
public abstract class Server implements Runnable {

    /**
     * Thread ecoutant le groupe multicast.
     */
    private final MutlicastGroupListener groupListener;

    /**
     * Delai avant coupure du serveur.
     */
    private final int delay;

    /**
     * Statut de travail du Serveur et du groupListener.
     */
    boolean working = true;

    /**
     * Port de la socket RV.
     */
    int port;

    /**
     * Constructeur.
     *
     * @param mutliCastAddress Adresse du groupe mutlicast.
     * @param upDelay Temps avant lequelle le serveur doit se stopper.
     * @throws IOException Si il y'a un probleme lors de l'instantiation du groupListener.
     */
    Server(InetSocketAddress mutliCastAddress, int upDelay) throws IOException {
        delay = upDelay;
        groupListener = new MutlicastGroupListener(mutliCastAddress);
    }

    /**
     * Invoque un Serveur.
     * @param rvSocketport Port Socket RV.
     * @param multiCastAddress Port groupe Multicast de diffusion.
     * @param upDelay Temps apres lequel le serveur doit s'arreter.
     * @param nioMode Si vrai Serveur utilise java NIO.
     * @return Serveur invoque.
     * @throws IOException Si probleme lors de l'ouverture du service.
     */
    public static Server invoke(int rvSocketport, InetSocketAddress multiCastAddress, int upDelay, boolean nioMode) throws IOException {
        return (nioMode ? new ServerImpNio(rvSocketport, multiCastAddress, upDelay) : new ServerImp(rvSocketport,multiCastAddress, upDelay));
    }

    @Override
    public void run() {
        System.out.println("TCP Server: Listening on 0.0.0.0:" + port);
        groupListener.start();
        if(delay > 0) ServerStopper.invoke(delay, this);
        while (working) {
            main();
        }
        System.out.println("TCP Server: Stopped");
    }

    /**
     * Stop proprement le serveur.
     */
    public void stopServer() {
        working = false;
        groupListener.close();
        end();
    }

    /**
     * Indique si le serveur est implemnetr avec java NIO.
     * @return Vrai si oui.
     */
    public boolean useNio() {
        return this instanceof ServerImpNio;
    }

    /**
     * Invoque un ConnectionManager pour le serveur courant.
     * @param client Client a gerer.
     * @return ConnectionManager associee au serveur et au client.
     * @throws IOException Si probleme d'instantiation du ConnectionManager sous-jacent.
     */
    ConnectionManager invoke(Socket client) throws IOException {
        return ConnectionManager.invoke(client, this);
    }

    /**
     * Methode s'executant en boucle jusqu'a l'arret du serveur.
     */
    abstract void main();

    /**
     * Methode d'arret du serveur selon l'implementation utilisee.
     */
    abstract void end();

    /**
     * Methode qui deconnecte un client.
     * @param obj Client a deconnecte.
     * @param rm Vrai si on veut le retirer des clients.
     */
    abstract public void disconnect(Object obj, boolean rm);

    /**
     * Methode qui permet de broadcast un object a tout les clients connectes.
     * @param obj Object a broadcast.
     */
    abstract public void broadcast(Object obj);

    /**
     * Classe interne gerant la transmission de l'adresse RV sur un groupe MultiCast.
     */
    private class MutlicastGroupListener extends Thread {

        /**
         * Socket mutlicast.
         */
        private final MulticastSocket socket;

        /**
         * Adresse du groupe multicast.
         */
        private final InetSocketAddress group;

        /**
         * Constructeur.
         * @param address Adresse du groupe multicast.
         * @throws IOException Si erreur lors de la creation/conexion au groupe de la socket.
         */
        MutlicastGroupListener(InetSocketAddress address) throws IOException {
            super("Group Listener");
            group = address;

            socket = new MulticastSocket(group.getPort());
            socket.joinGroup(group, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
        }

        @Override
        public void run() {
            InetSocketAddress rvSockAdr = null;
            try {
                rvSockAdr = new InetSocketAddress(InetAddress.getLocalHost(), port);
            } catch (UnknownHostException ignored) {}
            final DatagramPacket packet = new DatagramPacket(new byte[6], 6);
            System.out.println("Group Listener: Listening on " + group.getAddress().getHostAddress() + ":" + group.getPort());
            while(working) {
                try {
                    socket.receive(packet);
                    if (new String(packet.getData(), 0, packet.getLength()).equals("getadr")) {
                        System.out.println("Group Listener: Client asked for RV TCP Socket Address, responding....");
                        byte[] buf = Serialisation.serialiser(rvSockAdr);
                        socket.send(new DatagramPacket(buf, buf.length, group.getAddress(), group.getPort()));
                        System.out.println("Group Listener: Address sent.");
                    }
                } catch (Exception ignored) {}
            }
            System.out.println("Group Listener: Stopped");
        }

        /**
         * Ferme la socket multicast.
         */
        void close() {
            socket.close();
        }

    }

}

