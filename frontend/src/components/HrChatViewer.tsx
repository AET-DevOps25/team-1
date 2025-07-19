import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiConfig from '../utils/api';
import './Login.css';

interface ChatMessage {
  messageId?: string;
  sessionId?: string;
  sender: 'AI' | 'CANDIDATE';
  content: string;
  sentAt: string;
  finished?: boolean;
}

const HrChatViewer: React.FC = () => {
  const { applicationId } = useParams();
  const navigate = useNavigate();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [candidateInfo, setCandidateInfo] = useState<any>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (applicationId) {
      initializeChatViewer();
    }
  }, [applicationId]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const getMessages = async (sessionId: string, page: number = 0, size: number = 100) => {
    try {
      const response = await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/messages?page=${page}&size=${size}`), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      if (data.success && data.data) {
        return data.data.content.map((msg: any) => ({
          messageId: msg.messageId,
          sessionId: msg.sessionId,
          sender: msg.sender,
          content: msg.content,
          sentAt: msg.sentAt
        }));
      } else {
        throw new Error(data.message || 'Failed to fetch messages');
      }
    } catch (err) {
      console.error('Error fetching messages:', err);
      return [];
    }
  };

  const initializeChatViewer = async () => {
    try {
      setIsLoading(true);
      
      const appResponse = await fetch(apiConfig.getFullURL(`/api/v1/applications/${applicationId}`), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      });

      if (appResponse.ok) {
        const appData = await appResponse.json();
        if (appData.success && appData.data) {
          setCandidateInfo({
            name: appData.data.candidate?.fullName,
            email: appData.data.candidate?.email,
            jobTitle: appData.data.job?.title
          });
        }
      }

      const response = await fetch(apiConfig.getFullURL(`/api/v1/applications/${applicationId}/chat`), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      if (data.success && data.data) {
        const sessionId = data.data.session.sessionId;
        setSessionId(sessionId);
        
        const existingMessages = await getMessages(sessionId);
        setMessages(existingMessages);
      } else {
        throw new Error(data.message || 'Failed to get chat session');
      }
    } catch (err) {
      console.error('Error initializing chat viewer:', err);
      setMessages([{
        sender: 'AI',
        content: 'No chat session found for this application.',
        sentAt: new Date().toISOString()
      }]);
    } finally {
      setIsLoading(false);
    }
  };

  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleString();
  };

  if (isLoading) {
    return (
      <div className="login-container" style={{ maxWidth: 800 }}>
        <h2>Chat History Viewer</h2>
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <div>📋 Loading chat history...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="login-container" style={{ maxWidth: 800 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Chat History Viewer (HR View)</h2>
        <button 
          onClick={() => navigate('/dashboard')}
          style={{
            padding: '8px 16px',
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Back to Dashboard
        </button>
      </div>
      
      {candidateInfo && (
        <div style={{ 
          backgroundColor: '#f8f9fa', 
          padding: '15px', 
          borderRadius: '8px', 
          marginBottom: '20px',
          border: '1px solid #e9ecef'
        }}>
          <h3 style={{ margin: '0 0 10px 0', color: '#2c3e50' }}>Application Details</h3>
          <div><strong>Candidate:</strong> {candidateInfo.name}</div>
          <div><strong>Email:</strong> {candidateInfo.email}</div>
          <div><strong>Position:</strong> {candidateInfo.jobTitle}</div>
          <div><strong>Application ID:</strong> {applicationId}</div>
          {sessionId && <div><strong>Session ID:</strong> {sessionId}</div>}
        </div>
      )}

      <div style={{ 
        backgroundColor: '#fff3cd', 
        padding: '10px', 
        borderRadius: '4px', 
        marginBottom: '15px',
        border: '1px solid #ffeaa7',
        fontSize: '14px'
      }}>
        <strong>HR View:</strong> You are viewing the chat history in read-only mode. Only candidates can send messages during the interview.
      </div>

      <div style={{ 
        height: 500, 
        overflowY: 'auto', 
        border: '2px solid #ddd', 
        padding: '15px', 
        marginBottom: '15px',
        backgroundColor: '#f8f9fa',
        borderRadius: '8px'
      }}>
        {messages.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#666', padding: '40px' }}>
            <div style={{ fontSize: '48px', marginBottom: '10px' }}>💬</div>
            <div>No chat messages found for this application.</div>
            <div style={{ fontSize: '14px', marginTop: '8px' }}>
              The candidate may not have started the interview yet.
            </div>
          </div>
        ) : (
          messages.map((m, idx) => (
            <div key={idx} style={{ 
              textAlign: m.sender === 'CANDIDATE' ? 'right' : 'left', 
              marginBottom: '15px' 
            }}>
              <div style={{ 
                display: 'inline-block',
                maxWidth: '70%',
                textAlign: 'left'
              }}>
                <div style={{ 
                  background: m.sender === 'CANDIDATE' ? '#2c3e50' : '#3498db', 
                  color: '#fff', 
                  padding: '10px 15px', 
                  borderRadius: '12px',
                  marginBottom: '4px',
                  wordWrap: 'break-word'
                }}>
                  {m.content}
                </div>
                <div style={{ 
                  fontSize: '12px', 
                  color: '#666',
                  textAlign: m.sender === 'CANDIDATE' ? 'right' : 'left',
                  padding: '0 8px'
                }}>
                  <strong>{m.sender === 'CANDIDATE' ? 'Candidate' : 'AI Interviewer'}</strong> • {formatTimestamp(m.sentAt)}
                </div>
              </div>
            </div>
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <div style={{ 
        backgroundColor: '#e9ecef', 
        padding: '15px', 
        borderRadius: '8px',
        textAlign: 'center',
        color: '#6c757d'
      }}>
        <div style={{ fontSize: '16px', marginBottom: '5px' }}>📖 Read-Only Mode</div>
        <div style={{ fontSize: '14px' }}>
          This is a chat history viewer for HR personnel. You cannot send messages from this interface.
        </div>
      </div>
    </div>
  );
};

export default HrChatViewer;