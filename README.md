![Build Status](https://github.com/DarshaunDavis/flipping-medical-supplies-assistant/actions/workflows/android.yml/badge.svg)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

# Flipping Medical Supplies Assistant

> An Android app to streamline buying, pricing, and invoicing excess diabetic supplies for entrepreneurs in the medical‑supplies arbitrage market.

---

## 📝 Table of Contents

- [🔍 Overview](#overview)
- [📷 Screenshots](#screenshots)
- [✨ Features](#features)
- [🛠 Tech Stack](#tech-stack)
- [🚀 Getting Started](#getting-started)
- [📱 Usage](#usage)
- [🏗 Project Structure](#project-structure)
- [📆 Roadmap](#roadmap)
- [🤝 Contributing](#contributing)
- [📄 License](#license)
- [📬 Contact](#contact)

---

## Overview

Flipping Medical Supplies Assistant helps small businesses quickly scan UPC barcodes, look up wholesaler prices, set profit margins, manage inventory, and generate printable invoices—all from your Android device.

## Screenshots

> _Coming soon – I’ll add app screenshots here once I have polished UI mocks._

---

## Features

- **Barcode Scanning** via camera (ML Kit)
- **Real‑Time Price Lookup** from Firebase
- **Custom Profit Margins** per category (Test Strips, Devices, Inhalers, Insulin)
- **Dynamic Product List** with historical price data
- **Invoice Generation** PDF export or print
- **Role‑Based Access**: free vs. subscribed features
- **Localization**: multiple languages support

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Architecture:** MVVM + Repository Pattern
- **Navigation:** Jetpack Navigation Component
- **Backend:** Firebase Authentication & Firestore
- **Barcode:** ML Kit
- **Ads:** Google AdMob (banner & interstitial)

---

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or higher
- JDK 11+
- A Firebase project (Auth + Firestore) set up
- Google Services JSON (`google-services.json`) in `app/`

### Installation
1. **Clone the repo**
   ```bash
    git clone https://github.com/DarshaunDavis/flipping-medical-supplies-assistant.git
    cd flipping-medical-supplies-assistant
2. Open in Android Studio 
   - File ▶ Open ▶ select the project root
3. Add Firebase config
   - Place your google-services.json in app/
4. Build & Run
   - Let Gradle sync, then run on your emulator or device

---

## Usage
1. Register or log in
2. Switch to Scan tab and point your camera at a barcode
3. View pricing history and select a product
4. Set your profit margin in the Admin ▶ Profit Margin tab
5. Customize prices or view product list in Admin ▶ Prices/Products
6. Generate an invoice from the Invoice tab

---

## Project Structure
app/
├─ src/
│  ├─ main/
│  │  ├─ java/com/lislal/...
│  │  ├─ res/
│  │  ├─ AndroidManifest.xml
│  └─ test/  
└─ build.gradle

---

## Roadmap
* Offline caching & sync
* PDF invoice export
* Multi‑user collaboration
* White‑label theming support

---

## Contributing
* Fork the repo
* Create a feature branch (git checkout -b feat/your‑feature)
* Commit your changes (git commit -m "feat: add …")
* Push (git push origin feat/your‑feature)
* Open a Pull Request

---

## License
This project is licensed under the [MIT License](LICENSE).

---

## Contact
Darshaun Davis –darshaun.davis@gmail.com
Project Link: https://github.com/DarshaunDavis/flipping-medical-supplies-assistant
