import os
import sys
import time
import subprocess
import threading
from http.server import HTTPServer, SimpleHTTPRequestHandler

BASE_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
WEB_DIR = os.path.join(BASE_DIR, "admin-website")
SCREENSHOTS_DIR = os.path.join(BASE_DIR, "screenshots")
os.makedirs(SCREENSHOTS_DIR, exist_ok=True)

EDGE_EXE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
if not os.path.exists(EDGE_EXE):
    EDGE_EXE = r"C:\Program Files\Microsoft\Edge\Application\msedge.exe"

class CustomHandler(SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=WEB_DIR, **kwargs)

def start_server():
    server = HTTPServer(("127.0.0.1", 5555), CustomHandler)
    server.serve_forever()

server_thread = threading.Thread(target=start_server, daemon=True)
server_thread.start()
print("Local web server started on http://127.0.0.1:5555")

# Pages to capture
pages = [
    ("login.html", "01_doctor_login.png"),
    ("dashboard.html", "02_executive_dashboard.png"),
    ("appointments.html", "03_appointment_scheduling.png"),
    ("patients.html", "04_patient_registry.png"),
    ("medical-records.html", "05_clinical_records.png"),
    ("prescriptions.html", "06_digital_prescriptions.png"),
    ("billing.html", "07_billing_invoices.png"),
    ("reports.html", "08_analytics_reports.png"),
    ("settings.html", "09_clinic_settings.png"),
]

profile_dir = os.path.join(SCREENSHOTS_DIR, "edge_profile")
os.makedirs(profile_dir, exist_ok=True)

for page_file, output_name in pages:
    url = f"http://127.0.0.1:5555/{page_file}"
    out_path = os.path.join(SCREENSHOTS_DIR, output_name)
    
    print(f"Capturing {page_file} -> {output_name}...")
    cmd = [
        EDGE_EXE,
        "--headless=new",
        "--no-sandbox",
        "--disable-gpu",
        f"--user-data-dir={profile_dir}",
        "--window-size=1440,900",
        f"--screenshot={out_path}",
        "--virtual-time-budget=2000",
        url
    ]
    
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if os.path.exists(out_path):
        size = os.path.getsize(out_path)
        print(f"  [OK] Saved {output_name} ({size:,} bytes)")
    else:
        print(f"  [WARN] Failed to capture {output_name}")

print("\nScreenshot capture process finished.")

