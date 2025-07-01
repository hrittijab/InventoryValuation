import { DataServiceService } from '../../data-service.service';
import { Router } from '@angular/router';
import {
  isPlatformBrowser,
  formatDate,
  CommonModule,
  NgFor,
  NgIf
} from '@angular/common';
import { BidTracker } from '../../interface/bid-tracker';
import { BidTrackerDetailsComponentComponent } from '../bid-tracker-details-component/bid-tracker-details-component.component';

import {
  Component,
  OnInit,
  ViewContainerRef,
  Inject,
  PLATFORM_ID,
  ViewChild
} from '@angular/core';

import {
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexChart,
  ApexDataLabels,
  ApexTooltip,
  ApexAxisChartSeries,
  ApexPlotOptions,
  ApexXAxis,
  ApexLegend,
  ApexFill
} from 'ng-apexcharts';


export type PieChartOptions = {
  series: ApexNonAxisChartSeries;
  chart: ApexChart;
  responsive: ApexResponsive[];
  labels: any;
  dataLabels?: ApexDataLabels;
  tooltip?: ApexTooltip;
};

export type BarChartOptions = {
  series: ApexAxisChartSeries;
  chart: ApexChart;
  colors: string[];
  dataLabels: ApexDataLabels;
  plotOptions: ApexPlotOptions;
  responsive: ApexResponsive[];
  xaxis: ApexXAxis;
  legend: ApexLegend;
  fill: ApexFill;
};

@Component({
  selector: 'app-dashboard-ui',
  standalone: true,
  imports: [NgFor, NgIf, CommonModule, BidTrackerDetailsComponentComponent],
  templateUrl: './dashboard-ui.component.html',
  styleUrl: './dashboard-ui.component.scss',
})
export class DashboardUiComponent implements OnInit {
  fromDate: string = '';
  toDate: string = '';

  allSpradSheetData: BidTracker[] = [];
  paginatedData: any[] = [];
  currentPage = 1;
  pageSize = 20;
  selectedItem: BidTracker | null = null;

  Submissions = 0;
  Won = 0;
  Pending = 0;

  @ViewChild('chartContainer', { read: ViewContainerRef, static: true })
  chartContainer!: ViewContainerRef;

  isBrowser: boolean;
  isContainBidTrackerUrl = false;

  pieChartOptions: PieChartOptions = {
    series: [0, 0, 0],
    chart: {
      width: 380,
      type: 'pie',
    },
    labels: ['Submissions', 'Won', 'Pending'],
    dataLabels: {
      enabled: true,
      formatter: (val, opts) => {
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

  barChartOptions: BarChartOptions = {
    series: [
      {
        name: 'Submited',
        data: [44, 55, 41, 67, 22, 43],
      },
      {
        name: 'Not Submited',
        data: [13, 23, 20, 8, 13, 27],
      }
    ],
    chart: {
      type: 'bar',
      height: 350,
      stacked: true,
      toolbar: { show: false },
      zoom: { enabled: true },
    },
    colors: ['#1E90FF', '#DC3545'], // <-- CUSTOM COLORS
    plotOptions: {
      bar: {
        horizontal: false,
      },
    },
    xaxis: {
      type: 'category',
      categories: [
        '01/2025',
        '02/2025',
        '03/2025',
        '04/2025',
        '05/2025',
        '06/2025',
      ],
    },
    legend: {
      position: 'right',
      offsetY: 40,
    },
    fill: {
      opacity: 1,
    },
    dataLabels: {
      enabled: false,
    },
    responsive: [
      {
        breakpoint: 480,
        options: {
          legend: {
            position: 'bottom',
            offsetX: -10,
            offsetY: 0,
          },
        },
      },
    ],
  };

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

  ngOnInit() {
    this.isContainBidTrackerUrl = this.router.url.includes('bid-tracker');

    this.dataService.getBidTrackerData().subscribe({
      next: (r) => {
        this.allSpradSheetData = r;
        this.updatePaginatedData();
        this.countSubmission();
        if (!this.isContainBidTrackerUrl) {
          this.showReport();
        }
      },
      error: (err) => console.error(err),
    });

    if (this.isBrowser) {
      import('../pie-chart-component/pie-chart-component.component').then(
        ({ PieChartComponent }) => {
          const pieRef = this.chartContainer.createComponent(PieChartComponent);
          pieRef.setInput('chartOptions', this.pieChartOptions);
        }
      );

      import('../../bar-chart-component/bar-chart-component.component').then(
        ({ BarChartComponentComponent }) => {
          const barRef = this.chartContainer.createComponent(BarChartComponentComponent);
          barRef.setInput('chartOptions', this.barChartOptions);
        }
      );
    }
  }

  showReport() {
    const from = new Date(this.fromDate);
    const to = new Date(this.toDate);

    let count = 1;
    let skipped = 0;
    const filteredData = [];

    for (const item of this.allSpradSheetData) {
      if (item.submissionDate) {
        const subDate = new Date(item.submissionDate);
        if (subDate >= from && subDate <= to) {
          filteredData.push(item);
          console.log(`${count++}.`, item.submissionDate, item);
        } else {
          skipped++;
        }
      } else {
        skipped++;
      }
    }

    this.allSpradSheetData = filteredData;
    this.updatePaginatedData();

    console.log(`Skipped: ${skipped}`);
  }

  countSubmission() {
    this.Submissions = 0;
    this.Won = 0;
    this.Pending = 0;

    for (const item of this.allSpradSheetData) {
      if (
        item.submission &&
        item.submission.toLowerCase().includes('submitted')
      ) {
        this.Submissions++;
      }

      if (item.result && item.result.toLowerCase().includes('won')) {
        this.Won++;
      }

      if (item.result && item.result.toLowerCase().includes('pending')) {
        this.Pending++;
      }
    }

    this.pieChartOptions.series = [this.Submissions, this.Won, this.Pending];

    console.log(`✅ Total Submissions: ${this.Submissions}`);
    console.log(`🏆 Total Wins: ${this.Won}`);
    console.log(`⏳ Total Pending: ${this.Pending}`);
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
