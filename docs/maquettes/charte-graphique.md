# Charte graphique MDD (relevé des maquettes)

Relevé des valeurs des maquettes de Juana, qui servira à créer les **variables CSS** du front (`front/src/styles/_tokens.scss`, étape 6).

- **Sources** : export PDF [`maquettes-mdd.pdf`](maquettes-mdd.pdf) (couleurs exactes, extraites du fichier) et [maquette Figma](https://www.figma.com/design/Rflr3TVBog35BNMnn0DF09/Maquettes-MDD--desktop-et-mobile-?node-id=0-1) (tailles, espacements, police).
- **Objectif** : un respect global des écrans, pas un rendu au pixel près.
- **Règle** : une fois les variables créées dans `_tokens.scss`, **c'est ce fichier qui fait foi**. Ce document reste le relevé et l'explication des valeurs.

Légende de la colonne « Source » :

- **PDF** : valeur exacte extraite de l'export PDF par un script ;
- **Figma** : valeur relevée par l'utilisateur dans Figma ;
- **Validée** : valeur proposée par l'IA (absente de la maquette ou non lisible dans le PDF), vérifiée et validée par l'utilisateur ;
- **Demandée par l'utilisateur** : valeur ajoutée par l'utilisateur pendant l'intégration (contrôle visuel) ;
- **Proposée par l'IA** : valeur absente de la maquette, en attente de validation au contrôle visuel.

---

## 1. Couleurs

Les couleurs de l'export PDF sont exactes (lues dans le fichier, pas estimées à l'œil).

| Jeton (variable CSS) | Usage | Valeur | Source |
|----------------------|-------|--------|--------|
| `--color-primary` | Boutons, logo, lien actif de l'en-tête, bordures des champs, icône d'envoi | `#7763C5` | PDF |
| `--color-primary-hover` | Bouton survolé (absent de la maquette) | `#6652B3` (violet assombri de 10 %) | Validée |
| `--color-primary-alt` | Second violet, présent 3 fois (à confirmer à l'intégration : icône de profil active ou icône d'envoi) | `#6C5CCF` | PDF |
| `--color-on-primary` | Texte des boutons violets | `#FFFFFF` | PDF |
| `--color-danger` | Lien « Se déconnecter », messages d'erreur | `#A40F0F` | PDF |
| `--color-text` | Texte principal | `#000000` | PDF |
| `--color-text-muted` | Placeholders, texte secondaire | `#939393` | PDF + Figma |
| `--color-background` | Fond des pages | `#FFFFFF` | PDF |
| `--color-surface` | Fond des cartes d'article et de thème | `#F5F5F5` | PDF |
| `--color-surface-alt` | Bulle d'un commentaire | `#EEEEEE` | PDF |
| `--color-disabled` | Bouton « Déjà abonné » | `#939393` | PDF |
| `--color-border` | Séparateurs (sous l'en-tête, avant les commentaires et les abonnements), bord du menu mobile | `#000000` | Demandée par l'utilisateur (PDF : `#D9D9D9`) |
| `--color-backdrop` | Fond assombri derrière le menu mobile (absent de la maquette) | `rgb(0 0 0 / 40%)` | Validée |

Couleurs relevées mais non attribuées : `#EFEFEF` (une fois) et `#8A8C8C` (deux fois).

**Contrastes** : l'accessibilité avancée n'est pas demandée pour ce MVP (décision du 24/11/2026). Les couleurs de la maquette sont gardées telles quelles, y compris le gris `#939393` (texte blanc du bouton « Déjà abonné », placeholders), dont le contraste (environ 3:1) est sous le seuil recommandé de 4,5:1. Point à signaler dans la revue technique si Lighthouse le relève.

## 2. Typographie

| Jeton | Usage | Valeur | Source |
|-------|-------|--------|--------|
| `--font-family` | Toute l'application | Inter, puis `system-ui, sans-serif` | Figma |
| `--font-size-page-title` | Titres de page (« Inscription », « Profil utilisateur », « Créer un nouvel article ») | 24 px | Validée |
| `--font-size-card-title` | Titre d'une carte d'article ou de thème | 16 px | Validée |
| `--font-size-card-body` | Texte courant des cartes | 14 px | Figma |
| `--font-size-body` | Texte courant de toute l'application (taille du `body`, héritée partout sauf exceptions ci-dessus et ci-dessous) | 20 px | Figma |

Un jeton `--font-size-small` (14 px, proposé par l'IA pour la date, l'auteur et les labels) a été retiré le 25/11/2026 à la demande de l'utilisateur : le texte garde les 20 px de la maquette, seules les tailles relevées dans Figma (titres, cartes, boutons) la remplacent.
| `--font-size-button` | Texte des boutons | 16 px | Figma |
| `--font-weight-regular` | Texte courant | 400 | Validée |
| `--font-weight-bold` | Titres, boutons | 600 | Validée |
| `--line-height-body` | Texte courant | 1.5 | Validée |

## 3. Espacements

| Jeton | Usage | Valeur | Source |
|-------|-------|--------|--------|
| `--space-xs` | Entre un label et son champ | 12 px | Validée (4 px, puis +8 px à la demande de l'utilisateur) |
| `--space-sm` | Entre deux champs, dans une carte | 16 px | Validée (8 px, puis +8 px) |
| `--space-md` | Marge intérieure des cartes, entre deux cartes | 24 px | Validée (16 px, puis +8 px) |
| `--space-lg` | Entre deux sections (formulaire du profil / abonnements) | 32 px | Validée (24 px, puis +8 px) |
| `--space-xl` | Marges de page (desktop) | 40 px | Validée (32 px, puis +8 px) |

## 4. Formes et dimensions

| Jeton | Usage | Valeur | Source |
|-------|-------|--------|--------|
| `--radius-sm` | Boutons, champs de saisie | 6 px | Validée |
| `--radius-md` | Cartes, bulle de commentaire, zone de commentaire | 10 px | Validée |
| `--border-width` | Bordure de base : boutons, séparateurs, bord du menu mobile, zone de commentaire | 1 px | Validée |
| `--border-width-md` | Bordure des champs des formulaires et bordure basse de l'en-tête | 2 px | Demandée par l'utilisateur |
| `--border-width-lg` | Non utilisée pour l'instant (d'abord prévue pour l'en-tête) | 4 px | Demandée par l'utilisateur |
| `--input-height` | Hauteur d'un champ sur une ligne | 40 px | Validée |
| `--button-height` | Hauteur d'un bouton | 36 px | Validée |
| `--form-max-width` | Largeur des formulaires (inscription, connexion, création, profil) | 360 px | Validée |
| `--content-max-width` | Largeur maximale du contenu (fil, thèmes, article) | 1100 px | Validée |
| `--header-height` | Hauteur de l'en-tête | 64 px | Validée |
| `--avatar-size` | Icône de profil de l'en-tête | 36 px | Validée |
| `--transition-duration` | Durée du glissement du menu mobile et du fondu de son overlay (commentaire de la maquette) | 300 ms | Proposée par l'IA |

## 5. Mise en page responsive

| Élément | Valeur | Source |
|---------|--------|--------|
| Largeur du cadre desktop | 837 px | Figma |
| Largeur du cadre mobile | 294 px | Figma |
| Point de rupture mobile / desktop | 768 px : en dessous, une colonne et menu burger | Validée |
| Colonnes des cartes (desktop / mobile) | 2 / 1 | PDF |

## 6. Éléments graphiques

| Élément | Source | Remarque |
|---------|--------|----------|
| Logo MDD | `front/public/assets/logo_p6.png` (repris du projet initial) | Taille ajustée à l'intégration, proportionnelle à la maquette |
| Flèche retour, menu burger, icône d'envoi, icône de profil | Figma (export SVG : sélectionner l'icône → *Export* → SVG) | À défaut, icônes SVG simples dessinées dans le même style |
| Ombres | Maquette | Aucune ombre visible : pas d'ombre |
