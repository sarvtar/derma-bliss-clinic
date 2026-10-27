import React, { useState, useEffect, useMemo } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  TextInput,
  ActivityIndicator,
  Alert,
  SafeAreaView,
} from 'react-native';
import { Colors } from '../theme/colors';
import { apiClient } from '../services/api';

interface Props {
  onBack: () => void;
  onSuccess: () => void;
}

const DEFAULT_SERVICES = [
  {
    id: 1,
    serviceCode: 'REG-PATIENT',
    name: 'Regular Patient Consultation',
    category: 'REGULAR_PATIENT',
    estimatedDurationMin: 30,
    description: 'Routine checkup, regular follow-up assessment, medication review, and general clinical maintenance.',
  },
  {
    id: 2,
    serviceCode: 'SKN-HYDRA',
    name: 'HydraFacial MD & Glow Infusion',
    category: 'SKIN_CARE',
    estimatedDurationMin: 45,
    description: 'Medical grade 4-step deep hydradermabrasion with hyaluronic antioxidant serum.',
  },
  {
    id: 3,
    serviceCode: 'HAR-PRP',
    name: 'PRP Hair Restoration Therapy',
    category: 'HAIR_CARE',
    estimatedDurationMin: 60,
    description: 'Autologous platelet-rich plasma scalp microinjections with peptide growth factors.',
  },
  {
    id: 4,
    serviceCode: 'LSR-CARBON',
    name: 'Carbon Laser Peel (Hollywood Peel)',
    category: 'LASER_TREATMENTS',
    estimatedDurationMin: 45,
    description: 'Q-Switched Nd:YAG laser carbon nano-particle pore purification and collagen rejuvenation.',
  },
];

const formatDateString = (d: Date): string => {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const BookAppointmentScreen: React.FC<Props> = ({ onBack, onSuccess }) => {
  const today = useMemo(() => new Date(), []);

  // Compute only the current day and next two days (strictly allowed booking horizon)
  const allowedDates = useMemo(() => {
    return [0, 1, 2].map((offset) => {
      const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() + offset);
      const dateStr = formatDateString(d);
      const dayName = offset === 0 ? 'Today' : offset === 1 ? 'Tomorrow' : d.toLocaleDateString('en-US', { weekday: 'short' });
      const dayNum = d.getDate();
      const monthName = d.toLocaleDateString('en-US', { month: 'short' });
      const dayOfWeek = d.toLocaleDateString('en-US', { weekday: 'short' });
      return { offset, dateStr, dayName, dayNum, monthName, dayOfWeek };
    });
  }, [today]);

  const allowedDateStrings = useMemo(() => allowedDates.map((a) => a.dateStr), [allowedDates]);

  const [services, setServices] = useState<any[]>(DEFAULT_SERVICES);
  const [selectedServiceId, setSelectedServiceId] = useState<number | null>(1);
  const [date, setDate] = useState<string>(allowedDateStrings[0]);
  const [showCalendar, setShowCalendar] = useState<boolean>(true);
  const [slots, setSlots] = useState<any[]>([]);
  const [selectedTime, setSelectedTime] = useState<string | null>(null);
  const [patientNotes, setPatientNotes] = useState('');
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const isCurrentDateAllowed = allowedDateStrings.includes(date.trim());

  useEffect(() => {
    loadServices();
  }, []);

  useEffect(() => {
    if (isCurrentDateAllowed) {
      loadSlots(date.trim());
    } else {
      setSlots([]);
      setSelectedTime(null);
    }
  }, [date, isCurrentDateAllowed]);

  const loadServices = async () => {
    try {
      const resp = await apiClient.get('/clinic/services');
      let list = resp.data.data || [];
      const hasRegular = list.some(
        (s: any) => s.category === 'REGULAR_PATIENT' || s.serviceCode === 'REG-PATIENT' || s.name.toLowerCase().includes('regular')
      );
      if (!hasRegular) {
        list = [DEFAULT_SERVICES[0], ...list];
      }
      setServices(list.length > 0 ? list : DEFAULT_SERVICES);
      if (list.length > 0 && selectedServiceId === null) {
        setSelectedServiceId(list[0].id);
      }
    } catch (err) {
      setServices(DEFAULT_SERVICES);
      setSelectedServiceId(DEFAULT_SERVICES[0].id);
    }
  };

  const loadSlots = async (selectedDate: string) => {
    setLoadingSlots(true);
    setSelectedTime(null);
    try {
      const resp = await apiClient.get(`/appointments/available-slots?date=${selectedDate}`);
      const list = (resp.data.data?.slots || []).filter((s: any) => s.available);
      setSlots(list);
    } catch (err) {
      setSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  };

  const handleSelectDate = (newDateStr: string) => {
    if (!allowedDateStrings.includes(newDateStr)) {
      Alert.alert(
        'Date Not Available',
        `Bookings are only available for Today, Tomorrow, and Day After Tomorrow (${allowedDateStrings[0]} to ${allowedDateStrings[2]}).`
      );
      return;
    }
    setDate(newDateStr);
  };

  const handleBook = async () => {
    if (!isCurrentDateAllowed) {
      Alert.alert(
        'Invalid Appointment Date',
        `Appointments can only be booked for today or the next 2 days:\n\n• Today: ${allowedDateStrings[0]}\n• Tomorrow: ${allowedDateStrings[1]}\n• Day After: ${allowedDateStrings[2]}\n\nPlease select an allowed date.`
      );
      return;
    }

    if (!selectedServiceId || !selectedTime) {
      Alert.alert('Incomplete Reservation', 'Please select a service and an available time slot.');
      return;
    }

    setSubmitting(true);
    try {
      const resp = await apiClient.post('/appointments', {
        serviceId: selectedServiceId,
        appointmentDate: date.trim(),
        startTime: selectedTime,
        patientNotes,
      });

      const appt = resp.data.data;
      Alert.alert(
        'Appointment Confirmed!',
        `Your consultation has been reserved for ${date} at ${selectedTime.substring(0, 5)}.\n\nToken Number: #${appt.queueTokenNumber || '01'}`,
        [{ text: 'View Appointments', onPress: onSuccess }]
      );
    } catch (err: any) {
      const msg = err.response?.data?.message || 'Failed to book slot. It may have just been reserved.';
      Alert.alert('Booking Error', msg);
    } finally {
      setSubmitting(false);
    }
  };

  // Calendar generation for current month
  const calendarData = useMemo(() => {
    const currentMonthStart = new Date(today.getFullYear(), today.getMonth(), 1);
    const startDayOfWeek = currentMonthStart.getDay();
    const daysInMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0).getDate();
    const monthTitle = today.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });

    const cells: { key: string; dayNum: number | null; dateStr: string; isAllowed: boolean; isSelected: boolean; isToday: boolean }[] = [];
    for (let i = 0; i < startDayOfWeek; i++) {
      cells.push({ key: `empty-${i}`, dayNum: null, dateStr: '', isAllowed: false, isSelected: false, isToday: false });
    }
    for (let day = 1; day <= daysInMonth; day++) {
      const d = new Date(today.getFullYear(), today.getMonth(), day);
      const dateStr = formatDateString(d);
      cells.push({
        key: `day-${day}`,
        dayNum: day,
        dateStr,
        isAllowed: allowedDateStrings.includes(dateStr),
        isSelected: date === dateStr,
        isToday: dateStr === allowedDateStrings[0],
      });
    }
    return { monthTitle, cells };
  }, [today, allowedDateStrings, date]);

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.topBar}>
        <TouchableOpacity onPress={onBack} style={styles.backButton}>
          <Text style={styles.backText}>&larr; Back</Text>
        </TouchableOpacity>
        <Text style={styles.screenTitle}>Book Consultation</Text>
        <View style={{ width: 50 }} />
      </View>

      <ScrollView contentContainerStyle={styles.scroll}>
        {/* 1. Services Selection */}
        <Text style={styles.label}>1. Select Consultation Service</Text>
        <View style={styles.servicesGrid}>
          {services.map((s) => (
            <TouchableOpacity
              key={s.id || s.serviceCode}
              style={[
                styles.serviceCard,
                selectedServiceId === s.id && styles.serviceCardSelected,
              ]}
              onPress={() => setSelectedServiceId(s.id)}
            >
              <View style={styles.serviceHeaderRow}>
                <Text
                  style={[
                    styles.serviceName,
                    selectedServiceId === s.id && styles.serviceNameSelected,
                  ]}
                >
                  {s.category === 'REGULAR_PATIENT' ? '🩺 ' : s.category === 'SKIN_CARE' ? '✨ ' : s.category === 'HAIR_CARE' ? '🌿 ' : '⚡ '}
                  {s.name}
                </Text>
                {s.category === 'REGULAR_PATIENT' && (
                  <View style={styles.regularBadge}>
                    <Text style={styles.regularBadgeText}>Popular</Text>
                  </View>
                )}
              </View>
              <Text style={styles.serviceMeta}>
                {s.estimatedDurationMin} mins &bull; {s.category === 'REGULAR_PATIENT' ? 'Regular Checkup' : s.category}
              </Text>
              {s.description && (
                <Text style={styles.serviceDescription} numberOfLines={2}>
                  {s.description}
                </Text>
              )}
            </TouchableOpacity>
          ))}
        </View>

        {/* 2. Date Selection with Restriction (Current day + Next 2 days) */}
        <View style={styles.sectionHeaderRow}>
          <Text style={styles.label}>2. Select Appointment Date</Text>
          <TouchableOpacity onPress={() => setShowCalendar(!showCalendar)} style={styles.calendarToggleBtn}>
            <Text style={styles.calendarToggleText}>
              {showCalendar ? '📅 Hide Calendar' : '📅 Show Calendar'}
            </Text>
          </TouchableOpacity>
        </View>

        <Text style={styles.helperText}>
          Available only for today and the next two consecutive days.
        </Text>

        {/* Quick 3-Day Select Cards */}
        <View style={styles.quickDateRow}>
          {allowedDates.map((item) => {
            const isSelected = date === item.dateStr;
            return (
              <TouchableOpacity
                key={item.dateStr}
                style={[
                  styles.quickDateCard,
                  isSelected && styles.quickDateCardSelected,
                ]}
                onPress={() => handleSelectDate(item.dateStr)}
              >
                <Text style={[styles.quickDayName, isSelected && styles.quickDayNameSelected]}>
                  {item.dayName}
                </Text>
                <Text style={[styles.quickDayNum, isSelected && styles.quickDayNumSelected]}>
                  {item.dayNum}
                </Text>
                <Text style={[styles.quickMonth, isSelected && styles.quickMonthSelected]}>
                  {item.monthName} ({item.dayOfWeek})
                </Text>
              </TouchableOpacity>
            );
          })}
        </View>

        {/* Interactive Calendar Component */}
        {showCalendar && (
          <View style={styles.calendarContainer}>
            <View style={styles.calendarHeader}>
              <Text style={styles.calendarMonthText}>{calendarData.monthTitle}</Text>
              <Text style={styles.calendarNoticeText}>Green & Gold = Allowed Dates</Text>
            </View>

            {/* Days of Week Header */}
            <View style={styles.calendarWeekRow}>
              {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map((d) => (
                <Text key={d} style={styles.calendarWeekHeader}>
                  {d}
                </Text>
              ))}
            </View>

            {/* Days Grid */}
            <View style={styles.calendarGrid}>
              {calendarData.cells.map((cell) => {
                if (cell.dayNum === null) {
                  return <View key={cell.key} style={styles.calendarCellEmpty} />;
                }

                return (
                  <TouchableOpacity
                    key={cell.key}
                    disabled={!cell.isAllowed}
                    onPress={() => handleSelectDate(cell.dateStr)}
                    style={[
                      styles.calendarCell,
                      cell.isAllowed && styles.calendarCellAllowed,
                      cell.isSelected && styles.calendarCellSelected,
                      !cell.isAllowed && styles.calendarCellDisabled,
                    ]}
                  >
                    <Text
                      style={[
                        styles.calendarCellText,
                        cell.isAllowed && styles.calendarCellTextAllowed,
                        cell.isSelected && styles.calendarCellTextSelected,
                        !cell.isAllowed && styles.calendarCellTextDisabled,
                      ]}
                    >
                      {cell.dayNum}
                    </Text>
                    {cell.isToday && (
                      <View style={[styles.todayDot, cell.isSelected && styles.todayDotSelected]} />
                    )}
                  </TouchableOpacity>
                );
              })}
            </View>
          </View>
        )}

        {/* Manual Date Input Option */}
        <View style={styles.manualEntryContainer}>
          <Text style={styles.manualEntryLabel}>Or Enter Date Manually (YYYY-MM-DD):</Text>
          <TextInput
            style={[
              styles.input,
              !isCurrentDateAllowed && date.length >= 10 && styles.inputError,
              isCurrentDateAllowed && styles.inputSuccess,
            ]}
            value={date}
            onChangeText={setDate}
            placeholder="YYYY-MM-DD"
            placeholderTextColor={Colors.textLight}
            maxLength={10}
            autoCapitalize="none"
          />
          {isCurrentDateAllowed ? (
            <Text style={styles.validDateNotice}>
              ✓ Valid booking date selected ({date})
            </Text>
          ) : (
            <Text style={styles.errorDateNotice}>
              ⚠️ Only allowed dates: {allowedDateStrings.join(', ')} (Today & next 2 days)
            </Text>
          )}
        </View>

        {/* 3. Doctor Slots */}
        <Text style={styles.label}>3. Available Doctor Slots</Text>
        {loadingSlots ? (
          <ActivityIndicator color={Colors.primaryGoldDark} style={{ marginVertical: 20 }} />
        ) : !isCurrentDateAllowed ? (
          <Text style={styles.emptySlots}>Please select a valid date within the allowed 3-day window.</Text>
        ) : slots.length === 0 ? (
          <Text style={styles.emptySlots}>No available consultation slots for this date.</Text>
        ) : (
          <View style={styles.slotsGrid}>
            {slots.map((slot) => (
              <TouchableOpacity
                key={slot.startTime}
                style={[
                  styles.slotBadge,
                  selectedTime === slot.startTime && styles.slotBadgeSelected,
                ]}
                onPress={() => setSelectedTime(slot.startTime)}
              >
                <Text
                  style={[
                    styles.slotText,
                    selectedTime === slot.startTime && styles.slotTextSelected,
                  ]}
                >
                  {slot.startTime.substring(0, 5)}
                </Text>
              </TouchableOpacity>
            ))}
          </View>
        )}

        {/* 4. Consultation Reason */}
        <Text style={styles.label}>4. Reason for Consultation / Symptoms</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          placeholder="e.g. Regular follow-up checkup, acne outbreak, hair fall diagnosis"
          placeholderTextColor={Colors.textLight}
          multiline
          numberOfLines={3}
          value={patientNotes}
          onChangeText={setPatientNotes}
        />

        {/* Submit Reservation */}
        <TouchableOpacity
          style={[styles.bookBtn, (submitting || !isCurrentDateAllowed) && styles.bookBtnDisabled]}
          onPress={handleBook}
          disabled={submitting || !isCurrentDateAllowed}
        >
          {submitting ? (
            <ActivityIndicator color="#FFFFFF" />
          ) : (
            <Text style={styles.bookBtnText}>Confirm Consultation Reservation</Text>
          )}
        </TouchableOpacity>
      </ScrollView>
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
  scroll: {
    padding: 20,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 18,
    marginBottom: 4,
  },
  label: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginTop: 14,
    marginBottom: 8,
  },
  helperText: {
    fontSize: 12,
    color: Colors.textMuted,
    marginBottom: 10,
  },
  calendarToggleBtn: {
    paddingVertical: 4,
    paddingHorizontal: 10,
    backgroundColor: Colors.subtle,
    borderRadius: 6,
  },
  calendarToggleText: {
    fontSize: 12,
    color: Colors.primaryGoldDark,
    fontWeight: '600',
  },
  servicesGrid: {
    flexDirection: 'column',
    gap: 8,
  },
  serviceCard: {
    backgroundColor: Colors.surface,
    padding: 14,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
  },
  serviceCardSelected: {
    borderColor: Colors.primaryGoldDark,
    backgroundColor: Colors.accentWarm,
  },
  serviceHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  serviceName: {
    fontSize: 15,
    fontWeight: '600',
    color: Colors.textCharcoal,
    flex: 1,
  },
  serviceNameSelected: {
    color: Colors.primaryGoldDark,
    fontWeight: '700',
  },
  regularBadge: {
    backgroundColor: Colors.primaryGold,
    paddingHorizontal: 8,
    paddingVertical: 2,
    borderRadius: 12,
  },
  regularBadgeText: {
    fontSize: 10,
    color: '#FFFFFF',
    fontWeight: '700',
  },
  serviceMeta: {
    fontSize: 12,
    color: Colors.textMuted,
    marginTop: 3,
  },
  serviceDescription: {
    fontSize: 11,
    color: Colors.textLight,
    marginTop: 4,
    lineHeight: 16,
  },
  quickDateRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 12,
  },
  quickDateCard: {
    flex: 1,
    backgroundColor: Colors.surface,
    borderRadius: 10,
    paddingVertical: 12,
    paddingHorizontal: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
  },
  quickDateCardSelected: {
    borderColor: Colors.primaryGoldDark,
    backgroundColor: Colors.primaryGoldDark,
    shadowColor: Colors.primaryGoldDark,
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.2,
    shadowRadius: 4,
    elevation: 3,
  },
  quickDayName: {
    fontSize: 11,
    fontWeight: '700',
    color: Colors.textMuted,
    textTransform: 'uppercase',
  },
  quickDayNameSelected: {
    color: '#FFFFFF',
  },
  quickDayNum: {
    fontSize: 22,
    fontWeight: '800',
    color: Colors.textCharcoal,
    marginVertical: 2,
  },
  quickDayNumSelected: {
    color: '#FFFFFF',
  },
  quickMonth: {
    fontSize: 11,
    color: Colors.textLight,
  },
  quickMonthSelected: {
    color: '#F4ECE1',
  },
  calendarContainer: {
    backgroundColor: Colors.surface,
    borderRadius: 12,
    padding: 14,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    marginBottom: 14,
  },
  calendarHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
    paddingBottom: 8,
    borderBottomWidth: 1,
    borderColor: Colors.subtle,
  },
  calendarMonthText: {
    fontSize: 14,
    fontWeight: '700',
    color: Colors.textCharcoal,
  },
  calendarNoticeText: {
    fontSize: 10,
    color: Colors.primaryGoldDark,
    fontWeight: '600',
  },
  calendarWeekRow: {
    flexDirection: 'row',
    marginBottom: 6,
  },
  calendarWeekHeader: {
    flex: 1,
    textAlign: 'center',
    fontSize: 11,
    fontWeight: '700',
    color: Colors.textLight,
  },
  calendarGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  calendarCellEmpty: {
    width: '14.28%',
    height: 38,
  },
  calendarCell: {
    width: '14.28%',
    height: 38,
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 8,
    marginVertical: 1,
  },
  calendarCellAllowed: {
    backgroundColor: Colors.accentWarm,
    borderWidth: 1,
    borderColor: Colors.primaryGoldLight,
  },
  calendarCellSelected: {
    backgroundColor: Colors.primaryGoldDark,
    borderColor: Colors.primaryGoldDark,
  },
  calendarCellDisabled: {
    opacity: 0.28,
  },
  calendarCellText: {
    fontSize: 13,
    color: Colors.textCharcoal,
  },
  calendarCellTextAllowed: {
    color: Colors.primaryGoldDark,
    fontWeight: '700',
  },
  calendarCellTextSelected: {
    color: '#FFFFFF',
    fontWeight: '800',
  },
  calendarCellTextDisabled: {
    color: Colors.textLight,
  },
  todayDot: {
    width: 4,
    height: 4,
    borderRadius: 2,
    backgroundColor: Colors.primaryGoldDark,
    marginTop: 1,
  },
  todayDotSelected: {
    backgroundColor: '#FFFFFF',
  },
  manualEntryContainer: {
    marginBottom: 8,
  },
  manualEntryLabel: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.textMuted,
    marginBottom: 6,
  },
  input: {
    height: 46,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    borderRadius: 8,
    paddingHorizontal: 14,
    fontSize: 14,
    color: Colors.textCharcoal,
    backgroundColor: Colors.surface,
  },
  inputError: {
    borderColor: Colors.statusCancelled,
    backgroundColor: Colors.statusCancelledBg,
  },
  inputSuccess: {
    borderColor: Colors.statusConfirmed,
  },
  validDateNotice: {
    fontSize: 11,
    color: Colors.statusConfirmed,
    marginTop: 4,
    fontWeight: '600',
  },
  errorDateNotice: {
    fontSize: 11,
    color: Colors.statusCancelled,
    marginTop: 4,
    fontWeight: '600',
  },
  textArea: {
    height: 80,
    paddingTop: 12,
    textAlignVertical: 'top',
  },
  slotsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  slotBadge: {
    paddingVertical: 10,
    paddingHorizontal: 14,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    backgroundColor: Colors.surface,
  },
  slotBadgeSelected: {
    borderColor: Colors.primaryGoldDark,
    backgroundColor: Colors.primaryGoldDark,
  },
  slotText: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.textCharcoal,
  },
  slotTextSelected: {
    color: '#FFFFFF',
  },
  emptySlots: {
    fontSize: 13,
    color: Colors.textLight,
    paddingVertical: 10,
  },
  bookBtn: {
    height: 52,
    backgroundColor: Colors.primaryGoldDark,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 24,
    marginBottom: 40,
  },
  bookBtnDisabled: {
    opacity: 0.5,
  },
  bookBtnText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
});
