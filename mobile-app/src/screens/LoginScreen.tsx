import React, { useState } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
} from 'react-native';
import { Colors } from '../theme/colors';
import { apiClient } from '../services/api';
import { AuthStorage } from '../services/authStorage';

interface Props {
  onLoginSuccess: (user: any) => void;
  onNavigateToRegister: () => void;
}

export const LoginScreen: React.FC<Props> = ({ onLoginSuccess, onNavigateToRegister }) => {
  const [phoneNumber, setPhoneNumber] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (!phoneNumber || !password) {
      Alert.alert('Required Fields', 'Please enter your phone number and password.');
      return;
    }

    setLoading(true);
    try {
      const response = await apiClient.post('/auth/login', { phoneNumber, password });
      const { accessToken, refreshToken, user } = response.data.data;
      await AuthStorage.saveSession(accessToken, refreshToken, user);
      onLoginSuccess(user);
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Invalid credentials. Please verify and try again.';
      Alert.alert('Login Failed', msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      style={styles.container}
    >
      <ScrollView contentContainerStyle={styles.scroll}>
        <View style={styles.header}>
          <Text style={styles.tagline}>DERMA BLISS &bull; SKIN | HAIR | LASER</Text>
          <Text style={styles.title}>Derma Bliss Clinic</Text>
          <Text style={styles.doctor}>Dr. Ankita Santosh Koli</Text>
          <Text style={styles.qualifications}>BHMS, PGDMS, CCH, CGO, CSD &bull; Reg. 73892</Text>
          <Text style={styles.specialization}>Cosmetologist &amp; Trichologist</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Patient Sign In</Text>

          <Text style={styles.label}>Mobile Phone Number</Text>
          <TextInput
            style={styles.input}
            placeholder="+91 98765 43210"
            placeholderTextColor={Colors.textLight}
            keyboardType="phone-pad"
            value={phoneNumber}
            onChangeText={setPhoneNumber}
            autoCapitalize="none"
          />

          <Text style={styles.label}>Password</Text>
          <TextInput
            style={styles.input}
            placeholder="••••••••••••"
            placeholderTextColor={Colors.textLight}
            secureTextEntry
            value={password}
            onChangeText={setPassword}
          />

          <TouchableOpacity
            style={[styles.button, loading && styles.buttonDisabled]}
            onPress={handleLogin}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <Text style={styles.buttonText}>Sign In to Patient Portal</Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.linkButton}
            onPress={onNavigateToRegister}
          >
            <Text style={styles.linkText}>
              New Patient? <Text style={styles.linkHighlight}>Register Account</Text>
            </Text>
          </TouchableOpacity>
        </View>

        <View style={styles.footer}>
          <Text style={styles.footerText}>
            Opp. Sainagri, Chendani Koliwada, Thane East &bull; +91 789 789 2177
          </Text>
        </View>
      </ScrollView>
    </KeyboardAvoidingView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  scroll: {
    padding: 24,
    justifyContent: 'center',
    minHeight: '100%',
  },
  header: {
    alignItems: 'center',
    marginBottom: 28,
  },
  tagline: {
    fontSize: 11,
    letterSpacing: 2,
    color: Colors.primaryGoldDark,
    fontWeight: '700',
    marginBottom: 6,
  },
  title: {
    fontSize: 26,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 4,
  },
  doctor: {
    fontSize: 16,
    fontWeight: '600',
    color: Colors.textCharcoal,
  },
  qualifications: {
    fontSize: 12,
    color: Colors.textMuted,
    marginTop: 2,
  },
  specialization: {
    fontSize: 12,
    fontWeight: '600',
    color: Colors.primaryGoldDark,
    marginTop: 2,
  },
  card: {
    backgroundColor: Colors.surface,
    padding: 24,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    shadowColor: Colors.textCharcoal,
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.05,
    shadowRadius: 10,
    elevation: 2,
  },
  cardTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: Colors.textCharcoal,
    marginBottom: 20,
  },
  label: {
    fontSize: 13,
    fontWeight: '600',
    color: Colors.textCharcoal,
    marginBottom: 6,
  },
  input: {
    height: 48,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    borderRadius: 8,
    paddingHorizontal: 14,
    marginBottom: 16,
    fontSize: 15,
    color: Colors.textCharcoal,
    backgroundColor: Colors.background,
  },
  button: {
    height: 50,
    backgroundColor: Colors.primaryGoldDark,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 8,
  },
  buttonDisabled: {
    opacity: 0.7,
  },
  buttonText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '600',
  },
  linkButton: {
    marginTop: 18,
    alignItems: 'center',
  },
  linkText: {
    fontSize: 14,
    color: Colors.textMuted,
  },
  linkHighlight: {
    color: Colors.primaryGoldDark,
    fontWeight: '700',
  },
  footer: {
    marginTop: 32,
    alignItems: 'center',
  },
  footerText: {
    fontSize: 11,
    color: Colors.textLight,
    textAlign: 'center',
  },
});
