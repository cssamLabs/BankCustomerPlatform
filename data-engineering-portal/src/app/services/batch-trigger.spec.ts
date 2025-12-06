import { TestBed } from '@angular/core/testing';

import { BatchTrigger } from './batch-trigger';

describe('BatchTrigger', () => {
  let service: BatchTrigger;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BatchTrigger);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
