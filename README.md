# InventoryValuation
Project: Inventory/Asset Module Enhancement
GITHUB: https://github.com/hrittijab/InventoryValuation
1) Objective
To enhance the existing Inventory/Asset module by implementing:
Valuation methods: FIFO, LIFO, Weighted Average
Multi-location inventory tracking and transfer
Stock movement logging
Report generation (CSV & PDF)
2) System Architecture
Backend: Java (Spring Boot)
Database: PostgreSQL
Frontend: Angular
Other Tools: iText (PDF), Apache Commons CSV

3) Entities and Relationships
Entity
Description
InventoryItem
Represents items with SKU, location, price, and quantity
StockTransaction
IN/OUT transactions with qty, price, date
InventoryValuation
Stores' valuation method res
ults per item/location
StockMovementLog
Logs the  transfer of stock between locations


4) Feature/Logic
 Inventory Valuation
FIFO: Oldest purchases used first (sorted by date ASC)
LIFO: Newest purchases used first (sorted by date DESC)
Weighted Average: Average cost = Total Cost / Total Quantity


Note: Each method calculates the value based on the remaining quantity and persists it in InventoryValuation
Stock transfer
Transfers quantity from one location to another:
Deducts from source InventoryItem
Adds to the destination
Creates IN and OUT StockTransaction
Logs the movement in StockMovementLog


5) Database Schema
Table
Purpose
inventory_items
Store item details (name, SKU, category, etc.)
stock_transactions
Track all stock ins/outs with quantities, prices, and dates
inventory_valuation
Store valuation summaries per location/method
stock_movement_logs
Record stock transfers between locations with dates, quantities

6) Endpoints (API Spec) 
Method
Endpoint
Description
POST
/api/inventory/add-stock
Add stock to the existing item
POST
/api/inventory/save-item
Create a new item
POST
/api/inventory/transfer-stock
Transfer stock between locations
GET
/api/inventory/export-csv
Download valuation CSV
GET
/api/inventory/export-pdf
Download valuation PDF
GET
/api/inventory/items
List all inventory items
GET
/api/inventory/movements
List all stock movements

7. Testing
Unit tests created for:
FIFO, LIFO, Weighted Average
transferStock(), addStock(), saveItem()
Tools: JUnit, Mockito
8) Assumptions 
Each item is uniquely identified by a combination of SKU and location.
Price per unit is assumed to be constant during stock transfer.
No user authentication implemented yet
9) Implementation summary as of now
Implemented FIFO, LIFO, and Weighted Average valuation methods with persistence.
Developed stock addition, item creation, and multi-location transfer with proper quantity updates.
Logged all transfers in stock_movement_logs.
Enabled CSV and PDF valuation report exports.
Exposed all required REST APIs.
Added unit tests for valuation methods and key service methods (addStock, transferStock, saveItem) using JUnit and Mockito.

