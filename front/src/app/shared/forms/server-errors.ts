import { HttpErrorResponse } from '@angular/common/http';

export const GENERAL_ERROR = 'Une erreur est survenue. Réessayez plus tard.';

/** Erreurs du serveur prêtes à afficher : sous les champs, ou en message général. */
export interface ServerErrors {
  fields: Partial<Record<string, string>>;
  general: string | null;
}

/**
 * Convertit une erreur HTTP de l'API (`ProblemDetail`) en messages à afficher :
 * - `400` avec `errors` : un message par champ ;
 * - `409` avec `field` : le message sous ce champ (ex. e-mail déjà utilisé) ;
 * - `409` sans `field`, ou `404` : le message du serveur en message général ;
 * - toute autre erreur : un message général, sans détail technique.
 */
export function readServerErrors(error: unknown): ServerErrors {
  const body = error instanceof HttpErrorResponse ? error.error : null;
  const status = error instanceof HttpErrorResponse ? error.status : 0;

  if (status === 400 && body?.errors) {
    return { fields: body.errors, general: null };
  }
  if (status === 409 && body?.field) {
    return { fields: { [body.field]: body.detail }, general: null };
  }
  // 409 sans champ, ou 404 (ex. thème supprimé entre-temps) : le message du serveur est
  // destiné à l'utilisateur (aucune trace technique dans les ProblemDetail de l'API)
  if ((status === 409 || status === 404) && body?.detail) {
    return { fields: {}, general: body.detail };
  }
  return { fields: {}, general: GENERAL_ERROR };
}
