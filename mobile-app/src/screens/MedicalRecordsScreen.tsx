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

export const MedicalRecordsScreen: React.FC<Props> = ({ onBack }) => {
  const [records, setRecords] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadRecords();
  }, []);

  const loadRecords = async () => {
    setLoading(true);
    try {
      const resp = await apiClient.get('/medical-records/my');
      setRecords(resp.data.data || []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const renderItem = ({ item }: { item: any }) => (
    <View style={styles.card}>
      <View style={styles.cardHeader}>
        <Text style={styles.recNumber}>{item.recordNumber}</Text>
        <Text style={styles.recDate}>📅 {item.consultationDate}</Text>
      </View>

      <Text style={styles.secTitle}>Diagnosis</Text>
      <Text style={styles.diagnosis}>{item.diagnosis}</Text>

      <Text style={styles.secTitle}>Symptoms Presented</Text>
      <Text style={styles.bodyText}>{item.symptoms}</Text>

      {item.clinicalObservations && (
        <>
          <Text style={styles.secTitle}>Clinical Observations</Text>
          <Text style={styles.bodyText}>{item.clinicalObservations}</Text>
        </>
      )}

      <Text style={styles.secTitle}>Treatment Plan</Text>
      <Text style={styles.treatmentText}>{item.treatmentPlan}</Text>

      {item.followUpRecommendation && (
        <View style={styles.followUpBox}>
          <Text style={styles.followUpText}>
            💡 Follow-up: {item.followUpRecommendation}
          </Text>
        </View>
      )}
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.topBar}>
        <TouchableOpacity onPress={onBack} style={styles.backButton}>
          <Text style={styles.backText}>&larr; Back</Text>
        </TouchableOpacity>
        <Text style={styles.screenTitle}>Clinical Records</Text>
        <View style={{ width: 50 }} />
      </View>

      {loading ? (
        <ActivityIndicator color={Colors.primaryGoldDark} style={{ marginTop: 40 }} />
      ) : records.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyTitle}>No Medical Records Released</Text>
          <Text style={styles.emptySubtitle}>
            Consultation notes released by Dr. Ankita Santosh Koli will be archived securely here.
          </Text>
        </View>
      ) : (
        <FlatList
          data={records}
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
  recNumber: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.primaryGoldDark,
  },
  recDate: {
    fontSize: 12,
    color: Colors.textMuted,
  },
  secTitle: {
    fontSize: 11,
    fontWeight: '700',
    color: Colors.textLight,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginTop: 8,
    marginBottom: 2,
  },
  diagnosis: {
    fontSize: 15,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 4,
  },
  bodyText: {
    fontSize: 13,
    color: Colors.textCharcoal,
    lineHeight: 18,
    marginBottom: 4,
  },
  treatmentText: {
    fontSize: 13,
    color: Colors.primaryGoldDark,
    fontWeight: '600',
    lineHeight: 18,
    marginBottom: 6,
  },
  followUpBox: {
    backgroundColor: Colors.accentWarm,
    padding: 10,
    borderRadius: 6,
    marginTop: 8,
  },
  followUpText: {
    fontSize: 12,
    color: Colors.textCharcoal,
    fontWeight: '500',
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
