import { AbstractControl } from '@angular/forms';

/**
 * Message à afficher sous un champ de formulaire, partagé par l'inscription, le profil, la
 * création d'un article et le commentaire :
 * 1. l'erreur renvoyée par le serveur pour ce champ, s'il y en a une ;
 * 2. sinon, si le champ a été touché et est invalide, le message de sa première erreur
 *    (`required`, `maxlength`…), pris dans `messages` ;
 * 3. sinon, rien.
 */
export function fieldError(
  control: AbstractControl,
  serverError: string | undefined,
  messages: Record<string, string>,
): string | null {
  if (serverError) {
    return serverError;
  }
  if (!control.touched || !control.errors) {
    return null;
  }
  return messages[Object.keys(control.errors)[0]] ?? null;
}
