import api from './api';

/**
 * Notification Frontend Service.
 * Interfaces with backend notification endpoints using the shared Axios client.
 * Phase 12 — Dashboard & Notifications
 */
const notificationService = {
  /**
   * Retrieves all notifications for the authenticated user.
   *
   * @returns {Promise<Array>} List of notification response objects
   */
  getNotifications: async () => {
    const response = await api.get('/api/notifications');
    return response.data;
  },

  /**
   * Retrieves unread notification count for the authenticated user.
   *
   * @returns {Promise<number>} Count of unread notifications
   */
  getUnreadCount: async () => {
    const response = await api.get('/api/notifications/unread-count');
    return response.data.count;
  },

  /**
   * Marks a specific notification as read.
   *
   * @param {string} id Notification UUID
   * @returns {Promise<Object>} Updated notification object
   */
  markAsRead: async (id) => {
    const response = await api.patch(`/api/notifications/${id}/read`);
    return response.data;
  },

  /**
   * Marks all notifications for the authenticated user as read.
   *
   * @returns {Promise<Object>} Success message payload
   */
  markAllAsRead: async () => {
    const response = await api.patch('/api/notifications/read-all');
    return response.data;
  }
};

export default notificationService;
