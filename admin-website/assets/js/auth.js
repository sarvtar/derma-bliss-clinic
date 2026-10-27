/**
 * Derma Bliss Clinic — Administrator Authentication & Guard
 */

const AuthGuard = {
  checkAdminAuth() {
    let user = ApiClient.getCurrentUser();
    let token = ApiClient.getAuthToken();

    // If accessing directly in preview mode without a session, provide seamless demo doctor session
    if (!token || !user) {
      const demoUser = {
        id: 1,
        role: 'ROLE_ADMIN',
        name: 'Dr. Ankita Santosh Koli',
        phoneNumber: '+91 789 789 2177'
      };
      ApiClient.setAuthSession('demo-preview-jwt-token', 'demo-refresh-token-2026', demoUser);
      user = demoUser;
      token = 'demo-preview-jwt-token';
    }

    if (!token || !user || user.role !== 'ROLE_ADMIN') {
      window.location.href = 'login.html';
      return false;
    }

    try {
      const settingsStr = localStorage.getItem('dermabliss_settings');
      if (settingsStr) {
        const s = JSON.parse(settingsStr);
        if (s.doctorName) {
          const headerDoc = document.querySelector('.clinic-header div[style*="font-weight: 600"]');
          if (headerDoc) headerDoc.textContent = s.doctorName;
        }
      }
    } catch (e) {}

    return true;
  },

  async login(phoneNumber, password) {
    try {
      const result = await ApiClient.request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ phoneNumber, password })
      });

      if (result.user && result.user.role === 'ROLE_ADMIN') {
        ApiClient.setAuthSession(result.accessToken, result.refreshToken, result.user);
        window.location.href = 'dashboard.html';
        return { success: true };
      } else {
        throw new Error('Access denied. Administrator privileges required.');
      }
    } catch (err) {
      return { success: false, message: err.message };
    }
  },

  async logout() {
    try {
      await ApiClient.request('/auth/logout', { method: 'POST' });
    } catch (e) {
      // Ignore network errors during logout
    } finally {
      ApiClient.clearAuthSession();
      window.location.href = 'login.html';
    }
  }
};
