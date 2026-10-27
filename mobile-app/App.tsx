import React, { useState, useEffect } from 'react';
import { ActivityIndicator, View, StyleSheet } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { Colors } from './src/theme/colors';
import { AuthStorage } from './src/services/authStorage';
import { LoginScreen } from './src/screens/LoginScreen';
import { RegisterScreen } from './src/screens/RegisterScreen';
import { HomeScreen } from './src/screens/HomeScreen';
import { BookAppointmentScreen } from './src/screens/BookAppointmentScreen';
import { MyAppointmentsScreen } from './src/screens/MyAppointmentsScreen';
import { PrescriptionsScreen } from './src/screens/PrescriptionsScreen';
import { MedicalRecordsScreen } from './src/screens/MedicalRecordsScreen';

type Screen = 'Home' | 'BookAppointment' | 'MyAppointments' | 'Prescriptions' | 'MedicalRecords';

export default function App() {
  const [currentUser, setCurrentUser] = useState<any | null>(null);
  const [isRegistering, setIsRegistering] = useState(false);
  const [currentScreen, setCurrentScreen] = useState<Screen>('Home');
  const [checkingSession, setCheckingSession] = useState(true);

  useEffect(() => {
    checkSession();
  }, []);

  const checkSession = async () => {
    try {
      const user = await AuthStorage.getCurrentUser();
      if (user) {
        setCurrentUser(user);
      }
    } catch (e) {
      console.warn('Session check failed', e);
    } finally {
      setCheckingSession(false);
    }
  };

  const handleLogout = async () => {
    await AuthStorage.clearSession();
    setCurrentUser(null);
    setCurrentScreen('Home');
  };

  if (checkingSession) {
    return (
      <View style={styles.loadingContainer}>
        <StatusBar style="dark" />
        <ActivityIndicator size="large" color={Colors.primaryGoldDark} />
      </View>
    );
  }

  if (!currentUser) {
    return (
      <>
        <StatusBar style="dark" />
        {isRegistering ? (
          <RegisterScreen
            onRegisterSuccess={(user) => {
              setCurrentUser(user);
              setIsRegistering(false);
            }}
            onNavigateToLogin={() => setIsRegistering(false)}
          />
        ) : (
          <LoginScreen
            onLoginSuccess={(user) => setCurrentUser(user)}
            onNavigateToRegister={() => setIsRegistering(true)}
          />
        )}
      </>
    );
  }

  return (
    <>
      <StatusBar style="dark" />
      {currentScreen === 'Home' && (
        <HomeScreen
          user={currentUser}
          onNavigate={(screen) => setCurrentScreen(screen as Screen)}
          onLogout={handleLogout}
        />
      )}
      {currentScreen === 'BookAppointment' && (
        <BookAppointmentScreen
          onBack={() => setCurrentScreen('Home')}
          onSuccess={() => setCurrentScreen('MyAppointments')}
        />
      )}
      {currentScreen === 'MyAppointments' && (
        <MyAppointmentsScreen
          onBack={() => setCurrentScreen('Home')}
          onBookNew={() => setCurrentScreen('BookAppointment')}
        />
      )}
      {currentScreen === 'Prescriptions' && (
        <PrescriptionsScreen onBack={() => setCurrentScreen('Home')} />
      )}
      {currentScreen === 'MedicalRecords' && (
        <MedicalRecordsScreen onBack={() => setCurrentScreen('Home')} />
      )}
    </>
  );
}

const styles = StyleSheet.create({
  loadingContainer: {
    flex: 1,
    backgroundColor: Colors.background,
    justifyContent: 'center',
    alignItems: 'center',
  },
});
