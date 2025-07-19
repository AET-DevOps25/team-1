import React, { useState, useEffect, useRef } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box, Typography, IconButton, Chip } from '@mui/material';
import { Close as CloseIcon, Person as PersonIcon, SmartToy as AIIcon } from '@mui/icons-material';
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


  if (!isOpen) return null;

  return (
    <Dialog
      open={isOpen}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      PaperProps={{
        sx: {
          height: '85vh',
          display: 'flex',
          flexDirection: 'column'
        }
      }}
    >
      <DialogTitle sx={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        pb: 1,
        borderBottom: '1px solid #e0e0e0'
      }}>
        <Typography variant="h6" component="div">
          AI Interview Chat History
          {application && (
            <Typography variant="caption" display="block" color="text.secondary">
              {application.candidate_name || application.candidate?.fullName} - {application.job_title || application.job?.title}
            </Typography>
          )}
        </Typography>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent sx={{ 
        flex: 1, 
        display: 'flex', 
        flexDirection: 'column',
        p: 0,
        overflow: 'hidden'
      }}>
        {application && (
          <Box sx={{ 
            backgroundColor: '#f8f9fa', 
            p: 2, 
            borderBottom: '1px solid #e0e0e0'
          }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 'bold' }}>
                  {application.candidate_name || application.candidate?.fullName}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Application ID: {(application.applicationId || application.application_id || '').substring(0, 8)}...
                </Typography>
              </Box>
              {sessionId && (
                <Chip 
                  label={`Session: ${sessionId.substring(0, 8)}...`} 
                  size="small" 
                  variant="outlined"
                />
              )}
            </Box>
          </Box>
        )}

        <Box sx={{ 
          flex: 1, 
          overflowY: 'auto', 
          p: 2,
          backgroundColor: '#fafafa'
        }}>
          {isLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <Typography color="text.secondary">Loading chat history...</Typography>
            </Box>
          ) : error ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4, textAlign: 'center' }}>
              <Box>
                <Typography variant="h4" sx={{ mb: 2 }}>⚠️</Typography>
                <Typography color="error">{error}</Typography>
              </Box>
            </Box>
          ) : messages.length === 0 ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4, textAlign: 'center' }}>
              <Box>
                <Typography variant="h3" sx={{ mb: 2 }}>💬</Typography>
                <Typography variant="h6" sx={{ mb: 1 }}>No Chat History</Typography>
                <Typography variant="body2" color="text.secondary">
                  This candidate hasn't started the interview yet.
                </Typography>
              </Box>
            </Box>
          ) : (
            <>
              {messages.map((message, index) => (
                <Box
                  key={message.messageId || index}
                  sx={{
                    display: 'flex',
                    flexDirection: message.sender === 'CANDIDATE' ? 'row-reverse' : 'row',
                    alignItems: 'flex-start',
                    gap: 2,
                    mb: 3
                  }}
                >
                  <Box sx={{
                    width: 40,
                    height: 40,
                    borderRadius: '50%',
                    backgroundColor: message.sender === 'CANDIDATE' ? '#3498db' : '#2ecc71',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'white',
                    flexShrink: 0,
                    fontWeight: 'bold'
                  }}>
                    {message.sender === 'CANDIDATE' ? <PersonIcon /> : <AIIcon />}
                  </Box>

                  <Box sx={{ 
                    maxWidth: '70%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: message.sender === 'CANDIDATE' ? 'flex-end' : 'flex-start'
                  }}>
                    <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5 }}>
                      {message.sender === 'CANDIDATE' ? 'Candidate' : 'AI Interviewer'}
                    </Typography>

                    <Box sx={{
                      backgroundColor: message.sender === 'CANDIDATE' ? '#3498db' : '#ffffff',
                      color: message.sender === 'CANDIDATE' ? '#ffffff' : '#333333',
                      p: 2,
                      borderRadius: 2,
                      borderTopLeftRadius: message.sender === 'AI' ? 0.5 : 2,
                      borderTopRightRadius: message.sender === 'CANDIDATE' ? 0.5 : 2,
                      boxShadow: '0 1px 2px rgba(0, 0, 0, 0.1)',
                      lineHeight: 1.4,
                      wordWrap: 'break-word',
                      border: message.sender === 'AI' ? '1px solid #e1e8ed' : 'none'
                    }}>
                      {message.content}
                    </Box>
                  </Box>
                </Box>
              ))}
              <div ref={messagesEndRef} />
            </>
          )}
        </Box>
      </DialogContent>
        
      <DialogActions sx={{ 
        p: 2, 
        borderTop: '1px solid #e0e0e0',
        backgroundColor: '#f8f9fa'
      }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
          <Typography variant="body2" color="text.secondary">
            {messages.length > 0 ? (
              `${messages.length} message${messages.length !== 1 ? 's' : ''} in this conversation`
            ) : (
              'Read-only view for HR personnel'
            )}
          </Typography>
          <Button 
            variant="outlined"
            onClick={onClose}
            startIcon={<CloseIcon />}
          >
            Close
          </Button>
        </Box>
      </DialogActions>
    </Dialog>
  );
};

export default ChatHistoryModal;