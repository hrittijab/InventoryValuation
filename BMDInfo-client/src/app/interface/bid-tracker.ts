export interface BidTracker {
  id?: number; // optional if not present during creation

  client: string;
  description: string;
  cam: string;
  salesResource: string;
  bidManager: string;
  initiationMode: string;
  stage: string;
  procurementType: string;
  goods: string;
  works: string;
  service: string;
  tenderId: string;
  initiation: string;
  published_initiation: string;

  published: string;         // Format: "EEE, MMM dd, yyyy"
  preBidDate: string;        // Format: "EEE, MMM dd, yyyy"
  preBidTime: string;        // Format: "h:mm:ss a"
  submissionDate: string;    // Format: "EEE, MMM dd, yyyy"
  submissionTime: string;    // Format: "h:mm:ss a"

  securityMode: string;
  securityAmount: number;
  creditFacility: number;
  issuingDate: string;       // Format: "EEE, MMM dd, yyyy"

  referenceNumber: string;
  issuingBank: string;
  expiryDate: string;              // Format: "dd MMM,yyyy"
  submission: string;
  result: string;
  noName: string;
  remarks: string;
}
