import React, { useState, useEffect, useRef } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Box, Typography, IconButton } from '@mui/material';
import { Close as CloseIcon, Send as SendIcon } from '@mui/icons-material';
import apiConfig from '../../utils/api';
import { getValidToken } from '../../utils/auth';
import type { Application } from '../../types/dashboard';

const streamingCursorStyle = `
  .streaming-cursor {
    animation: blink 1s infinite;
    margin-left: 2px;
  }
  
  @keyframes blink {
    0%, 50% { opacity: 1; }
    51%, 100% { opacity: 0; }
  }
`;

if (typeof document !== 'undefined' && !document.querySelector('#chat-modal-styles')) {
  const style = document.createElement('style');
  style.id = 'chat-modal-styles';
  style.textContent = streamingCursorStyle;
  document.head.appendChild(style);
}

interface ChatMessage {
  messageId?: string;
  sessionId?: string;
  sender: 'AI' | 'CANDIDATE';
  content: string;
  sentAt: string;
  finished?: boolean;
  isStreaming?: boolean;
  message?: string;
  timestamp?: string;
}

interface CandidateChatModalProps {
  isOpen: boolean;
  application: Application | null;
  onClose: () => void;
  onInterviewComplete?: () => void;
}

const CandidateChatModal: React.FC<CandidateChatModalProps> = ({
  isOpen,
  application,
  onClose,
  onInterviewComplete
}) => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isInterviewFinished, setIsInterviewFinished] = useState(false);
  const [currentStreamingMessage, setCurrentStreamingMessage] = useState<string>('');
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, currentStreamingMessage]);

  useEffect(() => {
    if (isOpen && application && !isInitialized) {
      initializeChat();
    } else if (!isOpen) {
      setMessages([]);
      setInput('');
      setIsLoading(false);
      setIsInterviewFinished(false);
      setCurrentStreamingMessage('');
      setSessionId(null);
      setIsInitialized(false);
    }
  }, [isOpen, application]);

  const initializeChat = async () => {
    if (!application) return;
    
    const token = getValidToken();
    if (!token) {
      console.error('No valid token found');
      return;
    }

    try {
      setIsLoading(true);
      console.log('Initializing chat for application:', application.applicationId || application.application_id);
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/applications/${application.applicationId || application.application_id}/chat`), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Failed to initialize chat: ${response.status}`);
      }

      const data = await response.json();
      console.log('Chat initialization response:', data);

      if (data.success && data.data?.session) {
        const session = data.data.session;
        setSessionId(session.sessionId || session.session_id);
        setIsInterviewFinished(session.finished || false);
        
        if (session.finished) {
          console.log('Interview already finished');
        }
        
        await loadChatHistory(session.sessionId);
        setIsInitialized(true);
      } else {
        console.error('Failed to get session from response:', data);
      }
    } catch (error) {
      console.error('Error initializing chat:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const loadChatHistory = async (sessionId: string) => {
    const token = getValidToken();
    if (!token) return;

    try {
      const response = await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/messages?page=0&size=100`), {
        headers: {
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        if (data.success && data.data?.content) {
          const chatMessages = data.data.content.map((msg: any) => ({
            messageId: msg.messageId || msg.message_id,
            sessionId: msg.sessionId || msg.session_id,
            sender: msg.sender,
            content: msg.content,
            sentAt: msg.sentAt,
            finished: true
          }));
          setMessages(chatMessages);
        }
      }
    } catch (error) {
      console.error('Error loading chat history:', error);
    }
  };

  const sendMessage = async () => {
    if (!input.trim() || !sessionId || isLoading || isInterviewFinished) return;

    const token = getValidToken();
    if (!token) {
      console.error('No valid token found');
      return;
    }

    const messageContent = input.trim();
    setInput('');
    setIsLoading(true);

    const userMessage: ChatMessage = {
      sender: 'CANDIDATE',
      content: messageContent,
      sentAt: new Date().toISOString(),
      finished: true
    };
    setMessages(prev => [...prev, userMessage]);

    try {
      console.log('=== CHAT SEND DEBUG ===');
      console.log('Session ID:', sessionId);
      console.log('Message content:', messageContent);
      console.log('Token preview:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN');
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/messages`), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ content: messageContent }),
        credentials: 'include'
      });

      console.log('Send response status:', response.status);
      console.log('Send response headers:', Object.fromEntries(response.headers.entries()));
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Send response error text:', errorText);
        throw new Error(`Failed to send message: ${response.status} - ${errorText}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error('No response body reader available');
      }

      const decoder = new TextDecoder();

      const aiMessageIndex = messages.length + 1;
      const initialAiMessage: ChatMessage = {
        sender: 'AI',
        content: '',
        sentAt: new Date().toISOString(),
        isStreaming: true
      };
      setMessages(prev => [...prev, initialAiMessage]);
      setIsLoading(false);

      let fullMessage = '';
      let buffer = '';
      
      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          
          const chunk = decoder.decode(value, { stream: true });
          buffer += chunk;
          
          const lines = buffer.split('\n');
          buffer = lines.pop() || ''; // Keep incomplete line in buffer
          
          let eventType = '';
          let eventData = '';
          
          for (const line of lines) {
            if (line.startsWith('event:')) {
              eventType = line.slice(6).trim();
            } else if (line.startsWith('data:')) {
              eventData = line.slice(5).trim();
            } else if (line === '' && eventType && eventData) {
              if (eventType === 'message-chunk') {
                try {
                  const chunkData = JSON.parse(eventData);
                  console.log('Received chunk:', chunkData);
                  
                  fullMessage += chunkData.content;
                  setCurrentStreamingMessage(fullMessage);
                  
                  setMessages(prev => 
                    prev.map((msg, idx) => 
                      idx === aiMessageIndex ? { ...msg, content: fullMessage } : msg
                    )
                  );
                } catch (e) {
                  console.warn('Failed to parse chunk data:', eventData);
                }
              } else if (eventType === 'stream-end') {
                try {
                  const endData = JSON.parse(eventData);
                  console.log('Stream ended with final message data:', endData);
                  
                  setMessages(prev => 
                    prev.map((msg, idx) => 
                      idx === aiMessageIndex ? { 
                        ...msg, 
                        content: endData.content || fullMessage,
                        messageId: endData.messageId,
                        sentAt: endData.sentAt || msg.sentAt,
                        isStreaming: false,
                        finished: true
                      } : msg
                    )
                  );
                  console.log('Final AI message saved with ID:', endData.messageId);
                  return; // Exit the streaming loop
                } catch (e) {
                  console.warn('Failed to parse end data:', eventData);
                  setMessages(prev => 
                    prev.map((msg, idx) => 
                      idx === aiMessageIndex ? { ...msg, isStreaming: false, finished: true } : msg
                    )
                  );
                  return;
                }
              } else if (eventType === 'interview-finished') {
                console.log('Interview finished event received');
                setIsInterviewFinished(true);
                if (onInterviewComplete) {
                  setTimeout(() => {
                    onInterviewComplete();
                  }, 2000);
                }
              }
              
              eventType = '';
              eventData = '';
            }
          }
        }
      } finally {
        reader.releaseLock();
      }
      
      setMessages(prev => 
        prev.map((msg, idx) => 
          idx === aiMessageIndex ? { ...msg, isStreaming: false, finished: true } : msg
        )
      );
      setCurrentStreamingMessage('');

    } catch (error) {
      console.error('Error sending message:', error);
      
      const errorMessage: ChatMessage = {
        sender: 'AI',
        content: `Sorry, I'm experiencing technical difficulties. The interview system is currently unavailable. ${retryCount < 2 ? 'You can try sending your message again.' : 'Please contact support if this continues.'}`,
        sentAt: new Date().toISOString(),
        finished: true
      };
      
      setMessages(prev => [...prev.slice(0, -1), errorMessage]);
      setRetryCount(prev => prev + 1);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: true
    });
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
          height: '80vh',
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
          AI Interview Chat
          {application && (
            <Typography variant="caption" display="block" color="text.secondary">
              {application.candidate_name} - {application.job_title}
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
        <Box sx={{ 
          flex: 1, 
          overflowY: 'auto', 
          p: 2,
          backgroundColor: '#fafafa'
        }}>
          {isLoading && messages.length === 0 ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <Typography color="text.secondary">Initializing chat...</Typography>
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
                    fontSize: '18px',
                    flexShrink: 0,
                    color: 'white',
                    fontWeight: 'bold'
                  }}>
                    {message.sender === 'CANDIDATE' ? 'C' : 'AI'}
                  </Box>

                  <Box sx={{ 
                    maxWidth: '70%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: message.sender === 'CANDIDATE' ? 'flex-end' : 'flex-start'
                  }}>
                    <Typography variant="caption" color="text.secondary" sx={{ mb: 0.5 }}>
                      {message.sender === 'CANDIDATE' ? 'You' : 'AI Interviewer'} • {formatTimestamp(message.sentAt)}
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
                      border: message.sender === 'AI' ? '1px solid #e1e8ed' : 'none',
                      position: 'relative'
                    }}>
                      {message.content}
                      {message.isStreaming && (
                        <span className="streaming-cursor">▋</span>
                      )}
                    </Box>
                  </Box>
                </Box>
              ))}
              <div ref={messagesEndRef} />
            </>
          )}
        </Box>

        {isInterviewFinished && (
          <Box sx={{ 
            p: 2, 
            backgroundColor: '#e8f5e8', 
            borderTop: '1px solid #c8e6c9',
            textAlign: 'center'
          }}>
            <Typography variant="body2" color="success.main" fontWeight="bold">
              ✅ Interview Completed! Your responses have been recorded.
            </Typography>
          </Box>
        )}
      </DialogContent>

      {!isInterviewFinished && (
        <DialogActions sx={{ 
          p: 2, 
          borderTop: '1px solid #e0e0e0',
          backgroundColor: '#fff'
        }}>
          <Box sx={{ display: 'flex', width: '100%', gap: 1 }}>
            <TextField
              fullWidth
              multiline
              maxRows={3}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type your message..."
              disabled={isLoading || isInterviewFinished}
              variant="outlined"
              size="small"
            />
            <Button
              variant="contained"
              onClick={sendMessage}
              disabled={!input.trim() || isLoading || isInterviewFinished}
              startIcon={<SendIcon />}
              sx={{ minWidth: 'auto', px: 2 }}
            >
              Send
            </Button>
          </Box>
        </DialogActions>
      )}
    </Dialog>
  );
};

export default CandidateChatModal;