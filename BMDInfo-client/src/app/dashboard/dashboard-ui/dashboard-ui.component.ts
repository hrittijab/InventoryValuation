import { DataServiceService } from '../../data-service.service';
import { Router } from '@angular/router';
import {isPlatformBrowser, CommonModule, NgFor, NgIf } from '@angular/common';
import { BidTracker } from '../../interface/bid-tracker';
import { BidTrackerDetailsComponentComponent } from '../bid-tracker-details-component/bid-tracker-details-component.component';

import {
  Component,
  OnInit,
  ViewContainerRef,
  inject,
  Inject,
  PLATFORM_ID,
  ViewChild
} from '@angular/core';

import {
  ApexNonAxisChartSeries,
  ApexResponsive,
  ApexChart,
  ApexDataLabels,
  ApexTooltip
} from "ng-apexcharts";

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

  allSpradSheetData: any[] = [];
  paginatedData: any[] = [];
  currentPage = 1;
  pageSize = 20;
  selectedItem: BidTracker | null = null;


  // private vcr = inject(ViewContainerRef);
  @ViewChild('chartContainer', { read: ViewContainerRef, static: true })
  chartContainer!: ViewContainerRef;
  isBrowser: boolean;

  constructor(
    private router: Router,
    private dataService: DataServiceService,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

public chartOptions: ChartOptions = {
  series: [44, 55, 13],
  chart: {
    width: 380,
    type: 'pie',
  },
  labels: ['Submition', 'Published', 'Result'],
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


  ngOnInit() {
    this.dataService.getBidTrackerData().subscribe({
      next: (r) => {
        this.allSpradSheetData = r;
        this.updatePaginatedData();
      },
      error: (err) => console.error(err),
    });
      if (this.isBrowser) {
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
