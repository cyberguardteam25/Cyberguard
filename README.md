# 🛡️ CyberGuard

CyberGuard is an all-in-one Android cybersecurity toolkit designed for everyday users. It provides real-time threat detection, link and QR code scanning, and system checks through a clean, user-friendly interface. Built with Kotlin, CyberGuard leverages machine learning and backend APIs to offer intelligent, fast, and secure protection for mobile devices.

## 📱 Features

- **APK Scan** – Upload APKs for analysis using AI models.
- **Text Scan** – Check if text is malicious or suspicious using AI models.
- **URL Scan** -Check if links are malicious or suspicious using virustotal
- **QR Scan** – Extract URLs from QR codes and instantly scan for safety.
- **Device Info** – Collect basic system data.
- **User Authentication** – Secure login and registration.
- **History** – View your previous scans and results linked to your user ID.
- and even more...!

## 🧠 Tech Stack

**Frontend** (Android):
- Kotlin & XML
- Retrofit for API communication
- CameraX for QR scanning
- ConstraintLayout, CardView, and custom UI components

**Backend**:
- Python (Flask/FastAPI)
- Machine Learning for malware & phishing detection
- MariaDB/MySQL database (hosted on Kali Linux or server)
- Ngrok for testing local APIs

## 🚀 Getting Started

1. **Clone the repo**
   ```bash
   git clone https://github.com/cyberguardteam25/cyberguard.git

2. Android Setup
- Open in Android Studio
- Sync Gradle
- Add your backend URL in the Retrofit interface files (ApiClient.kt)
- Run the app on Android 8.0+ or emulator

3. Backend Setup (optional)
- Setup Python environment
- Run Flask/FastAPI server
- Use Ngrok to expose your local backend if needed

🔐 Security
CyberGuard never stores your passwords in plaintext. All communications are handled via secure HTTPS (when deployed). User data is linked only to registered accounts and kept minimal.

🧑‍💻 Contact
For questions, feedback, or contributions:
📧 Email: cyberguard.team.25@gmail.com

🙌 Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

📸 Screenshots
![Screenshot_20250507_211703_CyberGuard](https://github.com/user-attachments/assets/23151c4b-0db3-4089-b514-c492d13314a7)
![Screenshot_20250507_211707_CyberGuard](https://github.com/user-attachments/assets/9f312fb5-480c-485d-b5b7-d1bb58317020)
![Screenshot_20250507_202320_CyberGuard](https://github.com/user-attachments/assets/7ed5f7f0-bcb7-4c0b-8a35-4f20d443c30c)
![Screenshot_20250507_204306_CyberGuard](https://github.com/user-attachments/assets/a387c0be-b222-4fdd-a6ed-0778472cc9e4)
![Screenshot_20250507_204914_CyberGuard](https://github.com/user-attachments/assets/b0f6b1f6-b8a3-43a4-b4be-63ed974306c6)
![Screenshot_20250507_202257_CyberGuard](https://github.com/user-attachments/assets/059d0f8d-c3bf-4df5-896f-134fcc4ff021)
![Screenshot_20250507_202259_CyberGuard](https://github.com/user-attachments/assets/31b4f862-9cc6-41eb-8604-c7d78eebcba5)
![Screenshot_20250507_202047_CyberGuard](https://github.com/user-attachments/assets/659066b7-12ca-46ab-8baf-444e8f574018)
![Screenshot_20250507_201901_CyberGuard](https://github.com/user-attachments/assets/6b8dc905-4fa0-4644-8e05-993f565603cd)
![Screenshot_20250507_202253_CyberGuard](https://github.com/user-attachments/assets/22d608e6-2b2a-459a-a87a-5cad932304a8)






