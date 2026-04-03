# TRACEZERO: Metadata Privacy Firewall

**TRACEZERO** (also known as TraceZero Sentinel) is a comprehensive Java-based security tool designed to detect, analyze, and purge sensitive metadata from various file types. It serves as a privacy firewall to prevent accidental data leaks through embedded metadata in documents, images, and archives.

---

## 🚀 Key Features

* **Multi-Format Scanning:** Deeply inspects **PDF, DOCX, Images (JPG, PNG), and ZIP archives** for hidden metadata.
* **One-Click Sanitization:** Permanently strips author details, revision history, timestamps, software signatures, and GPS coordinates.
* **Background Folder Watcher:** Real-time monitoring of specific directories (like Downloads) to automatically sanitize incoming files.
* **Forensic Reporting:** Generates detailed security audit reports in PDF format, detailing exactly what metadata traces were destroyed.
* **Risk Assessment Dashboard:** Provides visual analytics on your system's privacy health, including exposure scores and threat counts.
* **System Integration:** Features a native OS system tray icon for background operation and desktop notifications for automated tasks.
* **Dual Operation Modes:** Supports both a rich JavaFX Graphical User Interface (GUI) and a Command-Line Interface (CLI) for automated scripts.

---

## 🛠️ Technology Stack

* **Language:** Java 21.
* **Framework:** JavaFX 21.
* **UI Components:** * **AtlantaFX:** Using the Dracula theme for a modern dark-mode aesthetic.
    * **Ikonli:** FontAwesome 5 icon packs.
    * **AnimateFX:** Smooth UI transitions and animations.
* **Core Libraries:**
    * **Apache PDFBox:** For PDF inspection and sanitization.
    * **Apache POI:** For Word (DOCX) metadata management.
    * **Metadata Extractor:** Comprehensive image EXIF and XMP detection.
    * **Google Gson:** Persistent settings and statistics management.
    * **JNA (Java Native Access):** Windows 11 DWM API integration for native dark title bars.

---

## ⚙️ How to Run

### Prerequisites
* **Java Development Kit (JDK) 21** or higher.
* **Maven** (optional, as the project includes the Maven Wrapper).

### 1. Build the Project
Use the provided Maven Wrapper to compile the application and download dependencies:

**On Windows:**
```cmd
./mvnw.cmd clean package
```

**On macOS/Linux:**
```bash
./mvnw clean package
```

### 2. Launch the Application

#### **GUI Mode (Default)**
To launch the full interactive dashboard:
```bash
./mvnw javafx:run
```

#### **CLI Mode (Manual Sanitization)**
To sanitize a single file directly via the terminal without opening the GUI:
```bash
java -cp target/Hellofx-1.0-SNAPSHOT.jar com.example.hellofx.Main "C:/path/to/your/file.pdf"
```
*Note: Replace the path with the actual path to the file you wish to clean.*

---

## 📂 Project Structure

* `src/main/java/com/example/hellofx/`
    * `cleaner/`: Specialized logic for stripping metadata from different file types.
    * `controllers/`: JavaFX UI controllers for the dashboard, scanner, and settings.
    * `core/`: Core engine logic and statistics/log management.
    * `scanner/`: Metadata extraction logic for different formats.
    * `utils/`: Helper classes for file detection, history, and system integration.
    * `watcher/`: Background file system monitoring service.
* `src/main/resources/com/example/hellofx/`
    * `views/`: FXML layout files and UI assets.
    * `styles.css`: Custom CSS styling extending the Dracula theme.

---

## 🛡️ Settings & Configuration
The application allows you to toggle several automated rules in the **Settings** panel:
* **Auto-Clean:** Immediately sanitizes files dropped into the dashboard.
* **Periodic Sweep:** Automatically scans watched folders on a set interval (1, 5, 15, 30, or 60 minutes).
* **Keep Original Files:** If disabled, the application will overwrite the original file instead of creating a `_cleaned` copy.
* **OS Notifications:** Toggles desktop popups for background cleaning tasks.
