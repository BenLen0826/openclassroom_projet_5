import { Component, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BackLink } from '../../shared/components/back-link';
import { PostService } from '../../core/services/post.service';
import { TopicStore } from '../../core/stores/topic.store';
import { readServerErrors } from '../../shared/forms/server-errors';

type Field = 'topicId' | 'title' | 'content';

/** Messages sous les champs, identiques à ceux du back (`CreatePostRequest`). */
const MESSAGES: Record<Field, Record<string, string>> = {
  topicId: { required: 'Le thème est obligatoire.' },
  title: {
    required: 'Le titre est obligatoire.',
    pattern: 'Le titre est obligatoire.',
    maxlength: 'Le titre ne doit pas dépasser 255 caractères.',
  },
  content: {
    required: 'Le contenu est obligatoire.',
    pattern: 'Le contenu est obligatoire.',
    maxlength: 'Le contenu ne doit pas dépasser 20 000 caractères.',
  },
};

/**
 * Page « Créer un article ». L'auteur et la date sont définis par le serveur ; après la
 * création, la page de l'article s'ouvre.
 */
@Component({
  selector: 'app-post-create',
  imports: [ReactiveFormsModule, BackLink],
  templateUrl: './post-create.html',
  styleUrl: './post-create.scss',
})
export class PostCreate implements OnInit {
  protected readonly topicStore = inject(TopicStore);
  private readonly postService = inject(PostService);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);

  // `pattern(/\S/)` : un champ fait seulement d'espaces compte comme vide, comme @NotBlank côté back
  protected readonly form = this.fb.group({
    topicId: this.fb.control<number | null>(null, Validators.required),
    title: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(255)]],
    content: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20_000)]],
  });

  protected readonly submitting = signal(false);
  protected readonly serverErrors = signal<Partial<Record<Field, string>>>({});
  protected readonly generalError = signal<string | null>(null);

  constructor() {
    // Les messages du dernier envoi disparaissent dès que l'utilisateur modifie un champ
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.serverErrors.set({});
      this.generalError.set(null);
    });
  }

  ngOnInit(): void {
    this.topicStore.load();
  }

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
    const { topicId, title, content } = this.form.getRawValue();
    // Bouton toujours actif : un formulaire invalide affiche tous ses messages, sans envoi
    if (this.form.invalid || topicId === null) {
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

    this.postService.create({ topicId, title, content }).subscribe({
      next: (post) => void this.router.navigateByUrl(`/posts/${post.id}`),
      error: (error: unknown) => {
        this.submitting.set(false);
        const { fields, general } = readServerErrors(error);
        this.serverErrors.set(fields);
        this.generalError.set(general);
      },
    });
  }
}
