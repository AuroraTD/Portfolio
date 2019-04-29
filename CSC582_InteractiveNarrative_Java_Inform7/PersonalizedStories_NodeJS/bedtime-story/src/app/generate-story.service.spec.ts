import { TestBed } from '@angular/core/testing';

import { GenerateStoryService } from './generate-story.service';

describe('GenerateStoryService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: GenerateStoryService = TestBed.get(GenerateStoryService);
    expect(service).toBeTruthy();
  });
});
