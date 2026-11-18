import { TestBed } from '@angular/core/testing';
import { CommentItem } from './comment-item';

describe('CommentItem', () => {
  it('shows the author and the content of the comment', async () => {
    TestBed.configureTestingModule({ imports: [CommentItem] });
    const fixture = TestBed.createComponent(CommentItem);
    fixture.componentRef.setInput('comment', {
      id: 1,
      content: 'Bravo',
      createdAt: '2026-11-18T12:00:00Z',
      authorUsername: 'alice',
    });
    await fixture.whenStable();
    const item = fixture.nativeElement as HTMLElement;

    expect(item.querySelector('strong')?.textContent?.trim()).toBe('alice');
    expect(item.textContent).toContain('Bravo');
  });
});
