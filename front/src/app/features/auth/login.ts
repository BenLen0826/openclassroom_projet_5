import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthStore } from '../../core/stores/auth.store';

type Field = 'identifier' | 'password';

/** Messages affichés sous les champs, identiques à ceux du back (`LoginRequest`). */
const REQUIRED_MESSAGES: Record<Field, string> = {
  identifier: "L'identifiant est obligatoire.",
  password: 'Le mot de passe est obligatoire.',
};

const GENERAL_ERROR = 'Une erreur est survenue. Réessayez plus tard.';

/** Page « Se connecter » par e-mail ou nom d'utilisateur (sans mise en forme avant l'étape 6). */
@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly authStore = inject(AuthStore);
  private readonly router = inject(Router);

  // `pattern(/\S/)` : un champ fait seulement d'espaces compte comme vide, comme @NotBlank côté back
  protected readonly form = inject(NonNullableFormBuilder).group({
    identifier: ['', [Validators.required, Validators.pattern(/\S/)]],
    password: ['', [Validators.required, Validators.pattern(/\S/)]],
  });

  protected readonly submitting = signal(false);
  protected readonly generalError = signal<string | null>(null);

  constructor() {
    // Le message d'erreur (ex. identifiants refusés) disparaît dès que l'utilisateur modifie un champ
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => this.generalError.set(null));
  }

  /** Message sous un champ touché et vide. */
  protected errorFor(field: Field): string | null {
    const control = this.form.controls[field];
    return control.touched && control.invalid ? REQUIRED_MESSAGES[field] : null;
  }

  protected submit(): void {
    if (this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.generalError.set(null);

    this.authStore.login(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl('/posts'),
      error: (error: unknown) => {
        this.submitting.set(false);
        // 401 : message unique du back (« Identifiant ou mot de passe incorrect. »)
        const isRefused = error instanceof HttpErrorResponse && error.status === 401;
        this.generalError.set(
          isRefused && error.error?.detail ? error.error.detail : GENERAL_ERROR,
        );
      },
    });
  }
}
