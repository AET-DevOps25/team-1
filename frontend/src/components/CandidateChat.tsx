import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import './Login.css';

interface ChatMessage {
  sender: 'AI' | 'CANDIDATE';
  message: string;
  timestamp: string;
  finished?: boolean;
}

const CandidateChat: React.FC = () => {
  const { sessionId } = useParams();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const sendMessage = async () => {
    if (!input.trim()) return;
    const userMsg: ChatMessage = {
      sender: 'CANDIDATE',
      message: input,
      timestamp: new Date().toISOString()
    };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setIsLoading(true);

    try {
      // real backend: await fetch(`/api/chat/${sessionId}`, { ... })
      const res = await fetch('/api/chat/mock', { method: 'POST', body: JSON.stringify({ question: userMsg.message }) });
      const data: ChatMessage = await res.json();
      setMessages(prev => [...prev, data]);
    } catch (err) {
      setMessages(prev => [...prev, { sender: 'AI', message: 'Error contacting server.', timestamp: new Date().toISOString() }]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') sendMessage();
  };

  return (
    <div className="login-container" style={{ maxWidth: 600 }}>
      <h2>Interview Chat</h2>
      <div style={{ height: 400, overflowY: 'auto', border: '1px solid #ccc', padding: 10, marginBottom: 10 }}>
        {messages.map((m, idx) => (
          <div key={idx} style={{ textAlign: m.sender === 'CANDIDATE' ? 'right' : 'left', marginBottom: 8 }}>
            <span style={{ background: m.sender === 'CANDIDATE' ? '#1a1a1a' : '#3498db', color: '#fff', padding: '6px 10px', borderRadius: 6 }}>
              {m.message}
            </span>
          </div>
        ))}
        {isLoading && <div>🤖 typing…</div>}
        <div ref={messagesEndRef} />
      </div>
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyDown={handleKeyPress}
        placeholder="Type your question here…"
        style={{ width: '100%', padding: 10, marginBottom: 10 }}
      />
      <button className="login-button" onClick={sendMessage} disabled={!input.trim()}>Send</button>
    </div>
  );
};

export default CandidateChat; 