const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://api.ai-hr-dev.student.k8s.aet.cit.tum.de';

export const apiConfig = {
  baseURL: API_BASE_URL,
  getFullURL: (path: string) => `${API_BASE_URL}${path}`,
};

export default apiConfig;