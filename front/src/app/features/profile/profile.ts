import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { User } from '../../core/models/user';
import { UserService } from '../../core/services/user.service';
import { TopicStore } from '../../core/stores/topic.store';
import { ACCOUNT_FIELD_MESSAGES, AccountField } from '../../shared/forms/account-field-messages';
import { readServerErrors } from '../../shared/forms/server-errors';
import { TopicCard } from '../../shared/components/topic-card';
import { passwordValidator } from '../../shared/validators/password.validator';

const LOAD_ERROR = 'Impossible de charger le profil. Réessayez plus tard.';

/**
 * Page « Profil » : consultation et modification du profil, puis les abonnements.
 * <p>
 * Les données du profil ne servent qu'à cette page : elles sont lues par le service,
 * sans store. Les abonnements viennent du `TopicStore`, partagé avec la page des thèmes.
 */
@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule, TopicCard],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  protected readonly store = inject(TopicStore);
  private readonly userService = inject(UserService);

  // Mot de passe facultatif : passwordValidator() accepte une valeur vide (= inchangé)
  protected readonly form = inject(NonNullableFormBuilder).group({
    username: ['', [Validators.required, Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
    password: ['', passwordValidator()],
  });

  protected readonly profileLoaded = signal(false);
  protected readonly loadError = signal<string | null>(null);
  protected readonly submitting = signal(false);
  protected readonly saved = signal(false);
  protected readonly serverErrors = signal<Partial<Record<AccountField, string>>>({});
  protected readonly generalError = signal<string | null>(null);

  constructor() {
    // Les messages du dernier envoi disparaissent dès que l'utilisateur modifie un champ
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.serverErrors.set({});
      this.generalError.set(null);
      this.saved.set(false);
    });
  }

  ngOnInit(): void {
    this.store.load();
    this.userService.getMe().subscribe({
      next: (user) => {
        this.showProfile(user);
        this.profileLoaded.set(true);
      },
      error: () => this.loadError.set(LOAD_ERROR),
    });
  }

  /** Message sous un champ : celui du serveur, sinon celui de la validation locale. */
  protected errorFor(field: AccountField): string | null {
    const serverError = this.serverErrors()[field];
    if (serverError) {
      return serverError;
    }
    const control = this.form.controls[field];
    if (!control.touched || !control.errors) {
      return null;
    }
    return ACCOUNT_FIELD_MESSAGES[field][Object.keys(control.errors)[0]] ?? null;
  }

  protected submit(): void {
    // Bouton toujours actif : un formulaire invalide affiche tous ses messages, sans envoi
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // Double clic pendant l'envoi : une seule requête
    if (this.submitting()) {
      return;
    }
    this.submitting.set(true);
    // Messages du dernier envoi effacés : un nouvel envoi sans modification ne passe pas par valueChanges
    this.saved.set(false);
    this.serverErrors.set({});
    this.generalError.set(null);

    this.userService.updateMe(this.form.getRawValue()).subscribe({
      next: (user) => {
        this.showProfile(user);
        this.submitting.set(false);
        // Après showProfile (qui modifie le formulaire et efface donc les messages)
        this.saved.set(true);
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        const { fields, general } = readServerErrors(error);
        this.serverErrors.set(fields);
        this.generalError.set(general);
      },
    });
  }

  /** Valeurs renvoyées par le serveur (e-mail normalisé) ; le mot de passe n'est jamais pré-rempli. */
  private showProfile(user: User): void {
    this.form.setValue({ username: user.username, email: user.email, password: '' });
    this.form.markAsUntouched();
  }
}
