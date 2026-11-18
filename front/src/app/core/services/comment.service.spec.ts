import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Comment } from '../models/comment';
import { CommentService } from './comment.service';

describe('CommentService', () => {
  const url = `${environment.apiUrl}/posts/42/comments`;
  const comment: Comment = {
    id: 1,
    content: 'Bravo',
    createdAt: '2026-11-18T12:00:00Z',
    authorUsername: 'bob',
  };
  let service: CommentService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CommentService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('getForPost calls GET /posts/{id}/comments', () => {
    let result: Comment[] | undefined;

    service.getForPost(42).subscribe((comments) => (result = comments));
    const req = httpTesting.expectOne(url);
    req.flush([comment]);

    expect(req.request.method).toBe('GET');
    expect(result).toEqual([comment]);
  });

  it('create calls POST /posts/{id}/comments with the content only', () => {
    let result: Comment | undefined;

    service.create(42, 'Bravo').subscribe((created) => (result = created));
    const req = httpTesting.expectOne(url);
    req.flush(comment);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ content: 'Bravo' });
    expect(result).toEqual(comment);
  });
});
