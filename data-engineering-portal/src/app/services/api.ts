import { Injectable } from '@angular/core';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Api {
  private apiUrlAnalytics = 'http://localhost:8082/api/v1/analytics';
  private apiUrlIngestion = 'http://localhost:8083/api/v1/ingestion';

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
}
