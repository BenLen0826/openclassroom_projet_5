import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { CreatePostRequest, Post } from '../models/post';
import { PostService } from './post.service';

describe('PostService', () => {
  const url = `${environment.apiUrl}/posts`;
  const post: Post = {
    id: 42,
    title: 'Titre',
    content: 'Contenu',
    createdAt: '2026-11-18T10:00:00Z',
    authorUsername: 'bob',
    topicId: 3,
    topicName: 'Java',
  };
  let service: PostService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(PostService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('getFeed calls GET /posts', () => {
    let result: Post[] | undefined;

    service.getFeed().subscribe((posts) => (result = posts));
    const req = httpTesting.expectOne(url);
    req.flush([post]);

    expect(req.request.method).toBe('GET');
    expect(result).toEqual([post]);
  });

  it('create calls POST /posts with the form data', () => {
    const request: CreatePostRequest = { topicId: 3, title: 'Titre', content: 'Contenu' };
    let result: Post | undefined;

    service.create(request).subscribe((created) => (result = created));
    const req = httpTesting.expectOne(url);
    req.flush(post);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    expect(result).toEqual(post);
  });

  it('getById calls GET /posts/{id}', () => {
    let result: Post | undefined;

    service.getById(42).subscribe((found) => (result = found));
    httpTesting.expectOne(`${url}/42`).flush(post);

    expect(result).toEqual(post);
  });
});
