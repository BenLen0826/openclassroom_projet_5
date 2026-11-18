import { PASSWORD_RULE_MESSAGE } from '../validators/password.validator';

/** Champs d'un compte, communs à l'inscription et au profil. */
export type AccountField = 'username' | 'email' | 'password';

/**
 * Messages de validation affichés sous les champs, par erreur Angular
 * (`required`, `maxlength`…). Identiques à ceux du back (`RegisterRequest`,
 * `UpdateProfileRequest`).
 */
export const ACCOUNT_FIELD_MESSAGES: Record<AccountField, Record<string, string>> = {
  username: {
    required: "Le nom d'utilisateur est obligatoire.",
    maxlength: "Le nom d'utilisateur ne doit pas dépasser 50 caractères.",
  },
  email: {
    required: "L'e-mail est obligatoire.",
    email: "L'e-mail n'est pas valide.",
    maxlength: "L'e-mail ne doit pas dépasser 255 caractères.",
  },
  password: {
    required: 'Le mot de passe est obligatoire.',
    password: PASSWORD_RULE_MESSAGE,
  },
};
