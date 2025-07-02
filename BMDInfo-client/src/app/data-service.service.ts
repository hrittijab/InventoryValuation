import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BidTracker } from './interface/bid-tracker';
import { SubmissionData } from './interface/submission-data';

@Injectable({
  providedIn: 'root'
})
export class DataServiceService {

  constructor(private http: HttpClient) { }

 // Local
  private baseUrl = "http://localhost:8080/api";

  // Production
  // private baseUrl = "http://43.225.151.41:4000/api/v1";

  // User Management.
  public bidTrackerUrl = "/bid-tracker-sheets";

  public getBidTrackerData(){
   return this.http.get<BidTracker[]>(this.baseUrl + this.bidTrackerUrl);
  }

  public sheetDataUrl = "/bid-tracker-sheets";

  public getSpradeSheetData(){
   return this.http.post(this.baseUrl + this.sheetDataUrl + "/import", {});
  }

  public getSpradeSheetDataByDate(fromDate: any, toDate: any){
   return this.http.get<BidTracker[]>(this.baseUrl + this.sheetDataUrl + "/get-by-date?fromDate=" + fromDate + "&toDate=" + toDate);
  }

  public getTotalSamary(){
    return this.http.get<any>(this.baseUrl + this.sheetDataUrl + "/total-summary");
  }

 public getSubmitionSamary(){
    return this.http.get<any>(this.baseUrl + this.sheetDataUrl + "/monthly-submission-summary");
  }

  
}
