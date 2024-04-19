package client.logic;

import client.ui.ChatController;
import io.data.Message;
import javafx.application.Platform;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Classe abstraite representant un client.
 */
public abstract class Client implements Runnable {

    /**
     * Controller de l'UI.
     */
    final ChatController controller;

    /**
     * Statut de travail du Receiver.
     */
    boolean working = true;

    /**
     * Envoie un Message.
     * @param msg Message a envoye.
     */
    public abstract void sendMessage(Message msg);

    /**
     * Ferme la socket client.
     */
    abstract void closeSocket();

    /**
     * Recois un Message.
     * Bloquante.
     *
     * @return Message recu.
     * @throws IOException si une erreur I/O se produit durant la lecture.
     * @throws ClassNotFoundException si la classe est inconnue dans le runtime.
     */
    public abstract Message receiveMessage() throws IOException, ClassNotFoundException;

    /**
     * Renvoie le statut de connexion du client.
     *
     * @return statut de connexion du client.
     */
    public abstract boolean isConnected();

    /**
     * Constructeur abstrait.
     *
     * @param controller controller de l'UI.
     */
    Client(ChatController controller) {
        this.controller = controller;
    }

    /**
     * Methode permettant aux enfants de demarrer le Receiver une fois qu'ils sont correctement instantie.
     */
    void startReceiver() {
        new Thread(this).start();
    }

    /**
     * Deconnecte le client.
     * @param retry si vrai le client tente de se reconnecter
     * 5fois sauf si les entrees utilisateurs sont incorrects.
     */
    public void stopConnection(boolean retry) {
        if(!working) return;
        working = false;
        closeSocket();
        controller.disconnect(retry);
    }

    /**
     * Methode d'invocation d'un client.
     * @param address Addresse du client.
     * @param controller Controller de l'UI.
     * @param nioMode Si vrai client implementer avec les {@link java.nio.channels.SocketChannel}.
     * @return Client.
     * @throws IOException Probleme lors de la connexion.
     */
    public static Client invoke(InetSocketAddress address, ChatController controller, boolean nioMode) throws FetchAddressFromMutlicastException, IOException {

        if(address.getAddress().isMulticastAddress()) {
            try {
                address = SocketAddressResolver.fetchAddressFromMutlicastGroup(address);
            } catch (IOException|ClassNotFoundException e) {
                throw new FetchAddressFromMutlicastException("Impossible de récupérer l'adresse RV depuis le groupe Multicast.");
            }
        }

        return (nioMode ? new ClientImpNio(address, controller) : new ClientImp(address, controller));
    }

    /**
     * Methode de reception des messages.
     */
    @Override
    public void run() {
        while (working) {
            try {
                controller.appendMessage(receiveMessage());
            } catch(IOException | ClassNotFoundException e) {
                break;
            }
        }
        /*
           JavaFX ne permet pas de set le text d'un label depuis un Thread utilisateur (résulte en IllegalStateException).
           Pour résoudre ce probleme on demande a JavaFX de lancer un runnable qui fera l'appel a stopConnection
           via le Thread principale JavaFX.
           Cela est necessaire pour la modification du label d'etat de la connexion si ce receveur plante.
           */
        Platform.runLater(() -> Client.this.stopConnection(true));
    }

}
