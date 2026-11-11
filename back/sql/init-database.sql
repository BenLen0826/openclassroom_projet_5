-- =============================================================================
-- MDD : création de la base et du compte applicatif (à lancer une fois, en root)
-- =============================================================================
--
-- /!\ AVANT DE LANCER CE SCRIPT : remplacer 'CHANGER_MOI' (étape 2) par le
--     mot de passe choisi pour mdd_user, le même que DB_PASSWORD dans back/.env.
--
-- Les tables ne sont pas créées ici : Hibernate les génère au démarrage de
-- l'API à partir des entités (spring.jpa.hibernate.ddl-auto=update).
-- =============================================================================

-- 1. Base de données (utf8mb4 : accents et emojis dans les articles et commentaires)
CREATE DATABASE IF NOT EXISTS mdd
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. Compte applicatif, utilisable uniquement depuis la machine locale
CREATE USER IF NOT EXISTS 'mdd_user'@'localhost'
    IDENTIFIED BY 'CHANGER_MOI'; -- /!\ à remplacer par la valeur de DB_PASSWORD

-- 3. Droits minimaux (moindre privilège), limités à la base mdd :
--    - SELECT, INSERT, UPDATE, DELETE : lecture et écriture des données
--    - CREATE, ALTER                  : génération du schéma par Hibernate
--    - INDEX                          : index et contraintes d'unicité
--    - REFERENCES                     : clés étrangères
--    Pas de DROP : le mode "update" d'Hibernate ne supprime ni table ni colonne.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
    ON mdd.* TO 'mdd_user'@'localhost';
