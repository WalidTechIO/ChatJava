package io.data;

import java.io.Serializable;

/**
 * Record representant un message de l'application.
 * @param pseudo Pseudo de l'utilisateur a l'origine du message.
 * @param contenu Contenu du message.
 */
public record Message(String pseudo, String contenu) implements Serializable {
    @Override
    public String toString() {
        return pseudo + " : " + contenu + "\n";
    }
}