-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 2. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  auth_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
  username VARCHAR(50) UNIQUE NOT NULL,
  display_name VARCHAR(100),
  bio TEXT,
  profile_picture_url TEXT,
  phone_number VARCHAR(20) UNIQUE,
  status VARCHAR(255) DEFAULT 'Hey there! I am using Vello',
  status_updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  last_seen_at TIMESTAMP WITH TIME ZONE,
  is_online BOOLEAN DEFAULT FALSE,
  is_blocked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

  CONSTRAINT username_length CHECK (LENGTH(username) >= 3),
  CONSTRAINT display_name_length CHECK (LENGTH(display_name) >= 1)
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_is_online ON users(is_online);

-- 3. USER CONNECTIONS (Browse Peoples)
CREATE TABLE IF NOT EXISTS user_connections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id_1 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_id_2 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(20) DEFAULT 'connected'
    CHECK (status IN ('pending', 'connected', 'blocked', 'rejected')),
  initiated_by UUID NOT NULL REFERENCES users(id),
  connected_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

  CONSTRAINT unique_connection UNIQUE (LEAST(user_id_1, user_id_2), GREATEST(user_id_1, user_id_2)),
  CONSTRAINT different_users CHECK (user_id_1 != user_id_2)
);

-- 4. CONVERSATIONS
CREATE TABLE IF NOT EXISTS conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id_1 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_id_2 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  last_message_id UUID,
  last_message_text TEXT,
  last_message_timestamp TIMESTAMP WITH TIME ZONE,
  last_message_sender_id UUID,
  is_muted_by_1 BOOLEAN DEFAULT FALSE,
  is_muted_by_2 BOOLEAN DEFAULT FALSE,
  is_archived_by_1 BOOLEAN DEFAULT FALSE,
  is_archived_by_2 BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

  CONSTRAINT unique_conversation UNIQUE (LEAST(user_id_1, user_id_2), GREATEST(user_id_1, user_id_2)),
  CONSTRAINT different_users_convo CHECK (user_id_1 != user_id_2)
);

-- 5. MESSAGES
CREATE TABLE IF NOT EXISTS messages (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
  sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  recipient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  content TEXT NOT NULL,
  message_type VARCHAR(20) DEFAULT 'text'
    CHECK (message_type IN ('text', 'image', 'video', 'audio', 'file', 'location', 'contact')),
  media_url TEXT,
  media_thumbnail_url TEXT,
  delivery_status VARCHAR(20) DEFAULT 'pending'
    CHECK (delivery_status IN ('pending', 'sent', 'delivered', 'read', 'failed')),

  sent_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  delivered_at TIMESTAMP WITH TIME ZONE,
  read_at TIMESTAMP WITH TIME ZONE,

  is_edited BOOLEAN DEFAULT FALSE,
  edited_at TIMESTAMP WITH TIME ZONE,
  is_deleted BOOLEAN DEFAULT FALSE,
  deleted_at TIMESTAMP WITH TIME ZONE,
  is_forwarded BOOLEAN DEFAULT FALSE,
  original_message_id UUID REFERENCES messages(id),

  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. CALLS
CREATE TABLE IF NOT EXISTS calls (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations(id),
  initiator_id UUID NOT NULL REFERENCES users(id),
  recipient_id UUID NOT NULL REFERENCES users(id),
  call_type VARCHAR(20) DEFAULT 'audio'
    CHECK (call_type IN ('audio', 'video')),
  status VARCHAR(20) DEFAULT 'ringing'
    CHECK (status IN ('ringing', 'ongoing', 'ended', 'missed', 'declined')),

  started_at TIMESTAMP WITH TIME ZONE,
  ended_at TIMESTAMP WITH TIME ZONE,
  duration_seconds INTEGER DEFAULT 0,
  is_missed BOOLEAN DEFAULT FALSE,

  signal_server_session_id VARCHAR(255),
  initiator_offer JSONB,
  recipient_answer JSONB,

  created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 7. RLS POLICIES
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_connections ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE calls ENABLE ROW LEVEL SECURITY;

-- Users can view all profiles
CREATE POLICY "Users can view profiles" ON users FOR SELECT USING (true);
-- Users can only update their own profile
CREATE POLICY "Users can update own profile" ON users FOR UPDATE
  USING (auth.uid() = auth_id) WITH CHECK (auth.uid() = auth_id);

-- Conversations visibility
CREATE POLICY "Users can view own conversations" ON conversations FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM users
      WHERE auth_id = auth.uid()
      AND (id = conversations.user_id_1 OR id = conversations.user_id_2)
    )
  );

-- Messages visibility
CREATE POLICY "Users can view messages in their conversations" ON messages FOR SELECT
  USING (
    EXISTS (
      SELECT 1 FROM conversations c
      JOIN users u ON (u.id = c.user_id_1 OR u.id = c.user_id_2)
      WHERE c.id = messages.conversation_id AND u.auth_id = auth.uid()
    )
  );

-- 8. TRIGGERS
-- Auto-create conversation when connection is accepted
CREATE OR REPLACE FUNCTION handle_connection_accepted()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.status = 'connected' AND OLD.status != 'connected' THEN
    INSERT INTO conversations (user_id_1, user_id_2)
    VALUES (
      LEAST(NEW.user_id_1, NEW.user_id_2),
      GREATEST(NEW.user_id_1, NEW.user_id_2)
    )
    ON CONFLICT (LEAST(user_id_1, user_id_2), GREATEST(user_id_1, user_id_2))
    DO NOTHING;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER connection_accepted_trigger
AFTER UPDATE ON user_connections
FOR EACH ROW
EXECUTE FUNCTION handle_connection_accepted();
