export interface ClinicProfile {
  clinicName: string;
  doctorName: string;
  doctorQualifications: string;
  doctorSpecialization: string;
  registrationNumber: string;
  contactNumber: string;
  address: string;
  instagramHandle: string;
  brandTagline: string;
  timezone: string;
  defaultConsultationFee: number;
  slotDurationMinutes: number;
}

export interface PatientUser {
  id: number;
  fullName: string;
  phoneNumber: string;
  email?: string;
  role: 'ROLE_ADMIN' | 'ROLE_PATIENT';
}

export interface AppointmentSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}

export interface Appointment {
  id: number;
  appointmentNumber: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW';
  serviceName?: string;
  patientNotes?: string;
  tokenNumber?: number;
}

export interface PrescriptionMedicine {
  id: number;
  medicineName: string;
  strength: string;
  dosageForm: string;
  frequency: string;
  durationDays: number;
  timingInstructions?: string;
  specificNotes?: string;
}

export interface Prescription {
  id: number;
  prescriptionNumber: string;
  prescriptionDate: string;
  diagnosisSummary?: string;
  followUpDate?: string;
  generalInstructions?: string;
  medicines: PrescriptionMedicine[];
}
