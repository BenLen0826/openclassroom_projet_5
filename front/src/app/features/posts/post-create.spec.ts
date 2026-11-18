import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Observable, Subject, of, throwError } from 'rxjs';
import { Post } from '../../core/models/post';
import { PostService } from '../../core/services/post.service';
import { TopicService } from '../../core/services/topic.service';
import { PostCreate } from './post-create';

describe('PostCreate', () => {
  const created: Post = {
    id: 42,
    title: 'Titre',
    content: 'Contenu',
    createdAt: '2026-11-18T12:00:00Z',
    authorUsername: 'bob',
    topicId: 17,
    topicName: 'Java',
  };
  let postService: { create: ReturnType<typeof vi.fn> };
  let navigateByUrl: ReturnType<typeof vi.spyOn>;
  let fixture: ComponentFixture<PostCreate>;
  let page: HTMLElement;

  beforeEach(async () => {
    localStorage.clear();
    localStorage.setItem('mdd_token', 'jwt');
    postService = { create: vi.fn() };
    TestBed.configureTestingModule({
      imports: [PostCreate],
      providers: [
        provideRouter([]),
        { provide: PostService, useValue: postService },
        {
          provide: TopicService,
          useValue: {
            getAll: () =>
              of([
                { id: 5, name: 'Angular', description: 'Front-end', subscribed: false },
                { id: 17, name: 'Java', description: 'Back-end', subscribed: true },
              ]),
          },
        },
      ],
    });
    navigateByUrl = vi.spyOn(TestBed.inject(Router), 'navigateByUrl').mockResolvedValue(true);
    fixture = TestBed.createComponent(PostCreate);
    page = fixture.nativeElement as HTMLElement;
    await fixture.whenStable();
  });

  const button = () => page.querySelector<HTMLButtonElement>('button[type="submit"]')!;
  const errorUnder = (name: string) => page.querySelector(`#${name}-error`)?.textContent?.trim();

  async function fill(field: string, value: string): Promise<void> {
    const element = page.querySelector<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>(
      `[name="${field}"]`,
    )!;
    element.value = value;
    element.dispatchEvent(new Event(element instanceof HTMLSelectElement ? 'change' : 'input'));
    element.dispatchEvent(new Event('blur'));
    await fixture.whenStable();
  }

  async function fillValidForm(): Promise<void> {
    const select = page.querySelector<HTMLSelectElement>('select[name="topicId"]')!;
    // Position 2 = « Java » (l'option 0 est « Sélectionner un thème ») ; son id est 17 :
    // ids non consécutifs et différents de la position, pour prouver que l'id est envoyé
    select.selectedIndex = 2;
    select.dispatchEvent(new Event('change'));
    await fill('title', 'Titre');
    await fill('content', 'Contenu');
  }

  async function submitWith(response: Observable<Post>): Promise<void> {
    postService.create.mockReturnValue(response);
    await fillValidForm();
    button().click();
    await fixture.whenStable();
  }

  it('lists all topics in the select', async () => {
    const options = [...page.querySelectorAll('select[name="topicId"] option')].map((o) =>
      o.textContent?.trim(),
    );

    expect(options).toEqual(['Sélectionner un thème', 'Angular', 'Java']);
  });

  it('disables the button while the form is invalid', async () => {
    expect(button().disabled).toBe(true);

    await fillValidForm();

    expect(button().disabled).toBe(false);
  });

  it('shows the messages under touched empty fields', async () => {
    await fill('title', '');
    await fill('content', '');
    page.querySelector('select')!.dispatchEvent(new Event('blur'));
    await fixture.whenStable();

    expect(errorUnder('topicId')).toBe('Le thème est obligatoire.');
    expect(errorUnder('title')).toBe('Le titre est obligatoire.');
    expect(errorUnder('content')).toBe('Le contenu est obligatoire.');
  });

  it('refuses a title over 255 characters', async () => {
    await fill('title', 'a'.repeat(256));

    expect(errorUnder('title')).toBe('Le titre ne doit pas dépasser 255 caractères.');
  });

  it('refuses a content over 20 000 characters', async () => {
    await fill('content', 'x'.repeat(20_001));

    expect(errorUnder('content')).toBe('Le contenu ne doit pas dépasser 20 000 caractères.');
  });

  it('sends the post and opens its page', async () => {
    await submitWith(of(created));

    expect(postService.create).toHaveBeenCalledWith({
      topicId: 17,
      title: 'Titre',
      content: 'Contenu',
    });
    expect(navigateByUrl).toHaveBeenCalledWith('/posts/42');
  });

  it('sends only once on a double click', async () => {
    await submitWith(new Subject<Post>());
    button().click();

    expect(button().disabled).toBe(true);
    expect(postService.create).toHaveBeenCalledTimes(1);
  });

  it('shows « Thème introuvable. » when the topic was deleted in the meantime', async () => {
    await submitWith(
      throwError(
        () => new HttpErrorResponse({ status: 404, error: { detail: 'Thème introuvable.' } }),
      ),
    );

    expect(page.querySelector('[role="alert"]')?.textContent?.trim()).toBe('Thème introuvable.');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('shows the server error', async () => {
    await submitWith(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { errors: { title: 'Le titre est obligatoire.' } },
          }),
      ),
    );

    expect(errorUnder('title')).toBe('Le titre est obligatoire.');
    expect(navigateByUrl).not.toHaveBeenCalled();
  });
});
