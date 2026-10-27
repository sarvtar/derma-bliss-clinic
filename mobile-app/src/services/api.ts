import axios from 'axios';
import { Platform } from 'react-native';
import { AuthStorage } from './authStorage';

// Dynamically resolve backend host from Expo's Metro hostUri,
// enabling physical phones scanning the QR code, emulators, and local browsers to connect
const getBaseUrl = (): string => {
  if (process.env.EXPO_PUBLIC_API_URL) {
    return process.env.EXPO_PUBLIC_API_URL;
  }
  try {
    const Constants = require('expo-constants').default || require('expo-constants');
    const hostUri = Constants?.expoConfig?.hostUri || Constants?.manifest?.hostUri;
    if (hostUri) {
      const host = hostUri.split(':')[0];
      return `http://${host}:8080/api/v1`;
    }
  } catch (e) {
    // Fallback if expo-constants is not loaded
  }
  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080/api/v1';
  }
  return 'http://localhost:8080/api/v1';
};

export const apiClient = axios.create({
  baseURL: getBaseUrl(),
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
});

apiClient.interceptors.request.use(
  async (config) => {
    const token = await AuthStorage.getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      await AuthStorage.clearSession();
    }
    return Promise.reject(error);
  }
);
