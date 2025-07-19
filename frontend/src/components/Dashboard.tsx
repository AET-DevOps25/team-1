import React, { useState, useMemo, useEffect } from 'react';
import { Container } from '@mui/material';
import './Dashboard.css';
import apiConfig from '../utils/api';
import DashboardHeader from './DashboardHeader';
import ApplicationsView from './ApplicationsView';
import JobsView from './JobsView';
import DetailsModal from './modals/DetailsModal';

type SortField = 'name' | 'job' | 'status' | 'date' | 'score' | 'finalScore' | 'hrDecision' | 'resumeScore' | 'chatScore';
type SortDirection = 'asc' | 'desc';

type Application = {
  applicationId: string;
  jobId: string;
  candidateId: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  candidate: {
    candidateId: string;
    fullName: string;
    email: string;
  };
  job: {
    jobId: string;
    title: string;
    description: string;
    status: string;
  };
  assessment?: {
    assessmentId: string;
    resumeScore: number;
    chatScore: number;
    finalScore: number;
    aiResumeAnalysis: string;
    aiChatSummary: string;
    aiRecommendStatus: string;
  };
  hrDecision?: string;
  hrComments?: string;
  application_id?: string;
  candidate_name?: string;
  email?: string;
  job_title?: string;
  submission_timestamp?: string;
  final_score?: number | null;
  avatarColor?: string;
  hr_decision?: string;
  hr_comment?: string;
};

type SortValue = string | number | Date;

const Dashboard: React.FC = () => {

  type ViewMode = 'applications' | 'jobs';
  const [selectedView, setSelectedView] = useState<ViewMode>('applications');

  interface Job {
    jobId: string; // API returns jobId (camelCase)
    jobID?: string; // backup field for compatibility
    id?: string; // backup field in case API uses different naming
    title: string;
    description: string;
    requirements: string;
    status: 'DRAFT' | 'OPEN' | 'CLOSED';
    createdAt: string;
    updatedAt?: string;
    hrCreator?: any;
  }
  const [jobs, setJobs] = useState<Job[]>([]);
  const [isJobsLoading, setIsJobsLoading] = useState(false);

  const [isJobModalOpen, setIsJobModalOpen] = useState(false);
  const [isEditingJob, setIsEditingJob] = useState(false);
  const [jobForm, setJobForm] = useState<{ jobId?: string; title: string; description: string; requirements: string; }>(
    { title: '', description: '', requirements: '' }
  );

  const handleJobInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setJobForm(prev => ({ ...prev, [name]: value } as any));
  };

  const openCreateJobModal = () => {
    setIsEditingJob(false);
    setJobForm({ title: '', description: '', requirements: '' });
    setIsJobModalOpen(true);
  };

  const openEditJobModal = (job: Job) => {
    console.log('Opening edit modal for job:', job);
    const actualJobId = getJobId(job);
    console.log('Edit modal - Resolved jobId:', actualJobId);
    
    setIsEditingJob(true);
    setJobForm({ 
      jobId: actualJobId, 
      title: job.title, 
      description: job.description || '', 
      requirements: job.requirements || ''
    });
    setIsJobModalOpen(true);
  };

  const saveJob = async () => {
    if (!jobForm.title) return;
    const token = localStorage.getItem('token') || '';
    const editing = Boolean(jobForm.jobId);
    const method = editing ? 'PATCH' : 'POST';
    const url = editing ? `/api/v1/jobs/${jobForm.jobId}` : '/api/v1/jobs';
    
    console.log('Saving job:', { editing, jobId: jobForm.jobId, method, url });
    
    try {
      const res = await fetch(apiConfig.getFullURL(url), {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          title: jobForm.title,
          description: jobForm.description,
          requirements: jobForm.requirements,
        })
      });
      const data = await res.json();
      if (res.ok && data?.success) {
        setIsJobModalOpen(false);
        setJobForm({ title: '', description: '', requirements: ''});
        setIsEditingJob(false);
        refreshJobs();
      } else {
        alert(data?.message || 'Save failed');
      }
    } catch (e) {
      console.error(e);
      alert('Error saving job');
    }
  };

  const [selectedStatus] = useState<string>('ALL');
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [sortField, setSortField] = useState<SortField>('date');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [selectedApplication, setSelectedApplication] = useState<Application | null>(null);
  const [newStatus, setNewStatus] = useState<string>('');
  const [isUpdating, setIsUpdating] = useState<boolean>(false);
  const [isHrDecisionModalOpen, setIsHrDecisionModalOpen] = useState<boolean>(false);
  const [newHrDecision, setNewHrDecision] = useState<string>('');
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState<boolean>(false);

  const [isHrRegisterModalOpen, setIsHrRegisterModalOpen] = useState<boolean>(false);
  const [hrForm, setHrForm] = useState({ fullName: '', email: '', password: '' });
  const [isHrRegistering, setIsHrRegistering] = useState<boolean>(false);

  const handleHrInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setHrForm(prev => ({ ...prev, [name]: value }));
  };

  const createHrAccount = async () => {
    if (!hrForm.fullName || !hrForm.email || !hrForm.password) return;
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
        alert('HR user created');
        setIsHrRegisterModalOpen(false);
        setHrForm({ fullName: '', email: '', password: '' });
      } else {
        alert(data?.message || 'Failed to create HR');
      }
    } catch (err) {
      console.error(err);
      alert('Error creating HR');
    } finally {
      setIsHrRegistering(false);
    }
  };

  const [currentPage] = useState(0);
  const [pageSize] = useState(20);
  const [, setTotalApplications] = useState(0);
  const [isLoadingApplications, setIsLoadingApplications] = useState(false);

  const fetchApplications = async (page: number = 0, size: number = 20, jobId?: string, status?: string) => {
    setIsLoadingApplications(true);
    try {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString()
      });
      
      if (jobId) params.append('jobId', jobId);
      if (status && status !== 'ALL') params.append('status', status);

      const response = await fetch(apiConfig.getFullURL(`/api/v1/applications?${params}`), {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
          'Content-Type': 'application/json'
        }
      });
      
      if (response.ok) {
        const data = await response.json();
        if (data.success && data.data) {
          const mappedApplications = data.data.content.map((app: any) => ({
            ...app,
            application_id: app.applicationId,
            candidate_name: app.candidate?.fullName,
            email: app.candidate?.email,
            job_title: app.job?.title,
            submission_timestamp: app.createdAt,
            final_score: app.assessment?.finalScore || null,
            chatStatus: app.chatStatus,
            avatarColor: ['blue', 'gray', 'green', 'purple', 'orange'][Math.floor(Math.random() * 5)],
            hr_decision: app.hrDecision,
            hr_comment: app.hrComments,
            assessment: app.assessment ? {
              resumeScore: app.assessment.resumeScore,
              chatScore: app.assessment.chatScore,
              interviewScore: app.assessment.interviewScore,
              finalScore: app.assessment.finalScore,
              resumeComment: app.assessment.resumeComment,
              interviewComment: app.assessment.interviewComment,
              recommendation: app.assessment.recommendation,
              aiResumeAnalysis: app.assessment.aiResumeAnalysis,
              aiChatSummary: app.assessment.aiChatSummary,
              aiRecommendStatus: app.assessment.aiRecommendStatus,
              // Keep snake_case for backward compatibility
              resume_score: app.assessment.resumeScore,
              chat_score: app.assessment.chatScore || app.assessment.interviewScore,
              final_score: app.assessment.finalScore,
              ai_resume_analysis: app.assessment.aiResumeAnalysis,
              ai_chat_summary: app.assessment.aiChatSummary,
              ai_recommend_status: app.assessment.aiRecommendStatus || app.assessment.recommendation
            } : undefined
          }));
          
          setApplications(mappedApplications);
          setTotalApplications(data.data.totalElements || 0);
        }
      } else {
        console.error('Failed to fetch applications');
      }
    } catch (err) {
      console.error('Error fetching applications:', err);
    } finally {
      setIsLoadingApplications(false);
    }
  };

  useEffect(() => {
    fetchApplications(currentPage, pageSize, undefined, selectedStatus);
  }, [currentPage, pageSize, selectedStatus]);

  useEffect(() => {
    if (selectedView !== 'jobs') return;
    const loadJobs = async () => {
      setIsJobsLoading(true);
      try {
        const res = await fetch(apiConfig.getFullURL('/api/v1/jobs?page=0&size=50'), {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token') || ''}` }
        });
        const data = await res.json();
        if (res.ok && data?.success) {
          const jobsData = Array.isArray(data.data) ? data.data : data.data?.content || [];
          console.log('Raw jobs data from API:', jobsData);
          console.log('First job sample:', jobsData[0]);
          setJobs(jobsData);
        }
      } catch (e) {
        console.error('Failed to fetch jobs', e);
      } finally {
        setIsJobsLoading(false);
      }
    };
    loadJobs();
  }, [selectedView]);

  const refreshJobs = async () => {
    if (selectedView !== 'jobs') return;
    
    setIsJobsLoading(true);
    try {
      const res = await fetch(apiConfig.getFullURL('/api/v1/jobs?page=0&size=50'), {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token') || ''}` }
      });
      const data = await res.json();
      if (res.ok && data?.success) {
        // backend might wrap list inside data.content or data
        const jobsData = Array.isArray(data.data) ? data.data : data.data?.content || [];
        console.log('Refresh - Raw jobs data from API:', jobsData);
        console.log('Refresh - First job sample:', jobsData[0]);
        setJobs(jobsData);
      }
    } catch (e) {
      console.error('Failed to fetch jobs', e);
    } finally {
      setIsJobsLoading(false);
    }
  };

  const getJobId = (job: any): string => {
    return job.jobId || job.jobID || job.id || job.Job_ID || job.job_id || '';
  };

  const toggleJobStatus = async (job: Job) => {
    const token = localStorage.getItem('token') || '';
    
    console.log('toggleJobStatus called with job:', job);
    console.log('Job keys:', Object.keys(job));
    
    const actualJobId = getJobId(job);
    console.log('Resolved jobId:', actualJobId);
    
    if (!actualJobId) {
      console.error('No job ID found in job object:', job);
      alert('Cannot find job ID - please refresh the page');
      return;
    }
    
    const actionEndpoint = job.status === 'OPEN' 
      ? `/api/v1/jobs/${actualJobId}/close`
      : `/api/v1/jobs/${actualJobId}/open`;
    
    console.log('Toggling job status:', { jobId: actualJobId, currentStatus: job.status, endpoint: actionEndpoint });
    
    try {
      const res = await fetch(apiConfig.getFullURL(actionEndpoint), {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      
      const data = await res.json();
             if (res.ok && data?.success) {
         const newStatus = job.status === 'OPEN' ? 'CLOSED' : 'OPEN';
         setJobs(prev => prev.map(j => getJobId(j) === actualJobId ? { ...j, status: newStatus } : j));
      } else {
        console.error('Toggle status failed:', data);
        alert(data?.message || 'Action failed');
      }
    } catch (e) {
      console.error('Toggle status error:', e);
      alert('Action error');
    }
  };

  const [applications, setApplications] = useState<Application[]>([]);

  const editableStatusOptions = [
    { value: 'SUBMITTED', label: 'Submitted', icon: '📝', description: 'Initial application received' },
    { value: 'AI_SCREENING', label: 'AI Screening', icon: '🤖', description: 'Automated resume analysis' },
    { value: 'AI_INTERVIEW', label: 'AI Interview', icon: '💬', description: 'AI-powered interview' },
    { value: 'COMPLETED', label: 'Assessment Complete', icon: '✅', description: 'All assessments finished' },
    { value: 'SHORTLISTED', label: 'Shortlisted', icon: '⭐', description: 'Selected for final review' },
    { value: 'REJECTED', label: 'Rejected', icon: '❌', description: 'Application declined' },
    { value: 'HIRED', label: 'Hired', icon: '🎉', description: 'Successfully hired' }
  ];

  const filteredAndSortedApplications = useMemo(() => {
    if (!Array.isArray(applications)) {
      return [];
    }
    
    let filtered = applications;

    if (selectedStatus !== 'ALL') {
      filtered = filtered.filter(app => app.status === selectedStatus);
    }

    if (searchTerm) {
      filtered = filtered.filter(app => {
        const name = app.candidate_name ?? '';
        const jobTitle = app.job_title ?? '';
        const mail = app.email ?? '';
        return (
          name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          jobTitle.toLowerCase().includes(searchTerm.toLowerCase()) ||
          mail.toLowerCase().includes(searchTerm.toLowerCase())
        );
      });
    }

    return filtered.sort((a, b) => {
      let aVal: SortValue, bVal: SortValue;

      switch (sortField) {
        case 'name':
          aVal = (a.candidate_name ?? '').toLowerCase();
          bVal = (b.candidate_name ?? '').toLowerCase();
          break;
        case 'job':
          aVal = (a.job_title ?? '').toLowerCase();
          bVal = (b.job_title ?? '').toLowerCase();
          break;
        case 'status':
          aVal = a.status;
          bVal = b.status;
          break;
        case 'date':
          aVal = new Date(a.submission_timestamp ?? a.createdAt ?? 0);
          bVal = new Date(b.submission_timestamp ?? b.createdAt ?? 0);
          break;
        case 'score':
          aVal = a.final_score ?? a.assessment?.finalScore ?? 0;
          bVal = b.final_score ?? b.assessment?.finalScore ?? 0;
          break;
        case 'finalScore':
          aVal = a.assessment?.finalScore ?? 0;
          bVal = b.assessment?.finalScore ?? 0;
          break;
        case 'resumeScore':
          aVal = a.assessment?.resumeScore ?? 0;
          bVal = b.assessment?.resumeScore ?? 0;
          break;
        case 'chatScore':
          aVal = (a.assessment as any)?.interviewScore ?? a.assessment?.chatScore ?? 0;
          bVal = (b.assessment as any)?.interviewScore ?? b.assessment?.chatScore ?? 0;
          break;
        case 'hrDecision':
          aVal = a.hr_decision || a.hrDecision || 'PENDING';
          bVal = b.hr_decision || b.hrDecision || 'PENDING';
          break;
        default:
          return 0;
      }

      if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }, [applications, selectedStatus, searchTerm, sortField, sortDirection]);

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

  const handleStatusUpdate = async () => {
    if (!selectedApplication || !newStatus) return;

    setIsUpdating(true);
    
    try {
      const response = await fetch(apiConfig.getFullURL(`/api/applications/${selectedApplication.application_id}/status`), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          status: newStatus,
          updated_by: 'team1_admin'
        }),
      });

      if (response.ok) {
        setApplications(prevApplications =>
          prevApplications.map(app =>
            app.application_id === selectedApplication.application_id
              ? { ...app, status: newStatus }
              : app
          )
        );
        setIsModalOpen(false);
        setSelectedApplication(null);
      } else {
        console.error('Failed to update status');
        alert('Failed to update status. Please try again.');
      }
    } catch (error) {
      console.error('Error updating status:', error);
      setApplications(prevApplications =>
        prevApplications.map(app =>
          app.application_id === selectedApplication.application_id
            ? { ...app, status: newStatus }
            : app
        )
      );
      setIsModalOpen(false);
      setSelectedApplication(null);
      alert('Status updated locally (backend connection failed)');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleHrDecisionClick = (application: Application) => {
    setSelectedApplication(application);
    setNewHrDecision(application.hr_decision || 'SHORTLISTED');
    setIsHrDecisionModalOpen(true);
  };

  const handleHrDecisionUpdate = async () => {
    if (!selectedApplication || !newHrDecision) return;

    setIsUpdating(true);
    
    try {
      const token = localStorage.getItem('token') || '';
      const applicationId = selectedApplication.applicationId || selectedApplication.application_id;
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/applications/${applicationId}`), {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          hrDecision: newHrDecision
        }),
      });

      if (response.ok) {
        setApplications(prevApplications =>
          prevApplications.map(app =>
            app.application_id === selectedApplication.application_id
              ? { ...app, hr_decision: newHrDecision as string }
              : app
          )
        );
        setIsHrDecisionModalOpen(false);
        setSelectedApplication(null);
      } else {
        console.error('Failed to update HR decision');
        alert('Failed to update HR decision. Please try again.');
      }
    } catch (error) {
      console.error('Error updating HR decision:', error);
      setApplications(prevApplications =>
        prevApplications.map(app =>
          app.application_id === selectedApplication.application_id
            ? { ...app, hr_decision: newHrDecision as string }
            : app
        )
      );
      setIsHrDecisionModalOpen(false);
      setSelectedApplication(null);
      alert('HR decision updated locally (backend connection failed)');
    } finally {
      setIsUpdating(false);
    }
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

  const handleDetailsClick = (application: Application) => {
    setSelectedApplication(application);
    setIsDetailsModalOpen(true);
  };

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

  const getHrDecisionDisplay = (decision?: string) => {
    switch (decision) {
      case 'SHORTLISTED': return 'Shortlisted';
      case 'REJECTED': return 'Rejected';
      case 'HIRED': return 'Hired';
      default: return 'No Decision';
    }
  };


  return (
    <Container maxWidth="xl" sx={{ py: 3 }}>
      <DashboardHeader
        selectedView={selectedView}
        searchTerm={searchTerm}
        resultsCount={filteredAndSortedApplications.length}
        onViewChange={setSelectedView}
        onSearchChange={setSearchTerm}
        onAddHrClick={() => setIsHrRegisterModalOpen(true)}
        onCreateJobClick={openCreateJobModal}
      />

      {selectedView === 'applications' && (
        <ApplicationsView
          applications={filteredAndSortedApplications}
          sortField={sortField}
          sortDirection={sortDirection}
          isLoading={isLoadingApplications}
          onSort={handleSort}
          onStatusClick={handleStatusClick}
          onHrDecisionClick={handleHrDecisionClick}
          onDetailsClick={handleDetailsClick}
          onRefreshApplications={() => {
            fetchApplications(currentPage, pageSize);
          }}
        />
      )}

      {selectedView === 'jobs' && (
        <JobsView
          jobs={jobs}
          isLoading={isJobsLoading}
          onEditJob={openEditJobModal}
          onToggleJobStatus={toggleJobStatus}
          onRefreshJobs={refreshJobs}
        />
      )}


      {isModalOpen && selectedApplication && (
        <div className="modal-overlay" onClick={handleModalClose}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Update Application Status</h3>
              <button className="modal-close" onClick={handleModalClose}>×</button>
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
                      onClick={() => setNewStatus(option.value)}
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
                onClick={handleModalClose}
                disabled={isUpdating}
              >
                Cancel
              </button>
              <button 
                className="btn-update" 
                onClick={handleStatusUpdate}
                disabled={isUpdating || newStatus === selectedApplication.status}
              >
                {isUpdating ? 'Updating...' : 'Update Status'}
              </button>
            </div>
          </div>
        </div>
      )}

      {isHrDecisionModalOpen && selectedApplication && (
        <div className="modal-overlay" onClick={handleHrDecisionModalClose}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Update HR Decision</h3>
              <button className="modal-close" onClick={handleHrDecisionModalClose}>×</button>
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
                  {[
                    { value: 'SHORTLISTED', label: 'Shortlisted', icon: '⭐', description: 'Candidate shortlisted for further consideration' },
                    { value: 'REJECTED', label: 'Rejected', icon: '❌', description: 'Candidate rejected' },
                    { value: 'HIRED', label: 'Hired', icon: '🎉', description: 'Candidate successfully hired' }
                  ].map(option => (
                    <div 
                      key={option.value}
                      className={`status-option ${newHrDecision === option.value ? 'selected' : ''}`}
                      onClick={() => setNewHrDecision(option.value)}
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
                onClick={handleHrDecisionModalClose}
                disabled={isUpdating}
              >
                Cancel
              </button>
              <button 
                className="btn-update" 
                onClick={handleHrDecisionUpdate}
                disabled={isUpdating || newHrDecision === selectedApplication.hr_decision}
              >
                {isUpdating ? 'Updating...' : 'Update Decision'}
              </button>
            </div>
          </div>
        </div>
      )}

      <DetailsModal
        isOpen={isDetailsModalOpen}
        selectedApplication={selectedApplication}
        onClose={handleDetailsModalClose}
      />

      {isHrRegisterModalOpen && (
        <div className="modal-overlay" onClick={() => setIsHrRegisterModalOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()} style={{ maxWidth: 400 }}>
            <div className="modal-header">
              <h3>Create HR Account</h3>
              <button className="modal-close" onClick={() => setIsHrRegisterModalOpen(false)}>×</button>
            </div>
            <div className="modal-body">
              <input
                type="text"
                name="fullName"
                placeholder="Full Name"
                value={hrForm.fullName}
                onChange={handleHrInputChange}
                style={{ width: '100%', marginBottom: 10, padding: 8 }}
              />
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={hrForm.email}
                onChange={handleHrInputChange}
                style={{ width: '100%', marginBottom: 10, padding: 8 }}
              />
              <input
                type="password"
                name="password"
                placeholder="Password (longer than 5 characters)"
                value={hrForm.password}
                onChange={handleHrInputChange}
                style={{ width: '100%', marginBottom: 10, padding: 8 }}
              />
              <div style={{ color: '#666', fontSize: '11px', marginBottom: 10 }}>
                Password must be longer than 5 characters
              </div>
            </div>
            <div className="modal-footer" style={{ display: 'flex', justifyContent: 'flex-end', gap: 10 }}>
              <button className="btn-cancel" onClick={() => setIsHrRegisterModalOpen(false)}>Cancel</button>
              <button className="btn-update" onClick={createHrAccount} disabled={isHrRegistering || !hrForm.fullName || !hrForm.email || !hrForm.password}>
                {isHrRegistering ? 'Creating...' : 'Create'}
              </button>
            </div>
          </div>
        </div>
      )}

      {isJobModalOpen && (
        <div className="modal-overlay" onClick={() => setIsJobModalOpen(false)}>
          <div className="modal-content" onClick={e=>e.stopPropagation()} style={{ maxWidth:500 }}>
            <div className="modal-header">
              <h3>{isEditingJob ? 'Edit Job' : 'Create Job'}</h3>
              <button className="modal-close" onClick={()=>setIsJobModalOpen(false)}>×</button>
            </div>
            <div className="modal-body">
              <input
                type="text"
                name="title"
                placeholder="Job Title"
                value={jobForm.title}
                onChange={handleJobInputChange}
                style={{ width:'100%', marginBottom:10, padding:8 }}
              />
              <textarea
                name="description"
                placeholder="Description"
                value={jobForm.description}
                onChange={handleJobInputChange}
                style={{ width:'100%', marginBottom:10, padding:8, minHeight:100 }}
              />
              <textarea
                name="requirements"
                placeholder="Requirements"
                value={jobForm.requirements}
                onChange={handleJobInputChange}
                style={{ width:'100%', marginBottom:10, padding:8, minHeight:80 }}
              />

            </div>
            <div className="modal-footer" style={{ display:'flex', justifyContent:'flex-end', gap:10 }}>
              <button className="btn-cancel" onClick={()=>setIsJobModalOpen(false)}>Cancel</button>
              <button className="btn-update" onClick={saveJob} disabled={!jobForm.title || !jobForm.description || !jobForm.requirements}>Save</button>
            </div>
          </div>
        </div>
      )}

    </Container>
  );
};

export default Dashboard; 