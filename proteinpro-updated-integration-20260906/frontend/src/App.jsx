import React, { useState, useEffect, useCallback } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { bookmarkService } from './services/api';
import { Navbar } from './components/Navbar';
import { AuthModal } from './components/AuthModal';
import { BookmarkModal } from './components/BookmarkModal';
import { ProteinExplorer } from './pages/ProteinExplorer';
import { BookmarksPage } from './pages/BookmarksPage';
import { ProfilePage } from './pages/ProfilePage';
import { CheckCircle, AlertCircle } from 'lucide-react';

function AppContent() {
  const { token, isAuthenticated } = useAuth();
  const [currentTab, setCurrentTab] = useState('explorer');

  // Modals state
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isBookmarkModalOpen, setIsBookmarkModalOpen] = useState(false);
  
  // Selected item / bookmark for modal operations
  const [selectedProteinItem, setSelectedProteinItem] = useState(null);
  const [selectedBookmark, setSelectedBookmark] = useState(null);

  // Bookmarks State
  const [bookmarks, setBookmarks] = useState([]);
  const [bookmarksLoading, setBookmarksLoading] = useState(false);

  // Toast System
  const [toasts, setToasts] = useState([]);

  const addToast = (message, type = 'success') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 4000);
  };

  // Fetch bookmarks whenever user is authenticated
  const fetchBookmarks = useCallback(async () => {
    if (!token) {
      setBookmarks([]);
      return;
    }
    setBookmarksLoading(true);
    try {
      const data = await bookmarkService.getBookmarks(token);
      setBookmarks(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error('Error fetching bookmarks:', err);
    } finally {
      setBookmarksLoading(false);
    }
  }, [token]);

  useEffect(() => {
    fetchBookmarks();
  }, [fetchBookmarks]);

  // Open modal to add a new bookmark
  const handleOpenBookmarkModal = (proteinItem) => {
    if (!isAuthenticated) {
      setIsAuthModalOpen(true);
      return;
    }
    setSelectedProteinItem(proteinItem);
    setSelectedBookmark(null);
    setIsBookmarkModalOpen(true);
  };

  // Open modal to edit an existing bookmark comment
  const handleOpenEditCommentModal = (bookmark) => {
    setSelectedBookmark(bookmark);
    setSelectedProteinItem(bookmark.proteinData);
    setIsBookmarkModalOpen(true);
  };

  // Save new bookmark or update comment
  const handleSaveBookmark = async (commentText) => {
    if (!token) return;

    try {
      if (selectedBookmark) {
        // Edit existing bookmark comment
        await bookmarkService.updateComment(token, selectedBookmark.id, commentText);
        addToast('Bookmark comment updated!', 'success');
      } else if (selectedProteinItem) {
        // Create new bookmark
        const pId = String(selectedProteinItem.id);
        await bookmarkService.createBookmark(token, pId, selectedProteinItem, commentText);
        addToast('Supplement added to bookmarks!', 'success');
      }
      fetchBookmarks();
    } catch (err) {
      addToast(err.message || 'Bookmark operation failed', 'error');
    }
  };

  // Delete bookmark
  const handleDeleteBookmark = async (bookmarkId) => {
    if (!token) return;
    try {
      await bookmarkService.deleteBookmark(token, bookmarkId);
      addToast('Bookmark removed', 'success');
      fetchBookmarks();
    } catch (err) {
      addToast(err.message || 'Failed to remove bookmark', 'error');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar
        currentTab={currentTab}
        setTab={setCurrentTab}
        openAuthModal={() => setIsAuthModalOpen(true)}
        bookmarkCount={bookmarks.length}
      />

      <main className="app-container" style={{ flex: 1 }}>
        {currentTab === 'explorer' && (
          <ProteinExplorer
            bookmarks={bookmarks}
            onBookmarkClick={handleOpenBookmarkModal}
            onEditComment={handleOpenEditCommentModal}
            onDeleteBookmark={handleDeleteBookmark}
            openAuthModal={() => setIsAuthModalOpen(true)}
            addToast={addToast}
          />
        )}

        {currentTab === 'bookmarks' && (
          <BookmarksPage
            bookmarks={bookmarks}
            loading={bookmarksLoading}
            onEditComment={handleOpenEditCommentModal}
            onDeleteBookmark={handleDeleteBookmark}
            setTab={setCurrentTab}
          />
        )}

        {currentTab === 'profile' && (
          <ProfilePage addToast={addToast} />
        )}
      </main>

      {/* Auth Modal */}
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        addToast={addToast}
      />

      {/* Bookmark Modal */}
      <BookmarkModal
        isOpen={isBookmarkModalOpen}
        onClose={() => setIsBookmarkModalOpen(false)}
        onSave={handleSaveBookmark}
        item={selectedProteinItem}
        existingBookmark={selectedBookmark}
      />

      {/* Toast Notifications */}
      <div className="toast-container">
        {toasts.map(toast => (
          <div key={toast.id} className={`toast ${toast.type}`}>
            {toast.type === 'success' ? <CheckCircle size={18} /> : <AlertCircle size={18} />}
            <span>{toast.message}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}
