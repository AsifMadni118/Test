import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { profileService, authService } from '../services/api';
import { User, Lock, Mail, Save, AlertCircle, CheckCircle, Shield } from 'lucide-react';

export const ProfilePage = ({ addToast }) => {
  const { token, user, updateUserProfile, refreshProfile } = useAuth();

  // Personal Info Form
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [profileLoading, setProfileLoading] = useState(false);
  const [profileError, setProfileError] = useState(null);

  // Password Reset Form
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passLoading, setPassLoading] = useState(false);
  const [passError, setPassError] = useState(null);

  useEffect(() => {
    if (user) {
      setFirstName(user.firstName || '');
      setLastName(user.lastName || '');
      setEmail(user.email || '');
    } else if (token) {
      refreshProfile(token).catch(() => {
        // AuthContext owns the visible session/error state for this background refresh.
      });
    }
  }, [user, token, refreshProfile]);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setProfileError(null);
    setProfileLoading(true);

    try {
      const updated = await profileService.updateProfile(token, firstName, lastName);
      updateUserProfile(updated || { firstName, lastName });
      addToast('Personal details updated successfully!', 'success');
    } catch (err) {
      setProfileError(err.message || 'Failed to update personal details.');
    } finally {
      setProfileLoading(false);
    }
  };

  const handlePasswordReset = async (e) => {
    e.preventDefault();
    setPassError(null);

    if (newPassword.length < 8 || newPassword.length > 72) {
      setPassError('Password must contain 8 to 72 characters.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setPassError('Passwords do not match.');
      return;
    }

    setPassLoading(true);

    try {
      await authService.resetPassword(token, newPassword);
      addToast('Password reset successfully!', 'success');
      setNewPassword('');
      setConfirmPassword('');
    } catch (err) {
      setPassError(err.message || 'Password reset failed.');
    } finally {
      setPassLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2.5rem' }}>
        <h1 style={{ fontSize: '2.2rem' }}>
          Personal <span className="hero-gradient">Account Settings</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
          Update your profile information and manage account security.
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '2rem' }}>
        {/* Personal Details Card */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.5rem' }}>
            <User className="text-emerald-400" size={22} color="var(--accent-primary)" />
            <h2 style={{ fontSize: '1.4rem' }}>Personal Profile Details</h2>
          </div>

          {profileError && (
            <div className="toast error" style={{ marginBottom: '1.5rem', position: 'static' }}>
              <AlertCircle size={18} />
              <span>{profileError}</span>
            </div>
          )}

          <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div className="form-group">
                <label>First Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={firstName}
                  onChange={(e) => setFirstName(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label>Last Name</label>
                <input
                  type="text"
                  className="form-input"
                  value={lastName}
                  onChange={(e) => setLastName(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Email Address (Primary Identity)</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="email"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem', opacity: 0.8 }}
                  value={email}
                  disabled
                />
                <Mail size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                Email identity is verified and locked to your JWT account.
              </span>
            </div>

            <button type="submit" className="btn-primary" style={{ alignSelf: 'flex-start', marginTop: '0.5rem' }} disabled={profileLoading}>
              <Save size={16} />
              <span>{profileLoading ? 'Saving Changes...' : 'Save Profile Changes'}</span>
            </button>
          </form>
        </div>

        {/* Password Reset Card */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.5rem' }}>
            <Shield className="text-cyan-400" size={22} color="var(--accent-secondary)" />
            <h2 style={{ fontSize: '1.4rem' }}>Reset Password</h2>
          </div>

          {passError && (
            <div className="toast error" style={{ marginBottom: '1.5rem', position: 'static' }}>
              <AlertCircle size={18} />
              <span>{passError}</span>
            </div>
          )}

          <form onSubmit={handlePasswordReset} style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
            <div className="form-group">
              <label>New Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem' }}
                  placeholder="Enter new strong password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  required
                  minLength={8}
                  maxLength={72}
                />
                <Lock size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <div className="form-group">
              <label>Confirm New Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type="password"
                  className="form-input"
                  style={{ width: '100%', paddingLeft: '2.5rem' }}
                  placeholder="Re-enter new password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  required
                />
                <Lock size={18} style={{ position: 'absolute', left: '0.8rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <button type="submit" className="btn-primary" style={{ alignSelf: 'flex-start', marginTop: '0.5rem' }} disabled={passLoading}>
              <Lock size={16} />
              <span>{passLoading ? 'Updating Password...' : 'Reset Password'}</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
