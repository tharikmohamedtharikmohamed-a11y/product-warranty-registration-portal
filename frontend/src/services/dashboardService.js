import api from './api';

/**
 * Customer Dashboard Frontend Service.
 * Communicates with backend customer dashboard APIs using the shared Axios instance.
 * Phase 12 — Dashboard & Notifications
 */
const dashboardService = {
  /**
   * Retrieves aggregated customer dashboard telemetry including metrics,
   * expiring warranties, recent claims, and real recent activity.
   *
   * @returns {Promise<Object>} Dashboard telemetry payload
   */
  getDashboard: async () => {
    const response = await api.get('/api/dashboard');
    return response.data;
  }
};

export default dashboardService;
