import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BidTracker } from './interface/bid-tracker';

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
  
}
