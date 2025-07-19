import React from 'react';
import type { JobForm } from '../../types/dashboard';

interface JobModalProps {
  isOpen: boolean;
  isEditing: boolean;
  jobForm: JobForm;
  errors?: {[key: string]: string};
  successMessage?: string;
  errorMessage?: string;
  isSubmitting?: boolean;
  onClose: () => void;
  onInputChange: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  onSubmit: () => Promise<void>;
}

const JobModal: React.FC<JobModalProps> = ({
  isOpen,
  isEditing,
  jobForm,
  errors = {},
  successMessage = '',
  errorMessage = '',
  isSubmitting = false,
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
      <div className="modal-content" onClick={e=>e.stopPropagation()} style={{ maxWidth:550 }}>
        <div className="modal-header">
          <h3>{isEditing ? 'Edit Job' : 'Create Job'}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          {successMessage && (
            <div style={{
              backgroundColor: '#d4edda',
              color: '#155724',
              padding: '12px',
              borderRadius: '4px',
              marginBottom: '15px',
              border: '1px solid #c3e6cb'
            }}>
              {successMessage}
            </div>
          )}

          {errorMessage && (
            <div style={{
              backgroundColor: '#f8d7da',
              color: '#721c24',
              padding: '12px',
              borderRadius: '4px',
              marginBottom: '15px',
              border: '1px solid #f5c6cb',
              whiteSpace: 'pre-line'
            }}>
              {errorMessage}
            </div>
          )}

          <div style={{ marginBottom: 15 }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
              Job Title <span style={{ color: '#e74c3c' }}>*</span>
            </label>
            <input
              type="text"
              name="title"
              placeholder="Enter job title (minimum 3 characters)"
              value={jobForm.title}
              onChange={onInputChange}
              style={{ 
                width:'100%', 
                padding:10,
                border: hasFieldError('title') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            />
            {hasFieldError('title') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('title')}
              </div>
            )}
            <div style={{ color: '#666', fontSize: '11px', marginTop: '4px' }}>
              Minimum 3 characters required for job title.
            </div>
          </div>

          <div style={{ marginBottom: 15 }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
              Job Description <span style={{ color: '#e74c3c' }}>*</span>
            </label>
            <textarea
              name="description"
              placeholder="Describe the job responsibilities, requirements, and qualifications..."
              value={jobForm.description}
              onChange={onInputChange}
              style={{ 
                width:'100%', 
                padding:10, 
                minHeight:120,
                border: hasFieldError('description') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
                resize: 'vertical'
              }}
            />
            {hasFieldError('description') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('description')}
              </div>
            )}
            <div style={{ color: '#666', fontSize: '11px', marginTop: '4px' }}>
              Provide a detailed description of the role, requirements, and what candidates can expect. Minimum 10 characters required.
            </div>
          </div>

          <div style={{ marginBottom: 15 }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
              Job Requirements <span style={{ color: '#e74c3c' }}>*</span>
            </label>
            <textarea
              name="requirements"
              placeholder="List specific requirements, skills, qualifications, and experience needed..."
              value={jobForm.requirements}
              onChange={onInputChange}
              style={{ 
                width:'100%', 
                padding:10, 
                minHeight:100,
                border: hasFieldError('requirements') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px',
                resize: 'vertical'
              }}
            />
            {hasFieldError('requirements') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('requirements')}
              </div>
            )}
            <div style={{ color: '#666', fontSize: '11px', marginTop: '4px' }}>
              Specify technical skills, experience level, certifications, and other requirements. Minimum 5 characters required.
            </div>
          </div>

          <div style={{ marginBottom: 15 }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold', fontSize: '14px' }}>
              Status
            </label>
            <select 
              name="status" 
              value={jobForm.status} 
              onChange={onInputChange} 
              style={{ 
                padding:10,
                width: '100%',
                border: hasFieldError('status') ? '1px solid #e74c3c' : '1px solid #ddd',
                borderRadius: '4px',
                fontSize: '14px'
              }}
            >
              <option value="DRAFT">Draft (not visible to candidates)</option>
              <option value="OPEN">Open (accepting applications)</option>
            </select>
            {hasFieldError('status') && (
              <div style={{ color: '#e74c3c', fontSize: '12px', marginTop: '4px' }}>
                {getFieldError('status')}
              </div>
            )}
          </div>
        </div>
        <div className="modal-footer" style={{ display:'flex', justifyContent:'flex-end', gap:10 }}>
          <button className="btn-cancel" onClick={onClose} disabled={isSubmitting}>Cancel</button>
          <button 
            className="btn-update" 
            onClick={onSubmit} 
            disabled={!jobForm.title.trim() || jobForm.title.trim().length < 3 || !jobForm.description.trim() || jobForm.description.trim().length < 10 || !jobForm.requirements.trim() || jobForm.requirements.trim().length < 5 || isSubmitting}
          >
            {isSubmitting ? 'Saving...' : 'Save Job'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default JobModal;