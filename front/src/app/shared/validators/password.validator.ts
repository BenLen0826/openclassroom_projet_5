import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/**
 * Règle du mot de passe, identique à `PasswordValidator` côté back : entre 8 et 72
 * caractères (72 = limite de BCrypt), dont une minuscule `[a-z]`, une majuscule
 * `[A-Z]`, un chiffre `[0-9]` et un caractère spécial (tout ce qui n'est ni une
 * lettre ASCII ni un chiffre : `é` ou un espace comptent comme caractères spéciaux).
 */
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}$/;

export const PASSWORD_RULE_MESSAGE =
  'Le mot de passe doit contenir entre 8 et 72 caractères, dont un chiffre, une minuscule, une majuscule et un caractère spécial.';

/** Valeur vide acceptée : l'obligation est portée par `Validators.required`. */
export function passwordValidator(): ValidatorFn {
  return (control: AbstractControl<string>): ValidationErrors | null => {
    const value = control.value;
    if (!value) {
      return null;
    }
    return PASSWORD_PATTERN.test(value) ? null : { password: true };
  };
}
