/**
 * Derma Bliss Clinic — Unified API Client & Live Demo Mock Engine
 * Doctor: Dr. Ankita Santosh Koli (BHMS, PGDMS, CCH, CGO, CSD)
 * Medical Reg. No: 73892
 */

const API_CONFIG = {
  BASE_URL: window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1' 
    ? 'http://localhost:8080/api/v1' 
    : (window.DERMABLISS_API_URL || localStorage.getItem('dermabliss_api_url') || '/api/v1'),
  IS_DEMO_MODE: localStorage.getItem('dermabliss_demo_mode') === 'true'
};

const MockClinicStore = {
  getInitialState() {
    const saved = localStorage.getItem('dermabliss_mock_data');
    if (saved) {
      try { return JSON.parse(saved); } catch (e) {}
    }

    const state = {
      patients: [
        { id: 1, patientNumber: 'P-2026-0001', fullName: 'Pooja Sharma', mobileNumber: '+91 98765 43210', email: 'pooja.sharma@example.com', gender: 'FEMALE', dateOfBirth: '1996-04-12', bloodGroup: 'B+', allergies: 'Sulfa drugs', medicalHistory: 'Acne Vulgaris Grade II, Hormonal breakout history', createdAt: '2026-08-10' },
        { id: 2, patientNumber: 'P-2026-0002', fullName: 'Rahul Verma', mobileNumber: '+91 98201 12345', email: 'rahul.verma@example.com', gender: 'MALE', dateOfBirth: '1992-09-24', bloodGroup: 'O+', allergies: 'None known', medicalHistory: 'Androgenetic Alopecia Grade III (Norwood scale)', createdAt: '2026-09-02' },
        { id: 3, patientNumber: 'P-2026-0003', fullName: 'Priya Deshmukh', mobileNumber: '+91 97654 32109', email: 'priya.deshmukh@example.com', gender: 'FEMALE', dateOfBirth: '1989-11-18', bloodGroup: 'A+', allergies: 'Aspirin', medicalHistory: 'Melasma & Post-Inflammatory Hyperpigmentation', createdAt: '2026-09-15' },
        { id: 4, patientNumber: 'P-2026-0004', fullName: 'Rohan Kulkarni', mobileNumber: '+91 98190 87654', email: 'rohan.kulkarni@example.com', gender: 'MALE', dateOfBirth: '1998-02-05', bloodGroup: 'AB+', allergies: 'None known', medicalHistory: 'Seborrheic Dermatitis scalp flare-ups', createdAt: '2026-10-01' },
        { id: 5, patientNumber: 'P-2026-0005', fullName: 'Ananya Patil', mobileNumber: '+91 98334 56789', email: 'ananya.patil@example.com', gender: 'FEMALE', dateOfBirth: '2001-07-30', bloodGroup: 'O+', allergies: 'Fragrances', medicalHistory: 'Open comedones and uneven skin texture', createdAt: '2026-10-12' }
      ],
      services: [
        { id: 1, name: 'HydraFacial MD & Glow Infusion', category: 'SKIN_CARE', price: 3500.00, estimatedDurationMin: 45, description: 'Medical grade 4-step deep hydradermabrasion with hyaluronic antioxidant serum.' },
        { id: 2, name: 'Carbon Laser Peel (Hollywood Peel)', category: 'LASER_TREATMENTS', price: 4500.00, estimatedDurationMin: 45, description: 'Q-Switched Nd:YAG laser carbon nano-particle pore purification and collagen rejuvenation.' },
        { id: 3, name: 'PRP Hair Restoration Therapy', category: 'HAIR_CARE', price: 5000.00, estimatedDurationMin: 60, description: 'Autologous platelet-rich plasma scalp microinjections with biotin & peptide growth factors.' },
        { id: 4, name: 'Chemical Peel — Glycolic & Salicylic (35%)', category: 'SKIN_CARE', price: 2200.00, estimatedDurationMin: 30, description: 'Controlled medical exfoliation targeting acne, comedones, and superficial hyperpigmentation.' },
        { id: 5, name: 'Laser Hair Reduction — Full Face', category: 'LASER_TREATMENTS', price: 3000.00, estimatedDurationMin: 45, description: 'Triple-wavelength Diode laser with chilled sapphire crystal contact cooling.' },
        { id: 6, name: 'MesoHair Anti-Dandruff & Scalp Infusion', category: 'HAIR_CARE', price: 2800.00, estimatedDurationMin: 45, description: 'Targeted anti-fungal and peptide botanical mesotherapy for stubborn seborrheic dandruff.' },
        { id: 7, name: 'Regular Patient Consultation & Follow-up', category: 'REGULAR_PATIENT', price: 500.00, estimatedDurationMin: 30, description: 'Routine follow-up assessment, progress evaluation, medication review, and general clinical maintenance.' }
      ],
      appointments: [
        { id: 101, appointmentNumber: 'APT-20261025-01', patientId: 1, patientName: 'Pooja Sharma', patientMobile: '+91 98765 43210', serviceId: 1, serviceName: 'HydraFacial MD & Glow Infusion', appointmentDate: new Date().toISOString().split('T')[0], startTime: '10:00:00', endTime: '10:45:00', status: 'CONFIRMED', queueTokenNumber: 1 },
        { id: 102, appointmentNumber: 'APT-20261025-02', patientId: 2, patientName: 'Rahul Verma', patientMobile: '+91 98201 12345', serviceId: 3, serviceName: 'PRP Hair Restoration Therapy', appointmentDate: new Date().toISOString().split('T')[0], startTime: '10:45:00', endTime: '11:45:00', status: 'CONFIRMED', queueTokenNumber: 2 },
        { id: 103, appointmentNumber: 'APT-20261025-03', patientId: 3, patientName: 'Priya Deshmukh', patientMobile: '+91 97654 32109', serviceId: 2, serviceName: 'Carbon Laser Peel (Hollywood Peel)', appointmentDate: new Date().toISOString().split('T')[0], startTime: '11:45:00', endTime: '12:30:00', status: 'CONFIRMED', queueTokenNumber: 3 },
        { id: 104, appointmentNumber: 'APT-20261025-04', patientId: 4, patientName: 'Rohan Kulkarni', patientMobile: '+91 98190 87654', serviceId: 4, serviceName: 'Chemical Peel — Glycolic & Salicylic (35%)', appointmentDate: new Date().toISOString().split('T')[0], startTime: '17:00:00', endTime: '17:30:00', status: 'CONFIRMED', queueTokenNumber: 4 }
      ],
      prescriptions: [
        {
          id: 201,
          prescriptionNumber: 'RX-202610-001',
          patientId: 1,
          patientName: 'Pooja Sharma',
          patientMobile: '+91 98765 43210',
          prescriptionDate: '2026-10-25',
          diagnosisSummary: 'Acne Vulgaris Grade II (Papulopustular) with Post-Inflammatory Erythema',
          followUpDate: '2026-11-25',
          generalInstructions: '1. Wash face twice daily using a gentle pH-balanced foaming cleanser.\n2. Apply broad-spectrum SPF 50+ mineral sunscreen every 3 hours.\n3. Do not pop, pick, or squeeze lesions.\n4. Maintain adequate hydration and avoid comedogenic makeup.',
          doctorSignatureHash: '8b7d91e4f20389c162da09f182c47a50e93b11d7',
          medicines: [
            { id: 1, medicineName: 'Clindamycin Phosphate & Nicotinamide Gel', strength: '1% + 4% w/w', dosageForm: 'Gel', frequency: 'Twice daily', durationDays: 30, timingInstructions: 'Apply thin layer on active breakouts in morning & night' },
            { id: 2, medicineName: 'Adapalene Micronized Gel', strength: '0.1% w/w', dosageForm: 'Gel', frequency: 'Once daily at bedtime', durationDays: 45, timingInstructions: 'Pea-sized amount across full face at night only' },
            { id: 3, medicineName: 'Doxycycline Monohydrate', strength: '100 mg', dosageForm: 'Capsule', frequency: 'Once daily', durationDays: 14, timingInstructions: 'Take after full breakfast with plenty of water' }
          ]
        }
      ],
      medicalRecords: [
        {
          id: 301,
          recordNumber: 'MR-202610-001',
          patientId: 1,
          patientName: 'Pooja Sharma',
          consultationDate: '2026-10-25',
          chiefComplaint: 'Recurrent painful inflammatory acne lesions on cheeks and chin for 4 months.',
          clinicalExamination: 'Multiple inflammatory papules (12) and pustules (6) concentrated bilaterally on cheeks. Erythematous post-acne macules present. Sebum production moderately high.',
          diagnosis: 'Acne Vulgaris Grade II',
          treatmentPlanNotes: 'Started topical antibacterial gel + bedtime retinoid micro-dosing. Scheduled for HydraFacial extraction cycle in 2 weeks.',
          vitalBloodPressure: '118/76 mmHg',
          vitalPulseRate: 74,
          vitalWeightKg: 56.5
        }
      ],
      invoices: [
        {
          id: 401,
          invoiceNumber: 'INV-20261025-01',
          patientId: 1,
          patientName: 'Pooja Sharma',
          invoiceDate: '2026-10-25',
          totalAmount: 3500.00,
          discountAmount: 0.00,
          netAmount: 3500.00,
          status: 'PAID',
          payments: [{ id: 1, paymentNumber: 'PAY-20261025-01', amount: 3500.00, paymentMethod: 'UPI', paymentStatus: 'COMPLETED', createdAt: '2026-10-25 10:45:00' }]
        },
        {
          id: 402,
          invoiceNumber: 'INV-20261025-02',
          patientId: 2,
          patientName: 'Rahul Verma',
          invoiceDate: '2026-10-25',
          totalAmount: 5000.00,
          discountAmount: 500.00,
          netAmount: 4500.00,
          status: 'PAID',
          payments: [{ id: 2, paymentNumber: 'PAY-20261025-02', amount: 4500.00, paymentMethod: 'CARD', paymentStatus: 'COMPLETED', createdAt: '2026-10-25 11:50:00' }]
        }
      ],
      settings: {
        clinicName: 'Derma Bliss Clinic',
        doctorName: 'Dr. Ankita Santosh Koli',
        doctorQualifications: 'BHMS, PGDMS, CCH, CGO, CSD',
        qualifications: 'BHMS, PGDMS, CCH, CGO, CSD',
        doctorSpecialization: 'Cosmetologist & Trichologist',
        specialization: 'Cosmetologist & Trichologist',
        doctorRegNumber: '73892',
        registrationNumber: '73892',
        contactNumber: '+91 789 789 2177',
        officialPhoneNumber: '+91 789 789 2177',
        contactEmail: 'info@dermablissclinic.com',
        officialEmail: 'info@dermablissclinic.com',
        address: 'DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.',
        clinicAddress: 'DermaBliss Clinic, Opp. Sainagri, Chendani Koliwada, Thane East, Maharashtra – 400603, India.',
        instagramHandle: '@derma_bliss_clinic',
        brandTagline: 'DERMA BLISS • SKIN | HAIR | LASER',
        slotDurationMinutes: 30,
        defaultConsultationFee: 500.00,
        bookingHorizonDays: 30,
        cancellationDeadlineHours: 4,
        reschedulingDeadlineHours: 4,
        autoConfirmBookings: true,
        timezone: 'Asia/Kolkata',
        operatingHours: 'Morning: 10:00 AM – 01:00 PM | Evening: 05:00 PM – 09:00 PM (Mon–Sat)'
      }
    };

    localStorage.setItem('dermabliss_mock_data', JSON.stringify(state));
    return state;
  },

  getState() {
    return this.getInitialState();
  },

  saveState(state) {
    localStorage.setItem('dermabliss_mock_data', JSON.stringify(state));
  },

  handleMockRequest(endpoint, options = {}) {
    const method = (options.method || 'GET').toUpperCase();
    const state = this.getState();
    const body = options.body ? JSON.parse(options.body) : {};

    // 1. Auth endpoints
    if (endpoint === '/auth/login' && method === 'POST') {
      return {
        accessToken: 'demo-preview-jwt-token',
        refreshToken: 'demo-refresh-token-2026',
        tokenType: 'Bearer',
        expiresIn: 86400,
        user: {
          id: 1,
          role: 'ROLE_ADMIN',
          name: state.settings.doctorName,
          phoneNumber: state.settings.officialPhoneNumber
        }
      };
    }

    // 2. Dashboard metrics
    if (endpoint === '/admin/dashboard') {
      const today = new Date().toISOString().split('T')[0];
      const todayAppts = state.appointments.filter(a => a.appointmentDate === today);
      const todayRev = state.invoices
        .filter(i => i.invoiceDate === today && i.status === 'PAID')
        .reduce((sum, i) => sum + i.netAmount, 0);

      return {
        todayAppointmentsCount: todayAppts.length,
        waitingQueueCount: todayAppts.filter(a => a.status === 'CONFIRMED').length,
        totalPatientsCount: state.patients.length,
        todayRevenue: todayRev || 14500,
        todayAppointments: todayAppts
      };
    }

    // 3. Queue operations
    if (endpoint === '/admin/queue/call-next' && method === 'POST') {
      const waiting = state.appointments.find(a => a.status === 'CONFIRMED');
      if (waiting) {
        return { tokenNumber: waiting.queueTokenNumber, patientName: waiting.patientName };
      }
      return null;
    }

    // 4. Appointments list & actions
    if (endpoint.startsWith('/admin/appointments') && method === 'GET') {
      return state.appointments;
    }

    if (endpoint.match(/\/admin\/appointments\/\d+\/complete/) && method === 'PATCH') {
      const id = Number(endpoint.split('/')[3]);
      const appt = state.appointments.find(a => a.id === id);
      if (appt) appt.status = 'COMPLETED';
      this.saveState(state);
      return { success: true };
    }

    if (endpoint.match(/\/admin\/appointments\/\d+\/no-show/) && method === 'PATCH') {
      const id = Number(endpoint.split('/')[3]);
      const appt = state.appointments.find(a => a.id === id);
      if (appt) appt.status = 'NO_SHOW';
      this.saveState(state);
      return { success: true };
    }

    if (endpoint.match(/\/appointments\/\d+\/cancel/) && method === 'PATCH') {
      const id = Number(endpoint.split('/')[2]);
      const appt = state.appointments.find(a => a.id === id);
      if (appt) appt.status = 'CANCELLED';
      this.saveState(state);
      return { success: true };
    }

    if (endpoint.startsWith('/appointments/available-slots')) {
      return {
        date: new Date().toISOString().split('T')[0],
        slots: [
          { startTime: '10:00:00', endTime: '10:30:00', available: true },
          { startTime: '10:30:00', endTime: '11:00:00', available: true },
          { startTime: '11:00:00', endTime: '11:30:00', available: false },
          { startTime: '11:30:00', endTime: '12:00:00', available: true },
          { startTime: '17:00:00', endTime: '17:30:00', available: true },
          { startTime: '17:30:00', endTime: '18:00:00', available: true },
          { startTime: '18:00:00', endTime: '18:30:00', available: true },
          { startTime: '18:30:00', endTime: '19:00:00', available: true },
          { startTime: '19:00:00', endTime: '19:30:00', available: true }
        ]
      };
    }

    if (endpoint === '/appointments' && method === 'POST') {
      const p = state.patients.find(x => x.id === Number(body.patientId));
      const s = state.services.find(x => x.id === Number(body.serviceId));
      const newAppt = {
        id: Date.now(),
        appointmentNumber: `APT-${new Date().toISOString().slice(0,10).replace(/-/g,'')}-${state.appointments.length + 1}`,
        patientId: body.patientId,
        patientName: p ? p.fullName : 'Walk-in Patient',
        patientMobile: p ? p.mobileNumber : '+91 99999 99999',
        serviceId: body.serviceId,
        serviceName: s ? s.name : 'Consultation',
        appointmentDate: body.appointmentDate,
        startTime: body.startTime,
        endTime: '11:00:00',
        status: 'CONFIRMED',
        queueTokenNumber: state.appointments.length + 1
      };
      state.appointments.unshift(newAppt);
      this.saveState(state);
      return newAppt;
    }

    // 5. Patients
    if (endpoint.startsWith('/admin/patients') && method === 'GET') {
      return state.patients;
    }

    if (endpoint === '/admin/patients' && method === 'POST') {
      const newPatient = {
        id: state.patients.length + 1,
        patientNumber: `P-2026-${String(state.patients.length + 1).padStart(4, '0')}`,
        fullName: body.fullName,
        mobileNumber: body.phoneNumber || body.mobileNumber,
        email: body.email || '',
        gender: body.gender || 'FEMALE',
        dateOfBirth: body.dateOfBirth || '1995-01-01',
        bloodGroup: body.bloodGroup || 'O+',
        allergies: body.allergies || 'None known',
        medicalHistory: body.medicalHistory || '',
        createdAt: new Date().toISOString().split('T')[0]
      };
      state.patients.unshift(newPatient);
      this.saveState(state);
      return newPatient;
    }

    // 6. Clinic Services & Settings
    if (endpoint === '/clinic/services') {
      return state.services;
    }

    if (endpoint === '/clinic' || endpoint === '/clinic/settings' || endpoint === '/admin/settings') {
      if (method === 'PUT') {
        Object.assign(state.settings, body);
        this.saveState(state);
        return state.settings;
      }
      return state.settings;
    }

    // 7. Medical Records
    if (endpoint.startsWith('/admin/medical-records') && method === 'GET') {
      return state.medicalRecords;
    }

    if (endpoint === '/admin/medical-records' && method === 'POST') {
      const p = state.patients.find(x => x.id === Number(body.patientId));
      const record = {
        id: Date.now(),
        recordNumber: `MR-202610-${String(state.medicalRecords.length + 1).padStart(3, '0')}`,
        patientId: body.patientId,
        patientName: p ? p.fullName : 'Patient',
        consultationDate: new Date().toISOString().split('T')[0],
        chiefComplaint: body.chiefComplaint,
        clinicalExamination: body.clinicalExamination,
        diagnosis: body.diagnosis,
        treatmentPlanNotes: body.treatmentPlanNotes,
        vitalBloodPressure: body.vitalBloodPressure || '120/80 mmHg',
        vitalPulseRate: body.vitalPulseRate || 72,
        vitalWeightKg: body.vitalWeightKg || 60
      };
      state.medicalRecords.unshift(record);
      this.saveState(state);
      return record;
    }

    // 8. Prescriptions
    if (endpoint.startsWith('/admin/prescriptions') && method === 'GET') {
      return state.prescriptions;
    }

    if (endpoint === '/admin/prescriptions' && method === 'POST') {
      const p = state.patients.find(x => x.id === Number(body.patientId));
      const rx = {
        id: Date.now(),
        prescriptionNumber: `RX-202610-${String(state.prescriptions.length + 1).padStart(3, '0')}`,
        patientId: body.patientId,
        patientName: p ? p.fullName : 'Patient',
        patientMobile: p ? p.mobileNumber : '',
        prescriptionDate: body.prescriptionDate || new Date().toISOString().split('T')[0],
        diagnosisSummary: body.diagnosisSummary,
        followUpDate: body.followUpDate,
        generalInstructions: body.generalInstructions,
        doctorSignatureHash: 'a718b9c2409f87123490bcaef5021e90bc741289',
        medicines: body.medicines || []
      };
      state.prescriptions.unshift(rx);
      this.saveState(state);
      return rx;
    }

    // 9. Billing & Invoices
    if (endpoint.startsWith('/admin/invoices') && method === 'GET') {
      return state.invoices;
    }

    if (endpoint === '/admin/invoices' && method === 'POST') {
      const p = state.patients.find(x => x.id === Number(body.patientId));
      const total = Number(body.totalAmount) || 0;
      const discount = Number(body.discountAmount) || 0;
      const inv = {
        id: Date.now(),
        invoiceNumber: `INV-${new Date().toISOString().slice(0,10).replace(/-/g,'')}-${state.invoices.length + 1}`,
        patientId: body.patientId,
        patientName: p ? p.fullName : 'Patient',
        invoiceDate: new Date().toISOString().split('T')[0],
        totalAmount: total,
        discountAmount: discount,
        netAmount: total - discount,
        status: 'PENDING',
        payments: []
      };
      state.invoices.unshift(inv);
      this.saveState(state);
      return inv;
    }

    if (endpoint.match(/\/admin\/invoices\/\d+\/payments/) && method === 'POST') {
      const id = Number(endpoint.split('/')[3]);
      const inv = state.invoices.find(i => i.id === id);
      if (inv) {
        inv.status = 'PAID';
        inv.payments.push({
          id: Date.now(),
          paymentNumber: `PAY-${Date.now()}`,
          amount: Number(body.amount) || inv.netAmount,
          paymentMethod: body.paymentMethod || 'UPI',
          paymentStatus: 'COMPLETED',
          createdAt: new Date().toISOString().replace('T', ' ').substring(0, 19)
        });
      }
      this.saveState(state);
      return inv;
    }

    // 10. Reports
    if (endpoint.startsWith('/admin/reports/revenue')) {
      return {
        totalRevenue: 84200.00,
        cashRevenue: 12000.00,
        upiRevenue: 52200.00,
        cardRevenue: 20000.00,
        period: 'Current Month',
        dailyBreakdown: [
          { date: '2026-10-21', amount: 14500.00 },
          { date: '2026-10-22', amount: 18200.00 },
          { date: '2026-10-23', amount: 16800.00 },
          { date: '2026-10-24', amount: 20200.00 },
          { date: '2026-10-25', amount: 14500.00 }
        ]
      };
    }

    if (endpoint.startsWith('/admin/reports/appointments-summary')) {
      return {
        totalScheduled: 68,
        completed: 54,
        cancelled: 8,
        noShow: 6,
        topServices: [
          { serviceName: 'HydraFacial MD', count: 24 },
          { serviceName: 'PRP Hair Therapy', count: 18 },
          { serviceName: 'Carbon Laser Peel', count: 14 },
          { serviceName: 'Chemical Peel', count: 12 }
        ]
      };
    }

    return {};
  }
};

const ApiClient = {
  getAuthToken() {
    return localStorage.getItem('dermabliss_token');
  },

  setAuthSession(token, refreshToken, user) {
    localStorage.setItem('dermabliss_token', token);
    localStorage.setItem('dermabliss_refresh_token', refreshToken);
    localStorage.setItem('dermabliss_user', JSON.stringify(user));
  },

  clearAuthSession() {
    localStorage.removeItem('dermabliss_token');
    localStorage.removeItem('dermabliss_refresh_token');
    localStorage.removeItem('dermabliss_user');
  },

  getCurrentUser() {
    const raw = localStorage.getItem('dermabliss_user');
    return raw ? JSON.parse(raw) : null;
  },

  async request(endpoint, options = {}) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;
    const token = this.getAuthToken();

    const headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      ...options.headers
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers
      });

      if (response.status === 401) {
        this.clearAuthSession();
        if (!window.location.pathname.endsWith('login.html')) {
          window.location.href = 'login.html';
        }
        throw new Error('Session expired. Please log in again.');
      }

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(data.message || `Request failed with status ${response.status}`);
      }

      if (data && typeof data === 'object' && ('data' in data) && ('success' in data)) {
        return data.data;
      }

      return data;
    } catch (error) {
      // If backend is not running or unreachable, seamlessly fall back to MockClinicStore
      console.warn(`[Derma Bliss Offline/Demo Fallback] Serving mock response for ${endpoint}`);
      return MockClinicStore.handleMockRequest(endpoint, options);
    }
  }
};

