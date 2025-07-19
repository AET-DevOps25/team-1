import { useState } from 'react';
import type { HrForm, ValidationError } from '../types/dashboard';
import apiConfig from '../utils/api';

export const useHrOperations = () => {
  const [isHrRegistering, setIsHrRegistering] = useState(false);

  const createHrAccount = async (hrForm: HrForm) => {
    if (!hrForm.fullName || !hrForm.email || !hrForm.password) {
      return { success: false, message: 'All fields are required' };
    }

    setIsHrRegistering(true);
    try {
      const token = localStorage.getItem('token') || '';
      const response = await fetch(apiConfig.getFullURL('/api/v1/auth/hr-register'), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(hrForm)
      });
      const data = await response.json();
      if (response.ok && data?.success) {
        return { success: true, message: 'HR user created successfully' };
      } else {
        if (data?.errors && Array.isArray(data.errors)) {
          const errorMessages = data.errors.map((error: ValidationError) => 
            `${error.field}: ${error.message}`
          ).join('\n');
          return { success: false, message: errorMessages, errors: data.errors };
        }
        return { success: false, message: data?.message || 'Failed to create HR' };
      }
    } catch (err) {
      console.error(err);
      return { success: false, message: 'Error creating HR' };
    } finally {
      setIsHrRegistering(false);
    }
  };

  return {
    isHrRegistering,
    createHrAccount
  };
};