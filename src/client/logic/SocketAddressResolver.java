package client.logic;

import io.utils.Serialisation;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

/**
 * Classe utilitaire ayant pour but de demander et recuperer une adresse depuis un groupe multicast.
 */
class SocketAddressResolver {

    /**
     * Addresse du groupe mutlicast.
     */
    private final InetSocketAddress group;

    /**
     * Socket MultiCast.
     */
    private final MulticastSocket mcs;

    /**
     * Constructeur.
     *
     * @param address Addresse du groupe mutlicast.
     * @throws IOException Si il y'a un probleme lors de la connexion au groupe.
     */
    private SocketAddressResolver(InetSocketAddress address) throws IOException {
        group = address;
        mcs = new MulticastSocket(group.getPort());
        mcs.joinGroup(group, NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
    }

    /**
     * Envoie un message "getadr" sur le groupe.
     *
     * @return L'instance courante.
     * @throws IOException Si il y'a un probleme lors de l'envoi.
     */
    private SocketAddressResolver askAddress() throws IOException {
        byte[] cmd = ("getadr").getBytes();
        mcs.send(new DatagramPacket(cmd, cmd.length, group.getAddress(), group.getPort()));
        return this;
    }

    /**
     * Renvoie l'adresse RV recuperer sur le groupe MutliCast
     *
     * @return Adresse RV.
     * @throws IOException Si il y'a un probleme lors de la reception.
     * @throws ClassNotFoundException Si la classe est inconnue dans le runtime.
     */
    private InetSocketAddress getAddress() throws IOException, ClassNotFoundException {
        DatagramPacket packet = new DatagramPacket(new byte[30000], 30000);
        mcs.setSoTimeout(100);
        mcs.receive(packet); //Reception de notre propre getadr
        while(new String(packet.getData(), 0, packet.getLength()).equals("getadr")) mcs.receive(packet); //On peut recevoir d'autres commandes getadr vu qu'on fait partie du groupe mais on veut une adresse
        Object obj = Serialisation.deserialiser(packet.getData());
        if(!(obj instanceof InetSocketAddress adr)) throw new IOException();
        mcs.close();
        return adr;
    }

    /**
     * Recupere l'adresse depuis un groupe mutlicast.
     * @param address Addresse du groupe mutlicast
     * @return Adresse RV
     * @throws IOException Si il y'a un probleme lors de la demande/reception.
     * @throws ClassNotFoundException Si la classe est inconnue dans le runtime.
     */
    static InetSocketAddress fetchAddressFromMutlicastGroup(InetSocketAddress address) throws IOException, ClassNotFoundException {
        return new SocketAddressResolver(address).askAddress().getAddress();
    }

}
