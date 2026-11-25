import { FormControl, Validators } from '@angular/forms';
import { fieldError } from './field-error';

describe('fieldError', () => {
  const messages = { required: 'Le titre est obligatoire.', maxlength: 'Titre trop long.' };

  it('returns the server error first, even on an untouched field', () => {
    const control = new FormControl('', Validators.required);

    expect(fieldError(control, 'Refusé par le serveur.', messages)).toBe('Refusé par le serveur.');
  });

  it('returns nothing while the field is untouched', () => {
    const control = new FormControl('', Validators.required);

    expect(fieldError(control, undefined, messages)).toBeNull();
  });

  it('returns the message of the first error of a touched invalid field', () => {
    const control = new FormControl('', Validators.required);
    control.markAsTouched();

    expect(fieldError(control, undefined, messages)).toBe('Le titre est obligatoire.');
  });

  it('returns nothing for a touched valid field, or an error without message', () => {
    const valid = new FormControl('Titre', Validators.required);
    valid.markAsTouched();
    const noMessage = new FormControl('abc', Validators.minLength(5));
    noMessage.markAsTouched();

    expect(fieldError(valid, undefined, messages)).toBeNull();
    expect(fieldError(noMessage, undefined, messages)).toBeNull();
  });
});
