CREATE TABLE users (id BIGSERIAL PRIMARY KEY, name VARCHAR(250) NOT NULL, email VARCHAR(254) NOT NULL UNIQUE);
CREATE TABLE categories (id BIGSERIAL PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE events (
 id BIGSERIAL PRIMARY KEY, annotation VARCHAR(2000) NOT NULL, description VARCHAR(7000), event_date TIMESTAMP NOT NULL,
 created_on TIMESTAMP NOT NULL, published_on TIMESTAMP, lat FLOAT NOT NULL, lon FLOAT NOT NULL, paid BOOLEAN NOT NULL,
 participant_limit INTEGER NOT NULL CHECK (participant_limit >= 0), request_moderation BOOLEAN NOT NULL,
 title VARCHAR(120) NOT NULL, category_id BIGINT NOT NULL REFERENCES categories(id), initiator_id BIGINT NOT NULL REFERENCES users(id),
 state VARCHAR(16) NOT NULL CHECK (state IN ('PENDING','PUBLISHED','CANCELED'))
);
CREATE INDEX ix_events_date_state ON events(event_date, state);
CREATE INDEX ix_events_category ON events(category_id);
CREATE INDEX ix_events_initiator ON events(initiator_id);
CREATE TABLE compilations (id BIGSERIAL PRIMARY KEY, title VARCHAR(50) NOT NULL, pinned BOOLEAN NOT NULL);
CREATE TABLE compilation_events (compilation_id BIGINT NOT NULL REFERENCES compilations(id) ON DELETE CASCADE,
 event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE, PRIMARY KEY(compilation_id,event_id));
CREATE TABLE participation_requests (id BIGSERIAL PRIMARY KEY, created TIMESTAMP NOT NULL, event_id BIGINT NOT NULL REFERENCES events(id),
 requester_id BIGINT NOT NULL REFERENCES users(id), status VARCHAR(16) NOT NULL,
 CONSTRAINT uq_request_event_user UNIQUE(event_id, requester_id));
CREATE INDEX ix_request_event_status ON participation_requests(event_id,status);
CREATE INDEX ix_request_requester ON participation_requests(requester_id);
