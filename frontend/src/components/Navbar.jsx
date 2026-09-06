import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Zap, Search, Bookmark, User, LogIn, LogOut, ShieldAlert } from 'lucide-react';

export const Navbar = ({ currentTab, setTab, openAuthModal, bookmarkCount }) => {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <nav className="navbar">
      <div className="nav-content">
        <div className="brand-logo" onClick={() => setTab('explorer')} title="ProteinPro Home">
          <img src="/logo.png" alt="PROTEINPRO Logo" className="brand-logo-img" />
        </div>

        <div className="nav-links">
          <button
            className={`nav-item ${currentTab === 'explorer' ? 'active' : ''}`}
            onClick={() => setTab('explorer')}
          >
            <Search size={18} />
            <span>Explorer</span>
          </button>

          {isAuthenticated ? (
            <>
              <button
                className={`nav-item ${currentTab === 'bookmarks' ? 'active' : ''}`}
                onClick={() => setTab('bookmarks')}
              >
                <Bookmark size={18} />
                <span>Bookmarks</span>
                {bookmarkCount > 0 && <span className="badge">{bookmarkCount}</span>}
              </button>

              <button
                className={`nav-item ${currentTab === 'profile' ? 'active' : ''}`}
                onClick={() => setTab('profile')}
              >
                <User size={18} />
                <span>{user?.firstName ? `${user.firstName}` : 'Profile'}</span>
              </button>

              <button className="btn-secondary" onClick={logout}>
                <LogOut size={16} />
                <span>Logout</span>
              </button>
            </>
          ) : (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span className="tag" style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                <ShieldAlert size={14} /> Guest Mode
              </span>
              <button className="btn-primary" onClick={openAuthModal}>
                <LogIn size={16} />
                <span>Login / Register</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};
