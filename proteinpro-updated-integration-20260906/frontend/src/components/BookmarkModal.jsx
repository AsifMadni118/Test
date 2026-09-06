import React, { useState, useEffect } from 'react';
import { X, MessageSquare, Bookmark } from 'lucide-react';

export const BookmarkModal = ({ isOpen, onClose, onSave, item, existingBookmark }) => {
  const [comment, setComment] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (existingBookmark) {
      setComment(existingBookmark.comment || '');
    } else {
      setComment('');
    }
  }, [existingBookmark, item]);

  if (!isOpen || (!item && !existingBookmark)) return null;

  const title = existingBookmark
    ? 'Edit Bookmark Comment'
    : `Bookmark ${item?.source || 'Protein item'}`;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await onSave(comment);
      onClose();
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Bookmark className="w-5 h-5 text-emerald-400" size={20} color="var(--accent-primary)" />
            <h3 style={{ fontSize: '1.2rem' }}>{title}</h3>
          </div>
          <button className="close-btn" onClick={onClose}>
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="form-group">
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <MessageSquare size={14} /> Personal Analysis / Note (Required)
            </label>
            <textarea
              className="form-textarea"
              rows={4}
              placeholder="e.g. Compare later, preferred post-workout option, high protein ratio..."
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              required
            />
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={loading}>
              {loading ? 'Saving...' : existingBookmark ? 'Update Comment' : 'Save Bookmark'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
