import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Snackbar, Alert } from '@mui/material';
import {
  CloudUpload as CloudUploadIcon,
  Send as SendIcon,
  Check as CheckIcon,
  Visibility as VisibilityIcon,
  Logout as LogoutIcon,
  Description as DescriptionIcon
} from '@mui/icons-material';
import type { Job, SortField, SortDirection, ViewMode } from '../types/dashboard';
import { useJobs } from '../hooks/useJobs';
import { useApplications, useApplicationFiltering } from '../hooks/useApplications';
import { logout, getValidToken } from '../utils/auth';
import Sidebar from './Sidebar';
import CandidateApplicationList from './CandidateApplicationList';
import apiConfig from '../utils/api';
import './Dashboard.css';

const CandidateDashboard: React.FC = () => {
  const navigate = useNavigate();
  const [selectedView, setSelectedView] = useState<ViewMode>('jobs');

  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: 'success' | 'error' | 'warning' | 'info';
  }>({
    open: false,
    message: '',
    severity: 'info'
  });

  const { jobs, isJobsLoading, refreshJobs } = useJobs(selectedView);

  const [selectedStatus] = useState<string>('ALL');
  const [searchTerm] = useState<string>('');
  const [sortField, setSortField] = useState<SortField>('date');
  const [sortDirection, setSortDirection] = useState<SortDirection>('desc');

  const {
    applications,
    isLoadingApplications,
    fetchApplications,
  } = useApplications(selectedStatus, 0, 20, undefined);

  const filteredApplications = useApplicationFiltering(
    applications,
    selectedStatus,
    searchTerm,
    sortField,
    sortDirection
  );

  useEffect(() => {
    if (selectedView === 'applications') {
      fetchApplications(0, 20, undefined, selectedStatus);
    }
  }, [selectedView, selectedStatus]);

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      setSortDirection((prev) => (prev === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortField(field);
      setSortDirection('asc');
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const hasAppliedToJob = (jobId: string) => {
    return applications.some(app =>
      app.jobId === jobId && app.status !== 'REJECTED'
    );
  };

  const getJobApplicationStatus = (jobId: string) => {
    const application = applications.find(app =>
      app.jobId === jobId
    );
    return application?.status;
  };

  const showSnackbar = (message: string, severity: 'success' | 'error' | 'warning' | 'info' = 'info') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };


  const validateFile = (file: File): string | null => {
    const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];
    if (!allowedTypes.includes(file.type)) {
      return 'Please upload a PDF, DOC, or DOCX file only.';
    }

    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      return 'File size must be less than 10MB.';
    }

    return null;
  };

  const applyForJob = async (job: Job, resumeFile?: File) => {
    const token = getValidToken();
    if (!token) {
      showSnackbar('Please login again to submit an application.', 'error');
      navigate('/login');
      return;
    }

    if (resumeFile) {
      const fileError = validateFile(resumeFile);
      if (fileError) {
        showSnackbar(fileError, 'error');
        return;
      }
    }

    try {

      if (resumeFile) {
        try {
          const actualJobId = job.jobId;

          if (!actualJobId) {
            throw new Error('No valid job ID found in job object for simple request');
          }
          let formdata = new FormData()
          formdata.append("jobId", actualJobId)
          formdata.append("resumeFile", resumeFile, "");

          let myHeaders = new Headers();
          myHeaders.append("Authorization", `Bearer ${token}`);
          // myHeaders.append("Content-Type", "multipart/form-data");

          var requestOptions = {
            method: 'POST',
            headers: myHeaders,
            body: formdata,
            credentials: 'include' as RequestCredentials
          };


          let simpleRes = await fetch(apiConfig.getFullURL('/api/v1/applications'), requestOptions);


          if (simpleRes.ok) {
            showSnackbar('Application submitted successfully!', 'success');
            setSelectedView('applications');
            fetchApplications();
            refreshJobs();
            return;
          }
        } catch (simpleErr) {
          console.error('Simple request also failed:', simpleErr);
        }
      }

      let requestBody;
      let requestHeaders;

      if (resumeFile) {
        const formData = new FormData();

        const actualJobId = job.jobId;
        console.log('Using job ID:', actualJobId);

        if (!actualJobId) {
          throw new Error('No valid job ID found in job object');
        }

        formData.append('jobId', actualJobId);
        formData.append('resumeFile', resumeFile);

        console.log('Using FormData approach with file');

        console.log('FormData contents:');
        for (const [key, value] of formData.entries()) {
          if (value instanceof File) {
            console.log(`${key}: File(${value.name}, ${value.type}, ${value.size} bytes)`);
          } else {
            console.log(`${key}: ${value}`);
          }
        }

        requestBody = formData;
        requestHeaders = {
          Authorization: `Bearer ${token}`,
        };
      } else {
        const actualJobId = job.jobId;
        console.log('Using job ID for JSON:', actualJobId);

        if (!actualJobId) {
          throw new Error('No valid job ID found in job object');
        }

        console.log('Using JSON approach without file');
        requestBody = JSON.stringify({ jobId: actualJobId });
        requestHeaders = {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json'
        };
      }

      console.log('Making request to /api/v1/applications...');
      const res = await fetch(apiConfig.getFullURL('/api/v1/applications'), {
        method: 'POST',
        headers: requestHeaders,
        body: requestBody,
        credentials: 'include'
      });

      console.log('Final request URL:', res.url);
      console.log('Request headers:', requestHeaders);
      console.log('Request body type:', typeof requestBody);
      console.log('Request body instanceof FormData:', requestBody instanceof FormData);

      console.log('Response status:', res.status);
      console.log('Response headers:', Object.fromEntries(res.headers.entries()));

      const responseText = await res.text();
      console.log('Raw response text:', responseText);

      let data;
      try {
        data = JSON.parse(responseText);
        console.log('Parsed response data:', data);
      } catch (parseError) {
        console.error('Failed to parse response as JSON:', parseError);
        throw new Error(`Server returned non-JSON response: ${responseText}`);
      }

      if (res.ok && data?.success) {
        showSnackbar('Application submitted successfully!', 'success');
        setSelectedView('applications');
        fetchApplications();
        refreshJobs();
      } else {
        const errorMessage = data?.message || `Failed to apply (HTTP ${res.status})`;
        showSnackbar(errorMessage, 'error');
        console.error('Application failed:', errorMessage);
      }
    } catch (e) {
      console.error('Error applying for job:', e);
      showSnackbar('Error submitting application. Please try again.', 'error');
    }
  };

  return (
    <div className="dashboard-container">
      <Sidebar />
      <div className="main-content">
        <div
          className="dashboard-header"
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '16px 24px',
          }}
        >
          <div>
            <Button
              variant={selectedView === 'jobs' ? 'contained' : 'outlined'}
              onClick={() => setSelectedView('jobs')}
              sx={{
                marginRight: 1,
                bgcolor: selectedView === 'jobs' ? '#1a1a1a' : 'transparent',
                color: selectedView === 'jobs' ? '#fff' : '#1a1a1a',
                borderColor: '#1a1a1a',
                '&:hover': {
                  bgcolor: selectedView === 'jobs' ? '#333' : '#f5f5f5'
                }
              }}
              startIcon={<DescriptionIcon />}
            >
              Jobs
            </Button>
            <Button
              variant={selectedView === 'applications' ? 'contained' : 'outlined'}
              onClick={() => setSelectedView('applications')}
              sx={{
                bgcolor: selectedView === 'applications' ? '#1a1a1a' : 'transparent',
                color: selectedView === 'applications' ? '#fff' : '#1a1a1a',
                borderColor: '#1a1a1a',
                '&:hover': {
                  bgcolor: selectedView === 'applications' ? '#333' : '#f5f5f5'
                }
              }}
              startIcon={<CheckIcon />}
            >
              Applications
            </Button>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div className="user-moon">CANDIDATE</div>
            <Button
              variant="contained"
              color="error"
              onClick={handleLogout}
              startIcon={<LogoutIcon />}
              sx={{
                textTransform: 'none',
                borderRadius: 1
              }}
            >
              Logout
            </Button>
          </div>
        </div>

        {selectedView === 'jobs' && (
          <div style={{ padding: 24 }}>
            {isJobsLoading && <p>Loading jobs…</p>}
            {!isJobsLoading &&
              jobs
                .filter((j) => j.status === 'OPEN')
                .map((job) => (
                  <div
                    key={job.jobId}
                    style={{
                      display: 'flex',
                      alignItems: 'flex-start',
                      justifyContent: 'space-between',
                      padding: '20px 16px',
                      borderBottom: '1px solid #ddd',
                      backgroundColor: hasAppliedToJob(job.jobId) ? '#f0f8ff' : '#fafafa',
                      marginBottom: '8px',
                      borderRadius: '8px',
                      border: hasAppliedToJob(job.jobId) ? '2px solid #3498db' : '1px solid #e0e0e0',
                      position: 'relative'
                    }}
                  >
                    <div style={{ flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                        <h3 style={{ margin: 0, fontSize: '16px', color: '#2c3e50' }}>
                          {job.title}
                        </h3>
                        {hasAppliedToJob(job.jobId) && (
                          <span style={{
                            backgroundColor: '#2ecc71',
                            color: 'white',
                            padding: '4px 8px',
                            borderRadius: '12px',
                            fontSize: '11px',
                            fontWeight: 'bold',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px'
                          }}>
                            ✓ Applied
                          </span>
                        )}
                      </div>
                      {job.description && (
                        <p style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#666', lineHeight: '1.4' }}>
                          {job.description.length > 150 ? `${job.description.substring(0, 150)}...` : job.description}
                        </p>
                      )}
                      {hasAppliedToJob(job.jobId) && (
                        <div style={{ fontSize: '12px', color: '#3498db', fontWeight: '500' }}>
                          Status: {getJobApplicationStatus(job.jobId)?.replace('_', ' ')}
                        </div>
                      )}
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'flex-end' }}>
                      <input
                        type="file"
                        accept=".pdf,.doc,.docx"
                        id={`file-${job.jobId}`}
                        style={{ display: 'none' }}
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (file) {
                            if (window.confirm(`Apply for "${job.title}" with resume "${file.name}"?`)) {
                              applyForJob(job, file);
                            }
                          }
                          e.target.value = '';
                        }}
                      />
                      {hasAppliedToJob(job.jobId) ? (
                        <>
                          <Button
                            disabled
                            variant="contained"
                            startIcon={<CheckIcon />}
                            sx={{
                              bgcolor: '#95a5a6',
                              color: '#fff',
                              marginBottom: 0.5,
                              textTransform: 'none',
                              cursor: 'not-allowed',
                              '&:disabled': {
                                bgcolor: '#95a5a6',
                                color: '#fff',
                                opacity: 0.7
                              }
                            }}
                            title="You have already applied to this job"
                          >
                            Already Applied
                          </Button>
                          <Button
                            variant="outlined"
                            color="primary"
                            size="small"
                            startIcon={<VisibilityIcon />}
                            onClick={() => {
                              setSelectedView('applications');
                            }}
                            sx={{
                              textTransform: 'none',
                              fontSize: '12px'
                            }}
                            title="View your application status"
                          >
                            View Application
                          </Button>
                        </>
                      ) : (
                        <>
                          <Button
                            variant="contained"
                            color="success"
                            startIcon={<CloudUploadIcon />}
                            onClick={() => {
                              const fileInput = document.getElementById(`file-${job.jobId}`) as HTMLInputElement;
                              fileInput?.click();
                            }}
                            sx={{
                              marginBottom: 0.5,
                              textTransform: 'none',
                              bgcolor: '#2ecc71',
                              '&:hover': {
                                bgcolor: '#27ae60'
                              }
                            }}
                            title="Click to select your resume (PDF, DOC, DOCX)"
                          >
                            Apply with Resume
                          </Button>
                          <Button
                            variant="outlined"
                            color="primary"
                            size="small"
                            startIcon={<SendIcon />}
                            onClick={() => {
                              if (window.confirm(`Apply for "${job.title}" without resume?`)) {
                                applyForJob(job);
                              }
                            }}
                            sx={{
                              textTransform: 'none',
                              fontSize: '12px'
                            }}
                            title="Apply without uploading resume"
                          >
                            Quick Apply
                          </Button>
                        </>
                      )}
                      <div style={{ fontSize: '12px', color: '#666', textAlign: 'right' }}>
                        Upload PDF, DOC, or DOCX (max 10MB)
                      </div>
                    </div>
                  </div>
                ))}
            {!isJobsLoading && jobs.filter((j) => j.status === 'OPEN').length === 0 && (
              <p>No open jobs at the moment.</p>
            )}
          </div>
        )}

        {selectedView === 'applications' && (
          <div style={{ padding: 24 }}>
            {isLoadingApplications && <p>Loading applications…</p>}
            {!isLoadingApplications && (
              <CandidateApplicationList
                applications={filteredApplications}
                sortField={sortField}
                sortDirection={sortDirection}
                onSort={handleSort}
                onRefreshApplications={() => {
                  fetchApplications();
                }}
              />
            )}
          </div>
        )}
      </div>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          variant="filled"
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
};

export default CandidateDashboard; 