import { FormControl } from '@angular/forms';
import { PASSWORD_RULE_MESSAGE, passwordValidator } from './password.validator';

/** Mêmes cas que `PasswordValidatorTest` côté back : les deux règles doivent donner le même résultat. */
describe('passwordValidator', () => {
  const validate = (value: string) => new FormControl(value, passwordValidator()).errors;

  it('accepts an empty value (required is checked by Validators.required)', () => {
    expect(validate('')).toBeNull();
  });

  it.each([
    ['8 characters', 'Abcdef1!'],
    ['72 characters', 'Aa1!' + 'a'.repeat(68)],
    ['an accented letter counted as special character', 'Abcdef1é'],
  ])('accepts a password with %s', (_case, password) => {
    expect(validate(password)).toBeNull();
  });

  it.each([
    ['7 characters', 'Abcde1!'],
    ['73 characters', 'Aa1!' + 'a'.repeat(69)],
    ['no digit', 'Abcdefg!'],
    ['no lowercase letter', 'ABCDEF1!'],
    ['no uppercase letter', 'abcdef1!'],
    ['no special character', 'Abcdefg1'],
  ])('rejects a password with %s', (_case, password) => {
    expect(validate(password)).toEqual({ password: true });
  });

  it('uses the same message as the back-end', () => {
    expect(PASSWORD_RULE_MESSAGE).toBe(
      'Le mot de passe doit contenir entre 8 et 72 caractères, dont un chiffre, une minuscule, une majuscule et un caractère spécial.',
    );
  });
});
