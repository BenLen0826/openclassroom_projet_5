import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { SubscriptionService } from './subscription.service';

describe('SubscriptionService', () => {
  const url = `${environment.apiUrl}/users/me/subscriptions/3`;
  let service: SubscriptionService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SubscriptionService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('subscribe calls POST /users/me/subscriptions/{topicId}', () => {
    const done = vi.fn();

    service.subscribe(3).subscribe({ complete: done });
    const req = httpTesting.expectOne(url);
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(req.request.method).toBe('POST');
    expect(done).toHaveBeenCalled();
  });

  it('unsubscribe calls DELETE /users/me/subscriptions/{topicId}', () => {
    service.unsubscribe(3).subscribe();
    const req = httpTesting.expectOne(url);
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(req.request.method).toBe('DELETE');
  });
});
