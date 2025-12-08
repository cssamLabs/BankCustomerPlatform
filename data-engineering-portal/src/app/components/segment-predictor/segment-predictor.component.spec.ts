import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SegmentPredictorComponent } from './segment-predictor.component';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { provideAnimations } from '@angular/platform-browser/animations'; // Import the standalone provider
import { Api } from '../../services/api';

describe('SegmentPredictorComponent', () => {
  let component: SegmentPredictorComponent;
  let fixture: ComponentFixture<SegmentPredictorComponent>;
  let apiServiceSpy: jasmine.SpyObj<Api>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('Api', ['predictCustomerSegment']);

    await TestBed.configureTestingModule({
      imports: [
        SegmentPredictorComponent, // Import the standalone component
        ReactiveFormsModule,
        HttpClientModule,
        MatCardModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
      ],
      providers: [
        FormBuilder,
        { provide: Api, useValue: spy },
        provideAnimations(), // Correctly provide animations for the test bed
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SegmentPredictorComponent);
    component = fixture.componentInstance;
    apiServiceSpy = TestBed.inject(Api) as jasmine.SpyObj<Api>;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  // ... (rest of the tests remain the same) ...
});
