import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Api } from '../services/api';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-data-generation-page',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatSelectModule,
    MatFormFieldModule,
    MatProgressSpinnerModule,
    RouterModule,
    MatIconModule,
    FormsModule,
  ],
  templateUrl: './data-generation-page.component.html',
  styleUrls: ['./data-generation-page.component.css'],
})
export class DataGenerationPageComponent {
  isLoading = false;
  message = '';
  count = 100; // Default count as per backend controller

  constructor(private apiService: Api, private cdr: ChangeDetectorRef) {}

  triggerGeneration(bankId: string) {
    this.isLoading = true;
    this.message = `Triggering generation for ${bankId} with ${this.count} records...`;

    // Use the correct API call
    this.apiService.generateDataForBank(bankId, this.count).subscribe({
      next: (response) => {
        console.log('API Success:', response);
        this.message = `Generated ${response.recordsGenerated} records for ${bankId}.`;
        this.isLoading = false;

        // Explicitly tell Angular to update the view now
        this.cdr.detectChanges();

        setTimeout(() => {
          this.message = '';
          this.cdr.detectChanges(); // Also detect changes when clearing message
        }, 3000);
      },
      error: (err) => {
        this.message = `Error triggering generation for ${bankId}.`;
        this.isLoading = false;
        this.cdr.detectChanges(); // Explicitly detect changes on error
        console.error('API Error:', err);
      },
      complete: () => {
        // This runs whether it was success or error, when observable completes
        console.log('API request completed.');
        // We can safely hide spinner here too if needed, but next/error should handle it
      },
    });
  }
}
