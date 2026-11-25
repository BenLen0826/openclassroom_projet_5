import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, Subject, of, throwError } from 'rxjs';
import { Comment } from '../../core/models/comment';
import { CommentService } from '../../core/services/comment.service';
import { CommentSection } from './comment-section';

describe('CommentSection', () => {
  const comment = (id: number, authorUsername: string, content: string): Comment => ({
    id,
    content,
    createdAt: '2026-11-18T13:00:00Z',
    authorUsername,
  });
  let commentService: { getForPost: ReturnType<typeof vi.fn>; create: ReturnType<typeof vi.fn> };
  let fixture: ComponentFixture<CommentSection>;
  let page: HTMLElement;

  async function render(comments: Observable<Comment[]> = of([])): Promise<void> {
    commentService = { getForPost: vi.fn().mockReturnValue(comments), create: vi.fn() };
    TestBed.configureTestingModule({
      imports: [CommentSection],
      providers: [{ provide: CommentService, useValue: commentService }],
    });
    fixture = TestBed.createComponent(CommentSection);
    fixture.componentRef.setInput('postId', 42);
    page = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  }

  const textarea = () => page.querySelector<HTMLTextAreaElement>('textarea[name="content"]')!;
  const sendButton = () => page.querySelector<HTMLButtonElement>('form button[type="submit"]')!;
  /** « auteur contenu » de chaque commentaire affiché, dans l'ordre. */
  const commentTexts = () =>
    [...page.querySelectorAll('app-comment-item')].map(
      (c) =>
        `${c.querySelector('strong')?.textContent?.trim()} ${c.querySelector('p')?.textContent?.trim()}`,
    );

  async function type(value: string): Promise<void> {
    textarea().value = value;
    textarea().dispatchEvent(new Event('input'));
    await fixture.whenStable();
  }

  /** Envoie par l'événement `submit` du formulaire. */
  async function submitForm(): Promise<void> {
    page.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  it('lists the comments of the post with author and content, oldest first', async () => {
    await render(of([comment(1, 'alice', 'Premier'), comment(2, 'bob', 'Second')]));

    expect(commentService.getForPost).toHaveBeenCalledWith(42);
    expect(commentTexts()).toEqual(['alice Premier', 'bob Second']);
  });

  it('shows a message when there is no comment', async () => {
    await render();

    expect(page.textContent).toContain("Aucun commentaire pour l'instant.");
  });

  it('adds the sent comment at the bottom and clears the text area', async () => {
    await render(of([comment(1, 'alice', 'Premier')]));
    commentService.create.mockReturnValue(of(comment(2, 'bob', 'Bravo')));
    await type('Bravo');

    sendButton().click();
    await fixture.whenStable();

    expect(commentService.create).toHaveBeenCalledWith(42, 'Bravo');
    expect(commentTexts()).toEqual(['alice Premier', 'bob Bravo']);
    expect(textarea().value).toBe('');
    expect(commentService.getForPost).toHaveBeenCalledTimes(1);
  });

  it('shows the required message and calls nothing when an empty or blank comment is sent', async () => {
    // Vide
    await render();
    expect(sendButton().disabled).toBe(false);
    sendButton().click();
    await fixture.whenStable();
    expect(page.querySelector('#content-error')?.textContent?.trim()).toBe(
      'Le commentaire est obligatoire.',
    );

    // Fait seulement d'espaces
    await type('   ');
    sendButton().click();
    await fixture.whenStable();
    expect(page.querySelector('#content-error')?.textContent?.trim()).toBe(
      'Le commentaire est obligatoire.',
    );
    expect(commentService.create).not.toHaveBeenCalled();
  });

  it('sends only once on a double submission', async () => {
    await render();
    commentService.create.mockReturnValue(new Subject<Comment>());
    await type('Bravo');

    await submitForm();
    await submitForm();

    expect(commentService.create).toHaveBeenCalledTimes(1);
  });

  it('shows the send icon, hidden from screen readers', async () => {
    await render();

    expect(sendButton().getAttribute('aria-label')).toBe('Envoyer le commentaire');
    const icon = sendButton().querySelector('.material-icons-outlined');
    expect(icon?.textContent?.trim()).toBe('send');
    expect(icon?.getAttribute('aria-hidden')).toBe('true');
  });

  it('shows the server error of a failed send and keeps the text', async () => {
    await render();
    commentService.create.mockReturnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { errors: { content: 'Le commentaire est obligatoire.' } },
          }),
      ),
    );
    await type('Bravo');

    sendButton().click();
    await fixture.whenStable();

    expect(page.querySelector('#content-error')?.textContent?.trim()).toBe(
      'Le commentaire est obligatoire.',
    );
    expect(textarea().value).toBe('Bravo');
  });

  it('shows an error and hides the form when the comments cannot be loaded', async () => {
    await render(throwError(() => new HttpErrorResponse({ status: 500 })));

    expect(page.textContent).toContain('Impossible de charger les commentaires.');
    expect(page.querySelector('form')).toBeNull();
  });
});
