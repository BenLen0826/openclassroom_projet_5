import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStore } from '../../core/stores/auth.store';
import {
  PASSWORD_RULE_MESSAGE,
  passwordValidator,
} from '../../shared/validators/password.validator';

type Field = 'username' | 'email' | 'password';

/** Messages affichés sous les champs, identiques à ceux du back (`RegisterRequest`). */
const MESSAGES: Record<Field, Record<string, string>> = {
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

const GENERAL_ERROR = 'Une erreur est survenue. Réessayez plus tard.';

/** Page « Inscription » (sans mise en forme avant l'étape 6). */
@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule],
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
    const serverError = this.serverErrors()[field];
    if (serverError) {
      return serverError;
    }
    const control = this.form.controls[field];
    if (!control.touched || !control.errors) {
      return null;
    }
    return MESSAGES[field][Object.keys(control.errors)[0]] ?? null;
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.serverErrors.set({});
    this.generalError.set(null);

    this.authStore.register(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/topics'),
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
    const body = error instanceof HttpErrorResponse ? error.error : null;
    const status = error instanceof HttpErrorResponse ? error.status : 0;

    if (status === 400 && body?.errors) {
      this.serverErrors.set(body.errors);
    } else if (status === 409 && body?.field) {
      this.serverErrors.set({ [body.field as Field]: body.detail });
    } else if (status === 409 && body?.detail) {
      this.generalError.set(body.detail);
    } else {
      this.generalError.set(GENERAL_ERROR);
    }
  }
}
