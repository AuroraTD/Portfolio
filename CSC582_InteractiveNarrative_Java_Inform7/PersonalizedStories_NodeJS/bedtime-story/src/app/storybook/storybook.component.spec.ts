import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { StorybookComponent } from './storybook.component';

describe('StorybookComponent', () => {
  let component: StorybookComponent;
  let fixture: ComponentFixture<StorybookComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ StorybookComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(StorybookComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
