
export const getUserRole = (): string | null => {
  const role = localStorage.getItem('role');
  if (role) {
    return role;
  }

  const token = localStorage.getItem('token');
  if (token) {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.role || null;
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  }

  return null;
};

export const isHrUser = (): boolean => {
  const role = getUserRole();
  return role === 'HR';
};

export const isCandidateUser = (): boolean => {
  const role = getUserRole();
  return role === 'CANDIDATE';
};

export const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('token');
  return !!token;
};

export const getCookieValue = (name: string): string | null => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(';').shift() || null;
  }
  return null;
};

export const getValidToken = (): string | null => {
  let token = localStorage.getItem('token');
  
  if (!token) {
    token = getCookieValue('auth_token');
  }
  
  if (!token) {
    console.warn('No token found in localStorage or cookies');
    return null;
  }

  try {
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.warn('Invalid token format');
      return null;
    }

    const payload = JSON.parse(atob(parts[1]));
    const currentTime = Math.floor(Date.now() / 1000);
    
    if (payload.exp && payload.exp < currentTime) {
      console.warn('Token has expired');
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      return null;
    }

    return token;
  } catch (error) {
    console.error('Error validating token:', error);
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    return null;
  }
};

export const logout = (): void => {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  
  document.cookie = 'auth_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict; Secure';
  
  document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict; Secure';
};