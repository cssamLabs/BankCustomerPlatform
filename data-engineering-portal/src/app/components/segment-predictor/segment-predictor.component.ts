import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Api } from '../../services/api';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CustomerProfile } from '../../models/segmentation.types';
import { MatIconModule } from '@angular/material/icon';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-segment-predictor',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterModule,
  ],
  templateUrl: './segment-predictor.component.html',
  styleUrls: ['./segment-predictor.component.css'],
})
export class SegmentPredictorComponent {
  predictionForm: FormGroup;
  predictedSegment: number | null = null;
  isLoading = false;

  // Form fields list for easy iteration in HTML template
  spendingCategories = ['Utilities', 'Groceries', 'Transport', 'Shopping', 'Dining', 'Other'];

  constructor(private fb: FormBuilder, private apiService: Api) {
    // Initialize form with default values and validation
    const formControls: any = {};
    this.spendingCategories.forEach((category) => {
      formControls[category] = this.fb.control(0, [Validators.required, Validators.min(0)]);
    });

    this.predictionForm = this.fb.group(formControls);
  }

  onSubmit(): void {
    if (this.predictionForm.valid) {
      this.isLoading = true;
      this.predictedSegment = null;

      const formValue = this.predictionForm.value;
      // Convert numeric inputs to string format expected by Python API
      const profile: CustomerProfile = {
        Utilities: formValue.Utilities.toString(),
        Groceries: formValue.Groceries.toString(),
        Transport: formValue.Transport.toString(),
        Shopping: formValue.Shopping.toString(),
        Dining: formValue.Dining.toString(),
        Other: formValue.Other.toString(),
      };

      this.apiService.predictCustomerSegment([profile]).subscribe({
        next: (response) => {
          // The response.predictions is a list, we take the first item
          this.predictedSegment = response.predictions[0];
          this.isLoading = false;
        },
        error: (err) => {
          console.error('Prediction failed:', err);
          this.isLoading = false;
          this.predictedSegment = null;
        },
      });
    }
  }
}
