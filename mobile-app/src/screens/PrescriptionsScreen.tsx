import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  SafeAreaView,
} from 'react-native';
import { Colors } from '../theme/colors';
import { apiClient } from '../services/api';

interface Props {
  onBack: () => void;
}

export const PrescriptionsScreen: React.FC<Props> = ({ onBack }) => {
  const [prescriptions, setPrescriptions] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadPrescriptions();
  }, []);

  const loadPrescriptions = async () => {
    setLoading(true);
    try {
      const resp = await apiClient.get('/prescriptions/my');
      setPrescriptions(resp.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const renderItem = ({ item }: { item: any }) => (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.rxNumber}>{item.prescriptionNumber}</Text>
        <Text style={styles.rxDate}>📅 {item.prescriptionDate}</Text>
      </View>

      <Text style={styles.diagnosisLabel}>Diagnosis:</Text>
      <Text style={styles.diagnosisValue}>{item.diagnosisSummary || 'Dermatology Consultation'}</Text>

      {item.medicines && item.medicines.length > 0 && (
        <View style={styles.medsContainer}>
          <Text style={styles.medsTitle}>Prescribed Medicines (Rx):</Text>
          {item.medicines.map((m: any, idx: number) => (
            <View key={idx} style={styles.medItem}>
              <Text style={styles.medName}>
                {idx + 1}. {m.medicineName} ({m.strength})
              </Text>
              <Text style={styles.medInstructions}>
                {m.frequency} &bull; {m.durationDays} days &bull; {m.timingInstructions || 'As directed'}
              </Text>
            </View>
          ))}
        </View>
      )}

      {item.generalInstructions && (
        <View style={styles.instructionsBox}>
          <Text style={styles.instructionsTitle}>Doctor's Advice:</Text>
          <Text style={styles.instructionsText}>{item.generalInstructions}</Text>
        </View>
      )}

      {item.followUpDate && (
        <Text style={styles.followUp}>
          Recommended Follow-up: <Text style={styles.followUpDate}>{item.followUpDate}</Text>
        </Text>
      )}

      <View style={styles.signFooter}>
        <Text style={styles.doctorName}>Dr. Ankita Santosh Koli</Text>
        <Text style={styles.doctorReg}>Reg. 73892 &bull; Digitally Signed</Text>
      </View>
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.topBar}>
        <TouchableOpacity onPress={onBack} style={styles.backButton}>
          <Text style={styles.backText}>&larr; Back</Text>
        </TouchableOpacity>
        <Text style={styles.screenTitle}>My Digital Prescriptions</Text>
        <View style={{ width: 50 }} />
      </View>

      {loading ? (
        <ActivityIndicator color={Colors.primaryGoldDark} style={{ marginTop: 40 }} />
      ) : prescriptions.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>No Prescriptions Issued</Text>
          <Text style={styles.emptySubtitle}>
            Your digital prescriptions will appear here once prescribed by Dr. Ankita Santosh Koli.
          </Text>
        </View>
      ) : (
        <FlatList
          data={prescriptions}
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
  list: {
    padding: 20,
    gap: 16,
  },
  card: {
    backgroundColor: Colors.surface,
    padding: 18,
    borderRadius: 14,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
  },
  cardHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
    borderBottomWidth: 1,
    borderColor: Colors.borderDelicate,
    paddingBottom: 8,
  },
  rxNumber: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.primaryGoldDark,
  },
  rxDate: {
    fontSize: 12,
    color: Colors.textMuted,
  },
  diagnosisLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.textMuted,
  },
  diagnosisValue: {
    fontSize: 15,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 12,
  },
  medsContainer: {
    backgroundColor: Colors.background,
    padding: 12,
    borderRadius: 8,
    marginBottom: 12,
  },
  medsTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 8,
  },
  medItem: {
    marginBottom: 6,
  },
  medName: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.textCharcoal,
  },
  medInstructions: {
    fontSize: 11,
    color: Colors.textMuted,
    marginTop: 2,
  },
  instructionsBox: {
    backgroundColor: Colors.accentWarm,
    padding: 10,
    borderRadius: 8,
    marginBottom: 10,
  },
  instructionsTitle: {
    fontSize: 12,
    fontWeight: '700',
    color: Colors.primaryGoldDark,
    marginBottom: 2,
  },
  instructionsText: {
    fontSize: 12,
    color: Colors.textCharcoal,
    lineHeight: 16,
  },
  followUp: {
    fontSize: 12,
    color: Colors.textMuted,
    marginTop: 4,
  },
  followUpDate: {
    fontWeight: '700',
    color: Colors.primaryGoldDark,
  },
  signFooter: {
    marginTop: 14,
    borderTopWidth: 1,
    borderColor: Colors.borderDelicate,
    paddingTop: 10,
    alignItems: 'flex-end',
  },
  doctorName: {
    fontSize: 13,
    fontWeight: '700',
    color: Colors.textCharcoal,
  },
  doctorReg: {
    fontSize: 10,
    color: Colors.textLight,
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
  },
});
