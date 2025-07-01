import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgApexchartsModule } from 'ng-apexcharts';
import {
  ApexAxisChartSeries,
  ApexChart,
  ApexDataLabels,
  ApexPlotOptions,
  ApexResponsive,
  ApexXAxis,
  ApexLegend,
  ApexFill
} from 'ng-apexcharts';

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
  selector: 'app-bar-chart',
  standalone: true,
  imports: [CommonModule, NgApexchartsModule],
  template: `
    <apx-chart
      [series]="chartOptions.series"
      [chart]="chartOptions.chart"
      [colors]="chartOptions.colors"
      [xaxis]="chartOptions.xaxis"
      [plotOptions]="chartOptions.plotOptions"
      [legend]="chartOptions.legend"
      [fill]="chartOptions.fill"
      [dataLabels]="chartOptions.dataLabels"
      [responsive]="chartOptions.responsive">
    </apx-chart>
  `,
})
export class BarChartComponentComponent {
    @Input() chartOptions!: BarChartOptions;
}
