# Personnel Address Book System - Documentation

## Project Overview

The Personnel Address Book System is a client-server architecture-based information management system that supports entry, query, display, and export of basic personnel information (name, date of birth, phone number, native place) and avatars. The frontend is developed using MATLAB App Designer, the backend is implemented in Java, real-time communication is via TCP protocol, and SQLite database is used for data persistence.

### Technical Architecture

* **Frontend**: MATLAB App Designer
* **Backend**: Java
* **Database**: SQLite
* **Communication Protocol**: TCP Socket (custom protocol)
* **Data Interaction**: Text protocol + binary transmission

## System Functions

### Core Functions

1. **Information Entry**: Support for filling in basic personnel information and uploading avatars
2. **Information Query**: Support for single record query (including avatar) and full query (list display)
3. **Data Export**: Query results can be exported to PDF files
4. **Data Management**: Backend supports data insertion, query, update, and deletion operations

### Functional Flow

1. The frontend automatically connects to the backend TCP service (127.0.0.1:1642) on startup
2. Users perform information entry or query operations through the frontend interface
3. The frontend encapsulates operations into TCP messages in a specific format and sends them to the backend
4. The backend processes requests, interacts with the database, and returns response results
5. The frontend receives and parses responses, and updates the interface display

## System Components

### Frontend Components (MATLAB App Designer)

#### Interface Layout

* Left Panel (Information Entry Area): Contains name input box, date of birth selector, phone input box, native place input box, entry button, avatar display area, and image upload button
* Right Panel (Query and Display Area): Contains query input box, query button, result table, information prompt area, and PDF export button

#### Core Methods

* `tcpLoop()`: TCP communication loop that handles data sending and receiving logic
* `parseAndUpdateTable(msg)`: Parses query results and updates the table
* `TCPSever()`: Establishes TCP connection and starts timer
* `AppClose()`: Closes TCP connection and timer, cleans up resources

#### Callback Functions

* `Select()`: Handles query requests
* `INSERT()`: Handles entry requests
* `ImageUpdate()`: Handles image uploads
* `printPDF()`: Generates PDF reports

### Backend Components (Java)

#### Architecture Layers

1. **Database Interface Layer**: Defines standard interfaces for database operations and SQLite implementation
2. **Business Logic Layer**: Encapsulates personnel information and processing logic
3. **Network Service Layer**: Multi-threaded Socket server that handles client connections and requests

#### Core Classes

* `DatabaseIO` interface and `SQLite` implementation class: Database operations
* `PersonPackageIO` interface and `PersonPackage` implementation class: Personnel information processing
* `JavaServer` class: TCP server that handles client connections and command distribution

## Communication Protocol

### Protocol Format

Uses **header + body** format, supporting text commands and binary data transmission

#### Request Format (Client → Server)

```
command=INSERT
name=Zhang San
birthday=2000-01
phone=13800138000
address=Beijing
fileName=avatar.png
fileSize=1024
END_HEADER
[Image binary data]
```

#### Response Format (Server → Client)

* Text response (operation result): `Data inserted successfully`
* Data packet response (query result):

```
command=SingleOut
name=Zhang San
phone=13800138000
birthday=1990-01-01
address=Beijing
fileName=img_1234567890.png
fileSize=1024
END_HEADER
[Binary image data]
```

#### Supported Commands

| Command | Function | Description |
|---------|----------|-------------|
| `INSERT` | Insert/update personnel information | Contains basic information and image data |
| `SELECT` | Query single person | Query by name, returns complete information and avatar |
| `SELECTall` | Query all persons | Returns basic information of all persons, no avatars |
| `DELETE` | Delete personnel information | Delete specified person information by name |

## Database Design

### Data Table Structure

#### Person Table (Basic Personnel Information)

| Field | Type | Description |
|-------|------|-------------|
| id | INTEGER PRIMARY KEY | Auto-increment primary key |
| Name | TEXT | Name (unique) |
| phoneNumber | TEXT | Phone number |
| Birthday | TEXT | Date of birth |
| Address | TEXT | Address |

#### FaceRecord Table (Avatar Information)

| Field    | Type                | Description                   |
| -------- | ------------------- | ----------------------------- |
| id       | INTEGER PRIMARY KEY | Auto-increment primary key    |
| personid | INTEGER             | Associated with **Person.id** |
| imageURL | TEXT                | Image storage path            |

## Running Environment

### Frontend Environment

* MATLAB R2020a or higher
* Dependent toolboxes: `mlreportgen` (PDF export), `Image Processing Toolbox` (image processing)

### Backend Environment

* Java 8+
* SQLite JDBC driver
* Disk write permission (for image storage)

## Usage Instructions

### Startup Steps

1. Start the backend server: Run the `JavaServer.main()` method
2. Start the frontend application: Run the App Designer application in MATLAB
3. The system automatically establishes a TCP connection, and all functions can be used after successful connection

### Operation Flow

1. **Information Entry**:

   * Fill in personnel information in the left panel
   * Click the "Upload Image" button to select an avatar
   * Click the "Entry" button to submit information

2. **Information Query**:

   * Enter a name in the right query box (supports fuzzy query)
   * Click the "Query" button to get results
   * If the query box is empty, all personnel information will be returned

3. **Data Export**:

   * After query results are displayed in the table, click the "Export PDF" button
   * Select a save path to generate a PDF report

## Notes

1. Ensure the backend service starts before the frontend and listens on port 1642
2. Image transmission depends on temporary files, ensure the system has write permissions
3. The database file is located at `database/Person.db`, and images are stored in `database/images/`
4. All text data uses UTF-8 encoding to avoid garbled characters
5. Name is the unique identifier used for data query and update operations

## Error Handling

* Network exception: Frontend prompts "Connection failed, please check if the server is started"
* Data parsing failure: Displays detailed error information, prompts users to check input format
* Image reading failure: Displays default white image and prompts users to re-upload
* Database error: Backend records error logs, frontend displays user-friendly prompt information

## Backend Detailed Design

### 1. System Overview

This system is a Java-based personnel address book management system using client-server architecture, supporting storage, query, update, and deletion of basic personnel information and facial images.

### Technology Stack
- **Database**: SQLite
- **Network Communication**: Socket + custom protocol
- **Data Processing**: JDBC + custom data encapsulation
- **Architecture Pattern**: Layered architecture (interface isolation)

### 2. System Architecture

```
Client → JavaServer → PersonPackage → DatabaseIO → SQLite Database
```

### 3. Core Component Description

#### 3.1 Database Interface Layer

##### Interface: DatabaseIO
**Package**: `com.Alfheim.DatabaseInterface`
**Responsibility**: Defines standard interface for database operations

| Method | Parameters | Return Value | Description |
|--------|------------|--------------|-------------|
| `connect()` | None | void | Connect to database |
| `disconnect()` | None | void | Disconnect from database |
| `executeQuery(String query)` | SQL query statement | ResultSet | Execute query operation |
| `executeUpdate(String command)` | SQL update statement | int | Execute update operation |
| `insertQuery(String command)` | SQL insert statement | void | Execute insert operation |
| `deleteQuery(String command)` | SQL delete statement | void | Execute delete operation |
| `ImageQuery(String command)` | Image-related SQL | void | Execute image operations |

##### Implementation Class: SQLite
**Package**: `com.Alfheim.DatabaseInterface`
**Responsibility**: Specific implementation for SQLite database

**Features**:
- Automatically connects to local SQLite database (`database/Person.db`)
- Complete exception handling mechanism
- Thread-safe connection management

#### 3.2 Business Logic Layer

##### Interface: PersonPackageIO
**Package**: `com.Alfheim.Person`
**Responsibility**: Defines interface for personnel information operations

| Method | Parameters | Return Value | Description |
|--------|------------|--------------|-------------|
| `Person()` | Personnel information + image | void | Initialize personnel object |
| `clear()` | None | void | Clear object data |
| `InsertOrUpdate()` | None | String | Insert or update personnel information |
| `deletePersonInfo()` | None | String | Delete personnel information |
| `queryPersonInfo()` | None | ResultSet | Query single person |
| `queryAllPersonInfo()` | None | ResultSet | Query all persons |
| `getPersonFromDatabase()` | None | String | Get complete information from database |
| Various getter methods | None | String | Get various attribute values |

##### Implementation Class: PersonPackage
**Package**: `com.Alfheim.Person`
**Responsibility**: Complete encapsulation and processing of personnel information

**Core Functions**:
- **Data Persistence**: Automatically saves images to file system (`database/images/`)
- **Database Association**: Maintains association between Person table and FaceRecord table
- **Intelligent Update**: Automatically determines insert or update operation based on name
- **Resource Management**: Automatically manages database connections and file resources

**Main Attributes**:
- `Name`: Name (primary key)
- `phoneNumber`: Phone number
- `Birthday`: Date of birth
- `Address`: Address
- `personId`: Personnel ID (database auto-increment)
- `imageURL`: Image storage path
- `image`: Image byte data

#### 3.3 Network Service Layer

##### Class: JavaServer
**Package**: `com.Alfheim.Server`
**Responsibility**: Multi-client Socket server

**Core Features**:
- **Multi-thread Support**: Each client connection is handled by an independent thread
- **Custom Protocol**: Header+Data based communication protocol
- **Command Distribution**: Supports SELECT/INSERT/DELETE and other operations
- **Binary Transmission**: Supports transmission of binary data such as images

**Server Configuration**:
- Port: 1642
- Protocol: TCP Socket
- Encoding: UTF-8

## Frontend Detailed Design

### 1. Interface Layout and Component Description

#### 1.1 Overall Layout
- Uses left-right column design, left side for information entry, right side for query and result display.
- Supports responsive layout, automatically switches to top-bottom layout when window width is less than threshold.

#### 1.2 Left Panel (LeftPanel)

| Component Type | Variable Name | Description |
|----------------|---------------|-------------|
| `EditField` | `EditField` | Input name |
| `DatePicker` | `DatePicker` | Select date of birth (format: yyyy-MM) |
| `EditField` | `EditField_3` | Input phone number (numbers only) |
| `EditField` | `EditField_4` | Input native place information |
| `Button` | `Button_2` | Click to perform information entry |
| `Image` | `Image` | Display person's avatar |
| `Button` | `Button_3` | Upload image button |

#### 1.3 Right Panel (RightPanel)

| Component Type | Variable Name | Description |
|----------------|---------------|-------------|
| `EditField` | `EditField_5` | Input query name (supports fuzzy query) |
| `Button` | `Button` | Perform query operation |
| `UITable` | `UITable` | Display query results (name, date of birth, phone, native place) |
| `TextArea` | `TextArea` | Display execution results or error messages |
| `Button` | `PDFButton` | Export query results to PDF file |

### 2. Functional Module Description

#### 2.1 Information Entry (INSERT)
- Users fill in name, date of birth, phone number, native place, and upload avatar.
- After clicking the "Entry" button, data is packaged into TCP message and sent to server.
- Supports image binary transmission in `header + image data` format.

#### 2.2 Information Query (SELECT)
- **Single Query**: Enter name on the right, click "Query", returns the first matching record (including avatar).
- **Full Query**: When query condition is empty, returns all personnel information (without avatars) displayed in table form.

#### 2.3 Data Export (PDF)
- Exports current table data to PDF report, including table title and style.
- Uses `mlreportgen` library to generate standardized PDF files.

#### 2.4 Image Upload and Display
- Supports common image formats (jpg, png, bmp, tif).
- Images are read as temporary files and displayed in `uiimage` component.

### 3. Communication Mechanism

#### 3.1 TCP Connection
- Automatically connects to local TCP service (127.0.0.1:1642) on startup.
- Uses timer (`commTimer`) to check data sending and receiving every 50ms.

#### 3.2 Message Format
Uses custom **header + body** format:
```
command=INSERT
name=Zhang San
birthday=2000-01
phone=13800138000
address=Beijing
fileName=avatar.png
fileSize=1024
END_HEADER
[Image binary data]
```

#### 3.3 Status Flags (SELECT_Mark)
- `0`: Normal response (e.g., INSERT result)
- `1`: Single query response (with image)
- `2`: Full query response (multiple records, no images)

### 4. Error Handling and User Prompt
- Uses `TextArea` to display operation results (success/failure/error information).
- Friendly prompts for network exceptions, data parsing failures, etc.
- Displays default white image when image reading fails.

### 5. Code Structure Summary

#### 5.1 Main Private Methods

| Method Name | Function |
|-------------|----------|
| `tcpLoop()` | TCP communication loop that handles sending and receiving logic |
| `parseAndUpdateTable(msg)` | Parses query results and updates table |
| `TCPSever()` | Establishes TCP connection and starts timer |
| `AppClose()` | Closes TCP and timer, cleans up resources |

#### 5.2 Callback Functions

| Callback Function | Function |
|-------------------|----------|
| `Select()` | Handles query requests |
| `INSERT()` | Handles entry requests |
| `ImageUpdate()` | Handles image uploads |
| `printPDF()` | Generates PDF report |
