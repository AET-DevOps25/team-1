import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiConfig from '../utils/api';
import './Login.css';

interface ModalProps {
  isOpen: boolean;
  message: string;
  onClose: () => void;
}

const Modal: React.FC<ModalProps> = ({ isOpen, message, onClose }) => {
  if (!isOpen) return null;

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <p>{message}</p>
        <button onClick={onClose} className="modal-close-button">
          Close
        </button>
      </div>
    </div>
  );
};

const Register: React.FC = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    company: ''
  });
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const navigate = useNavigate();

  const validateForm = () => {
    if (!formData.firstName || !formData.lastName || !formData.email || !formData.password || !formData.confirmPassword) {
      setError('Please fill in all required fields.');
      setIsModalOpen(true);
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('Please enter a valid email address.');
      setIsModalOpen(true);
      return false;
    }

    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters long.');
      setIsModalOpen(true);
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      setIsModalOpen(true);
      return false;
    }

    return true;
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleRegister = async () => {
    setError(null);
    setSuccess(null);
    setIsModalOpen(false);

    if (!validateForm()) {
      return;
    }

    try {
      const response = await fetch(apiConfig.getFullURL('/api/v1/auth/register'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: `${formData.firstName} ${formData.lastName}`.trim(),
          email: formData.email,
          password: formData.password
        })
      });

      const data = await response.json();

      if (response.ok && data?.success) {
        if (data.data?.accessToken) {
          const token = data.data.accessToken;
          const expires = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000).toUTCString();
          document.cookie = `auth_token=${token}; expires=${expires}; path=/; SameSite=Strict; Secure`;
          localStorage.setItem('token', token);
        }
        setSuccess('Registration successful! Redirecting...');
        setIsModalOpen(true);
        setTimeout(() => navigate('/'), 1500);
      } else {
        const backendMsg = data?.message || (data?.errors?.[0]?.message ?? 'Registration failed');
        setError(backendMsg);
        setIsModalOpen(true);
      }

    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Registration failed';
      setError(errorMessage);
      setIsModalOpen(true);
    }
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setError(null);
    setSuccess(null);
  };

  return (
    <div className="login-container">
      <div className="login-form">
        <h2>Register for HR App</h2>

        <div className="input-group">
          <input
            type="text"
            name="firstName"
            value={formData.firstName}
            onChange={handleInputChange}
            placeholder="First Name *"
            aria-label="First Name"
            required
          />
        </div>

        <div className="input-group">
          <input
            type="text"
            name="lastName"
            value={formData.lastName}
            onChange={handleInputChange}
            placeholder="Last Name *"
            aria-label="Last Name"
            required
          />
        </div>

        <div className="input-group">
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            placeholder="Email Address *"
            aria-label="Email Address"
            required
          />
        </div>

        <div className="input-group">
          <input
            type="text"
            name="company"
            value={formData.company}
            onChange={handleInputChange}
            placeholder="Company (Optional)"
            aria-label="Company"
          />
        </div>

        <div className="input-group">
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            placeholder="Password *"
            aria-label="Password"
            required
          />
        </div>

        <div className="input-group">
          <input
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleInputChange}
            placeholder="Confirm Password *"
            aria-label="Confirm Password"
            required
          />
        </div>

        <button 
          onClick={handleRegister} 
          className="login-button"
          disabled={!formData.firstName || !formData.lastName || !formData.email || !formData.password || !formData.confirmPassword}
        >
          Register
        </button>

        <div className="register-link-text" style={{ marginTop: '20px' }}> 
          Already have an account? <Link to="/login">Sign in</Link>
        </div>

      </div>
      <Modal isOpen={isModalOpen} message={error || success || ''} onClose={closeModal} />
    </div>
  );
};

export default Register; 