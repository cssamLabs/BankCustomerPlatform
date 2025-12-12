import { Component, EventEmitter, Output, ChangeDetectorRef } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { Api as ApiService } from '../services/api';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-data-modeling-page',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatProgressSpinnerModule, MatCardModule, MatIconModule],
  templateUrl: './data-modeling-page.component.html',
})
export class DataModelingPageComponent {
  isLoading = false;
  message: string | null = null;
  // ... (add methods triggerDataIngestion() and triggerModelRetraining() using the Api service) ...
  constructor(private apiService: ApiService, private cdr: ChangeDetectorRef) {}
  @Output() jobTriggered = new EventEmitter<void>();

  triggerDataIngestion(): void {
    this.isLoading = true;
    this.message = 'Job started, waiting for completion...';
    this.apiService.startIngestionForAllBanks().subscribe({
      next: (response) => {
        this.message = 'Ingestion command sent. Data will refresh shortly.';
        this.isLoading = false;
        console.log(response);
        // Explicitly tell Angular to update the view now
        this.cdr.detectChanges();
        // Notify the dashboard to reload data after a delay
        setTimeout(() => this.jobTriggered.emit(), 3000);
      },
      error: (err) => {
        this.message = 'Error starting job.';
        console.error(err);
        this.cdr.detectChanges(); // Explicitly detect changes on error
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      },
    });
  }

  // Add a new method to trigger retraining
  triggerModelRetraining(): void {
    this.isLoading = true;
    this.message = 'Model retraining initiated...';
    this.apiService.triggerModelRetraining().subscribe({
      next: (response) => {
        this.message = response.message || 'Retraining started successfully.';
        this.isLoading = false; // The backend returned 202 ACCEPTED immediately
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.message = 'Error initiating retraining.';
        console.error(err);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      complete: () => {
        this.isLoading = false;
      },
    });
  }
}
