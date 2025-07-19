import React from 'react';
import type { HrForm } from '../../types/dashboard';

interface HrRegisterModalProps {
  isOpen: boolean;
  hrForm: HrForm;
  isRegistering: boolean;
  errors?: {[key: string]: string};
  successMessage?: string;
  onClose: () => void;
  onInputChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onSubmit: () => Promise<void>;
}

const HrRegisterModal: React.FC<HrRegisterModalProps> = ({
  isOpen,
  hrForm,
  isRegistering,
  errors = {},
  successMessage = '',
  onClose,
  onInputChange,
  onSubmit
}) => {
  const getFieldError = (fieldName: string) => {
    return errors[fieldName];
  };

  const hasFieldError = (fieldName: string) => {
    return !!errors[fieldName];
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: 450 }}>
        <div className="modal-header">
          <h3>Create HR Account</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          {successMessage && (
            <div style={{
              backgroundColor: '#d4edda',
              color: '#155724',
              padding: '12px',
              borderRadius: '4px',
              marginBottom: '10px',
              border: '1px solid #c3e6cb'
            }}>
              {successMessage}
            </div>
          )}
          
          <div style={{ marginBottom: 15 }}>
            <input
              type="text"
              name="fullName"
              placeholder="Full Name"
              value={hrForm.fullName}
              onChange={onInputChange}
              style={{ 
                width: '100%', 
                padding: 8,
                border: hasFieldError('fullName') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
            {hasFieldError('fullName') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('fullName')}
              </div>
            )}
          </div>

          <div style={{ marginBottom: 15 }}>
            <input
              type="email"
              name="email"
              placeholder="Email"
              value={hrForm.email}
              onChange={onInputChange}
              style={{ 
                width: '100%', 
                padding: 8,
                border: hasFieldError('email') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
            {hasFieldError('email') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('email')}
              </div>
            )}
          </div>

          <div style={{ marginBottom: 15 }}>
            <input
              type="password"
              name="password"
              placeholder="Password (minimum 6 characters)"
              value={hrForm.password}
              onChange={onInputChange}
              style={{ 
                width: '100%', 
                padding: 8,
                border: hasFieldError('password') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px'
              }}
            />
            {hasFieldError('password') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('password')}
              </div>
            )}
            <div style={{ color: '#666', fontSize: '11px', marginTop: '4px' }}>
              Password must be at least 6 characters long
            </div>
          </div>
        </div>
        <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
          <button className="btn-cancel" onClick={onClose}>Cancel</button>
          <button 
            className="btn-update" 
            onClick={onSubmit} 
            disabled={isRegistering || !hrForm.fullName || !hrForm.email || !hrForm.password}
          >
            {isRegistering ? 'Creating...' : 'Create'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default HrRegisterModal;