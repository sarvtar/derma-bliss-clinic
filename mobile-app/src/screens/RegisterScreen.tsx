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
  onRegisterSuccess: (user: any) => void;
  onNavigateToLogin: () => void;
}

export const RegisterScreen: React.FC<Props> = ({ onRegisterSuccess, onNavigateToLogin }) => {
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [gender, setGender] = useState('FEMALE');
  const [loading, setLoading] = useState(false);

  const handleRegister = async () => {
    if (!fullName || !phoneNumber || !password) {
      Alert.alert('Required Fields', 'Please complete all required fields.');
      return;
    }

    setLoading(true);
    try {
      const response = await apiClient.post('/auth/register', {
        fullName,
        phoneNumber,
        email: email || undefined,
        password,
        gender,
      });

      const { accessToken, refreshToken, user } = response.data.data;
      await AuthStorage.saveSession(accessToken, refreshToken, user);
      Alert.alert('Welcome!', 'Your patient account has been created.');
      onRegisterSuccess(user);
    } catch (error: any) {
      const msg = error.response?.data?.message || 'Registration failed. Please try again.';
      Alert.alert('Registration Failed', msg);
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
          <Text style={styles.title}>Patient Registration</Text>
          <Text style={styles.subtitle}>Create your confidential clinic account</Text>
        </View>

        <View style={styles.card}>
          <Text style={styles.label}>Full Legal Name *</Text>
          <TextInput
            style={styles.input}
            placeholder="e.g. Pooja Sharma"
            placeholderTextColor={Colors.textLight}
            value={fullName}
            onChangeText={setFullName}
          />

          <Text style={styles.label}>Mobile Phone Number *</Text>
          <TextInput
            style={styles.input}
            placeholder="+91 98765 43210"
            placeholderTextColor={Colors.textLight}
            keyboardType="phone-pad"
            value={phoneNumber}
            onChangeText={setPhoneNumber}
          />

          <Text style={styles.label}>Email Address (Optional)</Text>
          <TextInput
            style={styles.input}
            placeholder="pooja@example.com"
            placeholderTextColor={Colors.textLight}
            keyboardType="email-address"
            autoCapitalize="none"
            value={email}
            onChangeText={setEmail}
          />

          <Text style={styles.label}>Account Password *</Text>
          <TextInput
            style={styles.input}
            placeholder="Min. 8 characters"
            placeholderTextColor={Colors.textLight}
            secureTextEntry
            value={password}
            onChangeText={setPassword}
          />

          <Text style={styles.label}>Gender</Text>
          <View style={styles.genderRow}>
            {['FEMALE', 'MALE', 'OTHER'].map((g) => (
              <TouchableOpacity
                key={g}
                style={[
                  styles.genderOption,
                  gender === g && styles.genderOptionSelected,
                ]}
                onPress={() => setGender(g)}
              >
                <Text
                  style={[
                    styles.genderText,
                    gender === g && styles.genderTextSelected,
                  ]}
                >
                  {g.charAt(0) + g.slice(1).toLowerCase()}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <TouchableOpacity
            style={[styles.button, loading && styles.buttonDisabled]}
            onPress={handleRegister}
            disabled={loading}
          >
            {loading ? (
              <ActivityIndicator color="#FFFFFF" />
            ) : (
              <Text style={styles.buttonText}>Complete Registration</Text>
            )}
          </TouchableOpacity>

          <TouchableOpacity
            style={styles.linkButton}
            onPress={onNavigateToLogin}
          >
            <Text style={styles.linkText}>
              Already have an account? <Text style={styles.linkHighlight}>Sign In</Text>
            </Text>
          </TouchableOpacity>
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
    marginBottom: 20,
  },
  tagline: {
    fontSize: 11,
    letterSpacing: 2,
    color: Colors.primaryGoldDark,
    fontWeight: '700',
    marginBottom: 6,
  },
  title: {
    fontSize: 24,
    fontWeight: '700',
    color: Colors.textCharcoal,
  },
  subtitle: {
    fontSize: 13,
    color: Colors.textMuted,
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
    marginBottom: 14,
    fontSize: 15,
    color: Colors.textCharcoal,
    backgroundColor: Colors.background,
  },
  genderRow: {
    flexDirection: 'row',
    gap: 10,
    marginBottom: 18,
  },
  genderOption: {
    flex: 1,
    height: 42,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: Colors.borderDelicate,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
  },
  genderOptionSelected: {
    borderColor: Colors.primaryGoldDark,
    backgroundColor: Colors.accentWarm,
  },
  genderText: {
    fontSize: 13,
    color: Colors.textMuted,
    fontWeight: '500',
  },
  genderTextSelected: {
    color: Colors.primaryGoldDark,
    fontWeight: '700',
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
});
