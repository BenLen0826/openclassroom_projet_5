import { HttpErrorResponse } from '@angular/common/http';
import { GENERAL_ERROR, readServerErrors } from './server-errors';

describe('readServerErrors', () => {
  const httpError = (status: number, error: unknown) => new HttpErrorResponse({ status, error });

  it('returns the field errors of a 400', () => {
    const result = readServerErrors(
      httpError(400, { errors: { email: "L'e-mail n'est pas valide." } }),
    );

    expect(result).toEqual({ fields: { email: "L'e-mail n'est pas valide." }, general: null });
  });

  it('puts a 409 with a field under that field', () => {
    const result = readServerErrors(
      httpError(409, { detail: 'Cet e-mail est déjà utilisé.', field: 'email' }),
    );

    expect(result).toEqual({ fields: { email: 'Cet e-mail est déjà utilisé.' }, general: null });
  });

  it('turns a 409 without field into a general message', () => {
    const result = readServerErrors(
      httpError(409, { detail: 'Cet e-mail ou ce nom d’utilisateur est déjà utilisé.' }),
    );

    expect(result).toEqual({
      fields: {},
      general: 'Cet e-mail ou ce nom d’utilisateur est déjà utilisé.',
    });
  });

  it('shows the server message of a 404 (e.g. topic deleted in the meantime)', () => {
    const result = readServerErrors(httpError(404, { detail: 'Thème introuvable.' }));

    expect(result).toEqual({ fields: {}, general: 'Thème introuvable.' });
  });

  it('uses the general message for any other error', () => {
    expect(readServerErrors(httpError(500, null))).toEqual({ fields: {}, general: GENERAL_ERROR });
    expect(readServerErrors(new Error('réseau'))).toEqual({ fields: {}, general: GENERAL_ERROR });
    expect(GENERAL_ERROR).toBe('Une erreur est survenue. Réessayez plus tard.');
  });
});
