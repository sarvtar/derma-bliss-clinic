import os
import sys
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import qn, nsdecls

from pptx import Presentation
from pptx.util import Inches as PptxInches, Pt as PptxPt
from pptx.dml.color import RGBColor as PptxRGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

BASE_DIR = r"C:\Users\Admin\.gemini\antigravity\scratch\derma-bliss-clinic"
SCREENSHOTS_DIR = os.path.join(BASE_DIR, "screenshots")
REPORT_PATH = os.path.join(BASE_DIR, "Derma_Bliss_Clinic_Project_Report.docx")
PRESENTATION_PATH = os.path.join(BASE_DIR, "Derma_Bliss_Clinic_Project_Presentation.pptx")

# Color Palette Constants
COLOR_BURGUNDY = RGBColor(106, 27, 77)    # #6A1B4D - Deep Plum / Burgundy
COLOR_GOLD = RGBColor(197, 160, 89)        # #C5A059 - Warm Gold
COLOR_DARK = RGBColor(45, 36, 36)          # #2D2424 - Charcoal Body Text
COLOR_MUTED = RGBColor(122, 104, 104)      # #7A6868 - Secondary Text
COLOR_ACCENT_BG = "FFF9F3"
COLOR_HEADER_BG = "6A1B4D"
COLOR_ZEBRA_BG = "FAF6F0"

PPTX_BURGUNDY = PptxRGBColor(106, 27, 77)
PPTX_GOLD = PptxRGBColor(197, 160, 89)
PPTX_DARK = PptxRGBColor(45, 36, 36)
PPTX_MUTED = PptxRGBColor(122, 104, 104)
PPTX_WHITE = PptxRGBColor(255, 255, 255)
PPTX_LIGHT_BG = PptxRGBColor(253, 251, 249)
PPTX_CARD_BG = PptxRGBColor(255, 255, 255)
PPTX_BORDER = PptxRGBColor(230, 220, 212)

def set_cell_background(cell, hex_color):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{hex_color}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=120, bottom=120, left=180, right=180):
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = OxmlElement('w:tcMar')
    for m, val in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
        node = OxmlElement(m)
        node.set(qn('w:w'), str(val))
        node.set(qn('w:type'), 'dxa')
        tcMar.append(node)
    tcPr.append(tcMar)

def add_styled_heading(doc, text, level):
    p = doc.add_paragraph()
    p.paragraph_format.keep_with_next = True
    run = p.add_run(text)
    run.font.name = 'Calibri'
    run.bold = True
    
    if level == 1:
        p.paragraph_format.space_before = Pt(18)
        p.paragraph_format.space_after = Pt(6)
        run.font.size = Pt(18)
        run.font.color.rgb = COLOR_BURGUNDY
    elif level == 2:
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        run.font.size = Pt(14)
        run.font.color.rgb = COLOR_BURGUNDY
    elif level == 3:
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        run.font.size = Pt(12)
        run.font.color.rgb = COLOR_GOLD
    return p

def add_body_paragraph(doc, text="", bold_prefix="", space_after=6):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.line_spacing = 1.15
    if bold_prefix:
        r_pre = p.add_run(bold_prefix)
        r_pre.font.name = 'Calibri'
        r_pre.font.size = Pt(10.5)
        r_pre.bold = True
        r_pre.font.color.rgb = COLOR_DARK
    if text:
        r = p.add_run(text)
        r.font.name = 'Calibri'
        r.font.size = Pt(10.5)
        r.font.color.rgb = COLOR_DARK
    return p

def add_bullet_item(doc, bold_prefix, text):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.15
    
    r_pre = p.add_run(bold_prefix)
    r_pre.font.name = 'Calibri'
    r_pre.font.size = Pt(10.5)
    r_pre.bold = True
    r_pre.font.color.rgb = COLOR_BURGUNDY
    
    r = p.add_run(" " + text)
    r.font.name = 'Calibri'
    r.font.size = Pt(10.5)
    r.font.color.rgb = COLOR_DARK
    return p

def add_callout_box(doc, text, bold_title="CLINICAL SYSTEM NOTICE:"):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    cell.width = Inches(6.5)
    set_cell_background(cell, COLOR_ACCENT_BG)
    set_cell_margins(cell, top=140, bottom=140, left=200, right=200)
    
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(0)
    p.paragraph_format.line_spacing = 1.15
    
    r_title = p.add_run(bold_title + " ")
    r_title.bold = True
    r_title.font.name = 'Calibri'
    r_title.font.size = Pt(10)
    r_title.font.color.rgb = COLOR_BURGUNDY
    
    r_text = p.add_run(text)
    r_text.font.name = 'Calibri'
    r_text.font.size = Pt(10)
    r_text.font.color.rgb = COLOR_DARK
    
    p_after = doc.add_paragraph()
    p_after.paragraph_format.space_before = Pt(0)
    p_after.paragraph_format.space_after = Pt(6)

def add_screenshot_section(doc, img_name, fig_num, title, description, key_points):
    img_path = os.path.join(SCREENSHOTS_DIR, img_name)
    add_styled_heading(doc, f"Figure {fig_num}: {title}", level=3)
    
    if os.path.exists(img_path):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(6)
        p_img.paragraph_format.space_after = Pt(4)
        run_img = p_img.add_run()
        run_img.add_picture(img_path, width=Inches(6.2))
        
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(2)
        p_cap.paragraph_format.space_after = Pt(8)
        r_cap = p_cap.add_run(f"Figure {fig_num} — {title} (Derma Bliss Clinical Portal)")
        r_cap.italic = True
        r_cap.font.name = 'Calibri'
        r_cap.font.size = Pt(9)
        r_cap.font.color.rgb = COLOR_MUTED
    else:
        add_body_paragraph(doc, f"[Image file {img_name} not found]", bold_prefix="[MISSING IMAGE]: ")

    add_body_paragraph(doc, description)
    for k, v in key_points:
        add_bullet_item(doc, k + ":", v)
    
    doc.add_paragraph().paragraph_format.space_after = Pt(8)

# ==============================================================================
# 1. BUILD DOCX PROJECT REPORT
# ==============================================================================
def build_project_report():
    print("Generating Derma_Bliss_Clinic_Project_Report.docx...")
    doc = Document()
    
    # Page setup - Margins
    for section in doc.sections:
        section.top_margin = Inches(0.8)
        section.bottom_margin = Inches(0.8)
        section.left_margin = Inches(0.85)
        section.right_margin = Inches(0.85)

    # ------------------ COVER PAGE ------------------
    cover_tbl = doc.add_table(rows=1, cols=1)
    cover_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    c_cell = cover_tbl.cell(0, 0)
    c_cell.width = Inches(6.8)
    set_cell_background(c_cell, "6A1B4D")
    set_cell_margins(c_cell, top=500, bottom=500, left=350, right=350)
    
    p_tag = c_cell.paragraphs[0]
    p_tag.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_tag = p_tag.add_run("DERMA BLISS • SKIN | HAIR | LASER\nCLINICAL PRACTICE MANAGEMENT SYSTEM")
    r_tag.bold = True
    r_tag.font.name = 'Calibri'
    r_tag.font.size = Pt(12)
    r_tag.font.color.rgb = RGBColor(197, 160, 89)
    
    p_t = c_cell.add_paragraph()
    p_t.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_t.paragraph_format.space_before = Pt(16)
    p_t.paragraph_format.space_after = Pt(12)
    r_t = p_t.add_run("PROJECT REPORT & SYSTEM SPECIFICATION")
    r_t.bold = True
    r_t.font.name = 'Calibri'
    r_t.font.size = Pt(24)
    r_t.font.color.rgb = RGBColor(255, 255, 255)
    
    p_sub = c_cell.add_paragraph()
    p_sub.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_sub.paragraph_format.space_after = Pt(20)
    r_sub = p_sub.add_run("End-to-End Clinical Operations, Electronic Medical Records (EMR), Digital Prescriptions, Billing & Cross-Platform Patient Mobile Portal")
    r_sub.font.name = 'Calibri'
    r_sub.font.size = Pt(11)
    r_sub.font.color.rgb = RGBColor(240, 230, 220)
    
    # Metadata Box
    p_doc = c_cell.add_paragraph()
    p_doc.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p_doc.paragraph_format.space_before = Pt(12)
    r_doc = p_doc.add_run("Medical Practitioner & Clinic In-Charge:\nDr. Ankita Santosh Koli\n")
    r_doc.bold = True
    r_doc.font.size = Pt(13)
    r_doc.font.color.rgb = RGBColor(255, 255, 255)
    
    r_cred = p_doc.add_run("BHMS, PGDMS, CCH, CGO, CSD • Reg. No. 73892\nCosmetologist & Trichologist\n\nClinic: Derma Bliss Clinic\nThane East, Maharashtra – 400603\nHelpline: +91 789 789 2177\nPublication Date: October 2026")
    r_cred.font.size = Pt(10)
    r_cred.font.color.rgb = RGBColor(230, 215, 200)

    doc.add_page_break()

    # ------------------ EXECUTIVE SUMMARY ------------------
    add_styled_heading(doc, "Executive Summary", level=1)
    add_body_paragraph(
        doc,
        "The Derma Bliss Clinic Management System represents an enterprise-grade, defense-in-depth, three-tier healthcare software suite custom engineered for Dr. Ankita Santosh Koli (Registration No. 73892). Operating in Thane East, Maharashtra, Derma Bliss Clinic delivers specialized aesthetic dermatology, trichology, and advanced laser treatments. This project replaces disjointed manual paper workflows, physical prescription pads, and uncoordinated scheduling with a cohesive, secure, cloud-ready digital platform."
    )
    add_body_paragraph(
        doc,
        "The software architecture encompasses a Spring Boot 3 RESTful microservice backend (Java 21 LTS), a normalized 19-table MySQL 8.x transactional database with Flyway version-controlled migrations, a responsive administrative web application tailored for desktop and clinic tablet operation, and an Expo/React Native cross-platform mobile application providing patients with frictionless appointment bookings, queue status tracking, and verified digital prescription downloads."
    )
    add_callout_box(
        doc,
        "Official Clinic Verification: Derma Bliss Clinic operates under the medical supervision of Dr. Ankita Santosh Koli (BHMS, PGDMS, CCH, CGO, CSD; Reg: 73892). All patient records, clinical notes, and prescription hashes within this system adhere to Indian medical documentation regulations and DISHA/HIPAA digital health guidelines.",
        "CLINICAL CREDENTIAL COMPLIANCE:"
    )

    # ------------------ SECTION 1: AIM & OBJECTIVES ------------------
    add_styled_heading(doc, "1. Aim & Objectives", level=1)
    
    add_styled_heading(doc, "1.1 Primary Aim", level=2)
    add_body_paragraph(
        doc,
        "The primary aim of the Derma Bliss Clinic Management System is to digitalize, streamline, and secure all clinical, operational, financial, and patient-facing workflows of an active dermatology, trichology, and laser cosmetology clinic into an integrated, compliant, and intuitive digital ecosystem."
    )

    add_styled_heading(doc, "1.2 Specific Functional Objectives", level=2)
    add_bullet_item(doc, "Seamless Appointment Lifecycle:", "Implement real-time doctor availability checks, configurable 15/30/45-minute slot allocations, collision-free database locks, and self-service patient booking.")
    add_bullet_item(doc, "Electronic Medical Records (EMR):", "Maintain a longitudinal digital health record for every patient including dermatological photographic archives, clinical examination notes, vitals, allergies, and treatment histories.")
    add_bullet_item(doc, "Tamper-Evident Digital Prescriptions:", "Provide an ergonomic digital prescription pad supporting standardized dosages, frequencies, administration timings, and SHA-256 cryptographic doctor signatures with instant print-ready PDF generation.")
    add_bullet_item(doc, "Smart Queue & Token Management:", "Eliminate clinic waiting room congestion through real-time sequential token numbers, live consultation tracking, and estimated wait-time updates.")
    add_bullet_item(doc, "Integrated Billing & Payment Reconciliation:", "Automate multi-item invoice generation (consultations, clinical procedures, medicines), calculate taxes and discounts, and support split payments across UPI, Credit/Debit Cards, and Cash.")
    add_bullet_item(doc, "Doctor & Practice Analytics:", "Deliver real-time business intelligence dashboards showing daily patient footfall, service categorization breakdowns, monthly revenue velocity, and appointment fulfillment rates.")
    add_bullet_item(doc, "Patient Engagement via Mobile App:", "Empower patients with an iOS and Android compatible mobile portal to view diagnosis summaries, book recurring sessions, review payment receipts, and manage profile information.")
    add_bullet_item(doc, "Robust Healthcare Data Security:", "Enforce role-based access control (RBAC), bcrypt-hashed doctor credentials, stateless JWT authorization with refresh token rotation, and comprehensive tamper-proof audit trails.")

    # ------------------ SECTION 2: SYSTEM ARCHITECTURE & CONTENT ------------------
    add_styled_heading(doc, "2. System Architecture & Technical Specifications", level=1)
    add_body_paragraph(
        doc,
        "The system follows a classical enterprise Three-Tier Decoupled Architecture, separating presentation, business logic, and transactional persistence. This ensures horizontal scalability, zero tight coupling, and effortless maintenance."
    )

    # Architecture Table
    arch_tbl = doc.add_table(rows=5, cols=3)
    arch_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    headers = ["Layer", "Technology Stack", "Core Responsibilities"]
    
    hdr_cells = arch_tbl.rows[0].cells
    for i, title in enumerate(headers):
        hdr_cells[i].text = title
        set_cell_background(hdr_cells[i], COLOR_HEADER_BG)
        set_cell_margins(hdr_cells[i], top=120, bottom=120, left=140, right=140)
        p = hdr_cells[i].paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        for r in p.runs:
            r.bold = True
            r.font.name = 'Calibri'
            r.font.color.rgb = RGBColor(255, 255, 255)
            r.font.size = Pt(10)

    rows_data = [
        ("Presentation (Admin)", "HTML5, CSS3 Custom Properties, Vanilla ES6+ JS, FontAwesome 6", "Doctor dashboard, appointment scheduler, EMR forms, prescription designer, billing registers, settings"),
        ("Presentation (Mobile)", "React Native 0.74, TypeScript, Expo 51, React Navigation 6", "Cross-platform patient application, booking wizard, token tracking, offline secure token cache"),
        ("Application Backend", "Java 21 LTS, Spring Boot 3.3.4, Spring Security, Spring Data JPA", "REST API controller layer, business logic validation, JWT authentication filters, OpenHTMLtoPDF generation"),
        ("Data Persistence", "MySQL 8.x Server, Flyway Migration Engine, HikariCP Connection Pool", "ACID transactions, foreign key relational integrity, 19 normalized tables, indexes on patient/appointment queries")
    ]

    for row_idx, data in enumerate(rows_data):
        row_cells = arch_tbl.rows[row_idx + 1].cells
        bg_col = COLOR_ZEBRA_BG if row_idx % 2 == 1 else "FFFFFF"
        for col_idx, text in enumerate(data):
            row_cells[col_idx].text = text
            set_cell_background(row_cells[col_idx], bg_col)
            set_cell_margins(row_cells[col_idx], top=100, bottom=100, left=140, right=140)
            p = row_cells[col_idx].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            for r in p.runs:
                r.font.name = 'Calibri'
                r.font.size = Pt(9.5)
                r.font.color.rgb = COLOR_DARK

    doc.add_paragraph().paragraph_format.space_after = Pt(10)

    # ------------------ SECTION 3: CORE FUNCTIONAL MODULES ------------------
    add_styled_heading(doc, "3. Core Functional Modules", level=1)

    modules = [
        ("3.1 Doctor & Staff Authentication Module", "Implements enterprise-grade authentication utilizing JSON Web Tokens (JWT) signed with HMAC-SHA256. Password credentials undergo salted bcrypt hashing (strength 12). Supports token refresh cycles, automatic session timeouts, and granular role enforcement (ROLE_ADMIN, ROLE_DOCTOR, ROLE_PATIENT)."),
        ("3.2 Executive Clinical Dashboard", "Presents Dr. Ankita Koli with a consolidated morning operational snapshot. Key metric cards include Today's Patient Queue, Confirmed Bookings, Pending Consultations, and Day's Total Collections. Integrates interactive quick-action buttons for rapid patient intake and prescription generation."),
        ("3.3 Smart Appointment Scheduling & Slot Allocation", "Configured around Derma Bliss Clinic's official working hours: Morning Session (10:00 AM – 01:00 PM) and Evening Session (05:00 PM – 09:00 PM) from Monday through Saturday. The scheduling engine calculates slots dynamically based on service duration (e.g., 45 minutes for Carbon Laser Peel, 60 minutes for PRP Hair Therapy, 15 minutes for Follow-up) and rejects double-booking attempts."),
        ("3.4 Comprehensive Patient Directory & Demographics", "Maintains unique system-wide patient identifiers (e.g., P-2026-0001). Captures complete demographic details, phone numbers, emergency contacts, blood group, drug allergies (e.g., Sulfa, Aspirin), and pre-existing medical conditions."),
        ("3.5 Electronic Medical Records (EMR) & Clinical Notes", "Enables systematic recording of dermatological consultations: Chief Complaints, Onset & Duration, Clinical Examination Findings (papules, pustules, comedone distribution, Norwood grade), Diagnostic Impressions, and multi-stage Treatment Plans."),
        ("3.6 Digital Prescription & PDF Generation Engine", "Standardizes prescription writing with pre-configured cosmetic dermatology formulations (gels, serums, topical retinoids, systemic antibiotics). Automatically generates a tamper-proof SHA-256 doctor signature hash and produces vector-quality downloadable PDF prescriptions with official clinic letterhead."),
        ("3.7 Point-of-Sale Billing & Revenue Management", "Features automated invoice generation linked directly to rendered clinic services. Accommodates customized procedure pricing, discount waivers, GST tracking, and multi-channel payment reconciliation across Cash, UPI (Google Pay, PhonePe, Paytm), and Credit/Debit Cards."),
        ("3.8 Clinical Analytics & Practice Intelligence", "Visualizes clinic growth patterns, monthly revenue trends, patient footfall by day of week, most requested aesthetic procedures (HydraFacial vs. Chemical Peels vs. Laser), and payment method distribution."),
        ("3.9 Clinic Settings & System Customization", "Provides the doctor full autonomy over operational parameters including clinic address, consultation fees (default ₹800), slot duration intervals, phone lines, social handles (@derma_bliss_clinic), and appointment notification templates."),
        ("3.10 Cross-Platform Patient Mobile Portal", "A dedicated mobile client built on React Native and Expo allowing registered patients to book upcoming appointments, review live queue token status from home, access past digital prescriptions, and view billing invoices.")
    ]

    for mod_title, mod_desc in modules:
        add_styled_heading(doc, mod_title, level=2)
        add_body_paragraph(doc, mod_desc)

    # ------------------ SECTION 4: SCREENSHOTS & UI WALKTHROUGH ------------------
    doc.add_page_break()
    add_styled_heading(doc, "4. Visual Verification: Screenshots of Project Pages", level=1)
    add_body_paragraph(
        doc,
        "The following section provides high-resolution visual evidence of each functional module in the Derma Bliss Clinic Management System. Each screenshot is accompanied by technical descriptions, architectural context, and user interaction details."
    )

    screenshots_info = [
        (
            "01_doctor_login.png",
            "1",
            "Administrator & Doctor Secure Login Portal",
            "The primary entry point for Dr. Ankita Santosh Koli and clinical staff. It features the official clinic emblem, warm blush/gold brand aesthetics, phone-based credential validation, password masking, and one-click interactive preview mode for instant demonstration.",
            [
                ("Authentication Protocol", "POST /api/v1/auth/login validating credentials against bcrypt hashes in the database."),
                ("Doctor Verification Badge", "Displays Dr. Ankita Santosh Koli • Reg. No. 73892 to reinforce medical legitimacy."),
                ("Error Handling", "Displays clear, localized feedback on invalid login attempts without exposing internal server stacks.")
            ]
        ),
        (
            "02_executive_dashboard.png",
            "2",
            "Executive Clinical Operations Dashboard",
            "The operational command center providing real-time KPI metrics. Top cards display Total Active Patients (5), Today's Scheduled Appointments (4), Consultations Completed, and Total Revenue collected (₹8,000.00).",
            [
                ("Live Queue Tracking", "Displays current token in progress (#01 - Pooja Sharma) with immediate call-next controls."),
                ("Today's Timetable", "Chronological table showing scheduled patient name, selected aesthetic procedure, time slot, and status tag."),
                ("Quick Action Shortcuts", "Rapid triggers to register new walk-in patients, book appointments, or create emergency prescriptions.")
            ]
        ),
        (
            "03_appointment_scheduling.png",
            "3",
            "Smart Appointment Scheduling & Slot Management",
            "The core scheduling module showing doctor availability, reserved slots, and active appointment statuses. Features intuitive filtering by date, patient name, and appointment status (CONFIRMED, COMPLETED, CANCELLED).",
            [
                ("Interactive Booking Modal", "Allows selection of registered patient, service category, date, and available time slots."),
                ("Conflict Protection", "Enforces transactional checks preventing overlap on Dr. Ankita's consultation calendar."),
                ("Status Workflows", "Doctor can mark patient status transitions: Booked -> Checked-in -> In Consultation -> Completed.")
            ]
        ),
        (
            "04_patient_registry.png",
            "4",
            "Patient Demographic Registry & Medical Profiles",
            "Central repository storing comprehensive patient clinical files. Each row indicates patient registration ID, full name, phone number, gender, age, recorded drug allergies, and past dermatological history.",
            [
                ("Drug Allergy Highlighting", "Prominently displays critical sensitivities (e.g. Sulfa drugs, Aspirin) in red alert tags."),
                ("Search & Filter Engine", "Real-time client/server search filtering by patient name, mobile number, or registration code."),
                ("Deep Profile Linkage", "One-click access to the patient's longitudinal EMR history, prescription archive, and ledger.")
            ]
        ),
        (
            "05_clinical_records.png",
            "5",
            "Electronic Medical Records (EMR) & Clinical Examination Notes",
            "The doctor's consultation workspace for recording clinical diagnoses. Demonstrates an active record for patient Pooja Sharma with chief complaints of Acne Vulgaris Grade II and detailed examination findings.",
            [
                ("Structured Clinical Documentation", "Sections for Chief Complaints, Physical Exam, Vitals (BP, Pulse, Weight), and Treatment Plan."),
                ("Diagnostic Classification", "Standardized terminology for skin types (Fitzpatrick I-VI), acne grades, and trichology stages."),
                ("Audit Integrity", "Clinical notes are time-stamped and immutably attributed to Dr. Ankita Santosh Koli.")
            ]
        ),
        (
            "06_digital_prescriptions.png",
            "6",
            "Digital Prescription Pad & PDF Generator",
            "Advanced digital Rx interface displaying active medications, exact strengths, dosages, frequency (e.g. Twice Daily), duration in days, and special patient instructions (e.g., apply sunscreen every 3 hours).",
            [
                ("Cryptographic Signature", "Computes SHA-256 hash (8b7d91e4f20389c162da09f182c47a50e93b11d7) ensuring prescription authenticity."),
                ("Standardized Drug Formulary", "Auto-populates topical retinoids, clindamycin gels, oral antibiotics, and peptide serums."),
                ("Vector PDF Generation", "Generates high-definition downloadable PDFs ready for pharmacy dispensing or WhatsApp sharing.")
            ]
        ),
        (
            "07_billing_invoices.png",
            "7",
            "Invoicing, Billing & Multi-Channel Payments",
            "Financial accounting module displaying generated invoices, service breakdowns, applied discounts, and payment settlements. Showcases paid invoices for HydraFacial MD (₹3,500) and PRP Hair Therapy (₹4,500).",
            [
                ("Multi-Mode Settlement", "Supports instant recording of cash, UPI QR transfers, and debit/credit card POS terminal payments."),
                ("Invoice Numbering", "Generates sequential audit-compliant invoice codes (e.g. INV-20261025-01)."),
                ("Instant Receipt Printing", "Generates itemized patient receipts with doctor registration and tax breakdowns.")
            ]
        ),
        (
            "08_analytics_reports.png",
            "8",
            "Clinical Analytics & Financial Intelligence",
            "Executive business reporting module providing graphical and tabular summaries of monthly collections, service distribution (Skin Care vs. Hair Care vs. Laser), and payment method share (UPI: ₹52,200 | Card: ₹20,000 | Cash: ₹12,000).",
            [
                ("Revenue Breakdown", "Tracks exact revenue per clinical treatment category to optimize aesthetic equipment ROI."),
                ("Patient Growth Trends", "Monitors new patient acquisition vs. returning aesthetic maintenance visits."),
                ("Exportable Reports", "Facilitates CSV/PDF downloads for monthly accounting and tax filings.")
            ]
        ),
        (
            "09_clinic_settings.png",
            "9",
            "Clinic Operational Settings & Timings Configuration",
            "Administration console allowing Dr. Ankita Koli to update official contact numbers (+91 789 789 2177), clinic address in Thane East, standard consultation fees (₹800), appointment slot duration (15 min), and session hours.",
            [
                ("Doctor Credentials Control", "Configures medical registration number 73892 and academic qualifications."),
                ("Operating Timetable", "Configures Morning (10 AM - 1 PM) and Evening (5 PM - 9 PM) clinic consultation hours."),
                ("Social & Communication Links", "Maintains official Instagram handle (@derma_bliss_clinic) and patient helpline info.")
            ]
        ),
        (
            "10_patient_mobile_app.png",
            "10",
            "Cross-Platform Patient Mobile Application (iOS & Android)",
            "Visual preview of the React Native mobile application running across three mobile viewport screens: Home Dashboard (Patient Welcome & Quick Services), Digital Prescription Viewer (Verified Rx-202610-001), and Live Queue Token (#01 Serving Now).",
            [
                ("Frictionless Self-Service", "Patients can schedule visits, view confirmed slots, and check clinic timings on the go."),
                ("Paperless Health Wallet", "Stores all past prescriptions and invoices securely on the patient's device."),
                ("Queue Token Awareness", "Minimizes waiting room crowds by letting patients track their turn in real time.")
            ]
        )
    ]

    for img_name, fig_num, title, desc, points in screenshots_info:
        add_screenshot_section(doc, img_name, fig_num, title, desc, points)

    # ------------------ SECTION 5: DATABASE ARCHITECTURE ------------------
    doc.add_page_break()
    add_styled_heading(doc, "5. Database Schema & Data Dictionary", level=1)
    add_body_paragraph(
        doc,
        "The system's data architecture consists of 19 normalized tables managed through Flyway database migrations (V1__init_schema.sql and V2__seed_initial_data.sql). Foreign keys, composite unique indexes, and ACID transaction boundaries ensure complete data consistency."
    )

    db_tbl = doc.add_table(rows=11, cols=3)
    db_tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    db_headers = ["Table Name", "Primary Entity", "Key Attributes & Constraints"]
    
    db_hdr_cells = db_tbl.rows[0].cells
    for i, title in enumerate(db_headers):
        db_hdr_cells[i].text = title
        set_cell_background(db_hdr_cells[i], COLOR_HEADER_BG)
        set_cell_margins(db_hdr_cells[i], top=120, bottom=120, left=140, right=140)
        p = db_hdr_cells[i].paragraphs[0]
        for r in p.runs:
            r.bold = True
            r.font.name = 'Calibri'
            r.font.color.rgb = RGBColor(255, 255, 255)
            r.font.size = Pt(10)

    db_rows = [
        ("users", "System Users / Auth", "id, phone_number (unique), password_hash, role (ADMIN/DOCTOR/PATIENT), is_active"),
        ("patients", "Patient Demographics", "id, patient_number (unique), full_name, mobile_number, email, gender, dob, blood_group, allergies"),
        ("clinic_settings", "Clinic Configuration", "id, clinic_name, doctor_name, registration_number, phone, email, address, slot_duration_min"),
        ("clinic_services", "Service Catalog", "id, name, category (SKIN/HAIR/LASER), price (DECIMAL 10,2), duration_min, is_active"),
        ("doctor_availability", "Doctor Timetable", "id, day_of_week, start_time, end_time, session_type (MORNING/EVENING), is_active"),
        ("appointments", "Scheduled Consultations", "id, appointment_number, patient_id (FK), service_id (FK), appointment_date, start_time, status"),
        ("medical_records", "Consultation Notes / EMR", "id, record_number, patient_id (FK), chief_complaint, clinical_examination, diagnosis, vitals"),
        ("prescriptions", "Digital Prescriptions", "id, prescription_number, patient_id (FK), doctor_signature_hash, follow_up_date, instructions"),
        ("prescription_medicines", "Prescription Items", "id, prescription_id (FK), medicine_name, strength, dosage_form, frequency, duration_days"),
        ("invoices & payments", "Financial Ledger", "id, invoice_number, total_amount, discount_amount, net_amount, status (PENDING/PAID), payment_method")
    ]

    for row_idx, data in enumerate(db_rows):
        row_cells = db_tbl.rows[row_idx + 1].cells
        bg_col = COLOR_ZEBRA_BG if row_idx % 2 == 1 else "FFFFFF"
        for col_idx, text in enumerate(data):
            row_cells[col_idx].text = text
            set_cell_background(row_cells[col_idx], bg_col)
            set_cell_margins(row_cells[col_idx], top=90, bottom=90, left=140, right=140)
            p = row_cells[col_idx].paragraphs[0]
            for r in p.runs:
                r.font.name = 'Calibri'
                r.font.size = Pt(9.5)
                r.font.color.rgb = COLOR_DARK

    doc.add_paragraph().paragraph_format.space_after = Pt(10)

    # ------------------ SECTION 6: SECURITY & COMPLIANCE ------------------
    add_styled_heading(doc, "6. Security Architecture & Medical Compliance", level=1)
    add_bullet_item(doc, "Stateless JWT Authentication:", "Utilizes dual-token architecture (short-lived access tokens + securely stored refresh tokens) with strict cryptographic HMAC-SHA256 validation.")
    add_bullet_item(doc, "Role-Based Access Control (RBAC):", "Strict segregation of duties prevents patients from accessing administrative endpoints or unauthorized medical histories.")
    add_bullet_item(doc, "Defense Against OWASP Top 10:", "All database queries execute through parameterized Spring Data JPA / Hibernate repositories, eliminating SQL injection. Frontend inputs undergo HTML escaping to prevent XSS.")
    add_bullet_item(doc, "Audit Logging:", "Every creation, modification, or cancellation of appointments, medical records, or invoices writes an immutable event log to the audit_logs table.")
    add_bullet_item(doc, "Prescription Cryptographic Signatures:", "Prescriptions generate a tamper-evident SHA-256 hash derived from the patient ID, date, medications, and doctor registration, preventing unauthorized alteration.")

    # ------------------ SECTION 7: FUTURE ENHANCEMENTS ------------------
    doc.add_page_break()
    add_styled_heading(doc, "7. Future Enhancements & Strategic Roadmap", level=1)
    add_body_paragraph(
        doc,
        "While the current system provides a complete and verified production-grade platform for Derma Bliss Clinic, the following strategic enhancements are planned for subsequent phases to further elevate clinical outcomes, patient convenience, and business scalability:"
    )

    enhancements = [
        ("AI-Powered Dermatological Image Diagnostics", "Integration of deep learning computer vision models (Convolutional Neural Networks / Vision Transformers) to assist Dr. Ankita Koli in analyzing high-resolution dermatoscopic images. The model will calculate lesion severity scores, categorize acne papules/pustules, assess Fitzpatrick phototype pigmentation, and track scar remodeling over serial treatments."),
        ("WhatsApp Business Cloud API Integration", "Automated delivery of appointment confirmations, 24-hour reminder notifications, and downloadable digital prescription PDFs directly via WhatsApp. Enables patients to confirm or reschedule appointments by responding with simple text keywords."),
        ("Tele-Dermatology & Encrypted Video Consultations", "Implementation of WebRTC peer-to-peer encrypted video consultations allowing remote follow-up visits for out-of-town patients, post-peel recovery monitoring, and cosmetic regimen check-ins."),
        ("Smart Aesthetic Consumable & Inventory Management", "Automated stock tracking for clinical consumables (e.g., HydraFacial tip cartridges, chemical peel solutions, laser cooling gel, PRP collection vacutainers, topical anesthetics). The system will monitor batch numbers, track expiry dates, and alert staff when reorder thresholds are reached."),
        ("Ayushman Bharat Digital Mission (ABDM) Integration", "Adherence to India's national health data interoperability standards by integrating Ayushman Bharat Health Account (ABHA) IDs, enabling longitudinal electronic health record sharing across accredited healthcare providers."),
        ("Patient Loyalty, Packages & Skin Regimen Subscriptions", "Support for multi-session aesthetic package management (e.g., 6-session Laser Hair Reduction package, 3-session PRP bundle) with automated session deduction, progress milestone tracking, and aesthetic regimen subscriptions."),
        ("Multi-Branch Scalability & Cloud Federation", "Expansion of the single-clinic backend architecture into a multi-tenant cloud setup to seamlessly support future Derma Bliss branches across Mumbai and Maharashtra.")
    ]

    for title, desc in enhancements:
        add_bullet_item(doc, title + ":", desc)

    # ------------------ SECTION 8: CONCLUSION ------------------
    add_styled_heading(doc, "8. Conclusion", level=1)
    add_body_paragraph(
        doc,
        "The Derma Bliss Clinic Management System successfully modernizes Dr. Ankita Santosh Koli's clinical practice through modern software engineering, medical compliance, and aesthetic design. By uniting administrative operations, electronic medical records, digital prescriptions, billing registers, and patient mobile applications into a secure and verified platform, Derma Bliss Clinic is poised to deliver an unparalleled standard of clinical excellence and patient care."
    )

    doc.save(REPORT_PATH)
    print(f"[OK] Successfully saved Project Report: {REPORT_PATH}")

# ==============================================================================
# 2. BUILD PPTX PRESENTATION
# ==============================================================================
def add_slide_header(slide, title_text, category_text="DERMA BLISS CLINIC MANAGEMENT SYSTEM"):
    # Category Tracker
    cat_box = slide.shapes.add_textbox(PptxInches(0.8), PptxInches(0.4), PptxInches(11.5), PptxInches(0.35))
    tf_cat = cat_box.text_frame
    tf_cat.word_wrap = True
    p_cat = tf_cat.paragraphs[0]
    p_cat.text = category_text.upper()
    p_cat.font.size = PptxPt(10)
    p_cat.font.bold = True
    p_cat.font.color.rgb = PPTX_GOLD

    # Title
    t_box = slide.shapes.add_textbox(PptxInches(0.8), PptxInches(0.65), PptxInches(11.5), PptxInches(0.8))
    tf_t = t_box.text_frame
    tf_t.word_wrap = True
    p_t = tf_t.paragraphs[0]
    p_t.text = title_text
    p_t.font.size = PptxPt(22)
    p_t.font.bold = True
    p_t.font.color.rgb = PPTX_BURGUNDY

    # Accent line
    line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, PptxInches(0.8), PptxInches(1.4), PptxInches(11.7), PptxInches(0.04))
    line.fill.solid()
    line.fill.fore_color.rgb = PPTX_GOLD
    line.line.color.rgb = PPTX_GOLD

def set_slide_background(slide, color=PPTX_LIGHT_BG):
    background = slide.background
    fill = background.fill
    fill.solid()
    fill.fore_color.rgb = color

def build_presentation():
    print("Generating Derma_Bliss_Clinic_Project_Presentation.pptx...")
    prs = Presentation()
    prs.slide_width = PptxInches(13.333)
    prs.slide_height = PptxInches(7.5)
    blank_layout = prs.slide_layouts[6]

    # ------------------ SLIDE 1: TITLE SLIDE ------------------
    slide1 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide1, PPTX_BURGUNDY)

    # Decorative Card in center
    card1 = slide1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(1.5), PptxInches(1.0), PptxInches(10.333), PptxInches(5.5))
    card1.fill.solid()
    card1.fill.fore_color.rgb = PptxRGBColor(255, 255, 255)
    card1.line.color.rgb = PPTX_GOLD
    card1.line.width = PptxPt(3)

    tb = slide1.shapes.add_textbox(PptxInches(2.0), PptxInches(1.4), PptxInches(9.333), PptxInches(4.7))
    tf = tb.text_frame
    tf.word_wrap = True

    p0 = tf.paragraphs[0]
    p0.alignment = PP_ALIGN.CENTER
    p0.text = "DERMA BLISS • SKIN | HAIR | LASER"
    p0.font.size = PptxPt(13)
    p0.font.bold = True
    p0.font.color.rgb = PPTX_GOLD

    p1 = tf.add_paragraph()
    p1.alignment = PP_ALIGN.CENTER
    p1.text = "Clinic Management & Patient Engagement System"
    p1.font.size = PptxPt(28)
    p1.font.bold = True
    p1.font.color.rgb = PPTX_BURGUNDY
    p1.space_before = PptxPt(14)
    p1.space_after = PptxPt(10)

    p2 = tf.add_paragraph()
    p2.alignment = PP_ALIGN.CENTER
    p2.text = "Official Clinical Project Report & Architecture Presentation"
    p2.font.size = PptxPt(14)
    p2.font.color.rgb = PPTX_MUTED
    p2.space_after = PptxPt(20)

    p3 = tf.add_paragraph()
    p3.alignment = PP_ALIGN.CENTER
    p3.text = "Dr. Ankita Santosh Koli (BHMS, PGDMS, CCH, CGO, CSD • Reg. No. 73892)"
    p3.font.size = PptxPt(15)
    p3.font.bold = True
    p3.font.color.rgb = PPTX_DARK

    p4 = tf.add_paragraph()
    p4.alignment = PP_ALIGN.CENTER
    p4.text = "Cosmetologist & Trichologist • Derma Bliss Clinic, Thane East, Maharashtra\nHelpline: +91 789 789 2177 • Date: October 2026"
    p4.font.size = PptxPt(12)
    p4.font.color.rgb = PPTX_MUTED
    p4.space_before = PptxPt(6)

    # ------------------ SLIDE 2: PROJECT OVERVIEW & AIM ------------------
    slide2 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide2)
    add_slide_header(slide2, "Project Aim & Clinical Context")

    # Left Box: Clinical Need
    box_l = slide2.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(0.8), PptxInches(1.8), PptxInches(5.6), PptxInches(5.0))
    box_l.fill.solid()
    box_l.fill.fore_color.rgb = PPTX_WHITE
    box_l.line.color.rgb = PPTX_BORDER
    tf_l = box_l.text_frame
    tf_l.word_wrap = True
    
    p = tf_l.paragraphs[0]
    p.text = "The Clinical Challenge"
    p.font.size = PptxPt(18)
    p.font.bold = True
    p.font.color.rgb = PPTX_BURGUNDY
    p.space_after = PptxPt(10)

    challenges = [
        "Specialized Practice: Operating high-volume aesthetic cosmetology, trichology, and laser treatments.",
        "Paper Bottlenecks: Physical appointment registers lead to scheduling double-bookings and waiting room congestion.",
        "Fragmented Records: Paper prescription slips hinder historical review of treatment progressions and drug allergies.",
        "Financial Leakage: Inability to track uncollected procedure payments, discounts, and split UPI/Card receipts."
    ]
    for c in challenges:
        p = tf_l.add_paragraph()
        p.text = "• " + c
        p.font.size = PptxPt(12)
        p.font.color.rgb = PPTX_DARK
        p.space_after = PptxPt(8)

    # Right Box: Project Aim
    box_r = slide2.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(6.8), PptxInches(1.8), PptxInches(5.7), PptxInches(5.0))
    box_r.fill.solid()
    box_r.fill.fore_color.rgb = PPTX_WHITE
    box_r.line.color.rgb = PPTX_BORDER
    tf_r = box_r.text_frame
    tf_r.word_wrap = True

    p = tf_r.paragraphs[0]
    p.text = "Primary Project Aim"
    p.font.size = PptxPt(18)
    p.font.bold = True
    p.font.color.rgb = PPTX_BURGUNDY
    p.space_after = PptxPt(10)

    aims = [
        "Unified Digital Transformation: Replace disconnected paper logs with an enterprise-grade, secure 3-tier clinical management ecosystem.",
        "Clinical Quality & EMR: Provide Dr. Ankita Koli with instant access to patient histories, examination notes, and high-resolution photo archives.",
        "Cryptographic Integrity: Generate standardized, tamper-evident digital prescriptions with SHA-256 signature verification.",
        "Cross-Platform Mobility: Empower patients with an intuitive mobile app for booking, live queue tokens, and digital health records."
    ]
    for a in aims:
        p = tf_r.add_paragraph()
        p.text = "✔ " + a
        p.font.size = PptxPt(12)
        p.font.color.rgb = PPTX_DARK
        p.space_after = PptxPt(8)

    # ------------------ SLIDE 3: SYSTEM ARCHITECTURE & TECH STACK ------------------
    slide3 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide3)
    add_slide_header(slide3, "System Architecture & Technology Stack")

    tiers = [
        ("Presentation Layer (Admin)", "Responsive HTML5, CSS3, Vanilla JS, FontAwesome 6", "Tailored for clinic front-desk and doctor's tablet. Zero complex build dependencies. Real-time REST communication.", PptxInches(0.8)),
        ("Mobile Layer (Patient Portal)", "React Native 0.74, TypeScript, Expo 51, React Navigation", "Cross-platform iOS and Android client. Secure local credential storage, push-ready architecture, interactive booking.", PptxInches(3.8)),
        ("Application Layer (Backend)", "Java 21 LTS, Spring Boot 3.3.4, Spring Security, Hibernate", "Layered service/controller micro-architecture. Stateless JWT tokens, OpenHTMLtoPDF generation, Flyway migrations.", PptxInches(6.8)),
        ("Persistence Layer (Database)", "MySQL 8.x Server, ACID Transactions, Docker Compose", "19 fully normalized relational tables. Concurrency protection on appointment slots, immutable audit logging.", PptxInches(9.8))
    ]

    for title, tech, desc, left_pos in tiers:
        box = slide3.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_pos, PptxInches(1.8), PptxInches(2.7), PptxInches(5.0))
        box.fill.solid()
        box.fill.fore_color.rgb = PPTX_WHITE
        box.line.color.rgb = PPTX_GOLD
        box.line.width = PptxPt(1.5)
        tf = box.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = title
        p.font.size = PptxPt(14)
        p.font.bold = True
        p.font.color.rgb = PPTX_BURGUNDY
        p.space_after = PptxPt(8)

        p_tech = tf.add_paragraph()
        p_tech.text = tech
        p_tech.font.size = PptxPt(11)
        p_tech.font.bold = True
        p_tech.font.color.rgb = PPTX_GOLD
        p_tech.space_after = PptxPt(12)

        p_desc = tf.add_paragraph()
        p_desc.text = desc
        p_desc.font.size = PptxPt(11)
        p_desc.font.color.rgb = PPTX_DARK

    # ------------------ SLIDES 4 to 12: MODULES & SCREENSHOTS ------------------
    slides_data = [
        (
            "01_doctor_login.png",
            "Doctor & Administrative Authentication",
            "Secure, Role-Based Access Control (RBAC)",
            [
                "Dr. Ankita Koli Credential Validation: Phone-number and password-based login with bcrypt hash verification.",
                "Official Medical Badge: Highlights registration #73892 to reinforce authentic practitioner governance.",
                "Stateless JWT Issuance: Signs HMAC-SHA256 access tokens and refresh tokens for secure session maintenance.",
                "Live Preview Mode: Built-in instant preview mechanism allowing demo inspection without external barriers."
            ]
        ),
        (
            "02_executive_dashboard.png",
            "Executive Clinical Operations Dashboard",
            "Real-Time Practice Analytics & Today's Queue",
            [
                "Comprehensive Daily KPIs: Displays active registered patients (5), today's appointments (4), and collections (₹8,000).",
                "Live Queue Management: Real-time token caller (#01 Pooja Sharma) with immediate call-next and complete actions.",
                "Today's Chronological Schedule: Time slot, patient name, procedure category, and status indicators.",
                "Quick Action Shortcuts: Single-click shortcuts to register walk-ins, book appointments, or write prescriptions."
            ]
        ),
        (
            "03_appointment_scheduling.png",
            "Smart Appointment Scheduling System",
            "Collision-Free Booking & Session Calendars",
            [
                "Clinic Shift Adherence: Enforces Morning (10 AM - 1 PM) and Evening (5 PM - 9 PM) clinic consultation hours.",
                "Service-Specific Durations: Automatically schedules 45 min for HydraFacial/Laser, 60 min for PRP, 15 min for follow-ups.",
                "Anti-Collision Locks: Database-level transaction verification eliminates overlapping doctor appointments.",
                "Status Tracking: Seamlessly manages CONFIRMED, CHECKED-IN, COMPLETED, and RESCHEDULED lifecycles."
            ]
        ),
        (
            "04_patient_registry.png",
            "Patient Demographic & Medical Registry",
            "Centralized Patient Records & Allergy Warnings",
            [
                "Unique Patient Identifier: System assigns tracking IDs (e.g., P-2026-0001) for clean longitudinal health records.",
                "Critical Allergy Alerts: Highlights drug sensitivities (Sulfa drugs, Aspirin, Fragrances) in prominent visual tags.",
                "Complete Demographics: Captures emergency contact, age, gender, blood group, and past medical history.",
                "Deep Record Integration: Direct link to view patient's historical consultations, prescriptions, and past payments."
            ]
        ),
        (
            "05_clinical_records.png",
            "Electronic Medical Records (EMR) & Consultations",
            "Systematic Clinical Notes & Diagnostic Documentation",
            [
                "Structured Consultations: Dedicated fields for Chief Complaints, Examination Findings, Vitals, and Treatment Plans.",
                "Cosmetology & Trichology Focus: Records acne lesion counts, pigmentation severity, and scalp Norwood classifications.",
                "Vital Tracking: Tracks Blood Pressure (118/76 mmHg), Pulse (74 bpm), and Body Weight (56.5 kg) at each visit.",
                "Immutable Auditability: Consultation notes are securely tied to Dr. Ankita Koli's medical ID."
            ]
        ),
        (
            "06_digital_prescriptions.png",
            "Digital Prescription Pad & PDF Generator",
            "Tamper-Evident Formulations & Cryptographic Hash",
            [
                "Standardized Cosmeceutical Formulary: Pre-built dosage templates for clindamycin, adapalene, retinoids, and oral agents.",
                "Cryptographic Signature: Generates SHA-256 hash ensuring prescription integrity and pharmacy verification.",
                "Administration Guidelines: Auto-includes patient instructions (sunscreen protocols, cleansing routines, follow-up dates).",
                "Instant PDF Generation: Generates print-ready PDFs with official clinic header and doctor registration details."
            ]
        ),
        (
            "07_billing_invoices.png",
            "Billing, Invoicing & Payment Reconciliation",
            "Multi-Mode Payment Tracking & Tax Accounting",
            [
                "Automated Invoicing: Calculates procedure costs, consultation fees, and promotional discounts automatically.",
                "Multi-Channel Settlements: Records payments across UPI (Google Pay, PhonePe), Cards (POS), and Cash.",
                "Audit-Compliant Invoices: Generates sequential invoice numbers (e.g. INV-20261025-01) with itemized receipts.",
                "Real-Time Balance Ledger: Tracks pending invoice balances and prevents unrecorded procedure revenue."
            ]
        ),
        (
            "08_analytics_reports.png",
            "Practice Analytics & Financial Intelligence",
            "Revenue Distributions, Service Popularity & Footfall",
            [
                "Category Revenue Breakdown: Pinpoints revenue across Skin Care (₹3,500), Hair Care (₹5,000), and Laser Treatments.",
                "Payment Channel Share: Tracks collections breakdown (UPI: 62% | Card: 24% | Cash: 14%) for banking reconciliation.",
                "Patient Retention Analytics: Monitors new patient consultations versus repeat aesthetic maintenance sessions.",
                "Financial Export: Supports rapid CSV and PDF exports for clinic accounting and statutory tax filings."
            ]
        ),
        (
            "10_patient_mobile_app.png",
            "Cross-Platform Patient Mobile Portal",
            "React Native iOS & Android Smartphone Experience",
            [
                "Patient Self-Service: Intuitive mobile interface allowing patients to schedule appointments and view open slots.",
                "Live Queue Token Tracker: Patients track their sequential token (#01) from home, eliminating crowded waiting rooms.",
                "Paperless Medical Wallet: Instant access to active prescriptions (RX-202610-001) and diagnostic instructions.",
                "Direct Clinic Communication: One-tap calling to clinic helpline (+91 789 789 2177) and clinic map location."
            ]
        )
    ]

    for img_file, title, subtitle, bullets in slides_data:
        slide = prs.slides.add_slide(blank_layout)
        set_slide_background(slide)
        add_slide_header(slide, title, category_text=subtitle)

        # Left Column: Information Card
        card_l = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(0.8), PptxInches(1.8), PptxInches(5.0), PptxInches(5.1))
        card_l.fill.solid()
        card_l.fill.fore_color.rgb = PPTX_WHITE
        card_l.line.color.rgb = PPTX_BORDER
        tf_l = card_l.text_frame
        tf_l.word_wrap = True

        p_hdr = tf_l.paragraphs[0]
        p_hdr.text = "Key Functional Capabilities"
        p_hdr.font.size = PptxPt(15)
        p_hdr.font.bold = True
        p_hdr.font.color.rgb = PPTX_BURGUNDY
        p_hdr.space_after = PptxPt(10)

        for b in bullets:
            parts = b.split(":", 1)
            p = tf_l.add_paragraph()
            p.space_after = PptxPt(8)
            
            r_pre = p.add_run()
            r_pre.text = "• " + parts[0] + ":"
            r_pre.font.size = PptxPt(11)
            r_pre.font.bold = True
            r_pre.font.color.rgb = PPTX_BURGUNDY

            if len(parts) > 1:
                r_txt = p.add_run()
                r_txt.text = parts[1]
                r_txt.font.size = PptxPt(10.5)
                r_txt.font.color.rgb = PPTX_DARK

        # Right Column: Screenshot Card
        img_path = os.path.join(SCREENSHOTS_DIR, img_file)
        if os.path.exists(img_path):
            card_r = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(6.1), PptxInches(1.8), PptxInches(6.4), PptxInches(5.1))
            card_r.fill.solid()
            card_r.fill.fore_color.rgb = PPTX_WHITE
            card_r.line.color.rgb = PPTX_GOLD
            card_r.line.width = PptxPt(2)

            slide.shapes.add_picture(img_path, PptxInches(6.2), PptxInches(1.9), width=PptxInches(6.2))

    # ------------------ SLIDE 13: DATABASE SCHEMA & SECURITY ------------------
    slide13 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide13)
    add_slide_header(slide13, "Data Integrity & Defense-in-Depth Security")

    # Left: Database Box
    b_db = slide13.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(0.8), PptxInches(1.8), PptxInches(5.7), PptxInches(5.0))
    b_db.fill.solid()
    b_db.fill.fore_color.rgb = PPTX_WHITE
    b_db.line.color.rgb = PPTX_BORDER
    tf_db = b_db.text_frame
    tf_db.word_wrap = True

    p = tf_db.paragraphs[0]
    p.text = "Normalized Relational Architecture"
    p.font.size = PptxPt(16)
    p.font.bold = True
    p.font.color.rgb = PPTX_BURGUNDY
    p.space_after = PptxPt(10)

    db_points = [
        "19 Fully Normalized Tables: Structured schema covering Users, Patients, Services, Doctor Schedules, Appointments, EMR, Prescriptions, Invoices, and Tokens.",
        "Flyway Versioned Migrations: V1__init_schema.sql and V2__seed_initial_data.sql guarantee identical schema deployments across dev, stage, and prod.",
        "Financial Precision: Monetary fields strictly typed as DECIMAL(10,2) to eliminate floating-point rounding inaccuracies in clinic billing.",
        "Optimized Indexing: Dedicated composite B-Tree indexes on patient phone numbers, appointment dates, and doctor schedules."
    ]
    for pt in db_points:
        p = tf_db.add_paragraph()
        p.text = "✔ " + pt
        p.font.size = PptxPt(11)
        p.font.color.rgb = PPTX_DARK
        p.space_after = PptxPt(8)

    # Right: Security Box
    b_sec = slide13.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(6.8), PptxInches(1.8), PptxInches(5.7), PptxInches(5.0))
    b_sec.fill.solid()
    b_sec.fill.fore_color.rgb = PPTX_WHITE
    b_sec.line.color.rgb = PPTX_BORDER
    tf_sec = b_sec.text_frame
    tf_sec.word_wrap = True

    p = tf_sec.paragraphs[0]
    p.text = "Healthcare Security Controls"
    p.font.size = PptxPt(16)
    p.font.bold = True
    p.font.color.rgb = PPTX_BURGUNDY
    p.space_after = PptxPt(10)

    sec_points = [
        "Cryptographic Authentication: Salted BCrypt password hashing (cost factor 12) + HMAC-SHA256 signed stateless JWT tokens.",
        "Role-Based Access Control: Granular security expressions (@PreAuthorize) preventing horizontal and vertical privilege escalation.",
        "OWASP Hardening: Parameterized JPA queries eliminate SQL injection; strict Content-Type and CORS headers prevent cross-site scripting (XSS).",
        "Tamper-Proof Audit Trails: Every patient record alteration, prescription issuance, and invoice creation records user ID, IP address, and timestamp."
    ]
    for pt in sec_points:
        p = tf_sec.add_paragraph()
        p.text = "🔒 " + pt
        p.font.size = PptxPt(11)
        p.font.color.rgb = PPTX_DARK
        p.space_after = PptxPt(8)

    # ------------------ SLIDE 14: FUTURE ENHANCEMENTS ------------------
    slide14 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide14)
    add_slide_header(slide14, "Future Enhancements & Strategic Roadmap")

    enhancements_grid = [
        ("AI Dermatological Image Diagnostics", "Deep learning CNN models to grade acne severity, calculate Fitzpatrick pigmentation index, and assess serial treatment recovery."),
        ("WhatsApp Business Cloud Messaging", "Automated appointment reminders, digital prescription delivery, and 2-way patient booking confirmation via WhatsApp."),
        ("Tele-Dermatology & Video Visits", "End-to-end encrypted WebRTC video consultations for remote aesthetic follow-ups and chemical peel healing monitoring."),
        ("Smart Consumable Stock Tracking", "Automated inventory management for aesthetic serums, laser cooling gels, and PRP vacutainers with batch expiry alerts."),
        ("Ayushman Bharat (ABDM) Integration", "National health digital connectivity through ABHA IDs, enabling verified longitudinal electronic health record sharing."),
        ("Multi-Branch Scalability", "Expanding current Spring Boot backend into a multi-tenant cloud architecture to seamlessly support new Derma Bliss branches.")
    ]

    for i, (e_title, e_desc) in enumerate(enhancements_grid):
        col = i % 2
        row = i // 2
        l = PptxInches(0.8 + col * 5.9)
        t = PptxInches(1.8 + row * 1.7)

        box = slide14.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, l, t, PptxInches(5.7), PptxInches(1.5))
        box.fill.solid()
        box.fill.fore_color.rgb = PPTX_WHITE
        box.line.color.rgb = PPTX_GOLD
        box.line.width = PptxPt(1)

        tf = box.text_frame
        tf.word_wrap = True

        p = tf.paragraphs[0]
        p.text = f"{i+1}. {e_title}"
        p.font.size = PptxPt(13)
        p.font.bold = True
        p.font.color.rgb = PPTX_BURGUNDY
        p.space_after = PptxPt(4)

        p_desc = tf.add_paragraph()
        p_desc.text = e_desc
        p_desc.font.size = PptxPt(10.5)
        p_desc.font.color.rgb = PPTX_DARK

    # ------------------ SLIDE 15: CONCLUSION & CLINIC CREDENTIALS ------------------
    slide15 = prs.slides.add_slide(blank_layout)
    set_slide_background(slide15, PPTX_BURGUNDY)

    box_end = slide15.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, PptxInches(2.0), PptxInches(1.2), PptxInches(9.333), PptxInches(5.1))
    box_end.fill.solid()
    box_end.fill.fore_color.rgb = PPTX_WHITE
    box_end.line.color.rgb = PPTX_GOLD
    box_end.line.width = PptxPt(2.5)

    tf_end = box_end.text_frame
    tf_end.word_wrap = True

    p = tf_end.paragraphs[0]
    p.alignment = PP_ALIGN.CENTER
    p.text = "Thank You!"
    p.font.size = PptxPt(30)
    p.font.bold = True
    p.font.color.rgb = PPTX_BURGUNDY
    p.space_after = PptxPt(8)

    p_sub = tf_end.add_paragraph()
    p_sub.alignment = PP_ALIGN.CENTER
    p_sub.text = "Derma Bliss Clinic Management System — Fully Verified & Production Ready"
    p_sub.font.size = PptxPt(14)
    p_sub.font.bold = True
    p_sub.font.color.rgb = PPTX_GOLD
    p_sub.space_after = PptxPt(18)

    p_doc = tf_end.add_paragraph()
    p_doc.alignment = PP_ALIGN.CENTER
    p_doc.text = "Dr. Ankita Santosh Koli (BHMS, PGDMS, CCH, CGO, CSD)\nMedical Registration No: 73892 • Cosmetologist & Trichologist"
    p_doc.font.size = PptxPt(13)
    p_doc.font.bold = True
    p_doc.font.color.rgb = PPTX_DARK
    p_doc.space_after = PptxPt(12)

    p_details = tf_end.add_paragraph()
    p_details.alignment = PP_ALIGN.CENTER
    p_details.text = "Clinic Address: DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603\nOfficial Helpline: +91 789 789 2177 • Instagram: @derma_bliss_clinic\nClinic Operating Hours: 10:00 AM – 01:00 PM & 05:00 PM – 09:00 PM (Mon–Sat)"
    p_details.font.size = PptxPt(11)
    p_details.font.color.rgb = PPTX_MUTED

    prs.save(PRESENTATION_PATH)
    print(f"[OK] Successfully saved Presentation: {PRESENTATION_PATH}")

if __name__ == "__main__":
    build_project_report()
    build_presentation()
    print("\nALL DELIVERABLES SUCCESSFULLY GENERATED!")

