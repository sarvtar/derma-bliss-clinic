import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  Alert,
  SafeAreaView,
} from 'react-native';
import { Colors } from '../theme/colors';
import { apiClient } from '../services/api';

interface Props {
  onBack: () => void;
  onBookNew: () => void;
}

export const MyAppointmentsScreen: React.FC<Props> = ({ onBack, onBookNew }) => {
  const [appointments, setAppointments] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAppointments();
  }, []);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      const resp = await apiClient.get('/appointments/my');
      setAppointments(resp.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = (id: number) => {
    Alert.alert(
      'Cancel Consultation',
      'Are you sure you wish to cancel this appointment?',
      [
        { text: 'Keep Appointment', style: 'cancel' },
        {
          text: 'Yes, Cancel',
          style: 'destructive',
          onPress: async () => {
            try {
              await apiClient.patch(`/appointments/${id}/cancel`, {
                reason: 'Patient cancelled via mobile app',
              });
              Alert.alert('Appointment Cancelled', 'Your slot has been released.');
              loadAppointments();
            } catch (err: any) {
              Alert.alert('Error', err.response?.data?.message || 'Failed to cancel appointment');
            }
          },
        },
      ]
    );
  };

  const renderItem = ({ item }: { item: any }) => {
    const isConfirmed = item.status === 'CONFIRMED';
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <Text style={styles.refNumber}>{item.appointmentNumber}</Text>
          <View
            style={[
              styles.statusBadge,
              isConfirmed ? styles.badgeConfirmed : styles.badgeOther,
            ]}
          >
            <Text
              style={[
                styles.statusText,
                isConfirmed ? styles.statusConfirmedText : styles.statusOtherText,
              ]}
            >
              {item.status}
            </Text>
          </View>
        </View>

        <Text style={styles.serviceName}>{item.serviceName || 'Dermatology Consultation'}</Text>
        <Text style={styles.dateTime}>
          📅 {item.appointmentDate} &bull; ⏰ {item.startTime?.substring(0, 5)} - {item.endTime?.substring(0, 5)}
        </Text>

        {item.queueTokenNumber ? (
          <View style={styles.tokenContainer}>
            <Text style={styles.tokenLabel}>Daily Clinic Queue Token</Text>
            <Text style={styles.tokenNumber}>#{item.queueTokenNumber}</Text>
          </View>
        ) : null}

        {item.patientNotes ? (
          <Text style={styles.notes}>Notes: {item.patientNotes}</Text>
        ) : null}

        {isConfirmed && (
          <TouchableOpacity
            style={styles.cancelBtn}
            onPress={() => handleCancel(item.id)}
          >
            <Text style={styles.cancelBtnText}>Cancel Reservation</Text>
          </TouchableOpacity>
        )}
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.topBar}>
        <TouchableOpacity onPress={onBack} style={styles.backButton}>
          <Text style={styles.backText}>&larr; Back</Text>
        </TouchableOpacity>
        <Text style={styles.screenTitle}>My Appointments</Text>
        <TouchableOpacity onPress={onBookNew} style={styles.newBtn}>
          <Text style={styles.newBtnText}>+ New</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <ActivityIndicator color={Colors.primaryGoldDark} style={{ marginTop: 40 }} />
      ) : appointments.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>No Scheduled Consultations</Text>
          <Text style={styles.emptySubtitle}>You do not have any active appointments reserved.</Text>
          <TouchableOpacity style={styles.bookNowBtn} onPress={onBookNew}>
            <Text style={styles.bookNowText}>Book an Appointment</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <FlatList
          data={appointments}
          renderItem={renderItem}
          keyExtractor={(item) => String(item.id)}
          contentContainerStyle={styles.list}
        />
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  topBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 14,
    borderBottomWidth: 1,
    borderColor: Colors.borderDelicate,
    backgroundColor: Colors.surface,
  },
  backButton: {
    padding: 6,
  },
  backText: {
    color: Colors.primaryGoldDark,
    fontSize: 15,
    fontWeight: '600',
  },
  screenTitle: {
    fontSize: 17,
    fontWeight: '700',
    color: Colors.textCharcoal,
  },
  newBtn: {
    padding: 6,
  },
  newBtnText: {
    color: Colors.primaryGoldDark,
    fontSize: 15,
    fontWeight: '700',
  },
  list: {
    padding: 20,
    gap: 14,
  },
  card: {
    backgroundColor: Colors.surface,
    padding: 18,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  refNumber: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.primaryGoldDark,
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 4,
  },
  badgeConfirmed: {
    backgroundColor: '#E8F5E9',
  },
  badgeOther: {
    backgroundColor: '#F1F5F9',
  },
  statusText: {
    fontSize: 11,
    fontWeight: '700',
  },
  statusConfirmedText: {
    color: '#2D6A4F',
  },
  statusOtherText: {
    color: '#64748B',
  },
  serviceName: {
    fontSize: 16,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 4,
  },
  dateTime: {
    fontSize: 13,
    color: Colors.textMuted,
    marginBottom: 8,
  },
  tokenContainer: {
    backgroundColor: Colors.accentWarm,
    padding: 10,
    borderRadius: 8,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginVertical: 8,
  },
  tokenLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.textCharcoal,
  },
  tokenNumber: {
    fontSize: 16,
    fontWeight: '800',
    color: Colors.primaryGoldDark,
  },
  notes: {
    fontSize: 12,
    color: Colors.textLight,
    fontStyle: 'italic',
    marginTop: 4,
  },
  cancelBtn: {
    marginTop: 12,
    paddingVertical: 8,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    borderRadius: 6,
    alignItems: 'center',
  },
  cancelBtnText: {
    fontSize: 13,
    color: '#9B2226',
    fontWeight: '600',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 30,
  },
  emptyTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 6,
  },
  emptySubtitle: {
    fontSize: 13,
    color: Colors.textMuted,
    textAlign: 'center',
    marginBottom: 20,
  },
  bookNowBtn: {
    backgroundColor: Colors.primaryGoldDark,
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 8,
  },
  bookNowText: {
    color: '#FFFFFF',
    fontWeight: '700',
    fontSize: 14,
  },
});
