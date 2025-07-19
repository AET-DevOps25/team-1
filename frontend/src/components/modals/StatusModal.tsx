import React from 'react';
import type { Application, StatusOption } from '../../types/dashboard';

interface StatusModalProps {
  isOpen: boolean;
  selectedApplication: Application | null;
  newStatus: string;
  isUpdating: boolean;
  onClose: () => void;
  onStatusChange: (status: string) => void;
  onUpdate: () => void;
}

const StatusModal: React.FC<StatusModalProps> = ({
  isOpen,
  selectedApplication,
  newStatus,
  isUpdating,
  onClose,
  onStatusChange,
  onUpdate
}) => {
  const editableStatusOptions: StatusOption[] = [
    { value: 'SUBMITTED', label: 'Submitted', icon: '📝', description: 'Initial application received' },
    { value: 'AI_SCREENING', label: 'AI Screening', icon: '🤖', description: 'Automated resume analysis' },
    { value: 'AI_INTERVIEW', label: 'AI Interview', icon: '💬', description: 'AI-powered interview' },
    { value: 'COMPLETED', label: 'Assessment Complete', icon: '✅', description: 'All assessments finished' },
    { value: 'SHORTLISTED', label: 'Shortlisted', icon: '⭐', description: 'Selected for final review' },
    { value: 'REJECTED', label: 'Rejected', icon: '❌', description: 'Application declined' },
    { value: 'HIRED', label: 'Hired', icon: '🎉', description: 'Successfully hired' }
  ];

  const getStatusDisplay = (status: string) => {
    switch (status) {
      case 'SUBMITTED': return 'New Application';
      case 'AI_SCREENING': return 'AI Screening';
      case 'AI_INTERVIEW': return 'AI Interview';
      case 'COMPLETED': return 'Assessment Complete';
      case 'SHORTLISTED': return 'Shortlisted';
      case 'REJECTED': return 'Rejected';
      case 'HIRED': return 'Hired';
      default: return status;
    }
  };

  if (!isOpen || !selectedApplication) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Update Application Status</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <div className="candidate-details">
            <h4>{selectedApplication.candidate_name}</h4>
            <p>{selectedApplication.job_title}</p>
            <p className="current-status">
              Current Status: <span className="status-highlight">{getStatusDisplay(selectedApplication.status)}</span>
            </p>
          </div>
          
          <div className="status-selection">
            <label>Select New Status:</label>
            <div className="status-grid">
              {editableStatusOptions.map(option => (
                <div 
                  key={option.value}
                  className={`status-option ${newStatus === option.value ? 'selected' : ''}`}
                  onClick={() => onStatusChange(option.value)}
                >
                  <div className="status-option-header">
                    <span className="status-icon">{option.icon}</span>
                    <span className="status-label">{option.label}</span>
                  </div>
                  <div className="status-description">{option.description}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
        
        <div className="modal-footer">
          <button 
            className="btn-cancel" 
            onClick={onClose}
            disabled={isUpdating}
          >
            Cancel
          </button>
          <button 
            className="btn-update" 
            onClick={onUpdate}
            disabled={isUpdating || newStatus === selectedApplication.status}
          >
            {isUpdating ? 'Updating...' : 'Update Status'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default StatusModal;