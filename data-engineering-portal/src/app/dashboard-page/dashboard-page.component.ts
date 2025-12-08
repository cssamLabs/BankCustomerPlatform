import { Component, inject, OnInit } from '@angular/core';
import { Breakpoints, BreakpointObserver } from '@angular/cdk/layout';
import { map } from 'rxjs/operators';
import { AsyncPipe } from '@angular/common';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { CommonModule, KeyValuePipe } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Api as ApiService } from '../services/api';
import { Observable } from 'rxjs';

import { TriggerCard as TriggerCardComponent } from '../components/trigger-card/trigger-card';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dashboard-page',
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.css',
  imports: [
    AsyncPipe,
    CommonModule,
    KeyValuePipe,
    MatGridListModule,
    MatMenuModule,
    MatIconModule,
    MatButtonModule,
    TriggerCardComponent,
    MatCardModule,
    HttpClientModule,
    RouterModule,
  ],
})
export class DashboardPageComponent implements OnInit {
  countsByLocation$!: Observable<any>;
  spendingByCategory$!: Observable<any>;
  bankAAverages$!: Observable<any>;
  bankBAverages$!: Observable<any>;
  overallAverage$!: Observable<any>;

  constructor(private apiService: ApiService) {}

  ngOnInit(): void {
    this.fetchData();
  }

  fetchData(): void {
    this.countsByLocation$ = this.apiService.getCountsByLocation();
    this.spendingByCategory$ = this.apiService.getSpendingByCategory();
    this.bankAAverages$ = this.apiService.getComparativeSpending('BANK_A');
    this.bankBAverages$ = this.apiService.getComparativeSpending('BANK_B');
    this.overallAverage$ = this.apiService.getOverallPlatformAverage();
  }
}
