import React from 'react';
import { ProteinCard } from '../components/ProteinCard';
import { Bookmark, Sparkles } from 'lucide-react';

export const BookmarksPage = ({
  bookmarks,
  loading,
  onEditComment,
  onDeleteBookmark,
  setTab
}) => {
  if (loading) {
    return (
      <div className="empty-state">
        <Bookmark size={36} />
        <p>Loading your saved bookmarks...</p>
      </div>
    );
  }

  if (!bookmarks || bookmarks.length === 0) {
    return (
      <div className="glass-panel empty-state">
        <Bookmark size={48} />
        <h2>No Saved Bookmarks Yet</h2>
        <p style={{ marginTop: '0.5rem', marginBottom: '1.5rem' }}>
          Explore the Protein API catalog and save products with custom analysis comments.
        </p>
        <button className="btn-primary" onClick={() => setTab('explorer')}>
          <Sparkles size={16} /> Explore Protein Products
        </button>
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2.2rem' }}>
          My Bookmarked <span className="hero-gradient">Supplements</span>
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: '0.4rem' }}>
          You have {bookmarks.length} saved protein {bookmarks.length === 1 ? 'record' : 'records'} with personal comments.
        </p>
      </div>

      <div className="cards-grid">
        {bookmarks.map((b) => (
          <ProteinCard
            key={b.id}
            protein={b.proteinData}
            bookmark={b}
            onEditComment={onEditComment}
            onDeleteBookmark={onDeleteBookmark}
          />
        ))}
      </div>
    </div>
  );
};
