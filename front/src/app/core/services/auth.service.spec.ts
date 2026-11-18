import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { AuthResponse, RegisterRequest } from '../models/auth';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('register calls POST /auth/register with the form data and returns the token', () => {
    // Arrange
    const request: RegisterRequest = {
      username: 'bob',
      email: 'bob@mail.fr',
      password: 'Secret1!',
    };
    let result: AuthResponse | undefined;

    // Act
    service.register(request).subscribe((response) => (result = response));
    const req = httpTesting.expectOne(`${environment.apiUrl}/auth/register`);
    req.flush({ token: 'jwt' });

    // Assert
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    expect(result).toEqual({ token: 'jwt' });
  });
});
