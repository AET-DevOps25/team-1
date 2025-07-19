import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import type { Application, SortField, SortDirection } from '../types/dashboard';
import { useApplications, useApplicationFiltering } from '../hooks/useApplications';
import ApplicationList from './ApplicationList';
import StatusModal from './modals/StatusModal';
import HrDecisionModal from './modals/HrDecisionModal';
import DetailsModal from './modals/DetailsModal';
import apiConfig from '../utils/api';
import './Dashboard.css';

interface JobInfo {
  jobId: string;
  title: string;
  description: string;
  status: string;
  createdAt: string;
}

const JobApplications: React.FC = () => {
  const { jobId } = useParams();
  const navigate = useNavigate();
  
  const [jobInfo, setJobInfo] = useState<JobInfo | null>(null);
  const [isLoadingJob, setIsLoadingJob] = useState(true);
  
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [sortField, setSortField] = useState<SortField>('date');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);
  
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [selectedApplication, setSelectedApplication] = useState<Application | null>(null);
  const [newStatus, setNewStatus] = useState<string>('');
  const [isUpdating, setIsUpdating] = useState<boolean>(false);
  const [isHrDecisionModalOpen, setIsHrDecisionModalOpen] = useState<boolean>(false);
  const [newHrDecision, setNewHrDecision] = useState<string>('');
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState<boolean>(false);
  
  const { applications, updateApplicationStatus, updateHrDecision } = useApplications(selectedStatus, currentPage, pageSize, jobId);
  
  const filteredAndSortedApplications = useApplicationFiltering(
    applications,
    selectedStatus,
    searchTerm,
    sortField,
    sortDirection
  );

  const statusOptions = [
    { value: 'ALL', label: 'All Applications' },
    { value: 'SUBMITTED', label: 'Submitted' },
    { value: 'AI_SCREENING', label: 'AI Screening' },
    { value: 'AI_INTERVIEW', label: 'AI Interview' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'SHORTLISTED', label: 'Shortlisted' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'HIRED', label: 'Hired' }
  ];

  useEffect(() => {
    if (jobId) {
      fetchJobInfo();
    }
  }, [jobId]);

  const fetchJobInfo = async () => {
    try {
      setIsLoadingJob(true);
      const response = await fetch(apiConfig.getFullURL(`/api/v1/jobs/${jobId}`), {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      });

      if (response.ok) {
        const data = await response.json();
        if (data.success && data.data) {
          setJobInfo(data.data);
        }
      } else {
        console.error('Failed to fetch job info');
      }
    } catch (err) {
      console.error('Error fetching job info:', err);
    } finally {
      setIsLoadingJob(false);
    }
  };

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleStatusClick = (application: Application) => {
    setSelectedApplication(application);
    setNewStatus(application.status);
    setIsModalOpen(true);
  };

  const handleHrDecisionClick = (application: Application) => {
    setSelectedApplication(application);
    setNewHrDecision(application.hr_decision || 'PENDING');
    setIsHrDecisionModalOpen(true);
  };

  const handleDetailsClick = (application: Application) => {
    setSelectedApplication(application);
    setIsDetailsModalOpen(true);
  };

  const handleStatusUpdate = async () => {
    if (!selectedApplication || !newStatus) return;
    setIsUpdating(true);
    
    const success = await updateApplicationStatus(selectedApplication.application_id!, newStatus);
    if (success) {
      setIsModalOpen(false);
      setSelectedApplication(null);
    } else {
      alert('Status updated locally (backend connection failed)');
      setIsModalOpen(false);
      setSelectedApplication(null);
    }
    setIsUpdating(false);
  };

  const handleHrDecisionUpdate = async () => {
    if (!selectedApplication || !newHrDecision) return;
    setIsUpdating(true);
    
    const success = await updateHrDecision(selectedApplication.application_id!, newHrDecision);
    if (success) {
      setIsHrDecisionModalOpen(false);
      setSelectedApplication(null);
    } else {
      alert('HR decision updated locally (backend connection failed)');
      setIsHrDecisionModalOpen(false);
      setSelectedApplication(null);
    }
    setIsUpdating(false);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedApplication(null);
    setNewStatus('');
  };

  const handleHrDecisionModalClose = () => {
    setIsHrDecisionModalOpen(false);
    setSelectedApplication(null);
    setNewHrDecision('');
  };

  const handleDetailsModalClose = () => {
    setIsDetailsModalOpen(false);
  };

  const getStatusCount = (status: string) => {
    if (status === 'ALL') return applications.length;
    return applications.filter(app => app.status === status).length;
  };

  if (isLoadingJob) {
    return (
      <div className="dashboard-container">
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
          <div>Loading job information...</div>
        </div>
      </div>
    );
  }

  if (!jobInfo) {
    return (
      <div className="dashboard-container">
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '400px' }}>
          <div>
            <h3>Job not found</h3>
            <button onClick={() => navigate('/dashboard')} className="login-button">
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      <div className="sidebar">
        <div className="logo">
          <button 
            onClick={() => navigate('/dashboard')}
            style={{
              background: 'none',
              border: 'none',
              color: 'inherit',
              fontSize: 'inherit',
              fontWeight: 'bold',
              cursor: 'pointer',
              textDecoration: 'underline'
            }}
          >
            ← 
          </button>
        </div>
        
        <div className="sidebar-section">
          <div className="sidebar-title">Job Information</div>
          <div style={{ padding: '10px 0', fontSize: '14px', lineHeight: '1.4' }}>
            <div><strong>Title:</strong> {jobInfo.title}</div>
            <div><strong>Status:</strong> 
              <span style={{ 
                marginLeft: '8px',
                padding: '2px 6px',
                borderRadius: '3px',
                backgroundColor: jobInfo.status === 'OPEN' ? '#27ae60' : '#95a5a6',
                color: 'white',
                fontSize: '12px'
              }}>
                {jobInfo.status}
              </span>
            </div>
            <div><strong>Created:</strong> {new Date(jobInfo.createdAt).toLocaleDateString()}</div>
          </div>
        </div>
        
        <div className="sidebar-section">
          <div className="sidebar-title">Application Status</div>
          {statusOptions.map(option => (
            <div 
              key={option.value} 
              className={`sidebar-item ${selectedStatus === option.value ? 'active' : ''}`}
              onClick={() => setSelectedStatus(option.value)}
              onKeyDown={(e) => e.key === 'Enter' && setSelectedStatus(option.value)}
              role="button"
              tabIndex={0}
            >
              <span>{option.label}</span>
              <span>{getStatusCount(option.value)}</span>
            </div>
          ))}
        </div>

 

        <div className="sidebar-section">
          <div className="sidebar-title">Navigation</div>
          <div style={{ padding: '10px 0' }}>
            <button 
              className="filter-select"
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              style={{ marginBottom: '8px', width: '100%' }}
            >
              ← Previous Page
            </button>
            <div style={{ textAlign: 'center', fontSize: '12px', margin: '8px 0' }}>
              Page {currentPage + 1}
            </div>
            <button 
              className="filter-select"
              onClick={() => setCurrentPage(currentPage + 1)}
              disabled={filteredAndSortedApplications.length < pageSize}
              style={{ width: '100%' }}
            >
              Next Page →
            </button>
          </div>
        </div>
      </div>

      <div className="main-content">
        <header className="dashboard-header">
          <div className="search-bar-container" style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
            <h2 style={{ margin: 0, fontSize: '24px' }}>Applications for: {jobInfo.title}</h2>
          </div>
          <div className="header-actions" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <input 
              type="text" 
              placeholder="Search candidates by name, email..." 
              className="search-input"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              style={{ width: '300px' }}
            />
            <span className="results-count">{filteredAndSortedApplications.length} applications</span>
          </div>
        </header>

        {jobInfo.description && (
          <div style={{ 
            backgroundColor: '#f8f9fa', 
            padding: '15px', 
            margin: '0 20px 20px 20px',
            borderRadius: '8px',
            border: '1px solid #e9ecef',
            wordWrap: 'break-word'
          }}>
            <h4 style={{ margin: '0 0 10px 0', color: '#2c3e50' }}>Job Description</h4>
            <p style={{ margin: 0, lineHeight: '1.5', color: '#555', textWrap: 'wrap' }}>{jobInfo.description}</p>
          </div>
        )}

        {filteredAndSortedApplications.length > 0 ? (
          <ApplicationList
            applications={filteredAndSortedApplications}
            sortField={sortField}
            sortDirection={sortDirection}
            onSort={handleSort}
            onStatusClick={handleStatusClick}
            onHrDecisionClick={handleHrDecisionClick}
            onDetailsClick={handleDetailsClick}
          />
        ) : (
          <div className="empty-state" style={{ 
            textAlign: 'center', 
            padding: '60px 20px', 
            color: '#7f8c8d' 
          }}>
            <div style={{ fontSize: '48px', marginBottom: '20px' }}>📝</div>
            {selectedStatus !== 'ALL' && (
              <button 
                onClick={() => setSelectedStatus('ALL')}
                style={{
                  marginTop: '10px',
                  padding: '8px 16px',
                  backgroundColor: '#3498db',
                  color: 'white',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: 'pointer'
                }}
              >
                Show All Applications
              </button>
            )}
          </div>
        )}
      </div>

      <StatusModal
        isOpen={isModalOpen}
        selectedApplication={selectedApplication}
        newStatus={newStatus}
        isUpdating={isUpdating}
        onClose={handleModalClose}
        onStatusChange={setNewStatus}
        onUpdate={handleStatusUpdate}
      />

      <HrDecisionModal
        isOpen={isHrDecisionModalOpen}
        selectedApplication={selectedApplication}
        newHrDecision={newHrDecision}
        isUpdating={isUpdating}
        onClose={handleHrDecisionModalClose}
        onDecisionChange={setNewHrDecision}
        onUpdate={handleHrDecisionUpdate}
      />

      <DetailsModal
        isOpen={isDetailsModalOpen}
        selectedApplication={selectedApplication}
        onClose={handleDetailsModalClose}
      />
    </div>
  );
};

export default JobApplications;