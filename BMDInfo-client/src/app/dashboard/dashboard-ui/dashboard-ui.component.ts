import { DataServiceService } from '../../data-service.service';
import { Router } from '@angular/router';
import {isPlatformBrowser, formatDate, CommonModule, NgFor, NgIf } from '@angular/common';
import { BidTracker } from '../../interface/bid-tracker';
import { BidTrackerDetailsComponentComponent } from '../bid-tracker-details-component/bid-tracker-details-component.component';

import {
  Component,
  OnInit,
  ViewContainerRef,
  inject,
  Inject,
  PLATFORM_ID,
  ViewChild,
  ElementRef
} from '@angular/core';

import {
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexChart,
  ApexDataLabels,
  ApexTooltip
} from "ng-apexcharts";
import { log } from 'node:console';

export type ChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  responsive: ApexResponsive[];
  labels: any;
  dataLabels?: ApexDataLabels;
  tooltip?: ApexTooltip;
};


@Component({
  selector: 'app-dashboard-ui',
  standalone: true,
  imports: [NgFor, NgIf, CommonModule, BidTrackerDetailsComponentComponent,],
  templateUrl: './dashboard-ui.component.html',
  styleUrl: './dashboard-ui.component.scss'
})

export class DashboardUiComponent implements OnInit {
  fromDate: string = '';
  toDate: string = '';

  allSpradSheetData: BidTracker[] = [];
  paginatedData: any[] = [];
  currentPage = 1;
  pageSize = 20;
  selectedItem: BidTracker | null = null;

  Submissions: number = 0;
  Won: number = 0;
  Pending: number = 0;

  // private vcr = inject(ViewContainerRef);
  @ViewChild('chartContainer', { read: ViewContainerRef, static: true })
  chartContainer!: ViewContainerRef;
  isBrowser: boolean;
  isContainBidTrackerUrl: boolean = false;

  constructor(
    private router: Router,
    private dataService: DataServiceService,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
        const today = new Date();
    const before7 = new Date(today);
    before7.setDate(today.getDate() - 7);

    const after21 = new Date(today);
    after21.setDate(today.getDate() + 21);

    this.fromDate = formatDate(before7, 'EEE, MMM d, y', 'en-US');
    this.toDate = formatDate(after21, 'EEE, MMM d, y', 'en-US');
  }

public chartOptions: ChartOptions = {
  series: [0, 0, 0],

  chart: {
    width: 380,
    type: 'pie',
  },
  labels: ['Submissions', 'Won', 'Pending'],
  dataLabels: {
    enabled: true,
    formatter: (val, opts) => {
      // opts.w.globals.series[opts.seriesIndex] is the raw value
      return opts.w.globals.series[opts.seriesIndex].toString();
    },
  },
  tooltip: {
    y: {
      formatter: (val) => val.toString(),
    },
  },
  responsive: [
    {
      breakpoint: 480,
      options: {
        chart: {
          width: 200,
        },
        legend: {
          position: 'bottom',
        },
      },
    },
  ],
};
showReport() {
  const from = new Date(this.fromDate);
  const to = new Date(this.toDate);

  let count = 1;
  let skipped = 0;

  const filteredData = [];  // collect matching items

  for (const item of this.allSpradSheetData) {
    if (item.submissionDate) {
      const subDate = new Date(item.submissionDate);

      if (subDate >= from && subDate <= to) {
        filteredData.push(item);  // add to result array
        console.log(`${count++}.`, item.submissionDate, item);
      } else {
        skipped++;
      }
    } else {
      skipped++;
    }
  }

  this.allSpradSheetData = filteredData;  // assign once after loop
  this.updatePaginatedData();

  console.log(`Skipped: ${skipped}`);
}

countSubmission() {
  this.Submissions = 0;
  this.Won = 0;
  this.Pending = 0;

  for (const item of this.allSpradSheetData) {
    // Count "Submitted" in submission field
    if (item.submission && item.submission.toLowerCase().includes('submitted')) {
      this.Submissions++;
    }

    // Count "Won" in result field
    if (item.result && item.result.toLowerCase().includes('won')) {
      this.Won++;
    }

    // Count "Pending" in result field
    if (item.result && item.result.toLowerCase().includes('pending')) {
      this.Pending++;
    }

  }
  
  this.chartOptions.series = [this.Submissions, this.Won, this.Pending];

  console.log(`✅ Total Submissions: ${this.Submissions}`);
  console.log(`🏆 Total Wins: ${this.Won}`);
  console.log(`⏳ Total Pending: ${this.Pending}`);


}



  ngOnInit() {
    this.isContainBidTrackerUrl = this.router.url.includes('bid-tracker');
    this.dataService.getBidTrackerData().subscribe({
      next: (r) => {
        this.allSpradSheetData = r;
        this.updatePaginatedData();
        this.countSubmission();
        if(this.isContainBidTrackerUrl == false) {
        this.showReport();
        }
      },
      error: (err) => console.error(err),
    });
   if (this.isBrowser && this.isContainBidTrackerUrl) {
    import('../pie-chart-component/pie-chart-component.component').then(({ PieChartComponent }) => {
     const chartRef = this.chartContainer.createComponent(PieChartComponent);
      chartRef.setInput('chartOptions', this.chartOptions);
    });
   }
  }

  updatePaginatedData() {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedData = this.allSpradSheetData.slice(startIndex, endIndex);
  }

  totalPages(): number {
    return Math.ceil(this.allSpradSheetData.length / this.pageSize);
  }

  changePage(page: number) {
    if (page < 1 || page > this.totalPages()) return;
    this.currentPage = page;
    this.updatePaginatedData();
  }

  goBack() {
    this.selectedItem = null;
  }
}
