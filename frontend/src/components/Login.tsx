import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Login.css'; 

interface ModalProps {
  isOpen: boolean;
  message: string;
  onClose: () => void;
}

const Modal: React.FC<ModalProps> = ({ isOpen, message, onClose }) => {
  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-overlay"> 
      <div className="modal-content"> 
        <p>{message}</p>
        <button onClick={onClose}>Close</button>
      </div>
    </div>
  );
};

const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const navigate = useNavigate(); 

  const validateForm = () => {
    if (!email || !password) {
      setError('Please enter both email and password.');
      setIsModalOpen(true);
      return false;
    }
    return true;
  };

  const handleLogin = async () => {
    setError(null);
    setIsModalOpen(false);

    if (!validateForm()) {
      return;
    }

    const loginData = { email, password };
    console.log('Login data:', loginData);

    try {
      alert("Logged in"); 
      navigate('/dashboard'); 

    } catch (err) {
      const errorMessage = (err instanceof Error) ? err.message : 'Error';
      setError(errorMessage);
      setIsModalOpen(true);
      console.error('Login error:', errorMessage);
    }
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setError(null);
  };

  return (
    <div className="login-container">
      <div className="login-form">
        <h2>HR App</h2>

        <div className="input-group">
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="nikola@tum.de"
            aria-label="Email"
          />
        </div>

        <div className="input-group">
          <input
            type={password ? 'password' : 'text'} 
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••••"
            aria-label="Password"
          />
        </div>
        
        <Link to="/forgot-password" className="forgot-password-link">
          Forgot password
        </Link>

        <button 
          onClick={handleLogin} 
          className="login-button"
          disabled={!email || !password} 
        >
          Sign in
        </button>

        <div className="register-link-text" style={{ marginTop: '20px' }}> 
          Don't have an account? <Link to="/register">Register</Link>
        </div>

      </div>
      <Modal isOpen={isModalOpen} message={error || ''} onClose={closeModal} />
    </div>
  );
};

export default Login; 