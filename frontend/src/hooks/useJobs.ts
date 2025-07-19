import { useState, useEffect } from 'react';
import type { Job, JobForm, ValidationError } from '../types/dashboard';
import apiConfig from '../utils/api';

export const useJobs = (selectedView: string) => {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [isJobsLoading, setIsJobsLoading] = useState(false);

  const fetchJobs = async () => {
    setIsJobsLoading(true);
    try {
      const res = await fetch(apiConfig.getFullURL('/api/v1/jobs?page=0&size=50'), {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token') || ''}` }
      });
      const data = await res.json();
      if (res.ok && data?.success) {
        const jobsArray = Array.isArray(data.data) ? data.data : data.data?.content || [];
        
        const mappedJobs = jobsArray.map((job: any) => ({
          jobId: job.jobId || job.jobID || job.id || job.job_id,
          title: job.title,
          status: job.status,
          createdAt: job.createdAt || job.created_at,
          description: job.description
        }));
        setJobs(mappedJobs);
      }
    } catch (e) {
      console.error('Failed to fetch jobs', e);
    } finally {
      setIsJobsLoading(false);
    }
  };

  useEffect(() => {
    if (selectedView === 'jobs') {
      fetchJobs();
    }
  }, [selectedView]);

  const saveJob = async (jobForm: JobForm) => {
    const token = localStorage.getItem('token') || '';
    const editing = Boolean(jobForm.jobId);
    const method = editing ? 'PATCH' : 'POST';
    const url = editing ? `/api/v1/jobs/${jobForm.jobId}` : '/api/v1/jobs';
    
    const requestBody = {
      title: jobForm.title,
      description: jobForm.description,
      status: jobForm.status
    };
    
    console.log('Saving job:', { method, url: apiConfig.getFullURL(url), body: requestBody });
    
    try {
      const res = await fetch(apiConfig.getFullURL(url), {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(requestBody)
      });
      const data = await res.json();
      console.log('Job save response:', { status: res.status, data });
      
      if (res.ok && data?.success) {
        fetchJobs(); // Refresh the list
        return { success: true, message: 'Job saved successfully' };
      } else {
        if (data?.errors && Array.isArray(data.errors)) {
          const errorMessages = data.errors.map((error: ValidationError) => 
            `${error.field}: ${error.message}`
          ).join('\n');
          return { success: false, message: errorMessages, errors: data.errors };
        }
        
        if (data?.message === 'Validation failed' || res.status === 400) {
          const formData = { title: jobForm.title, description: jobForm.description, status: jobForm.status };
          console.log('Job form data sent:', formData);
          
          let errorMessage = 'Validation failed. Please check:\n';
          if (!jobForm.title || jobForm.title.trim().length === 0) {
            errorMessage += '• Title is required\n';
          }
          if (!jobForm.description || jobForm.description.trim().length === 0) {
            errorMessage += '• Description is required\n';
          }
          if (!jobForm.status) {
            errorMessage += '• Status is required\n';
          }
          
          if (errorMessage === 'Validation failed. Please check:\n') {
            errorMessage = data?.message || 'Validation failed - please check all required fields';
          }
          
          return { success: false, message: errorMessage.trim() };
        }
        
        return { success: false, message: data?.message || 'Save failed' };
      }
    } catch (e) {
      console.error(e);
      return { success: false, message: 'Error saving job' };
    }
  };

  const toggleJobStatus = async (job: Job) => {
    const token = localStorage.getItem('token') || '';
    const endpoint = job.status === 'OPEN'
      ? `/api/v1/jobs/${job.jobId}/close`
      : `/api/v1/jobs/${job.jobId}/open`;
    
    try {
      const res = await fetch(apiConfig.getFullURL(endpoint), {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (res.ok && data?.success) {
        setJobs(prev => prev.map(j => 
          j.jobId === job.jobId 
            ? { ...j, status: job.status === 'OPEN' ? 'CLOSED' : 'OPEN' } 
            : j
        ));
        return { success: true };
      } else {
        return { success: false, message: data?.message || 'Action failed' };
      }
    } catch (e) {
      console.error(e);
      return { success: false, message: 'Action error' };
    }
  };

  const refreshJobs = () => {
    fetchJobs();
  };

  return {
    jobs,
    isJobsLoading,
    saveJob,
    toggleJobStatus,
    refreshJobs
  };
};