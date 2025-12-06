import { Component, EventEmitter, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { Api as ApiService } from '../../services/api';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-trigger-card',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatProgressSpinnerModule, MatCardModule],
  templateUrl: './trigger-card.html',
  styleUrl: './trigger-card.css',
})
export class TriggerCard {
  @Output() jobTriggered = new EventEmitter<void>();
  isLoading = false;
  message: string | null = null;

  constructor(private apiService: ApiService) {}

  runIngestion(): void {
    this.isLoading = true;
    this.message = 'Job started, waiting for completion...';
    this.apiService.startIngestionForAllBanks().subscribe({
      next: (response) => {
        this.message = 'Ingestion command sent. Data will refresh shortly.';
        console.log(response);
        // Notify the dashboard to reload data after a delay
        setTimeout(() => this.jobTriggered.emit(), 3000);
      },
      error: (err) => {
        this.message = 'Error starting job.';
        console.error(err);
        this.isLoading = false;
      },
      complete: () => {
        this.isLoading = false;
      },
    });
  }
}
