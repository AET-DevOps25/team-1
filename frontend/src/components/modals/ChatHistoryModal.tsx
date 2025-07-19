import React, { useState, useEffect, useRef } from 'react';
import type { Application } from '../../types/dashboard';
import apiConfig from '../../utils/api';
import { getValidToken } from '../../utils/auth';

interface ChatMessage {
  messageId?: string;
  sessionId?: string;
  sender: 'AI' | 'CANDIDATE';
  content: string;
  sentAt: string;
}

interface ChatHistoryModalProps {
  isOpen: boolean;
  application: Application | null;
  onClose: () => void;
}

const ChatHistoryModal: React.FC<ChatHistoryModalProps> = ({
  isOpen,
  application,
  onClose
}) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (isOpen && application) {
      loadChatHistory();
    } else {
      setMessages([]);
      setSessionId(null);
      setError(null);
    }
  }, [isOpen, application]);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadChatHistory = async () => {
    if (!application) return;
    
    setIsLoading(true);
    setError(null);
    
    try {
      const appId = application.applicationId || application.application_id;
      console.log('=== CHAT HISTORY DEBUG ===');
      console.log('Full application object keys:', Object.keys(application));
      console.log('Application object:', JSON.stringify(application, null, 2));
      console.log('Available ID fields:');
      console.log('- application.applicationId:', application.applicationId);
      console.log('- application.application_id:', application.application_id);
      console.log('- application.jobId:', application.jobId);
      console.log('- application.candidateId:', application.candidateId);
      console.log('Final extracted application ID:', appId);
      console.log('ID type:', typeof appId);
      console.log('ID length:', appId?.length);
      
      if (!appId) {
        console.error('No application ID found in application object');
        throw new Error('No application ID found');
      }
      
      const token = getValidToken();
      console.log('Auth token available:', !!token);
      console.log('Auth token preview:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN');
      
      if (!token) {
        throw new Error('No valid authentication token found. Please login again.');
      }

      const userRole = localStorage.getItem('role');
      console.log('User role:', userRole);
      
      if (userRole === 'HR') {
        console.log('Using HR endpoint: /api/v1/applications/{applicationId}/messages');
        const messagesResponse = await fetch(apiConfig.getFullURL(`/api/v1/applications/${appId}/messages?page=0&size=100`), {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          },
          credentials: 'include'
        });

        console.log('HR Messages response status:', messagesResponse.status);
        console.log('HR Messages response ok:', messagesResponse.ok);

        if (!messagesResponse.ok) {
          const errorText = await messagesResponse.text();
          console.error('HR Messages response error details:', errorText);
          throw new Error(`Failed to fetch chat messages: ${messagesResponse.status} - ${errorText}`);
        }

        const messagesData = await messagesResponse.json();
        console.log('HR Messages data received:', JSON.stringify(messagesData, null, 2));
        
        if (messagesData.success && messagesData.data) {
          const chatMessages = messagesData.data.content.map((msg: any) => ({
            messageId: msg.messageId,
            sessionId: msg.sessionId,
            sender: msg.sender,
            content: msg.content,
            sentAt: msg.sentAt
          }));
          console.log('HR Parsed chat messages count:', chatMessages.length);
          console.log('HR Parsed chat messages:', chatMessages);
          setMessages(chatMessages);
        } else {
          console.log('HR No messages found or invalid response structure');
          console.log('HR Messages data structure:', messagesData);
          setMessages([]);
        }
      } else {
        console.log('Using candidate endpoint: session-based flow');
        console.log('Making session request to:', `/api/v1/applications/${appId}/chat`);
        const sessionResponse = await fetch(apiConfig.getFullURL(`/api/v1/applications/${appId}/chat`), {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          credentials: 'include'
        });

        console.log('Session response status:', sessionResponse.status);
        console.log('Session response ok:', sessionResponse.ok);

        if (!sessionResponse.ok) {
          const errorText = await sessionResponse.text();
          console.error('Session response error details:', errorText);
          throw new Error(`Failed to get chat session: ${sessionResponse.status} - ${errorText}`);
        }

        const sessionData = await sessionResponse.json();
        console.log('Session data received:', JSON.stringify(sessionData, null, 2));
        
        if (sessionData.success && sessionData.data) {
          const sessionId = sessionData.data.session?.sessionId;
          console.log('Extracted session ID:', sessionId);
          
          if (!sessionId) {
            console.error('No session ID in response. Full data:', sessionData.data);
            throw new Error('No session ID returned from server');
          }
          
          setSessionId(sessionId);
          
          console.log('Making messages request to:', `/api/v1/chat/${sessionId}/messages?page=0&size=100`);
          const messagesResponse = await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/messages?page=0&size=100`), {
            method: 'GET',
            headers: {
              'Authorization': `Bearer ${token}`
            },
            credentials: 'include'
          });

          console.log('Messages response status:', messagesResponse.status);
          console.log('Messages response ok:', messagesResponse.ok);

          if (!messagesResponse.ok) {
            const errorText = await messagesResponse.text();
            console.error('Messages response error details:', errorText);
            throw new Error(`Failed to fetch chat messages: ${messagesResponse.status} - ${errorText}`);
          }

          const messagesData = await messagesResponse.json();
          console.log('Messages data received:', JSON.stringify(messagesData, null, 2));
          
          if (messagesData.success && messagesData.data) {
            const chatMessages = messagesData.data.content.map((msg: any) => ({
              messageId: msg.messageId,
              sessionId: msg.sessionId,
              sender: msg.sender,
              content: msg.content,
              sentAt: msg.sentAt
            }));
            console.log('Parsed chat messages count:', chatMessages.length);
            console.log('Parsed chat messages:', chatMessages);
            setMessages(chatMessages);
          } else {
            console.log('No messages found or invalid response structure');
            console.log('Messages data structure:', messagesData);
            setMessages([]);
          }
        } else {
          console.error('Invalid session response structure. Full response:', sessionData);
          throw new Error('Failed to initialize chat session - invalid response structure');
        }
      }
    } catch (err) {
      console.error('=== CHAT HISTORY ERROR ===');
      console.error('Error details:', err);
      console.error('Error type:', typeof err);
      console.error('Error message:', err instanceof Error ? err.message : String(err));
      setError(`Unable to load chat history: ${err instanceof Error ? err.message : 'Unknown error'}`);
      setMessages([]);
    } finally {
      setIsLoading(false);
      console.log('=== CHAT HISTORY DEBUG END ===');
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
  };

  const getMessageIcon = (sender: string) => {
    return sender === 'AI' ? 'AI' : 'USER';
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div 
        className="modal-content" 
        onClick={(e) => e.stopPropagation()} 
        style={{ 
          maxWidth: '800px', 
          maxHeight: '80vh',
          display: 'flex',
          flexDirection: 'column'
        }}
      >
        <div className="modal-header">
          <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '10px' }}>
            Chat History
            {application && (
              <span style={{ fontSize: '14px', fontWeight: 'normal', color: '#666' }}>
                - {application.candidate_name || application.candidate?.fullName}
              </span>
            )}
          </h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body" style={{ flex: 1, display: 'flex', flexDirection: 'column', padding: 0 }}>
          {application && (
            <div style={{ 
              backgroundColor: '#f8f9fa', 
              padding: '15px 20px', 
              borderBottom: '1px solid #e9ecef',
              fontSize: '14px'
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <strong>{application.candidate_name || application.candidate?.fullName}</strong>
                  <span style={{ margin: '0 10px', color: '#666' }}>•</span>
                  <span style={{ color: '#666' }}>
                    {application.job_title || application.job?.title}
                  </span>
                </div>
                {sessionId && (
                  <div style={{ fontSize: '12px', color: '#666' }}>
                    Session: {sessionId.substring(0, 8)}...
                  </div>
                )}
              </div>
            </div>
          )}

          <div style={{ 
            flex: 1, 
            overflowY: 'auto', 
            padding: '20px',
            backgroundColor: '#fafafa',
            minHeight: '400px'
          }}>
            {isLoading ? (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '200px',
                color: '#666'
              }}>
                <div>
                  <div style={{ fontSize: '24px', marginBottom: '10px' }}>💬</div>
                  Loading chat history...
                </div>
              </div>
            ) : error ? (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '200px',
                color: '#e74c3c',
                textAlign: 'center'
              }}>
                <div>
                  <div style={{ fontSize: '24px', marginBottom: '10px' }}>⚠️</div>
                  <div>{error}</div>
                </div>
              </div>
            ) : messages.length === 0 ? (
              <div style={{ 
                display: 'flex', 
                justifyContent: 'center', 
                alignItems: 'center', 
                height: '200px',
                color: '#666',
                textAlign: 'center'
              }}>
                <div>
                  <div style={{ fontSize: '48px', marginBottom: '15px' }}>💬</div>
                  <div style={{ fontSize: '18px', marginBottom: '8px' }}>No Chat History</div>
                  <div style={{ fontSize: '14px' }}>
                    This candidate hasn't started the interview yet.
                  </div>
                </div>
              </div>
            ) : (
              <>
                {messages.map((message, index) => (
                  <div 
                    key={message.messageId || index} 
                    style={{ 
                      marginBottom: '20px',
                      display: 'flex',
                      flexDirection: message.sender === 'CANDIDATE' ? 'row-reverse' : 'row',
                      alignItems: 'flex-start',
                      gap: '12px'
                    }}
                  >
                    <div style={{
                      width: '40px',
                      height: '40px',
                      borderRadius: '50%',
                      backgroundColor: message.sender === 'CANDIDATE' ? '#3498db' : '#2ecc71',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontSize: '18px',
                      flexShrink: 0
                    }}>
                      {getMessageIcon(message.sender)}
                    </div>

                    <div style={{ 
                      maxWidth: '70%',
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: message.sender === 'CANDIDATE' ? 'flex-end' : 'flex-start'
                    }}>
                      <div style={{
                        fontSize: '12px',
                        color: '#666',
                        marginBottom: '4px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px'
                      }}>
                        <span style={{ fontWeight: 'bold' }}>
                          {message.sender === 'CANDIDATE' ? 'Candidate' : 'AI Interviewer'}
                        </span>
                        <span>•</span>
                        <span>{formatTimestamp(message.sentAt)}</span>
                      </div>

                      <div style={{
                        backgroundColor: message.sender === 'CANDIDATE' ? '#3498db' : '#ffffff',
                        color: message.sender === 'CANDIDATE' ? '#ffffff' : '#333333',
                        padding: '12px 16px',
                        borderRadius: '18px',
                        borderTopLeftRadius: message.sender === 'AI' ? '6px' : '18px',
                        borderTopRightRadius: message.sender === 'CANDIDATE' ? '6px' : '18px',
                        boxShadow: '0 1px 2px rgba(0, 0, 0, 0.1)',
                        lineHeight: '1.4',
                        wordWrap: 'break-word',
                        border: message.sender === 'AI' ? '1px solid #e1e8ed' : 'none'
                      }}>
                        {message.content}
                      </div>
                    </div>
                  </div>
                ))}
                <div ref={messagesEndRef} />
              </>
            )}
          </div>
        </div>
        
        <div className="modal-footer" style={{ 
          padding: '15px 20px', 
          borderTop: '1px solid #e9ecef',
          backgroundColor: '#f8f9fa'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ fontSize: '14px', color: '#666' }}>
              {messages.length > 0 ? (
                `${messages.length} message${messages.length !== 1 ? 's' : ''} in this conversation`
              ) : (
                'Read-only view for HR personnel'
              )}
            </div>
            <button 
              className="btn-cancel" 
              onClick={onClose}
              style={{ 
                backgroundColor: '#6c757d', 
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '4px',
                cursor: 'pointer'
              }}
            >
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ChatHistoryModal;