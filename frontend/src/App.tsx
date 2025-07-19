import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './components/Login';
import Register from './components/Register';
import Dashboard from './components/Dashboard';
import CandidateDashboard from './components/CandidateDashboard';
import CandidateChat from './components/CandidateChat';
import HrChatViewer from './components/HrChatViewer';
import JobApplications from './components/JobApplications';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route 
          path="/dashboard" 
          element={<ProtectedRoute allowedRoles={['HR']} element={<Dashboard />} />} 
        />
        <Route 
          path="/" 
          element={<ProtectedRoute allowedRoles={['CANDIDATE']} element={<CandidateDashboard />} />} 
        />
        <Route 
          path="/chat/:applicationId" 
          element={<ProtectedRoute allowedRoles={['CANDIDATE']} element={<CandidateChat />} />} 
        />
        <Route 
          path="/hr-chat/:applicationId" 
          element={<ProtectedRoute allowedRoles={['HR']} element={<HrChatViewer />} />} 
        />
        <Route 
          path="/job/:jobId/applications" 
          element={<ProtectedRoute allowedRoles={['HR']} element={<JobApplications />} />} 
        />
        <Route path="*" element={<Login />} /> 
      </Routes>
    </Router>
  );
}

export default App;
