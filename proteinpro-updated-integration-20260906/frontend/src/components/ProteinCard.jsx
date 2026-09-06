import React, { useState, useEffect, useRef } from 'react';
import { Bookmark, Edit3, Trash2 } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const ProteinCard = ({
  protein,
  bookmark,
  onBookmarkClick,
  onEditComment,
  onDeleteBookmark,
  openAuthModal,
  index = 0
}) => {
  const { isAuthenticated } = useAuth();
  const [isVisible, setIsVisible] = useState(false);
  const cardRef = useRef(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          if (cardRef.current) {
            observer.unobserve(cardRef.current);
          }
        }
      },
      {
        threshold: 0.1,
        rootMargin: '0px 0px -30px 0px'
      }
    );

    const currentElem = cardRef.current;
    if (currentElem) {
      observer.observe(currentElem);
    }

    return () => {
      if (currentElem) {
        observer.unobserve(currentElem);
      }
    };
  }, []);

  // ProteinResponse and BookmarkResponse share the same typed protein snapshot shape.
  const item = bookmark ? bookmark.proteinData : protein;
  const proteinId = String(item.id);
  const source = item.source;
  
  // Real API fields: cost_grams (number), cost_package (string e.g. "$42.74/tub")
  const costGrams = item.cost_grams !== undefined ? `$${item.cost_grams}/g` : null;
  const costPackage = item.cost_package;

  // Real API fields: vegetarian ("T"/"F"), vegen ("T"/"F")
  const isVegetarian = item.vegetarian === 'T';
  const isVegan = item.vegen === 'T';

  // Real API field: "protein_ per_pack" or "protein_per_pack"
  const proteinPerPack = `${item['protein_ per_pack']}g`;

  const isBookmarked = !!bookmark;

  const handleBookmarkBtnClick = () => {
    if (!isAuthenticated) {
      openAuthModal();
      return;
    }
    if (onBookmarkClick) {
      onBookmarkClick(item);
    }
  };

  return (
    <div
      ref={cardRef}
      className={`glass-panel protein-card lazy-card ${isVisible ? 'visible' : ''}`}
      style={{ transitionDelay: isVisible ? `${(index % 6) * 0.08}s` : '0s' }}
    >
      <div className="protein-header">
        <div>
          <span className="protein-brand">ID: {proteinId}</span>
          <h3 className="protein-title">{source}</h3>
        </div>
        <div style={{ textAlign: 'right' }}>
          <span className="protein-price">{costPackage || costGrams || '$0.00'}</span>
          {costGrams && costPackage && (
            <div style={{ fontSize: '0.75rem', color: 'var(--accent-secondary)', fontWeight: 600, marginTop: '0.1rem' }}>
              {costGrams}
            </div>
          )}
        </div>
      </div>

      <div className="protein-tags">
        <span className="tag tag-source">{source.split(' ')[0]}</span>
        {isVegetarian ? (
          <span className="tag tag-veg">✓ Vegetarian</span>
        ) : (
          <span className="tag tag-nonveg">Non-Veg</span>
        )}
        {isVegan && <span className="tag tag-veg">🌱 Vegan</span>}
      </div>

      <div className="nutrition-specs" style={{ gridTemplateColumns: 'repeat(2, 1fr)' }}>
        <div className="spec-item">
          <span className="label">Protein per Pack</span>
          <div className="value" style={{ color: 'var(--accent-primary)' }}>{proteinPerPack}</div>
        </div>
        <div className="spec-item">
          <span className="label">Cost / Gram</span>
          <div className="value">{costGrams || 'N/A'}</div>
        </div>
      </div>

      {bookmark && bookmark.comment && (
        <div className="bookmark-comment-box">
          "{bookmark.comment}"
        </div>
      )}

      <div className="card-footer">
        {bookmark ? (
          <>
            <button
              className="btn-secondary"
              style={{ flex: 1 }}
              onClick={() => onEditComment(bookmark)}
            >
              <Edit3 size={15} /> Edit Note
            </button>
            <button
              className="btn-secondary btn-danger"
              onClick={() => onDeleteBookmark(bookmark.id)}
              title="Remove Bookmark"
            >
              <Trash2 size={16} />
            </button>
          </>
        ) : (
          <button
            className={isBookmarked ? 'btn-secondary' : 'btn-primary'}
            style={{ width: '100%' }}
            onClick={handleBookmarkBtnClick}
          >
            <Bookmark size={16} fill={isBookmarked ? 'currentColor' : 'none'} />
            <span>{isBookmarked ? 'Bookmarked' : 'Bookmark Item'}</span>
          </button>
        )}
      </div>
    </div>
  );
};
