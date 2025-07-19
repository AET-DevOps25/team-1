import React from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Button, 
  Paper, 
  Typography, 
  Box, 
  Tabs, 
  Tab, 
  Chip,
  TextField
} from '@mui/material';
import { 
  People as PeopleIcon,
  Work as WorkIcon,
  PersonAdd as PersonAddIcon,
  Add as AddIcon,
  Dashboard as DashboardIcon,
  Logout as LogoutIcon
} from '@mui/icons-material';
import type { ViewMode } from '../types/dashboard';
import { logout } from '../utils/auth';

interface DashboardHeaderProps {
  selectedView: ViewMode;
  searchTerm: string;
  resultsCount: number;
  onViewChange: (view: ViewMode) => void;
  onSearchChange: (term: string) => void;
  onAddHrClick: () => void;
  onCreateJobClick: () => void;
}

const DashboardHeader: React.FC<DashboardHeaderProps> = ({
  selectedView,
  searchTerm,
  resultsCount,
  onViewChange,
  onSearchChange,
  onAddHrClick,
  onCreateJobClick
}) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <Paper elevation={1} sx={{ mb: 3 }}>
      <Box sx={{ borderBottom: 1, borderColor: 'divider', px: 3, pt: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <DashboardIcon sx={{ fontSize: 28, color: 'primary.main' }} />
            <Typography variant="h5" component="h1" sx={{ fontWeight: 'bold' }}>
              HR Dashboard
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Chip 
              label="TEAM1 ADMIN" 
              color="primary" 
              variant="outlined"
              sx={{ fontWeight: 'bold' }}
            />
            <Button 
              variant="contained"
              color="success"
              startIcon={<PersonAddIcon />}
              onClick={onAddHrClick}
              sx={{ textTransform: 'none' }}
            >
              Add HR
            </Button>
            <Button 
              variant="contained"
              color="error"
              startIcon={<LogoutIcon />}
              onClick={handleLogout}
              sx={{ textTransform: 'none' }}
            >
              Logout
            </Button>
          </Box>
        </Box>
        
        <Tabs 
          value={selectedView === 'applications' ? 0 : 1} 
          onChange={(_, newValue) => onViewChange(newValue === 0 ? 'applications' : 'jobs')}
          sx={{ mb: 2 }}
        >
          <Tab 
            icon={<PeopleIcon />} 
            label={`Applications (${resultsCount})`} 
            iconPosition="start"
            sx={{ textTransform: 'none', fontWeight: 'bold' }}
          />
          <Tab 
            icon={<WorkIcon />} 
            label="Jobs" 
            iconPosition="start"
            sx={{ textTransform: 'none', fontWeight: 'bold' }}
          />
        </Tabs>
      </Box>
      
      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 3 }}>
          <TextField
            placeholder="Search candidates, jobs, emails..."
            value={searchTerm}
            onChange={(e) => onSearchChange(e.target.value)}
            size="small"
            sx={{ minWidth: 300 }}
          />
          <Chip 
            label={`${resultsCount} results`} 
            color="default" 
            size="small"
          />
          {selectedView === 'jobs' && (
            <Button 
              variant="contained"
              color="success"
              startIcon={<AddIcon />}
              onClick={onCreateJobClick}
              sx={{ textTransform: 'none' }}
            >
              Create Job
            </Button>
          )}
        </Box>
      </Box>
    </Paper>
  );
};

export default DashboardHeader;