import React from 'react';
import { Box, Typography } from '@mui/material';
import ApplicationList from './ApplicationList';
import type { Application, SortField, SortDirection } from '../types/dashboard';

interface ApplicationsViewProps {
  applications: Application[];
  sortField: SortField;
  sortDirection: SortDirection;
  isLoading: boolean;
  onSort: (field: SortField) => void;
  onStatusClick: (application: Application) => void;
  onHrDecisionClick: (application: Application) => void;
  onDetailsClick: (application: Application) => void;
  onRefreshApplications: () => void;
}

const ApplicationsView: React.FC<ApplicationsViewProps> = ({
  applications,
  sortField,
  sortDirection,
  isLoading,
  onSort,
  onStatusClick,
  onHrDecisionClick,
  onDetailsClick,
  onRefreshApplications
}) => {
  if (isLoading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <Typography color="text.secondary">Loading applications...</Typography>
      </Box>
    );
  }

  if (applications.length === 0) {
    return (
      <Box sx={{ 
        display: 'flex', 
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        p: 6,
        textAlign: 'center'
      }}>
        <Typography variant="h4" sx={{ mb: 2 }}>📋</Typography>
        <Typography variant="h6" sx={{ mb: 1 }}>No Applications Found</Typography>
        <Typography variant="body2" color="text.secondary">
          No applications match your current search criteria.
        </Typography>
      </Box>
    );
  }

  return (
    <ApplicationList
      applications={applications}
      sortField={sortField}
      sortDirection={sortDirection}
      onSort={onSort}
      onStatusClick={onStatusClick}
      onHrDecisionClick={onHrDecisionClick}
      onDetailsClick={onDetailsClick}
      onRefreshApplications={onRefreshApplications}
    />
  );
};

export default ApplicationsView;