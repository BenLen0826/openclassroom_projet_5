import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Topic } from '../models/topic';
import { TopicService } from './topic.service';

describe('TopicService', () => {
  let service: TopicService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TopicService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Aucune requête inattendue ne doit rester en attente
    httpTesting.verify();
  });

  it('getAll calls GET /topics and returns the topics', () => {
    // Arrange
    const topics: Topic[] = [
      { id: 1, name: 'Angular', description: 'Front-end', subscribed: false },
    ];
    let result: Topic[] | undefined;

    // Act
    service.getAll().subscribe((value) => (result = value));
    const request = httpTesting.expectOne(`${environment.apiUrl}/topics`);
    request.flush(topics);

    // Assert
    expect(request.request.method).toBe('GET');
    expect(result).toEqual(topics);
  });
});
