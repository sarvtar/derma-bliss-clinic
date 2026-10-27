import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  SafeAreaView,
} from 'react-native';
import { Colors } from '../theme/colors';

interface Props {
  user: any;
  onNavigate: (screen: string) => void;
  onLogout: () => void;
}

export const HomeScreen: React.FC<Props> = ({ user, onNavigate, onLogout }) => {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.scroll}>
        {/* Header */}
        <View style={styles.topBar}>
          <View>
            <Text style={styles.greeting}>Welcome,</Text>
            <Text style={styles.patientName}>{user?.fullName || 'Patient'}</Text>
          </View>
          <TouchableOpacity style={styles.logoutBtn} onPress={onLogout}>
            <Text style={styles.logoutText}>Sign Out</Text>
          </TouchableOpacity>
        </View>

        {/* Doctor & Clinic Profile Card */}
        <View style={styles.doctorCard}>
          <Text style={styles.brandTagline}>DERMA BLISS &bull; SKIN | HAIR | LASER</Text>
          <Text style={styles.clinicTitle}>Derma Bliss Clinic</Text>
          <Text style={styles.doctorName}>Dr. Ankita Santosh Koli</Text>
          <Text style={styles.doctorQuals}>BHMS, PGDMS, CCH, CGO, CSD &bull; Reg. 73892</Text>
          <Text style={styles.doctorSpec}>Cosmetologist &amp; Trichologist</Text>

          <View style={styles.divider} />

          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Location:</Text>
            <Text style={styles.infoValue}>
              Opp. Sainagri, Chendani Koliwada, Thane East &bull; 400603
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Direct Line:</Text>
            <Text style={styles.infoValueHighlight}>+91 789 789 2177</Text>
          </View>
        </View>

        {/* Quick Clinical Actions */}
        <Text style={styles.sectionTitle}>Patient Portal Actions</Text>
        <View style={styles.actionsGrid}>
          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => onNavigate('BookAppointment')}
          >
            <Text style={styles.actionIcon}>📅</Text>
            <Text style={styles.actionTitle}>Book Appointment</Text>
            <Text style={styles.actionDesc}>Schedule visit with Dr. Ankita</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => onNavigate('MyAppointments')}
          >
            <Text style={styles.actionIcon}>📋</Text>
            <Text style={styles.actionTitle}>My Appointments</Text>
            <Text style={styles.actionDesc}>View slots &amp; live queue token</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => onNavigate('Prescriptions')}
          >
            <Text style={styles.actionIcon}>💊</Text>
            <Text style={styles.actionTitle}>My Prescriptions</Text>
            <Text style={styles.actionDesc}>Digital Rx &amp; medication instructions</Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.actionCard}
            onPress={() => onNavigate('MedicalRecords')}
          >
            <Text style={styles.actionIcon}>📁</Text>
            <Text style={styles.actionTitle}>Medical Records</Text>
            <Text style={styles.actionDesc}>Clinical consultation summaries</Text>
          </TouchableOpacity>
        </View>

        {/* Specialized Services */}
        <Text style={styles.sectionTitle}>Specialized Practice Areas</Text>
        <View style={styles.serviceItem}>
          <Text style={styles.serviceTitle}>🩺 Regular Patient Checkup &amp; Follow-up</Text>
          <Text style={styles.serviceDesc}>
            Routine clinical consultations, treatment review, follow-up examinations, and continuous dermatological care.
          </Text>
        </View>

        <View style={styles.serviceItem}>
          <Text style={styles.serviceTitle}>✨ Skin Care &amp; Clinical Cosmetology</Text>
          <Text style={styles.serviceDesc}>
            Acne therapy, chemical peels, hyperpigmentation correction, and anti-aging dermatology.
          </Text>
        </View>

        <View style={styles.serviceItem}>
          <Text style={styles.serviceTitle}>🌿 Trichology &amp; Hair Restoration</Text>
          <Text style={styles.serviceDesc}>
            Hair fall diagnosis, scalp health treatments, and personalized hair growth protocols.
          </Text>
        </View>

        <View style={styles.serviceItem}>
          <Text style={styles.serviceTitle}>⚡ Laser Treatments</Text>
          <Text style={styles.serviceDesc}>
            Advanced aesthetic laser resurfacing, scar reduction, and precision laser hair removal.
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  scroll: {
    padding: 20,
  },
  topBar: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  greeting: {
    fontSize: 13,
    color: Colors.textMuted,
  },
  patientName: {
    fontSize: 20,
    fontWeight: '700',
    color: Colors.textCharcoal,
  },
  logoutBtn: {
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 6,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    backgroundColor: Colors.surface,
  },
  logoutText: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.textCharcoal,
  },
  doctorCard: {
    backgroundColor: Colors.surface,
    padding: 20,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    marginBottom: 24,
    shadowColor: Colors.textCharcoal,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.04,
    shadowRadius: 8,
    elevation: 2,
  },
  brandTagline: {
    fontSize: 10,
    letterSpacing: 2,
    color: Colors.primaryGoldDark,
    fontWeight: '700',
    marginBottom: 4,
  },
  clinicTitle: {
    fontSize: 22,
    fontWeight: '700',
    color: Colors.textCharcoal,
  },
  doctorName: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.textCharcoal,
    marginTop: 2,
  },
  doctorQuals: {
    fontSize: 11,
    color: Colors.textMuted,
    marginTop: 2,
  },
  doctorSpec: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.primaryGoldDark,
    marginTop: 2,
  },
  divider: {
    height: 1,
    backgroundColor: Colors.borderDelicate,
    marginVertical: 14,
  },
  infoRow: {
    flexDirection: 'row',
    marginBottom: 4,
  },
  infoLabel: {
    fontSize: 11,
    fontWeight: '600',
    color: Colors.textCharcoal,
    width: 80,
  },
  infoValue: {
    flex: 1,
    fontSize: 11,
    color: Colors.textMuted,
  },
  infoValueHighlight: {
    flex: 1,
    fontSize: 12,
    fontWeight: '700',
    color: Colors.primaryGoldDark,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 12,
  },
  actionsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 24,
  },
  actionCard: {
    width: '48%',
    backgroundColor: Colors.surface,
    padding: 16,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
  },
  actionIcon: {
    fontSize: 24,
    marginBottom: 8,
  },
  actionTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.textCharcoal,
    marginBottom: 4,
  },
  actionDesc: {
    fontSize: 11,
    color: Colors.textLight,
    lineHeight: 15,
  },
  serviceItem: {
    backgroundColor: Colors.surface,
    padding: 16,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    marginBottom: 10,
  },
  serviceTitle: {
    fontSize: 14,
    fontWeight: '600',
    color: Colors.textCharcoal,
    marginBottom: 4,
  },
  serviceDesc: {
    fontSize: 12,
    color: Colors.textMuted,
    lineHeight: 17,
  },
});
