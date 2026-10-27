import * as SecureStore from 'expo-secure-store';

const ACCESS_TOKEN_KEY = 'dermabliss_access_token';
const REFRESH_TOKEN_KEY = 'dermabliss_refresh_token';
const USER_KEY = 'dermabliss_patient_user';

export const AuthStorage = {
  async saveTokens(accessToken: string, refreshToken: string): Promise<void> {
    await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, accessToken);
    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken);
  },

  async getAccessToken(): Promise<string | null> {
    return await SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
  },

  async getRefreshToken(): Promise<string | null> {
    return await SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
  },

  async saveUser(user: object): Promise<void> {
    await SecureStore.setItemAsync(USER_KEY, JSON.stringify(user));
  },

  async saveSession(accessToken: string, refreshToken: string, user: object): Promise<void> {
    await this.saveTokens(accessToken, refreshToken);
    await this.saveUser(user);
  },

  async getUser(): Promise<any | null> {
    const raw = await SecureStore.getItemAsync(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  },

  async getCurrentUser(): Promise<any | null> {
    return await this.getUser();
  },

  async clearSession(): Promise<void> {
    await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
    await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
    await SecureStore.deleteItemAsync(USER_KEY);
  }
};
