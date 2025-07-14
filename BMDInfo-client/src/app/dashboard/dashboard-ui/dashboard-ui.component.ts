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
  ViewChild,
  ElementRef
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
import { FormsModule } from '@angular/forms';
import { SubmissionData } from '../../interface/submission-data';

import * as html2pdf from 'html2pdf.js';

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
  imports: [NgFor, FormsModule, NgIf, CommonModule, BidTrackerDetailsComponentComponent],
  templateUrl: './dashboard-ui.component.html',
  styleUrl: './dashboard-ui.component.scss',
})
export class DashboardUiComponent implements OnInit {

  fromDateRaw!: string;  // bound to input[type=date]
  toDateRaw!: string;

  fromDate: string = '';
  toDate: string = '';

  allSpradSheetData: BidTracker[] = [];
  paginatedData: any[] = [];
  currentPage = 1;
  pageSize = 20;
  selectedItem: BidTracker | null = null;

  submitted = 0;
  notSubmitted = 0;
  own = 0;
  lost = 0;
  pending = 0;
  unknown = 0;

  month1Submitted = 0;
  month2Submitted = 0;
  month3Submitted = 0;
  month4Submitted = 0;
  month5Submitted = 0;
  month6Submitted = 0;
  month7Submitted = 0;
  month8Submitted = 0;
  month9Submitted = 0;
  month10Submitted = 0;
  month11Submitted = 0;
  month12Submitted = 0;

  month1NotSubmitted = 0;
  month2NotSubmitted = 0;
  month3NotSubmitted = 0;
  month4NotSubmitted = 0;
  month5NotSubmitted = 0;
  month6NotSubmitted = 0;
  month7NotSubmitted = 0;
  month8NotSubmitted = 0;
  month9NotSubmitted = 0;
  month10NotSubmitted = 0;
  month11NotSubmitted = 0;
  month12NotSubmitted = 0;

  @ViewChild('chartContainer', { read: ViewContainerRef, static: true })
  chartContainer!: ViewContainerRef;

  @ViewChild('chartByClientContainer', { read: ViewContainerRef, static: true })
  chartByClientContainer!: ViewContainerRef;

  isBrowser: boolean;
  isContainBidTrackerUrl = false;

  pieChartOptions: PieChartOptions = {
    series: [0, 0, 0, 0, 0, 0],
    chart: {
      width: 380,
      type: 'pie',
    },
    labels: ['Bid Submitted', 'Not Submitted', 'Won', 'Lost', 'Result Pending', 'Unknown'],
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
        {  name: 'Submited',
           data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        },
        {
           name: 'Not Submited',
           data: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        }
      ],
    chart: {
      type: 'bar',
      height: 350,
      width: 400,
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
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
        '',
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

  barChartOptionsClient: BarChartOptions = {
    series: [
        {  name: 'Submited',
           data: [0],
        },
        {
           name: 'Not Submited',
           data: [0],
        }
      ],
    chart: {
      type: 'bar',
      height: 500,
      width: 1250,
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
      categories: [],
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

    // Raw values in yyyy-MM-dd for date input
    this.fromDateRaw = before7.toISOString().split('T')[0];
    this.toDateRaw = after21.toISOString().split('T')[0];

    // Display values in formatted style
    this.fromDate = formatDate(before7, 'EEE, MMM d, y', 'en-US');
    this.toDate = formatDate(after21, 'EEE, MMM d, y', 'en-US');

  }

  ngOnInit() {
    this.isContainBidTrackerUrl = this.router.url.includes('bid-tracker');

    this.dataService.getBidTrackerData().subscribe({
      next: (r) => {
        if (!this.isContainBidTrackerUrl) {
          this.showReport();
        }else {
        this.allSpradSheetData = r;
        this.updatePaginatedData();
        this.countSubmission();
        }
       
      },
      error: (err) => console.error(err),
    });
  }

  showReport() {
  if (!this.fromDateRaw || !this.toDateRaw) return;

  // Convert raw date strings to Date objects
  this.fromDate = this.formatDateToISO(this.fromDateRaw);
  this.toDate = this.formatDateToISO(this.toDateRaw);
 
  this.dataService.getSpradeSheetDataByDate(this.fromDate, this.toDate).subscribe({
    next: (r) => {
      this.allSpradSheetData = r;
      this.updatePaginatedData();
      this.countSubmission();
     },
      error: (err) => console.error(err),
    });

  }

  totalSumary: any;
  allData: SubmissionData[] = [];
  monthList: string[] = [];
  
  countSubmission() {
    this.submitted = 0;
    this.notSubmitted = 0;
    this.own = 0;
    this.lost = 0;
    this.pending = 0;
    this.unknown = 0;

  if (this.isContainBidTrackerUrl) {
      this.dataService.getTotalSamary().subscribe({
      next: (r) => {
        this.totalSumary = r;
        this.notSubmitted = this.totalSumary[0].total;
        this.submitted = this.totalSumary[1].total;
        this.lost = this.totalSumary[2].total;
        this.pending = this.totalSumary[3].total;
        this.unknown = this.totalSumary[4].total;
        this.own = this.totalSumary[5].total;
        // Update pie chart data
        this.pieChartOptions.series = [
          this.submitted,
          this.notSubmitted,
          this.own,
          this.lost,
          this.pending,
          this.unknown
      ];
     },
      error: (err) => console.error(err),
    });
  }else{
        this.dataService.getTotalSamaryByDateRange(this.fromDateRaw, this.toDateRaw).subscribe({
        next: (r) => {
          console.log(r);
          this.totalSumary = r;
          this.notSubmitted = this.totalSumary[0].total;
          this.submitted = this.totalSumary[1].total;
          this.lost = this.totalSumary[2].total;
          this.pending = this.totalSumary[3].total;
          this.unknown = this.totalSumary[4].total;
          this.own = this.totalSumary[5].total;
          // Update pie chart data
          this.pieChartOptions.series = [
            this.submitted,
            this.notSubmitted,
            this.own,
            this.lost,
            this.pending,
            this.unknown
        ];
      },
        error: (err) => console.error(err),
      });
  }



    this.month1Submitted = 0;
    this.month2Submitted = 0;
    this.month3Submitted = 0;
    this.month4Submitted = 0;
    this.month5Submitted = 0;
    this.month6Submitted = 0;
    this.month7Submitted = 0;
    this.month8Submitted = 0;
    this.month9Submitted = 0;
    this.month10Submitted = 0;
    this.month11Submitted = 0;
    this.month12Submitted = 0;
    
    this.month1NotSubmitted = 0;
    this.month2NotSubmitted = 0;
    this.month3NotSubmitted = 0;
    this.month4NotSubmitted = 0;
    this.month5NotSubmitted = 0;
    this.month6NotSubmitted = 0;
    this.month7NotSubmitted = 0;
    this.month8NotSubmitted = 0;
    this.month9NotSubmitted = 0;
    this.month10NotSubmitted = 0;
    this.month11NotSubmitted = 0;
    this.month12NotSubmitted = 0;


    this.dataService.getSubmitionSamary().subscribe({
    next: (r) => {
      this.month1NotSubmitted = r[0].total;
      this.month1Submitted = r[1].total;
      this.month2NotSubmitted = r[2].total;
      this.month2Submitted = r[3].total;
      this.month3NotSubmitted = r[4].total;
      this.month3Submitted = r[5].total;
      this.month4NotSubmitted = r[6].total;
      this.month4Submitted = r[7].total;
      this.month5NotSubmitted = r[8].total;
      this.month5Submitted = r[9].total;
      this.month6NotSubmitted = r[10].total;
      this.month6Submitted = r[11].total;
      this.month7NotSubmitted = r[12].total;
      this.month7Submitted = r[13].total;
      this.month8NotSubmitted = r[14].total;
      this.month8Submitted = r[15].total;
      this.month9NotSubmitted = r[16].total;
      this.month9Submitted = r[17].total;
      this.month10NotSubmitted = r[18].total;
      this.month10Submitted = r[19].total;
      this.month11NotSubmitted = r[20].total;
      this.month11Submitted = r[21].total;
      this.month12NotSubmitted = r[22].total;
      this.month12Submitted = r[23].total;

     this.allData = r;

      const filtered: SubmissionData[] = this.allData.filter((_, index: number) => index % 2 === 0);

      const months: string[] = filtered.map(item => item.month);

      this.monthList = months;

      this.barChartOptions.series[0].data = [
        this.month1Submitted,
        this.month2Submitted,
        this.month3Submitted,
        this.month4Submitted,
        this.month5Submitted,
        this.month6Submitted,
        this.month7Submitted,
        this.month8Submitted,
        this.month9Submitted,
        this.month10Submitted,
        this.month11Submitted,
        this.month12Submitted
      ];

      this.barChartOptions.series[1].data = [
        this.month1NotSubmitted,
        this.month2NotSubmitted,
        this.month3NotSubmitted,
        this.month4NotSubmitted,
        this.month5NotSubmitted,
        this.month6NotSubmitted,
        this.month7NotSubmitted,
        this.month8NotSubmitted,
        this.month9NotSubmitted,
        this.month10NotSubmitted,
        this.month11NotSubmitted,
        this.month12NotSubmitted
      ];


      this.barChartOptions.xaxis.categories = this.monthList;

   if (this.isBrowser) {
        this.createPiChart();
        this.createBarChart();
    }

     },
      error: (err) => console.error(err),
    });

    if(this.isContainBidTrackerUrl){
       this.getClientSubmitionSamary();
    }
    else {
      this.getClientSubmitionSamaryByDate(this.fromDateRaw, this.toDateRaw);
    }
    
  }

  createPiChart() {
    
    this.chartContainer.clear();

     import('../pie-chart-component/pie-chart-component.component').then(
        ({ PieChartComponent }) => {
          const pieRef = this.chartContainer.createComponent(PieChartComponent);
          pieRef.setInput('chartOptions', this.pieChartOptions);
        }  
      );
  }

  createBarChart(){

    this.chartContainer.clear();

      // only render chart now
      import('../../bar-chart-component/bar-chart-component.component').then(
        ({ BarChartComponentComponent }) => {
          const barRef = this.chartContainer.createComponent(BarChartComponentComponent);
          barRef.setInput('chartOptions', this.barChartOptions);
        }
      );

  }

  createBarChartByClient() {
    this.chartByClientContainer.clear();

          // only render chart now
      import('../../bar-chart-component/bar-chart-component.component').then(
        ({ BarChartComponentComponent }) => {
          const barRef = this.chartByClientContainer.createComponent(BarChartComponentComponent);
          barRef.setInput('chartOptions', this.barChartOptionsClient);
        }
      );
  }
    
  showDetails() {
    this.chartContainer.clear();
    this.chartByClientContainer.clear();
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
    this.createPiChart();
    this.createBarChart();
    this.createBarChartByClient();
    this.selectedItem = null;
  }

  updateFormattedDates() {
  if (this.fromDateRaw) {
    this.fromDate = formatDate(this.fromDateRaw, 'EEE, MMM d, y', 'en-US');
  }
  if (this.toDateRaw) {
    this.toDate = formatDate(this.toDateRaw, 'EEE, MMM d, y', 'en-US');
  }
  }

  formatDateToISO(date: string | Date): string {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0'); // Months are 0-based
  const day = String(d.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
  }

// Client Submission Summary
clientSubmissionSummary: any;

getClientSubmitionSamary() {
  this.dataService.getClientSubmitionSamary().subscribe({
    next: (r) => {
      console.log('Client Submission Summary:', r);
      this.clientSubmissionSummary = r;

      // Map data from API
      const clients: string[] = r.map((item: any) => item.client);
      const submittedCounts: number[] = r.map((item: any) => item.submittedCount);
      const notSubmittedCounts: number[] = r.map((item: any) => item.notSubmittedCount);

      // Inject into chart options
      this.barChartOptionsClient.series[0].data = submittedCounts;
      this.barChartOptionsClient.series[1].data = notSubmittedCounts;
      this.barChartOptionsClient.xaxis.categories = clients;

      // Only render chart if in browser
      if (this.isBrowser) {
        this.createBarChartByClient();
      }
    },
    error: (err) => console.error(err),
  });
}

getClientSubmitionSamaryByDate(from: any, to: any) {
  this.dataService.getClientSubmitionSamaryByDate(from, to).subscribe({
    next: (r) => {
      console.log('Client Submission Summary:', r);
      this.clientSubmissionSummary = r;

      // Map data from API
      const clients: string[] = r.map((item: any) => item.client);
      const submittedCounts: number[] = r.map((item: any) => item.submittedCount);
      const notSubmittedCounts: number[] = r.map((item: any) => item.notSubmittedCount);

      // Inject into chart options
      this.barChartOptionsClient.series[0].data = submittedCounts;
      this.barChartOptionsClient.series[1].data = notSubmittedCounts;
      this.barChartOptionsClient.xaxis.categories = clients;
      // this.barChartOptionsClient.chart.height = clients.length * 150; // Adjust height based on number of clients
      // this.barChartOptionsClient.chart.width = clients.length * 100; // Adjust width based on number of clients
      // console.log(clients);
      // Only render chart if in browser
      if (this.isBrowser) {
        this.createBarChartByClient();
      }
    },
    error: (err) => console.error(err),
  });
}

 @ViewChild('pdfContent', { static: false }) pdfContent!: ElementRef;

async downloadPDF(): Promise<void> {
  const html2pdf = await import('html2pdf.js');

  const options = {
    margin: 0.8,
    filename: 'bid-tracker-details of ' + this.selectedItem?.client + '.pdf',
    image: { type: 'jpeg', quality: 0.98 },
    html2canvas: { scale: 2 },
    jsPDF: { unit: 'in', format: 'a4', orientation: 'portrait' }
  };

  html2pdf.default()
    .from(this.pdfContent.nativeElement)
    .set(options)
    .save();
}


}
