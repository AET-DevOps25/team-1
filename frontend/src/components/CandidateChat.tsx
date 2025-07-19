import React, { useState, useEffect, useRef } from 'react';
import apiConfig from '../utils/api';
import { useParams, useNavigate } from 'react-router-dom';
import { getValidToken } from '../utils/auth';
import './Login.css';

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

if (typeof document !== 'undefined') {
  const style = document.createElement('style');
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

const CandidateChat: React.FC = () => {
  const { applicationId } = useParams();
  const navigate = useNavigate();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isInterviewFinished, setIsInterviewFinished] = useState(false);
  const [, setCurrentStreamingMessage] = useState<string>('');
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isInitialized, setIsInitialized] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    if (applicationId && !isInitialized) {
      initializeChatSession();
    }
  }, [applicationId, isInitialized]);

  const getMessages = async (sessionId: string, page: number = 0, size: number = 100) => {
    try {
      const token = getValidToken();
      if (!token) {
        console.warn('No valid token for getting messages');
        return [];
      }
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/messages?page=${page}&size=${size}`), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
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

  const initializeChatSession = async () => {
    try {
      const token = getValidToken();
      console.log('Initializing chat with token:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN');
      
      if (!token) {
        throw new Error('No valid authentication token found. Please login again.');
      }
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/applications/${applicationId}/chat`), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('Chat session initialization response:', data);
      
      if (data.success && data.data) {
        const newSessionId = data.data.session.sessionId;
        console.log('Chat session initialized with ID:', newSessionId);
        setSessionId(newSessionId);
        
        const existingMessages = await getMessages(newSessionId);
        
        if (data.data.initialMessage && existingMessages.length === 0) {
          const initialMessage: ChatMessage = {
            messageId: data.data.initialMessage.messageId,
            sessionId: data.data.initialMessage.sessionId,
            sender: 'AI',
            content: data.data.initialMessage.content,
            sentAt: data.data.initialMessage.sentAt
          };
          setMessages([initialMessage]);
        } else if (existingMessages.length > 0) {
          setMessages(existingMessages);
        }
        
        setIsInitialized(true);
      } else {
        throw new Error(data.message || 'Failed to initialize chat session');
      }
    } catch (err) {
      console.error('Error initializing chat session:', err);
      setMessages([{
        sender: 'AI',
        content: 'Error initializing interview session. Please try again.',
        sentAt: new Date().toISOString()
      }]);
    }
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const sendMessage = async () => {
    if (!input.trim() || isInterviewFinished) return;
    
    const userMsg: ChatMessage = {
      sender: 'CANDIDATE',
      content: input,
      sentAt: new Date().toISOString()
    };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsLoading(true);
    setCurrentStreamingMessage('');

    abortControllerRef.current = new AbortController();

    try {
      const token = getValidToken();
      if (!token) {
        throw new Error('No valid authentication token found. Please login again.');
      }
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/stream`), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ 
          content: userMsg.content
        }),
        credentials: 'include',
        signal: abortControllerRef.current.signal
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      
      if (!reader) {
        throw new Error('No response body');
      }

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
      
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        
        const chunk = decoder.decode(value, { stream: true });
        buffer += chunk;
        
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';
        
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
                if (chunkData.sender === 'AI' && chunkData.content) {
                  fullMessage += chunkData.content;
                  setCurrentStreamingMessage(fullMessage);
                  
                  setMessages(prev => 
                    prev.map((msg, idx) => 
                      idx === aiMessageIndex ? { ...msg, content: fullMessage } : msg
                    )
                  );
                }
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
                      messageId: endData.messageId,
                      content: endData.content || fullMessage,
                      sentAt: endData.sentAt || msg.sentAt,
                      isStreaming: false,
                      finished: true
                    } : msg
                  )
                );
                console.log('Final AI message saved with ID:', endData.messageId);
                return;
              } catch (e) {
                console.warn('Failed to parse end data:', eventData);
                setMessages(prev => 
                  prev.map((msg, idx) => 
                    idx === aiMessageIndex ? { ...msg, isStreaming: false, finished: true } : msg
                  )
                );
                return;
              }
            } else if (eventType === 'error') {
              throw new Error('Stream error: ' + eventData);
            }
            
            eventType = '';
            eventData = '';
          }
        }
      }

      setMessages(prev => 
        prev.map((msg, idx) => 
          idx === aiMessageIndex ? { ...msg, isStreaming: false, finished: true } : msg
        )
      );
      
    } catch (err) {
      if (err instanceof Error && err.name === 'AbortError') {
        console.log('Request aborted');
        return;
      }
      
      console.error('Error in streaming chat:', err);
      setMessages(prev => [...prev, { 
        sender: 'AI', 
        content: 'Error contacting server. Please try again.', 
        sentAt: new Date().toISOString() 
      }]);
    } finally {
      setIsLoading(false);
      setCurrentStreamingMessage('');
      abortControllerRef.current = null;
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !isInterviewFinished) sendMessage();
  };

  const finishInterviewEarly = async () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    setIsInterviewFinished(true);
    setIsLoading(false);
    
    const finalMessage: ChatMessage = {
      sender: 'AI',
      content: 'Interview finished early by candidate. Thank you for your time!',
      sentAt: new Date().toISOString(),
      finished: true
    };
    setMessages(prev => [...prev, finalMessage]);
    
    try {
      await fetch(apiConfig.getFullURL(`/api/v1/chat/${sessionId}/finish`), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token') || ''}`
        }
      });
    } catch (err) {
      console.error('Error finishing interview:', err);
    }
    
    setTimeout(() => {
      navigate('/candidate-dashboard');
    }, 2000);
  };

  if (!isInitialized) {
    return (
      <div className="login-container" style={{ maxWidth: 600 }}>
        <h2>Interview Chat</h2>
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <div>🤖 Initializing interview session...</div>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      height: '100vh',
      backgroundColor: '#f7f7f8',
      display: 'flex',
      flexDirection: 'column'
    }}>
      <div style={{
        backgroundColor: '#fff',
        borderBottom: '1px solid #e5e5e7',
        padding: '16px 24px',
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center'
      }}>
        <div>
          <h1 style={{ 
            margin: 0, 
            fontSize: '20px', 
            fontWeight: '600',
            color: '#1a1a1a'
          }}>
            AI Interview
          </h1>
          <div style={{ 
            fontSize: '14px', 
            color: '#6b6b6b',
            marginTop: '2px'
          }}>
            Session: {sessionId?.substring(0, 8)}...
          </div>
        </div>
        <button
          onClick={finishInterviewEarly}
          disabled={isInterviewFinished}
          style={{
            backgroundColor: isInterviewFinished ? '#d1d5db' : '#dc2626',
            color: isInterviewFinished ? '#6b7280' : '#fff',
            border: 'none',
            padding: '8px 16px',
            borderRadius: '8px',
            fontSize: '14px',
            fontWeight: '500',
            cursor: isInterviewFinished ? 'not-allowed' : 'pointer',
            transition: 'all 0.2s'
          }}
        >
          {isInterviewFinished ? 'Redirecting...' : 'Finish Interview'}
        </button>
      </div>

      <div style={{
        flex: 1,
        overflowY: 'auto',
        padding: '24px',
        maxWidth: '768px',
        margin: '0 auto',
        width: '100%'
      }}>
        {messages.length === 0 ? (
          <div style={{
            textAlign: 'center',
            paddingTop: '60px',
            color: '#6b6b6b'
          }}>
            <div style={{ fontSize: '48px', marginBottom: '16px' }}>🤖</div>
            <h2 style={{ fontSize: '24px', fontWeight: '600', marginBottom: '8px', color: '#1a1a1a' }}>
              Welcome to your AI Interview
            </h2>
            <p style={{ fontSize: '16px', margin: 0 }}>
              I'm here to assess your skills. Feel free to introduce yourself when you're ready.
            </p>
          </div>
        ) : (
          messages.map((m, idx) => (
            <div key={idx} style={{
              display: 'flex',
              marginBottom: '24px',
              justifyContent: m.sender === 'CANDIDATE' ? 'flex-end' : 'flex-start'
            }}>
              <div style={{
                maxWidth: '85%',
                display: 'flex',
                flexDirection: m.sender === 'CANDIDATE' ? 'row-reverse' : 'row',
                alignItems: 'flex-start',
                gap: '12px'
              }}>
                <div style={{
                  width: '32px',
                  height: '32px',
                  borderRadius: '16px',
                  backgroundColor: m.sender === 'CANDIDATE' ? '#2563eb' : '#10b981',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '16px',
                  flexShrink: 0,
                  marginTop: '4px'
                }}>
                  {m.sender === 'CANDIDATE' ? '👤' : '🤖'}
                </div>
                
                <div style={{
                  backgroundColor: m.sender === 'CANDIDATE' ? '#2563eb' : '#fff',
                  color: m.sender === 'CANDIDATE' ? '#fff' : '#1a1a1a',
                  padding: '12px 16px',
                  borderRadius: '16px',
                  borderTopLeftRadius: m.sender === 'AI' ? '4px' : '16px',
                  borderTopRightRadius: m.sender === 'CANDIDATE' ? '4px' : '16px',
                  border: m.sender === 'AI' ? '1px solid #e5e5e7' : 'none',
                  fontSize: '15px',
                  lineHeight: '1.5',
                  wordWrap: 'break-word',
                  boxShadow: m.sender === 'AI' ? '0 1px 3px rgba(0, 0, 0, 0.1)' : 'none',
                  opacity: m.isStreaming ? 0.9 : 1,
                  position: 'relative'
                }}>
                  {m.content || m.message}
                  {m.isStreaming && (
                    <span style={{
                      opacity: 0.7,
                      animation: 'pulse 1.5s infinite'
                    }}>▊</span>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
        
        {isLoading && (
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            color: '#6b6b6b',
            fontSize: '14px'
          }}>
            <div style={{
              width: '32px',
              height: '32px',
              borderRadius: '16px',
              backgroundColor: '#10b981',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '16px'
            }}>
              🤖
            </div>
            <div style={{
              backgroundColor: '#f3f4f6',
              padding: '12px 16px',
              borderRadius: '16px',
              borderTopLeftRadius: '4px'
            }}>
              <div style={{ display: 'flex', gap: '4px', alignItems: 'center' }}>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  backgroundColor: '#9ca3af',
                  animation: 'bounce 1.4s infinite ease-in-out'
                }}></div>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  backgroundColor: '#9ca3af',
                  animation: 'bounce 1.4s infinite ease-in-out 0.2s'
                }}></div>
                <div style={{
                  width: '8px',
                  height: '8px',
                  borderRadius: '50%',
                  backgroundColor: '#9ca3af',
                  animation: 'bounce 1.4s infinite ease-in-out 0.4s'
                }}></div>
              </div>
            </div>
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      <div style={{
        backgroundColor: '#fff',
        borderTop: '1px solid #e5e5e7',
        padding: '16px 24px'
      }}>
        <div style={{
          maxWidth: '768px',
          margin: '0 auto',
          display: 'flex',
          gap: '12px',
          alignItems: 'flex-end'
        }}>
          <div style={{ flex: 1, position: 'relative' }}>
            <input
              type="text"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyPress}
              placeholder={isInterviewFinished ? "Interview completed" : "Message the interviewer..."}
              disabled={isInterviewFinished || isLoading}
              style={{
                width: '100%',
                padding: '12px 48px 12px 16px',
                border: '1px solid #d1d5db',
                borderRadius: '24px',
                fontSize: '15px',
                outline: 'none',
                backgroundColor: isInterviewFinished ? '#f9fafb' : '#fff',
                color: isInterviewFinished ? '#9ca3af' : '#1a1a1a',
                transition: 'all 0.2s',
                boxSizing: 'border-box'
              }}
            />
            <button
              onClick={sendMessage}
              disabled={!input.trim() || isInterviewFinished || isLoading}
              style={{
                position: 'absolute',
                right: '6px',
                top: '50%',
                transform: 'translateY(-50%)',
                width: '36px',
                height: '36px',
                borderRadius: '18px',
                border: 'none',
                backgroundColor: (!input.trim() || isInterviewFinished || isLoading) ? '#e5e7eb' : '#2563eb',
                color: '#fff',
                cursor: (!input.trim() || isInterviewFinished || isLoading) ? 'not-allowed' : 'pointer',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '14px',
                transition: 'all 0.2s'
              }}
            >
              {isLoading ? '...' : '↑'}
            </button>
          </div>
        </div>
        
        <div style={{
          maxWidth: '768px',
          margin: '8px auto 0',
          fontSize: '12px',
          color: '#9ca3af',
          textAlign: 'center'
        }}>
          Press Enter to send • {isInterviewFinished ? 'Interview completed, redirecting to dashboard...' : 'Be thoughtful and detailed in your responses'}
        </div>
      </div>
      
      <style>{`
        @keyframes pulse {
          0%, 100% { opacity: 1; }
          50% { opacity: 0.5; }
        }
        
        @keyframes bounce {
          0%, 80%, 100% {
            transform: scale(0);
          }
          40% {
            transform: scale(1);
          }
        }
      `}</style>
    </div>
  );
};

export default CandidateChat; 