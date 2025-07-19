import { useState, useEffect, useMemo } from 'react';
import type { Application, SortField, SortDirection, SortValue } from '../types/dashboard';
import apiConfig from '../utils/api';

export const useApplications = (selectedStatus: string, currentPage: number = 0, pageSize: number = 20, jobId?: string) => {
  const [applications, setApplications] = useState<Application[]>([]);
  const [totalApplications, setTotalApplications] = useState(0);
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
            avatarColor: ['blue', 'gray', 'green', 'purple', 'orange'][Math.floor(Math.random() * 5)],
            hr_decision: app.hrDecision,
            hr_comment: app.hrComments,
            assessment: app.assessment ? {
              resume_score: app.assessment.resumeScore,
              chat_score: app.assessment.chatScore,
              final_score: app.assessment.finalScore,
              ai_resume_analysis: app.assessment.aiResumeAnalysis,
              ai_chat_summary: app.assessment.aiChatSummary,
              ai_recommend_status: app.assessment.aiRecommendStatus
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
    fetchApplications(currentPage, pageSize, jobId, selectedStatus);
  }, [currentPage, pageSize, selectedStatus, jobId]);

  const updateApplicationStatus = async (applicationId: string, status: string) => {
    try {
      const response = await fetch(apiConfig.getFullURL(`/api/applications/${applicationId}/status`), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          status: status,
          updated_by: 'team1_admin'
        }),
      });

      if (response.ok) {
        setApplications(prevApplications =>
          prevApplications.map(app =>
            app.application_id === applicationId
              ? { ...app, status: status }
              : app
          )
        );
        return true;
      } else {
        console.error('Failed to update status');
        return false;
      }
    } catch (error) {
      console.error('Error updating status:', error);
      setApplications(prevApplications =>
        prevApplications.map(app =>
          app.application_id === applicationId
            ? { ...app, status: status }
            : app
        )
      );
      return false;
    }
  };

  const updateHrDecision = async (applicationId: string, hrDecision: string) => {
    try {
      const response = await fetch(apiConfig.getFullURL(`/api/applications/${applicationId}/hr-decision`), {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          hr_decision: hrDecision,
          updated_by: 'team1_admin'
        }),
      });

      if (response.ok) {
        setApplications(prevApplications =>
          prevApplications.map(app =>
            app.application_id === applicationId
              ? { ...app, hr_decision: hrDecision }
              : app
          )
        );
        return true;
      } else {
        console.error('Failed to update HR decision');
        return false;
      }
    } catch (error) {
      console.error('Error updating HR decision:', error);
      setApplications(prevApplications =>
        prevApplications.map(app =>
          app.application_id === applicationId
            ? { ...app, hr_decision: hrDecision }
            : app
        )
      );
      return false;
    }
  };

  return {
    applications,
    totalApplications,
    isLoadingApplications,
    fetchApplications,
    updateApplicationStatus,
    updateHrDecision,
    setApplications
  };
};

export const useApplicationFiltering = (
  applications: Application[], 
  selectedStatus: string, 
  searchTerm: string, 
  sortField: SortField, 
  sortDirection: SortDirection
) => {
  return useMemo(() => {
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
};