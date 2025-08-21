import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

type InventoryItem = {
  id: string;
  sku: string;
  name: string;
  location: string;
  quantity: number;
  unitPrice: number;
};

type DialogData = {
  items: InventoryItem[];
  prefill?: { sku?: string; fromLocation?: string };
};

@Component({
  selector: 'app-transfer-stock-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Transfer Stock</h2>

    <form [formGroup]="form" (ngSubmit)="submit()" style="min-width:340px; display:block; padding-top:8px;">
      <!-- SKU/Item -->
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>Item (SKU — Name)</mat-label>
        <mat-select formControlName="sku" (selectionChange)="onSkuChange()">
          <mat-option *ngFor="let opt of skuOptions" [value]="opt.sku">{{ opt.sku }} — {{ opt.name }}</mat-option>
        </mat-select>
      </mat-form-field>

      <!-- From Location -->
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>From Location</mat-label>
        <mat-select formControlName="fromLocation" (selectionChange)="onFromChange()">
          <mat-option *ngFor="let loc of fromLocationOptions" [value]="loc">{{ loc }}</mat-option>
        </mat-select>
        <div style="font-size:12px; opacity:.75; margin-top:4px" *ngIf="selectedFromAvailable !== null">
          Available here: {{ selectedFromAvailable }}
        </div>
      </mat-form-field>

      <!-- To Location -->
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>To Location</mat-label>
        <mat-select formControlName="toLocation" (selectionChange)="onToChange()">
          <mat-option *ngFor="let loc of toLocationOptions" [value]="loc">{{ loc }}</mat-option>
          <mat-option value="__custom">Other (type new…)</mat-option>
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-100" *ngIf="form.value.toLocation === '__custom'">
        <mat-label>New Location</mat-label>
        <input matInput formControlName="customToLocation" placeholder="e.g., Dhaka-2 depot">
      </mat-form-field>

      <!-- Quantity -->
      <mat-form-field appearance="outline" class="w-100">
        <mat-label>Quantity</mat-label>
        <input matInput type="number" formControlName="quantity" min="1">
        <mat-error *ngIf="form.get('quantity')?.hasError('required')">Quantity is required</mat-error>
        <mat-error *ngIf="form.get('quantity')?.hasError('min')">Must be at least 1</mat-error>
        <mat-error *ngIf="form.get('quantity')?.hasError('exceedsAvailable')">Cannot exceed available ({{ selectedFromAvailable }})</mat-error>
      </mat-form-field>

      <div style="text-align:right; margin-top: 12px;">
        <button mat-button type="button" (click)="close()">Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Transfer</button>
      </div>
    </form>
  `
})
export class TransferStockDialogComponent {
  form: FormGroup;

  skuOptions: { sku: string; name: string }[] = [];
  fromLocationOptions: string[] = [];
  toLocationOptions: string[] = [];

  // for validation
  selectedFromAvailable: number | null = null;
  selectedFromItemId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private ref: MatDialogRef<TransferStockDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData
  ) {
    this.form = this.fb.group({
      sku: ['', Validators.required],
      fromLocation: ['', Validators.required],
      toLocation: ['', Validators.required],
      customToLocation: [''],
      quantity: [1, [Validators.required, Validators.min(1)]],
    }, { validators: this.quantityNotExceedingAvailable });

    const map = new Map<string, string>();
    for (const it of data.items) {
      if (!map.has(it.sku)) map.set(it.sku, it.name);
    }
    this.skuOptions = Array.from(map.entries()).map(([sku, name]) => ({ sku, name }));

    // Optional prefill
    if (data.prefill?.sku) {
      this.form.patchValue({ sku: data.prefill.sku });
      this.onSkuChange();
    }
    if (data.prefill?.fromLocation) {
      this.form.patchValue({ fromLocation: data.prefill.fromLocation });
      this.onFromChange();
    }
  }

  private quantityNotExceedingAvailable = (group: AbstractControl): ValidationErrors | null => {
    const qty = Number(group.get('quantity')?.value || 0);
    if (this.selectedFromAvailable !== null && qty > this.selectedFromAvailable) {
      group.get('quantity')?.setErrors({ ...(group.get('quantity')?.errors || {}), exceedsAvailable: true });
      return { exceedsAvailable: true };
    } else {
      const errs = group.get('quantity')?.errors || {};
      if ('exceedsAvailable' in errs) {
        delete (errs as any)['exceedsAvailable'];
        if (Object.keys(errs).length === 0) group.get('quantity')?.setErrors(null);
        else group.get('quantity')?.setErrors(errs);
      }
      return null;
    }
  };

  onSkuChange() {
    const sku = this.form.value.sku as string;
    if (!sku) return;

    const candidates = this.data.items.filter(i => i.sku === sku && Number(i.quantity) > 0);
    this.fromLocationOptions = Array.from(new Set(candidates.map(i => i.location))).sort();

    const allLocs = Array.from(new Set(this.data.items.map(i => i.location)));
    this.toLocationOptions = allLocs;

    this.form.patchValue({ fromLocation: '', toLocation: '', customToLocation: '' });
    this.selectedFromAvailable = null;
    this.selectedFromItemId = null;
  }

  onFromChange() {
    const sku = this.form.value.sku as string;
    const fromLoc = this.form.value.fromLocation as string;
    if (!sku || !fromLoc) return;

    const matches = this.data.items.filter(i => i.sku === sku && i.location === fromLoc);
    const totalQty = matches.reduce((sum, it) => sum + Number(it.quantity || 0), 0);
    this.selectedFromAvailable = totalQty;

    this.selectedFromItemId = matches[0]?.id || null;

    
    this.toLocationOptions = Array.from(new Set(this.data.items.map(i => i.location)))
      .filter(loc => loc !== fromLoc);
    this.form.patchValue({ toLocation: '', customToLocation: '' });

    this.form.updateValueAndValidity();
  }

  onToChange() {
    if (this.form.value.toLocation !== '__custom') {
      this.form.patchValue({ customToLocation: '' });
    }
  }

  submit() {
    if (!this.form.valid || !this.selectedFromItemId) return;

    const to = (this.form.value.toLocation === '__custom')
      ? (this.form.value.customToLocation || '').trim()
      : this.form.value.toLocation;

    const payload = {
      itemId: this.selectedFromItemId,
      fromLocation: this.form.value.fromLocation,
      toLocation: to,
      quantity: Number(this.form.value.quantity)
    };

    this.ref.close(payload);
  }

  close() {
    this.ref.close();
  }
}
