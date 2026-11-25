import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStore } from '../../core/stores/auth.store';
import { BackLink } from '../../shared/components/back-link';
import { ACCOUNT_FIELD_MESSAGES, AccountField } from '../../shared/forms/account-field-messages';
import { fieldError } from '../../shared/forms/field-error';
import { readServerErrors } from '../../shared/forms/server-errors';
import { passwordValidator } from '../../shared/validators/password.validator';

type Field = AccountField;
const MESSAGES = ACCOUNT_FIELD_MESSAGES;

/** Page « Inscription ». */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, BackLink],
  templateUrl: './register.html',
})
export class Register {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  protected readonly form = inject(NonNullableFormBuilder).group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', [Validators.required, passwordValidator()]],
  });

  protected readonly submitting = signal(false);
  /** Erreurs renvoyées par le back, affichées sous le champ concerné. */
  protected readonly serverErrors = signal<Partial<Record<Field, string>>>({});
  protected readonly generalError = signal<string | null>(null);

  constructor() {
    // Le message du serveur sur un champ disparaît dès que l'utilisateur le corrige
    for (const field of Object.keys(MESSAGES) as Field[]) {
      this.form.controls[field].valueChanges
        .pipe(takeUntilDestroyed())
        .subscribe(() => this.clearServerError(field));
    }
    // Le message général (ex. erreur inattendue) disparaît dès que l'utilisateur modifie un champ
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.generalError.set(null));
  }

  /** Message à afficher sous un champ : celui du serveur, sinon celui de la validation locale. */
  protected errorFor(field: Field): string | null {
    // Mot de passe : la première erreur est « required » s'il est vide, sinon « password » (la règle)
    return fieldError(this.form.controls[field], this.serverErrors()[field], MESSAGES[field]);
  }

  protected submit(): void {
    // Bouton toujours actif : un formulaire invalide affiche tous ses messages
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // Double clic pendant l'envoi : une seule requête
    if (this.submitting()) {
      return;
    }
    this.submitting.set(true);
    this.serverErrors.set({});
    this.generalError.set(null);

    this.authStore.register(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/posts'),
      error: (error: unknown) => {
        this.submitting.set(false);
        this.showError(error);
      },
    });
  }

  private clearServerError(field: Field): void {
    if (this.serverErrors()[field]) {
      this.serverErrors.update((errors) => {
        const remaining = { ...errors };
        delete remaining[field];
        return remaining;
      });
    }
  }

  private showError(error: unknown): void {
    const { fields, general } = readServerErrors(error);
    this.serverErrors.set(fields);
    this.generalError.set(general);
  }
}
