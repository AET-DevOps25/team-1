import React, { useState, useMemo } from 'react';
import './Dashboard.css';

type SortField = 'name' | 'job' | 'status' | 'date' | 'score' | 'finalScore' | 'hrDecision';
type SortDirection = 'asc' | 'desc';

type Application = {
  application_id: string;
  candidate_name: string;
  email: string;
  job_title: string;
  status: string;
  submission_timestamp: string;
  final_score: number | null;
  avatarColor: string;
  assessment?: {
    resume_score: number;
    chat_score: number;
    final_score: number;
    ai_resume_analysis: string;
    ai_chat_summary: string;
    ai_recommend_status: string;
  };
  hr_decision?: string;
  hr_comment?: string;
};

type SortValue = string | number | Date;

const Dashboard: React.FC = () => {
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
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

  const [applications, setApplications] = useState<Application[]>([
    {
      application_id: '1',
      candidate_name: 'DuGuYifei',
      email: 'yifei.du@team1.com',
      job_title: 'Senior Full Stack Developer',
      status: 'AI_INTERVIEW',
      submission_timestamp: '2024-01-15T10:30:00Z',
      final_score: 92,
      avatarColor: 'blue',
      assessment: {
        resume_score: 95,
        chat_score: 89,
        final_score: 92,
        ai_resume_analysis: 'Excellent technical skills demonstrated with strong problem-solving abilities. Shows deep understanding of full-stack development principles and modern frameworks. Strong educational background and relevant work experience.',
        ai_chat_summary: 'Great communication skills and collaborative mindset. Demonstrates leadership potential and ability to work effectively in team environments. Shows enthusiasm for the role and company culture.',
        ai_recommend_status: 'RECOMMEND'
      },
      hr_decision: 'APPROVE',
      hr_comment: 'Strong candidate with excellent technical background and cultural fit. Recommend proceeding to final interview round.'
    },
    {
      application_id: '2',
      candidate_name: 'Nikola Selic',
      email: 'nikola.selic@team1.com',
      job_title: 'DevOps Engineer',
      status: 'COMPLETED',
      submission_timestamp: '2024-01-14T14:20:00Z',
      final_score: 88,
      avatarColor: 'gray',
      assessment: {
        resume_score: 90,
        chat_score: 86,
        final_score: 88,
        ai_resume_analysis: 'Strong DevOps knowledge with experience in containerization, CI/CD pipelines, and cloud infrastructure management. Solid background in AWS and Kubernetes.',
        ai_chat_summary: 'Shows good problem-solving approach and attention to detail. Demonstrates understanding of automation and infrastructure as code principles. Good cultural fit.',
        ai_recommend_status: 'RECOMMEND'
      },
      hr_decision: 'INTERVIEW',
      hr_comment: 'Good technical skills but need to assess soft skills and team fit through in-person interview.'
    },
    {
      application_id: '3',
      candidate_name: 'DevOps25 Bot',
      email: 'devops25@team1.com',
      job_title: 'Automation Specialist',
      status: 'SHORTLISTED',
      submission_timestamp: '2024-01-13T09:15:00Z',
      final_score: 85,
      avatarColor: 'blue',
      assessment: {
        resume_score: 88,
        chat_score: 82,
        final_score: 85,
        ai_resume_analysis: 'Very strong automation and scripting skills. Demonstrates expertise in various automation tools and frameworks with practical implementation experience. Impressive portfolio of automation projects.',
        ai_chat_summary: 'Good analytical thinking and systematic approach to problem-solving. Shows ability to optimize processes and improve efficiency through automation. Communicates technical concepts clearly.',
        ai_recommend_status: 'CONDITIONAL'
      },
      hr_decision: 'FOLLOW_UP',
      hr_comment: 'Excellent automation skills. Need to follow up on availability and salary expectations before making final decision.'
    },
    {
      application_id: '4',
      candidate_name: 'AET-DevOps-Bot',
      email: 'aet.devops@team1.com',
      job_title: 'Infrastructure Engineer',
      status: 'SUBMITTED',
      submission_timestamp: '2024-01-16T16:45:00Z',
      final_score: null,
      avatarColor: 'gray',
      hr_decision: 'PENDING',
      hr_comment: 'Initial application received. Waiting for assessment completion before review.'
    }
  ]);

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
    let filtered = applications;

    if (selectedStatus !== 'ALL') {
      filtered = filtered.filter(app => app.status === selectedStatus);
    }

    if (searchTerm) {
      filtered = filtered.filter(app =>
        app.candidate_name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        app.job_title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        app.email.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    return filtered.sort((a, b) => {
      let aVal: SortValue, bVal: SortValue;

      switch (sortField) {
        case 'name':
          aVal = a.candidate_name.toLowerCase();
          bVal = b.candidate_name.toLowerCase();
          break;
        case 'job':
          aVal = a.job_title.toLowerCase();
          bVal = b.job_title.toLowerCase();
          break;
        case 'status':
          aVal = a.status;
          bVal = b.status;
          break;
        case 'date':
          aVal = new Date(a.submission_timestamp);
          bVal = new Date(b.submission_timestamp);
          break;
        case 'score':
          aVal = a.final_score || 0;
          bVal = b.final_score || 0;
          break;
        case 'finalScore':
          aVal = a.assessment?.final_score || 0;
          bVal = b.assessment?.final_score || 0;
          break;
        case 'hrDecision':
          aVal = a.hr_decision || 'PENDING';
          bVal = b.hr_decision || 'PENDING';
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

  const getSortIcon = (field: SortField): string => {
    if (sortField !== field) return '';
    return sortDirection === 'asc' ? ' ↑' : ' ↓';
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
      const response = await fetch(`/api/applications/${selectedApplication.application_id}/status`, {
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
    setNewHrDecision(application.hr_decision || 'PENDING');
    setIsHrDecisionModalOpen(true);
  };

  const handleHrDecisionUpdate = async () => {
    if (!selectedApplication || !newHrDecision) return;

    setIsUpdating(true);
    
    try {
      const response = await fetch(`/api/applications/${selectedApplication.application_id}/hr-decision`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          hr_decision: newHrDecision,
          updated_by: 'team1_admin'
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



  const copyEmailToClipboard = async (email: string) => {
    try {
      await navigator.clipboard.writeText(email);
      alert('Email copied to clipboard!');
    } catch (err) {
      console.error('Failed to copy email:', err);
      alert('Failed to copy email');
    }
  };

  const getAiRecommendDisplay = (status?: string) => {
    switch (status) {
      case 'RECOMMEND': return 'Recommend';
      case 'NOT_RECOMMEND': return 'Not Recommend';
      case 'CONDITIONAL': return 'Conditional';
      default: return 'Pending';
    }
  };

  const getAiRecommendColor = (status?: string) => {
    switch (status) {
      case 'RECOMMEND': return '#27ae60';
      case 'NOT_RECOMMEND': return '#e74c3c';
      case 'CONDITIONAL': return '#f39c12';
      default: return '#95a5a6';
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

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUBMITTED': return '#3498db';
      case 'AI_SCREENING': return '#f39c12';
      case 'AI_INTERVIEW': return '#e67e22';
      case 'COMPLETED': return '#9b59b6';
      case 'SHORTLISTED': return '#27ae60';
      case 'REJECTED': return '#e74c3c';
      case 'HIRED': return '#2ecc71';
      default: return '#95a5a6';
    }
  };

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

  const getHrDecisionColor = (decision?: string) => {
    switch (decision) {
      case 'PENDING': return '#95a5a6';
      case 'APPROVE': return '#27ae60';
      case 'REJECT': return '#e74c3c';
      case 'INTERVIEW': return '#3498db';
      case 'FOLLOW_UP': return '#f39c12';
      default: return '#95a5a6';
    }
  };

  return (
    <div className="dashboard-container">
      <div className="sidebar">
        <div className="logo">TEAM1</div>
        
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
              <span>
                {option.value === 'ALL' 
                  ? applications.length 
                  : applications.filter(app => app.status === option.value).length
                }
              </span>
            </div>
          ))}
        </div>

        <div className="sidebar-section">
          <div className="sidebar-title">Sort & Filter</div>
          <div className="filter-controls">
            <select 
              className="filter-select"
              value={sortField}
              onChange={(e) => setSortField(e.target.value as SortField)}
            >
              <option value="date">Sort by Date</option>
              <option value="name">Sort by Name</option>
              <option value="job">Sort by Job</option>
              <option value="status">Sort by Status</option>
              <option value="score">Sort by Score</option>
              <option value="finalScore">Sort by Final Score</option>
              <option value="hrDecision">Sort by HR Decision</option>
            </select>
            <button 
              className="sort-direction-btn"
              onClick={() => setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc')}
            >
              {sortDirection === 'asc' ? '↑' : '↓'}
            </button>
          </div>
        </div>
      </div>

      <div className="main-content">
        <header className="dashboard-header">
          <div className="search-bar-container">
            <input 
              type="text" 
              placeholder="Search candidates, jobs, emails..." 
              className="search-input"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
            <span className="results-count">{filteredAndSortedApplications.length} applications</span>
          </div>
          <div className="header-actions">
            <div className="user-moon">TEAM1 ADMIN</div>
          </div>
        </header>

        <div className="candidate-list">
          <div className="candidate-list-header">
            <div 
              className="name-company-header sortable"
              onClick={() => handleSort('name')}
              onKeyDown={(e) => e.key === 'Enter' && handleSort('name')}
              role="button"
              tabIndex={0}
              style={{ flexBasis: '25%' }}
            >
              CANDIDATE / JOB {getSortIcon('name')}
            </div>
            <div 
              className="jobs-header sortable"
              onClick={() => handleSort('status')}
              onKeyDown={(e) => e.key === 'Enter' && handleSort('status')}
              role="button"
              tabIndex={0}
              style={{ flexBasis: '10%' }}
            >
              STATUS {getSortIcon('status')}
            </div>
            <div 
              className="assessment-header sortable"
              onClick={() => handleSort('finalScore')}
              onKeyDown={(e) => e.key === 'Enter' && handleSort('finalScore')}
              role="button"
              tabIndex={0}
              style={{ flexBasis: '15%' }}
            >
              SCORES {getSortIcon('finalScore')}
            </div>
            <div 
              className="ai-recommend-header"
              style={{ flexBasis: '12%' }}
            >
              AI RECOMMEND
            </div>
            <div 
              className="hr-decision-header sortable"
              onClick={() => handleSort('hrDecision')}
              onKeyDown={(e) => e.key === 'Enter' && handleSort('hrDecision')}
              role="button"
              tabIndex={0}
              style={{ flexBasis: '12%' }}
            >
              HR DECISION {getSortIcon('hrDecision')}
            </div>
            <div 
              className="details-header"
              style={{ flexBasis: '18%' }}
            >
              DETAILS
            </div>
            <div 
              className="stage-header sortable"
              onClick={() => handleSort('date')}
              style={{ flexBasis: '8%' }}
            >
              DATE {getSortIcon('date')}
            </div>
          </div>
          
          {filteredAndSortedApplications.map(application => (
            <div key={application.application_id} className="candidate-item">
              <div className="name-company" style={{ flexBasis: '25%' }}>
                <span className={`avatar-dot ${application.avatarColor}`}></span>
                <div className="candidate-info">
                  <span className="candidate-name">
                    {application.candidate_name}
                    <span 
                      style={{ 
                        marginLeft: '8px', 
                        cursor: 'pointer', 
                        color: '#3498db',
                        fontSize: '12px'
                      }}
                      onClick={() => copyEmailToClipboard(application.email)}
                      title={`Copy email: ${application.email}`}
                    >
                      📧
                    </span>
                  </span>
                  <span className="candidate-company">
                    {application.job_title}
                  </span>
                </div>
              </div>
              
              <div className="jobs" style={{ flexBasis: '10%' }}>
                <span 
                  className="status-badge clickable"
                  style={{ 
                    backgroundColor: getStatusColor(application.status),
                    color: 'white',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '11px',
                    fontWeight: 'bold',
                    cursor: 'pointer'
                  }}
                  onClick={() => handleStatusClick(application)}
                  title="Click to change status"
                >
                  {getStatusDisplay(application.status)}
                </span>
              </div>
              
              <div className="assessment-scores" style={{ flexBasis: '15%' }}>
                {application.assessment ? (
                  <div style={{ fontSize: '12px' }}>
                    <div>Resume: {application.assessment.resume_score}</div>
                    <div>Chat: {application.assessment.chat_score}</div>
                    <div style={{ fontWeight: 'bold' }}>Final: {application.assessment.final_score}</div>
                  </div>
                ) : (
                  <span style={{ color: '#95a5a6' }}>Pending</span>
                )}
              </div>
              
              <div className="ai-recommend" style={{ flexBasis: '12%' }}>
                {application.assessment ? (
                  <div style={{ fontSize: '12px' }}>
                    <span 
                      style={{ 
                        backgroundColor: getAiRecommendColor(application.assessment.ai_recommend_status),
                        color: 'white',
                        padding: '4px 8px',
                        borderRadius: '4px',
                        fontSize: '10px',
                        fontWeight: 'bold'
                      }}
                    >
                      {getAiRecommendDisplay(application.assessment.ai_recommend_status)}
                    </span>
                  </div>
                ) : (
                  <span style={{ color: '#95a5a6' }}>No recommendation</span>
                )}
              </div>
              
              <div className="hr-decision" style={{ flexBasis: '12%' }}>
                <span 
                  className="status-badge clickable"
                  style={{ 
                    backgroundColor: getHrDecisionColor(application.hr_decision),
                    color: 'white',
                    padding: '4px 6px',
                    borderRadius: '4px',
                    fontSize: '10px',
                    fontWeight: 'bold',
                    cursor: 'pointer'
                  }}
                  onClick={() => handleHrDecisionClick(application)}
                  title="Click to change HR decision"
                >
                  {getHrDecisionDisplay(application.hr_decision)}
                </span>
              </div>
              
              <div className="details" style={{ flexBasis: '18%' }}>
                <button 
                  style={{
                    backgroundColor: '#3498db',
                    color: 'white',
                    border: 'none',
                    padding: '6px 12px',
                    borderRadius: '4px',
                    fontSize: '12px',
                    cursor: 'pointer'
                  }}
                  onClick={() => {
                    setSelectedApplication(application);
                    setIsDetailsModalOpen(true);
                  }}
                >
                  View Details
                </button>
              </div>
              
              <div className="stage" style={{ flexBasis: '8%' }}>
                <div style={{ fontSize: '12px' }}>
                  {new Date(application.submission_timestamp).toLocaleDateString()}
                </div>
              </div>
            </div>
          ))}
        </div>

        {filteredAndSortedApplications.length === 0 && (
          <div className="empty-state" style={{ 
            textAlign: 'center', 
            padding: '40px', 
            color: '#7f8c8d' 
          }}>
            <p>No applications found matching your criteria.</p>
          </div>
        )}
      </div>

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
                    { value: 'PENDING', label: 'Pending Review', icon: '⏳', description: 'Awaiting HR review' },
                    { value: 'APPROVE', label: 'Approved', icon: '✅', description: 'Candidate approved for hire' },
                    { value: 'REJECT', label: 'Rejected', icon: '❌', description: 'Candidate rejected' },
                    { value: 'INTERVIEW', label: 'Schedule Interview', icon: '📅', description: 'Schedule additional interview' },
                    { value: 'FOLLOW_UP', label: 'Follow Up', icon: '📞', description: 'Requires follow-up action' }
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

      {isDetailsModalOpen && selectedApplication && (
        <div className="modal-overlay" onClick={handleDetailsModalClose}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '700px' }}>
            <div className="modal-header">
              <h3>Candidate Details - {selectedApplication.candidate_name}</h3>
              <button className="modal-close" onClick={handleDetailsModalClose}>×</button>
            </div>
            
            <div className="modal-body">
              <div className="candidate-details">
                <h4>{selectedApplication.candidate_name}</h4>
                <p>{selectedApplication.job_title}</p>
                <p>Email: {selectedApplication.email}</p>
                <p>Status: <span className="status-highlight">{getStatusDisplay(selectedApplication.status)}</span></p>
                <p>HR Decision: <span className="status-highlight">{getHrDecisionDisplay(selectedApplication.hr_decision)}</span></p>
                
                {selectedApplication.assessment && (
                  <div style={{ marginTop: '20px' }}>
                    <div style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
                      <div>
                        <strong>Resume Score:</strong> {selectedApplication.assessment.resume_score}
                      </div>
                      <div>
                        <strong>Chat Score:</strong> {selectedApplication.assessment.chat_score}
                      </div>
                      <div>
                        <strong>Final Score:</strong> {selectedApplication.assessment.final_score}
                      </div>
                      <div>
                        <strong>AI Recommendation:</strong> 
                        <span style={{ 
                          marginLeft: '8px',
                          backgroundColor: getAiRecommendColor(selectedApplication.assessment.ai_recommend_status),
                          color: 'white',
                          padding: '2px 6px',
                          borderRadius: '4px',
                          fontSize: '12px'
                        }}>
                          {getAiRecommendDisplay(selectedApplication.assessment.ai_recommend_status)}
                        </span>
                      </div>
                    </div>
                    
                    <div style={{ marginBottom: '20px' }}>
                      <h5 style={{ color: '#2c3e50', marginBottom: '10px' }}>AI Resume Analysis:</h5>
                      <div style={{ 
                        padding: '12px', 
                        backgroundColor: '#f8f9fa', 
                        borderRadius: '4px',
                        lineHeight: '1.5',
                        fontSize: '14px',
                        border: '1px solid #e9ecef'
                      }}>
                        {selectedApplication.assessment.ai_resume_analysis}
                      </div>
                    </div>
                    
                    <div style={{ marginBottom: '20px' }}>
                      <h5 style={{ color: '#2c3e50', marginBottom: '10px' }}>AI Chat Summary:</h5>
                      <div style={{ 
                        padding: '12px', 
                        backgroundColor: '#f8f9fa', 
                        borderRadius: '4px',
                        lineHeight: '1.5',
                        fontSize: '14px',
                        border: '1px solid #e9ecef'
                      }}>
                        {selectedApplication.assessment.ai_chat_summary}
                      </div>
                    </div>
                  </div>
                )}
                
                {selectedApplication.hr_comment && (
                  <div style={{ marginTop: '20px' }}>
                    <h5 style={{ color: '#2c3e50', marginBottom: '10px' }}>HR Comment:</h5>
                    <div style={{ 
                      padding: '12px', 
                      backgroundColor: '#fff3cd', 
                      borderRadius: '4px',
                      lineHeight: '1.5',
                      fontSize: '14px',
                      border: '1px solid #ffeaa7'
                    }}>
                      {selectedApplication.hr_comment}
                    </div>
                  </div>
                )}
              </div>
            </div>
            
            <div className="modal-footer">
              <button 
                className="btn-cancel" 
                onClick={handleDetailsModalClose}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dashboard; 