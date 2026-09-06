import axios from 'axios';

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '');

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
});

api.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status;
    const body = error.response?.data;
    const message = body?.message || body?.error || error.message || 'Request failed';
    const apiError = new Error(message);
    apiError.status = status;
    apiError.data = body;

    if (status === 401 && !error.config?.suppressUnauthorizedEvent) {
      window.dispatchEvent(new Event('proteinpro:unauthorized'));
    }
    return Promise.reject(apiError);
  }
);

const authConfig = (token, extra = {}) => ({
  ...extra,
  headers: token ? { Authorization: `Bearer ${token}` } : undefined
});

export const authService = {
  login: async (email, password) => {
    const { data } = await api.post('/api/auth/login', { email, password }, {
      suppressUnauthorizedEvent: true
    });
    return data;
  },

  register: async (firstName, lastName, email, password) => {
    const { data } = await api.post('/api/profiles/register', { firstName, lastName, email, password });
    return data;
  },

  resetPassword: async (token, newPassword) => {
    await api.post('/api/auth/password-reset', { newPassword }, authConfig(token));
    return null;
  },

  logout: async (token) => {
    try {
      if (token) {
        await api.post('/api/auth/logout', null, authConfig(token));
      }
    } catch (e) {
      console.warn('Stateless logout call completed locally.', e);
    }
  }
};

export const profileService = {
  getProfile: async (token) => {
    const { data } = await api.get('/api/profiles/me', authConfig(token));
    return data;
  },

  updateProfile: async (token, firstName, lastName) => {
    const { data } = await api.put('/api/profiles/me', { firstName, lastName }, authConfig(token));
    return data;
  }
};

export const proteinService = {
  getProteins: async (params = {}) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        query.append(key, value);
      }
    });
    const { data } = await api.get('/api/proteins', { params: Object.fromEntries(query.entries()) });
    return data;
  }
};

export const bookmarkService = {
  getBookmarks: async (token) => {
    const { data } = await api.get('/api/bookmarks', authConfig(token));
    return data;
  },

  createBookmark: async (token, proteinId, proteinData, comment) => {
    const { data } = await api.post('/api/bookmarks', { proteinId, proteinData, comment }, authConfig(token));
    return data;
  },

  updateComment: async (token, bookmarkId, comment) => {
    const { data } = await api.put(`/api/bookmarks/${encodeURIComponent(bookmarkId)}/comment`, { comment }, authConfig(token));
    return data;
  },

  deleteBookmark: async (token, bookmarkId) => {
    await api.delete(`/api/bookmarks/${encodeURIComponent(bookmarkId)}`, authConfig(token));
    return null;
  }
};
