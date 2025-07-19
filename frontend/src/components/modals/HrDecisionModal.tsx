import React from 'react';
import type { Application, StatusOption } from '../../types/dashboard';

interface HrDecisionModalProps {
  isOpen: boolean;
  selectedApplication: Application | null;
  newHrDecision: string;
  isUpdating: boolean;
  onClose: () => void;
  onDecisionChange: (decision: string) => void;
  onUpdate: () => void;
}

const HrDecisionModal: React.FC<HrDecisionModalProps> = ({
  isOpen,
  selectedApplication,
  newHrDecision,
  isUpdating,
  onClose,
  onDecisionChange,
  onUpdate
}) => {
  const hrDecisionOptions: StatusOption[] = [
    { value: 'PENDING', label: 'Pending Review', icon: '⏳', description: 'Awaiting HR review' },
    { value: 'APPROVE', label: 'Approved', icon: '✅', description: 'Candidate approved for hire' },
    { value: 'REJECT', label: 'Rejected', icon: '❌', description: 'Candidate rejected' },
    { value: 'INTERVIEW', label: 'Schedule Interview', icon: '📅', description: 'Schedule additional interview' },
    { value: 'FOLLOW_UP', label: 'Follow Up', icon: '📞', description: 'Requires follow-up action' }
  ];

  const getHrDecisionDisplay = (decision?: string) => {
    switch (decision) {
      case 'PENDING': return 'Pending Review';
      case 'APPROVE': return 'Approved';
      case 'REJECT': return 'Rejected';
      case 'INTERVIEW': return 'Schedule Interview';
      case 'FOLLOW_UP': return 'Follow Up';
      default: return 'Pending Review';
    }
  };

  if (!isOpen || !selectedApplication) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Update HR Decision</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <div className="candidate-details">
            <h4>{selectedApplication.candidate_name}</h4>
            <p>{selectedApplication.job_title}</p>
            <p className="current-status">
              Current Decision: <span className="status-highlight">{getHrDecisionDisplay(selectedApplication.hr_decision)}</span>
            </p>
          </div>
          
          <div className="status-selection">
            <label>Select HR Decision:</label>
            <div className="status-grid">
              {hrDecisionOptions.map(option => (
                <div 
                  key={option.value}
                  className={`status-option ${newHrDecision === option.value ? 'selected' : ''}`}
                  onClick={() => onDecisionChange(option.value)}
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
            disabled={isUpdating || newHrDecision === selectedApplication.hr_decision}
          >
            {isUpdating ? 'Updating...' : 'Update Decision'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default HrDecisionModal;