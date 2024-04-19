package client.ui;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import client.logic.Client;
import client.logic.FetchAddressFromMutlicastException;
import io.data.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controleur de l'UI.
 */
public class ChatController {

    /**
     * Logique metier client.
     */
    private Client client = null;

    /**
     * Resources.
     */
    @FXML
    private ResourceBundle resources;

    /**
     * Location.
     */
    @FXML
    private URL location;

    /**
     * Checkbox mode NIO (ajoute).
     */
    @FXML
    public CheckBox entreeSelector;

    /**
     * Bouton connexion/deconnexion (modifie).
     */
    @FXML
    private Button boutonConnexion;

    /**
     * Zone de chat.
     */
    @FXML
    private TextArea areaDiscussion;

    /**
     * Entree Adresse IP/Nom d'hote de connexion.
     */
    @FXML
    private TextField entreeAdresseIP;

    /**
     * Entree message.
     */
    @FXML
    private TextField entreeMessage;

    /**
     * Entree port de connexion.
     */
    @FXML
    private TextField entreePort;

    /**
     * Entree pseudo utilisateur.
     */
    @FXML
    private TextField entreePseudo;

    /**
     * Checkbox reconnexion atuomatique (ajoute).
     */
    @FXML
    private CheckBox entreeReconnexionAuto;

    /**
     * Label d'etat de la connexion.
     */
    @FXML
    private Label labelEtatConnexion;

    /**
     * Clique bouton connexion.
     * @param event evenement clique.
     */
    @FXML
    void actionBoutonConnexion(ActionEvent event) {
        if(!isConnected()) connect(true);
        else alertError("Vous êtes déjà connecté a un serveur");
    }

    /**
     * Clique bouton deconnexion.
     * @param event evenement clique.
     */
    @FXML
    void actionBoutonDeconnexion(ActionEvent event) {
        if(isConnected()) client.stopConnection(false);
        else alertError("Vous n'êtes connecté a aucun serveur.");
    }

    /**
     * Clique bouton envoyer.
     * @param event evenement clique.
     */
    @FXML
    void actionBoutonEnvoyer(ActionEvent event) {
        if(!isConnected()) {
            alertError("Vous n'êtes connecté a aucun serveur.");
            return;
        }
        if(entreePseudo.getText().isBlank()) {
            alertError("Vous devez renseigner un pseudo.");
            return;
        }
        if(entreeMessage.getText().isBlank()) {
            alertError("Vous ne pouvez pas envoyer de message vide.");
            return;
        }
        entreePseudo.setDisable(true); //Permet plus a l'utilisateur de changer de pseudo une fois un msg envoyé
        client.sendMessage(new Message(entreePseudo.getText(), entreeMessage.getText())); //Envoie du msg
        entreeMessage.clear(); //Nettoie la zone texte d'ecriture
    }

    /**
     * Init de la Scene.
     */
    @FXML
    void initialize() {
        labelEtatConnexion.setText("Déconnecté");
    }

    /**
     * Init et demarre un client.
     * @param alert Afficher alert si erreur de connexion independante de l'utilisateur.
     * @return boolean indiquant si une tentative de connexion a ete effectuée.
     */
    private boolean connect(boolean alert) {
        InetSocketAddress address;

        try {
            address = new InetSocketAddress(InetAddress.getByName(entreeAdresseIP.getText()), Integer.parseInt(entreePort.getText()));
        } catch (IllegalArgumentException e) {
            alertError("Port incorrect.");
            return false;
        } catch (UnknownHostException e) {
            alertError("Hôte introuvable.");
            return false;
        }

        //Initialise le client
        try {
            client = Client.invoke(address, this, entreeSelector.isSelected());
            entreeSelector.setDisable(true);
            entreeAdresseIP.setDisable(true);
            entreePort.setDisable(true);
            boutonConnexion.setOnAction(this::actionBoutonDeconnexion);
            boutonConnexion.setText("Déconnexion");
            labelEtatConnexion.setText("Connecté");
        } catch (IOException e) {
            if(alert) alertError("Impossible de se connecter au serveur.");
        } catch (FetchAddressFromMutlicastException e) {
            if(alert) alertError(e.getMessage());
        }
        return true;
    }

    /**
     * Gere la deconnexion.
     * @param retry boolean indiquant si on doit tenter de se reconnecter.
     */
    public void disconnect(boolean retry) {
        if(client == null) return;
        client = null;

        areaDiscussion.clear();

        int maxRetry = 5;
        if(entreeReconnexionAuto.isSelected() && retry) {
            for(int i = 0; i<maxRetry; i++) {
                if(!connect(false)) break;
                if(isConnected()) return;
            }
            alertError("Impossible de se connecter au serveur.");
            entreeReconnexionAuto.setSelected(false);
        }

        entreeMessage.clear();
        entreeAdresseIP.clear();
        entreePort.clear();
        entreePseudo.clear();
        entreeAdresseIP.setDisable(false);
        entreePort.setDisable(false);
        entreePseudo.setDisable(false);
        entreeSelector.setDisable(false);
        boutonConnexion.setOnAction(this::actionBoutonConnexion);
        boutonConnexion.setText("Connexion");
        labelEtatConnexion.setText("Déconnecté");
    }

    /**
     * Statut de connexion de l'UI.
     * @return Statut de connexion de l'UI.
     */
    private boolean isConnected () {
        return client != null && client.isConnected();
    }

    /**
     * Ajoute un message a l'UI.
     * @param msg Message a ajouter.
     */
    public void appendMessage(Message msg) {
        areaDiscussion.appendText(msg.toString());
    }

    /**
     * Affiche une bulle d'alerte type erreur.
     * @param msg Message a afficher.
     */
    private void alertError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).show();
    }

}
