import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/api';
import { X, Lock, Mail, User, AlertCircle, CheckCircle } from 'lucide-react';

export const AuthModal = ({ isOpen, onClose, addToast }) => {
  const { login } = useAuth();
  const [activeTab, setActiveTab] = useState('login'); // 'login' or 'register'
  
  // Login Form State
  const [loginEmail, setLoginEmail] = useState('');
  const [loginPassword, setLoginPassword] = useState('');
  
  // Register Form State
  const [regFirstName, setRegFirstName] = useState('');
  const [regLastName, setRegLastName] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [regPassword, setRegPassword] = useState('');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [infoMessage, setInfoMessage] = useState(null);

  if (!isOpen) return null;

  const handleLogin = async (e) => {
    e.preventDefault();
    setError(null);
    setInfoMessage(null);
    setLoading(true);

    try {
      const data = await authService.login(loginEmail, loginPassword);
      if (data && data.accessToken) {
        await login(data.accessToken);
        addToast('Successfully logged in!', 'success');
        onClose();
      } else {
        throw new Error('Invalid server response');
      }
    } catch (err) {
      setError(err.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(null);
    setInfoMessage(null);
    setLoading(true);

    try {
      await authService.register(regFirstName, regLastName, regEmail, regPassword);
      setInfoMessage('Account created! Kafka is activating your credential. Logging you in...');
      addToast('Registration successful! Logging in...', 'success');
      
      // Retry the real login briefly while the USER_CREATED event activates the credential.
      const tryLoginAfterActivation = async () => {
        for (let attempt = 1; attempt <= 5; attempt += 1) {
          await new Promise(resolve => setTimeout(resolve, attempt * 400));
          try {
            const data = await authService.login(regEmail, regPassword);
            if (data?.accessToken) {
              await login(data.accessToken);
              setLoading(false);
              onClose();
              return;
            }
          } catch (loginErr) {
            if (loginErr.status !== 401 || attempt === 5) break;
          }
        }

        setActiveTab('login');
        setLoginEmail(regEmail);
        setError('Credential activation is still processing. Please click Login again.');
        setLoading(false);
      };

      await tryLoginAfterActivation();

    } catch (err) {
      setError(err.message || 'Registration failed. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2 style={{ fontSize: '1.4rem' }}>Welcome to ProteinPro</h2>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <div className="tab-header">
          <button
            className={`tab-btn ${activeTab === 'login' ? 'active' : ''}`}
            onClick={() => { setActiveTab('login'); setError(null); setInfoMessage(null); }}
          >
            Login
          </button>
          <button
            className={`tab-btn ${activeTab === 'register' ? 'active' : ''}`}
            onClick={() => { setActiveTab('register'); setError(null); setInfoMessage(null); }}
          >
            Register Account
          </button>
        </div>

        {error && (
          <div className="toast error" style={{ marginBottom: '1rem', position: 'static' }}>
            <AlertCircle size={18} />
            <span>{error}</span>
          </div>
        )}

        {infoMessage && (
          <div className="toast success" style={{ marginBottom: '1rem', position: 'static' }}>
            <CheckCircle size={18} />
            <span>{infoMessage}</span>
          </div>
        )}

        {activeTab === 'login' ? (
          <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div className="form-group">
              <label>Email Address</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem' }}
                  placeholder="alex@example.com"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                  required
                />
                <Mail size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <div className="form-group">
              <label>Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem' }}
                  placeholder="••••••••"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                  required
                />
                <Lock size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <button type="submit" className="btn-primary" style={{ marginTop: '0.5rem', width: '100%' }} disabled={loading}>
              {loading ? 'Authenticating...' : 'Sign In'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleRegister} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
              <div className="form-group">
                <label>First Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Alex"
                  value={regFirstName}
                  onChange={(e) => setRegFirstName(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="Smith"
                  value={regLastName}
                  onChange={(e) => setRegLastName(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Email Address</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem' }}
                  placeholder="alex@example.com"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                  required
                />
                <Mail size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <div className="form-group">
              <label>Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem' }}
                  placeholder="8 to 72 characters"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                  required
                  minLength={8}
                  maxLength={72}
                />
                <Lock size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <button type="submit" className="btn-primary" style={{ marginTop: '0.5rem', width: '100%' }} disabled={loading}>
              {loading ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
};
