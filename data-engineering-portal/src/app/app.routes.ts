import { Routes } from '@angular/router';
import { DashboardPageComponent } from './dashboard-page/dashboard-page.component';
import { SegmentPredictorComponent } from './components/segment-predictor/segment-predictor.component';
import { DataGenerationPageComponent } from './data-generation-page/data-generation-page.component';
import { DataModelingPageComponent } from './data-modeling-page/data-modeling-page.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardPageComponent },
  { path: 'predict', component: SegmentPredictorComponent },
  { path: 'generate-data', component: DataGenerationPageComponent },
  { path: 'ingestion', component: DataModelingPageComponent },
];
