import { Injectable } from '@angular/core';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { CustomerProfile, PredictionResponse } from '../models/segmentation.types';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Api {
  private baseApiUrl = environment.apiUrl;
  private apiUrlAnalytics = '${this.baseApiUrl}/api/v1/analytics';
  private apiUrlIngestion = '${this.baseApiUrl}/api/v1/ingestion';
  private apiUrlSegmentation = '${this.baseApiUrl}/api/v1/segmentation';

  constructor(private http: HttpClient) {}

  getCountsByLocation(): Observable<any> {
    return this.http.get(`${this.apiUrlAnalytics}/counts-by-location`);
  }

  getSpendingByCategory(): Observable<any> {
    return this.http.get(`${this.apiUrlAnalytics}/spending-by-category`);
  }

  getComparativeSpending(bankId: string): Observable<any> {
    // Note: The backend expects BANK_A/BANK_B in uppercase in our current setup
    return this.http.get(`${this.apiUrlAnalytics}/compare-spending/${bankId.toUpperCase()}`);
  }

  getOverallPlatformAverage(): Observable<any> {
    return this.http.get<any>(`${this.apiUrlAnalytics}/average-spending/overall`);
  }

  startIngestionForAllBanks(): Observable<any> {
    return this.http.post(`${this.apiUrlIngestion}/start-all`, {});
  }

  triggerModelRetraining(): Observable<any> {
    return this.http.post(`${this.apiUrlSegmentation}/trigger-training`, {});
  }

  predictCustomerSegment(profiles: CustomerProfile[]): Observable<PredictionResponse> {
    return this.http.post<PredictionResponse>(
      `${this.apiUrlSegmentation}/predict-segment`,
      profiles
    );
  }
}
