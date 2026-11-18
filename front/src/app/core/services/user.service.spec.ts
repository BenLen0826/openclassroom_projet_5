import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { UpdateProfileRequest, User } from '../models/user';
import { UserService } from './user.service';

describe('UserService', () => {
  const url = `${environment.apiUrl}/users/me`;
  const bob: User = { id: 7, username: 'bob', email: 'bob@mail.fr' };
  let service: UserService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UserService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('getMe calls GET /users/me', () => {
    let result: User | undefined;

    service.getMe().subscribe((user) => (result = user));
    const req = httpTesting.expectOne(url);
    req.flush(bob);

    expect(req.request.method).toBe('GET');
    expect(result).toEqual(bob);
  });

  it('updateMe calls PUT /users/me with the form data', () => {
    const request: UpdateProfileRequest = { username: 'bob', email: 'bob@mail.fr', password: '' };
    let result: User | undefined;

    service.updateMe(request).subscribe((user) => (result = user));
    const req = httpTesting.expectOne(url);
    req.flush(bob);

    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    expect(result).toEqual(bob);
  });
});
