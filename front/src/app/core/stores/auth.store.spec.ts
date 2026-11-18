import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RegisterRequest } from '../models/auth';
import { AuthService } from '../services/auth.service';
import { AuthStore, TOKEN_STORAGE_KEY } from './auth.store';

describe('AuthStore', () => {
  const request: RegisterRequest = { username: 'bob', email: 'bob@mail.fr', password: 'Secret1!' };
  let authService: { register: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    localStorage.clear();
    authService = { register: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: authService }],
    });
  });

  it('starts without token when nothing is stored', () => {
    // Act
    const store = TestBed.inject(AuthStore);

    // Assert
    expect(TOKEN_STORAGE_KEY).toBe('mdd_token');
    expect(store.token()).toBeNull();
    expect(store.isAuthenticated()).toBe(false);
  });

  it('restores the token kept in localStorage (persistent login)', () => {
    // Arrange
    localStorage.setItem('mdd_token', 'saved-jwt');

    // Act
    const store = TestBed.inject(AuthStore);

    // Assert
    expect(store.token()).toBe('saved-jwt');
    expect(store.isAuthenticated()).toBe(true);
  });

  it('register stores the token in the signal and in localStorage', () => {
    // Arrange
    authService.register.mockReturnValue(of({ token: 'jwt' }));
    const store = TestBed.inject(AuthStore);
    const next = vi.fn();

    // Act
    store.register(request).subscribe(next);

    // Assert
    expect(authService.register).toHaveBeenCalledWith(request);
    expect(next).toHaveBeenCalledTimes(1);
    expect(store.token()).toBe('jwt');
    expect(store.isAuthenticated()).toBe(true);
    expect(localStorage.getItem('mdd_token')).toBe('jwt');
  });

  it('register relays the error and stores nothing', () => {
    // Arrange
    const failure = new Error('409');
    authService.register.mockReturnValue(throwError(() => failure));
    const store = TestBed.inject(AuthStore);
    const error = vi.fn();

    // Act
    store.register(request).subscribe({ error });

    // Assert
    expect(error).toHaveBeenCalledWith(failure);
    expect(store.token()).toBeNull();
    expect(localStorage.getItem('mdd_token')).toBeNull();
  });
});
