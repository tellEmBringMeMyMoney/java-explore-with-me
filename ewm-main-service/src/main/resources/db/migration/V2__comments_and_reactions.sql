CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    text VARCHAR(2000) NOT NULL CHECK (length(trim(text)) > 0),
    created TIMESTAMP NOT NULL,
    updated TIMESTAMP NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(id),
    event_id BIGINT NOT NULL REFERENCES events(id)
);
CREATE INDEX ix_comments_event_created ON comments(event_id, created DESC, id DESC);
CREATE INDEX ix_comments_author ON comments(author_id);

CREATE TABLE comment_reactions (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comments(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    reaction_type VARCHAR(8) NOT NULL CHECK (reaction_type IN ('FIRE', 'AHH')),
    CONSTRAINT uq_comment_reaction_user UNIQUE (comment_id, user_id)
);
CREATE INDEX ix_comment_reactions_comment_type ON comment_reactions(comment_id, reaction_type);
