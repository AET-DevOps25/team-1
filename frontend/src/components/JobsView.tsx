import React from 'react';
import { Box, Typography } from '@mui/material';
import JobsList from './JobsList';
import type { Job } from '../types/dashboard';

interface JobsViewProps {
  jobs: Job[];
  isLoading: boolean;
  onEditJob: (job: Job) => void;
  onToggleJobStatus: (job: Job) => void;
  onRefreshJobs: () => void;
}

const JobsView: React.FC<JobsViewProps> = ({
  jobs,
  isLoading,
  onEditJob,
  onToggleJobStatus,
  onRefreshJobs
}) => {
  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <Typography color="text.secondary">Loading jobs...</Typography>
      </Box>
    );
  }

  if (jobs.length === 0) {
    return (
      <Box sx={{ 
        display: 'flex', 
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        p: 6,
        textAlign: 'center'
      }}>
        <Typography variant="h4" sx={{ mb: 2 }}>💼</Typography>
        <Typography variant="h6" sx={{ mb: 1 }}>No Jobs Found</Typography>
        <Typography variant="body2" color="text.secondary">
          Create your first job posting to get started.
        </Typography>
      </Box>
    );
  }

  return (
    <JobsList
      jobs={jobs}
      isLoading={isLoading}
      onEditJob={onEditJob}
      onToggleJobStatus={onToggleJobStatus}
      onRefresh={onRefreshJobs}
    />
  );
};

export default JobsView;