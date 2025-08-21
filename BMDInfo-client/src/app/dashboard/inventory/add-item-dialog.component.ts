import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-add-item-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Add New Item</h2>
    <form [formGroup]="form" (ngSubmit)="save()" style="padding: 8px 0;">
      <mat-form-field class="w-100"><mat-label>Name</mat-label>
        <input matInput formControlName="name" required>
      </mat-form-field>

      <mat-form-field class="w-100"><mat-label>SKU</mat-label>
        <input matInput formControlName="sku" required>
      </mat-form-field>

      <mat-form-field class="w-100"><mat-label>Category</mat-label>
        <input matInput formControlName="category">
      </mat-form-field>

      <mat-form-field class="w-100"><mat-label>Location</mat-label>
        <input matInput formControlName="location" required>
      </mat-form-field>

      <mat-form-field class="w-100"><mat-label>Unit Price</mat-label>
        <input matInput type="number" formControlName="unitPrice" required>
      </mat-form-field>

      <mat-form-field class="w-100"><mat-label>Quantity</mat-label>
        <input matInput type="number" formControlName="quantity" required>
      </mat-form-field>

      <div style="text-align:right; margin-top: 12px;">
        <button mat-button type="button" (click)="cancel()">Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Save</button>
      </div>
    </form>
  `
})
export class AddItemDialogComponent {
  form: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<AddItemDialogComponent>
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      sku: ['', Validators.required],
      category: [''],
      location: ['', Validators.required],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      quantity: [0, [Validators.required, Validators.min(0)]],
    });
  }

  save() {
    if (this.form.valid) this.dialogRef.close(this.form.value);
  }
  cancel() {
    this.dialogRef.close();
  }
}
