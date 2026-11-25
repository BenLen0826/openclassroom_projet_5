import { Component, OnInit, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Comment } from '../../core/models/comment';
import { CommentService } from '../../core/services/comment.service';
import { CommentItem } from '../../shared/components/comment-item';
import { readServerErrors } from '../../shared/forms/server-errors';

const LOAD_ERROR = 'Impossible de charger les commentaires. Réessayez plus tard.';

/** Messages sous la zone de commentaire, identiques à ceux du back (`CreateCommentRequest`). */
const MESSAGES: Record<string, string> = {
  required: 'Le commentaire est obligatoire.',
  pattern: 'Le commentaire est obligatoire.',
  maxlength: 'Le commentaire ne doit pas dépasser 20 000 caractères.',
};

/**
 * Section « Commentaires » de la page d'un article : liste et ajout.
 * Les données ne servent qu'ici : pas de store.
 */
@Component({
  selector: 'app-comment-section',
  imports: [ReactiveFormsModule, CommentItem],
  templateUrl: './comment-section.html',
  styleUrl: './comment-section.scss',
})
export class CommentSection implements OnInit {
  private readonly commentService = inject(CommentService);

  readonly postId = input.required<number>();

  protected readonly comments = signal<Comment[]>([]);
  protected readonly loadError = signal<string | null>(null);

  // `pattern(/\S/)` : un commentaire fait seulement d'espaces compte comme vide (@NotBlank)
  protected readonly form = inject(NonNullableFormBuilder).group({
    content: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(20_000)]],
  });
  protected readonly sending = signal(false);
  protected readonly serverError = signal<string | null>(null);
  protected readonly generalError = signal<string | null>(null);

  constructor() {
    // Les messages du dernier envoi disparaissent dès que l'utilisateur modifie le texte
    this.form.valueChanges.pipe(takeUntilDestroyed()).subscribe(() => {
      this.serverError.set(null);
      this.generalError.set(null);
    });
  }

  ngOnInit(): void {
    this.commentService.getForPost(this.postId()).subscribe({
      next: (comments) => this.comments.set(comments),
      error: () => this.loadError.set(LOAD_ERROR),
    });
  }

  /** Message sous la zone : celui du serveur, sinon celui de la validation locale. */
  protected contentError(): string | null {
    const control = this.form.controls.content;
    if (this.serverError()) {
      return this.serverError();
    }
    if (!control.touched || !control.errors) {
      return null;
    }
    return MESSAGES[Object.keys(control.errors)[0]] ?? null;
  }

  protected send(): void {
    // Bouton toujours actif : un commentaire vide affiche son message, sans envoi
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    // Double envoi pendant la requête : une seule requête
    if (this.sending()) {
      return;
    }
    this.sending.set(true);
    this.serverError.set(null);
    this.generalError.set(null);

    this.commentService.create(this.postId(), this.form.getRawValue().content).subscribe({
      next: (comment) => {
        // Ajouté en bas de la liste, sans recharger les commentaires
        this.comments.update((comments) => [...comments, comment]);
        this.form.reset();
        this.sending.set(false);
      },
      error: (error: unknown) => {
        this.sending.set(false);
        const { fields, general } = readServerErrors(error);
        this.serverError.set(fields['content'] ?? null);
        this.generalError.set(general);
      },
    });
  }
}
