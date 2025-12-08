import { Routes } from '@angular/router';
import { DashboardPageComponent } from './dashboard-page/dashboard-page.component';
import { SegmentPredictorComponent } from './components/segment-predictor/segment-predictor.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardPageComponent },
  { path: 'predict', component: SegmentPredictorComponent },
];
