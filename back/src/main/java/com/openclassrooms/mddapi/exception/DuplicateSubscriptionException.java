package com.openclassrooms.mddapi.exception;

/**
 * Deux abonnements identiques sont arrivés en même temps (deux onglets) : la clé
 * primaire de la table {@code subscriptions} a refusé le second. L'abonnement existe
 * bien, donc la réponse est {@code 204}, comme pour un abonnement déjà présent.
 * <p>
 * Lever cette exception annule la transaction en échec (une transaction dont
 * l'insertion a échoué ne peut plus être validée).
 */
public class DuplicateSubscriptionException extends RuntimeException {

    public DuplicateSubscriptionException() {
        super("Abonnement déjà enregistré.");
    }
}
