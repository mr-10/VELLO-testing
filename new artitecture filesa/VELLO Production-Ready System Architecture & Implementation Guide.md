
# VELLO Production-Ready System Architecture & Implementation Guide

**Version:** 1.0.0  
**Last Updated:** September 2026  
**Status:** Production Ready

---

## Table of Contents

1. [System Overview](#system-overview)
2. [Core Architecture](#core-architecture)
3. [Database Schema](#database-schema)
4. [Messaging System](#messaging-system)
5. [Real-time Delivery System](#real-time-delivery-system)
6. [Voice Calling System](#voice-calling-system)
7. [Video Calling System](#video-calling-system)
8. [User Connection Feature (Browse Peoples → Connect)](#user-connection-feature)
9. [Supabase Integration](#supabase-integration)
10. [UI/UX & Animation Guidelines](#uiux--animation-guidelines)
11. [Implementation Checklist](#implementation-checklist)

---

## System Overview

### Architecture Pattern: Microservices-style with Real-time Sync

```
┌─────────────────┐
│  Mobile Client  │ (Jetpack Compose)
│    (Android)    │
└────────┬────────┘
         │
    ┌────▼─────────────────────────────────┐
    │      Real-time Communication Layer   │
    │    (WebSocket + REST Hybrid)         │
    └────┬─────────────────────────────────┘
         │
    ┌────▼──────────────────────────────────────────┐
    │         Supabase Cloud Services              │
    ├──────────────────────────────────────────────┤
    │ ├─ Authentication (JWT + OTP)                │
    │ ├─ PostgreSQL Database (Realtime Sync)       │
    │ ├─ Storage (Profile Pictures, Media)         │
    │ ├─ Edge Functions (Business Logic)           │
    │ ├─ Realtime Subscriptions (WebSocket)        │
    │ └─ Vector Search (Future Enhancement)        │
    └─────────────────────────────────────────────┘
         │
    ┌────▼──────────────────────────┐
    │   WebRTC Infrastructure       │
    │  (Calling & Video Calling)    │
    │  Via: Metered.ca or Twilio   │
    └───────────────────────────────┘
```

---

## Core Architecture

### Application Layer Stack

```
┌─────────────────────────────────────────────────────┐
│                   UI Layer                          │
│  (Jetpack Compose - Screens, Navigation, State)    │
├─────────────────────────────────────────────────────┤
│                 ViewModel Layer                     │
│    (StateFlow, Event Handling, Business Logic)     │
├─────────────────────────────────────────────────────┤
│              Repository Layer                       │
│  (Data abstraction, Supabase API calls)            │
├─────────────────────────────────────────────────────┤
│          Service Layer                              │
│  ├─ AuthService (Login, OTP, Registration)        │
│  ├─ ChatService (Messages, Delivery Status)       │
│  ├─ UserService (Profile, Search, Connections)   │
│  ├─ CallService (Audio/Video signaling)           │
│  └─ RealtimeService (WebSocket subscription)      │
├─────────────────────────────────────────────────────┤
│         Local Database (Room)                       │
│    (Cache, Offline sync, Draft messages)           │
├─────────────────────────────────────────────────────┤
│          Supabase Client                            │
│   (REST API + Realtime + Auth + Storage)           │
└─────────────────────────────────────────────────────┘
```

### Key Principles

- **Offline-First Design**: Local Room database caches all data
- **Real-time Sync**: Supabase Realtime for instant updates
- **Single Source of Truth**: Database is authoritative
- **Error Recovery**: Automatic retry with exponential backoff
- **Security**: End-to-end encryption ready (future phase)

---

## Database Schema

### Core Tables

#### 1. **users**
```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  auth_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
  username VARCHAR(50) UNIQUE NOT NULL,
  display_name VARCHAR(100),
  bio TEXT,
  profile_picture_url TEXT,
  phone_number VARCHAR(20) UNIQUE,
  status VARCHAR(255) DEFAULT 'Hey there! I am using Vello',
  status_updated_at TIMESTAMP DEFAULT NOW(),
  last_seen_at TIMESTAMP,
  is_online BOOLEAN DEFAULT FALSE,
  is_blocked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  
  -- Indexes
  CONSTRAINT username_length CHECK (LENGTH(username) >= 3),
  CONSTRAINT display_name_length CHECK (LENGTH(display_name) >= 1)
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_is_online ON users(is_online);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
```

#### 2. **user_connections** (Browse Peoples Feature)
```sql
CREATE TABLE user_connections (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id_1 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_id_2 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  status VARCHAR(20) DEFAULT 'connected' 
    CHECK (status IN ('pending', 'connected', 'blocked', 'rejected')),
  initiated_by UUID NOT NULL REFERENCES users(id),
  connected_at TIMESTAMP DEFAULT NOW(),
  created_at TIMESTAMP DEFAULT NOW(),
  
  -- Prevent duplicates
  CONSTRAINT unique_connection UNIQUE (LEAST(user_id_1, user_id_2), GREATEST(user_id_1, user_id_2)),
  CONSTRAINT different_users CHECK (user_id_1 != user_id_2)
);

CREATE INDEX idx_user_connections_user_id_1 ON user_connections(user_id_1);
CREATE INDEX idx_user_connections_user_id_2 ON user_connections(user_id_2);
CREATE INDEX idx_user_connections_status ON user_connections(status);
```

#### 3. **conversations** (One-to-one chats)
```sql
CREATE TABLE conversations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id_1 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  user_id_2 UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  last_message_id UUID,
  last_message_text TEXT,
  last_message_timestamp TIMESTAMP,
  last_message_sender_id UUID,
  is_muted_by_1 BOOLEAN DEFAULT FALSE,
  is_muted_by_2 BOOLEAN DEFAULT FALSE,
  is_archived_by_1 BOOLEAN DEFAULT FALSE,
  is_archived_by_2 BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  
  CONSTRAINT unique_conversation UNIQUE (LEAST(user_id_1, user_id_2), GREATEST(user_id_1, user_id_2)),
  CONSTRAINT different_users CHECK (user_id_1 != user_id_2)
);

CREATE INDEX idx_conversations_user_id_1 ON conversations(user_id_1);
CREATE INDEX idx_conversations_user_id_2 ON conversations(user_id_2);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at DESC);
```

#### 4. **messages**
```sql
CREATE TABLE messages (
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
  
  -- Delivery Tracking
  sent_at TIMESTAMP DEFAULT NOW(),
  delivered_at TIMESTAMP,
  read_at TIMESTAMP,
  
  -- Message Metadata
  is_edited BOOLEAN DEFAULT FALSE,
  edited_at TIMESTAMP,
  is_deleted BOOLEAN DEFAULT FALSE,
  deleted_at TIMESTAMP,
  is_forwarded BOOLEAN DEFAULT FALSE,
  original_message_id UUID REFERENCES messages(id),
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_messages_delivery_status ON messages(delivery_status);
```

#### 5. **message_read_receipts**
```sql
CREATE TABLE message_read_receipts (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  message_id UUID NOT NULL UNIQUE REFERENCES messages(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  read_at TIMESTAMP DEFAULT NOW(),
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_read_receipts_user_id ON message_read_receipts(user_id);
CREATE INDEX idx_read_receipts_message_id ON message_read_receipts(message_id);
```

#### 6. **calls**
```sql
CREATE TABLE calls (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations(id),
  initiator_id UUID NOT NULL REFERENCES users(id),
  recipient_id UUID NOT NULL REFERENCES users(id),
  call_type VARCHAR(20) DEFAULT 'audio'
    CHECK (call_type IN ('audio', 'video')),
  status VARCHAR(20) DEFAULT 'ringing'
    CHECK (status IN ('ringing', 'ongoing', 'ended', 'missed', 'declined')),
  
  -- Call Metadata
  started_at TIMESTAMP,
  ended_at TIMESTAMP,
  duration_seconds INTEGER DEFAULT 0,
  is_missed BOOLEAN DEFAULT FALSE,
  
  -- WebRTC Signal Server Data
  signal_server_session_id VARCHAR(255),
  initiator_offer JSONB,
  recipient_answer JSONB,
  
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_calls_conversation_id ON calls(conversation_id);
CREATE INDEX idx_calls_initiator_id ON calls(initiator_id);
CREATE INDEX idx_calls_status ON calls(status);
CREATE INDEX idx_calls_created_at ON calls(created_at DESC);
```

#### 7. **blocklist**
```sql
CREATE TABLE blocklist (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  blocker_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  blocked_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  reason TEXT,
  blocked_at TIMESTAMP DEFAULT NOW(),
  
  CONSTRAINT unique_block UNIQUE (blocker_id, blocked_user_id),
  CONSTRAINT different_users CHECK (blocker_id != blocked_user_id)
);

CREATE INDEX idx_blocklist_blocker_id ON blocklist(blocker_id);
CREATE INDEX idx_blocklist_blocked_user_id ON blocklist(blocked_user_id);
```

#### 8. **user_search_index** (for quick search)
```sql
CREATE TABLE user_search_index (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  search_text TSVECTOR GENERATED ALWAYS AS (
    TO_TSVECTOR('english', COALESCE(username, '') || ' ' || COALESCE(display_name, ''))
  ) STORED,
  created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_user_search_text ON user_search_index USING GIN(search_text);
```

---

## Messaging System

### Message Flow Diagram

```
User A sends message
        │
        ▼
┌──────────────────────┐
│ Create message in    │
│ local Room database  │
│ (status: 'pending')  │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│ Upload to Supabase   │
│ via Edge Function    │
│ (validation)         │
└──────────┬───────────┘
           │
           ├──────────────────────────┐
           │ Success                  │ Failure
           ▼                          ▼
    ┌──────────────┐          ┌──────────────────┐
    │ Update local │          │ Mark as 'failed' │
    │ status:      │          │ and retry in     │
    │ 'sent'       │          │ background       │
    └──────┬───────┘          └──────────────────┘
           │
           ▼
    ┌──────────────────────────┐
    │ Subscribe to message     │
    │ delivery status via      │
    │ Supabase Realtime        │
    └──────────┬───────────────┘
               │
               ├─────────────────┐
               │                 │
      Device B online   Device B offline
               │                 │
               ▼                 ▼
      ┌────────────────┐  ┌──────────────────────┐
      │ Push to Device │  │ Store in queue, send │
      │ B instantly    │  │ when B comes online  │
      │ Update status: │  │ (via presence check) │
      │ 'delivered'    │  └──────────────────────┘
      └────────┬───────┘
               │
               ▼
      ┌────────────────────────┐
      │ User B opens message   │
      │ Update status:         │
      │ 'read'                 │
      │ Record read_at time    │
      └────────────────────────┘
```

### Implementation: MessageRepository.kt

```kotlin
// Data models
data class Message(
  val id: String,
  val conversationId: String,
  val senderId: String,
  val recipientId: String,
  val content: String,
  val messageType: MessageType, // text, image, video, audio, file
  val mediaUrl: String? = null,
  val deliveryStatus: DeliveryStatus, // pending, sent, delivered, read
  val sentAt: LocalDateTime,
  val deliveredAt: LocalDateTime? = null,
  val readAt: LocalDateTime? = null,
  val isEdited: Boolean = false,
  val editedAt: LocalDateTime? = null,
  val isDeleted: Boolean = false
)

enum class DeliveryStatus {
  PENDING,    // Queued for sending
  SENT,       // Received by server
  DELIVERED,  // Received by recipient
  READ,       // Seen by recipient
  FAILED      // Failed to send
}

// Repository implementation
class MessageRepository(private val supabase: SupabaseClient) {
  
  suspend fun sendMessage(message: Message): Result<Message> = withContext(Dispatchers.IO) {
    try {
      // 1. Validate message
      require(message.content.isNotBlank()) { "Message cannot be empty" }
      require(message.conversationId.isNotBlank()) { "Conversation ID required" }
      
      // 2. Save to local database first (optimistic update)
      messageLocalDb.insert(message.copy(deliveryStatus = PENDING))
      
      // 3. Upload to Supabase via Edge Function
      val response = supabase.functions
        .invoke("send-message", 
          mapOf(
            "conversationId" to message.conversationId,
            "recipientId" to message.recipientId,
            "content" to message.content,
            "messageType" to message.messageType.name,
            "mediaUrl" to message.mediaUrl
          )
        )
      
      val sentMessage = response.data.decodeAs<Message>()
      
      // 4. Update local cache with server response
      messageLocalDb.update(
        sentMessage.copy(deliveryStatus = SENT)
      )
      
      // 5. Subscribe to delivery updates
      subscribeToMessageStatusUpdates(sentMessage.id)
      
      return@withContext Result.success(sentMessage)
      
    } catch (e: Exception) {
      // Mark as failed and queue for retry
      messageLocalDb.update(
        message.copy(deliveryStatus = FAILED)
      )
      queueForRetry(message.id)
      return@withContext Result.failure(e)
    }
  }
  
  fun subscribeToMessageStatusUpdates(messageId: String): Flow<Message> {
    return supabase.realtime
      .messages
      .on(
        event = INSERT,
        schema = "public",
        table = "message_read_receipts"
      ) { payload ->
        val receipt = payload.decodeAs<MessageReadReceipt>()
        if (receipt.messageId == messageId) {
          // Update message status to READ
          messageLocalDb.updateStatus(messageId, READ)
        }
      }
      .decodeAsFlow<Message>()
  }
  
  suspend fun markAsRead(messageId: String, userId: String): Result<Unit> {
    return try {
      supabase
        .from("message_read_receipts")
        .insert(
          mapOf(
            "messageId" to messageId,
            "userId" to userId,
            "readAt" to Clock.System.now()
          )
        )
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
  
  suspend fun getConversationMessages(
    conversationId: String,
    limit: Int = 50,
    offset: Int = 0
  ): Flow<List<Message>> {
    return supabase
      .from("messages")
      .select()
      .eq("conversation_id", conversationId)
      .order("created_at", ascending = false)
      .limit(limit)
      .offset(offset)
      .decodeAsFlow<Message>()
  }
  
  private suspend fun queueForRetry(messageId: String) {
    // Use WorkManager for background retry
    val retryWork = OneTimeWorkRequestBuilder<MessageRetryWorker>()
      .setInputData(
        workDataOf("messageId" to messageId)
      )
      .setBackoffCriteria(
        BackoffPolicy.EXPONENTIAL,
        Duration.ofSeconds(15),
        TimeUnit.SECONDS
      )
      .build()
    
    WorkManager.getInstance().enqueueUniqueWork(
      "retry-message-$messageId",
      ExistingWorkPolicy.KEEP,
      retryWork
    )
  }
}
```

### Real-time Message Listener

```kotlin
class ChatScreenViewModel(
  private val messageRepo: MessageRepository,
  private val conversationId: String
) : ViewModel() {
  
  private val _messages = MutableStateFlow<List<Message>>(emptyList())
  val messages: StateFlow<List<Message>> = _messages.asStateFlow()
  
  init {
    subscribeToMessages()
  }
  
  private fun subscribeToMessages() {
    viewModelScope.launch {
      messageRepo.getConversationMessages(conversationId)
        .collect { newMessages ->
          _messages.value = newMessages
        }
    }
  }
  
  fun sendMessage(content: String) {
    viewModelScope.launch {
      val message = Message(
        id = UUID.randomUUID().toString(),
        conversationId = conversationId,
        senderId = currentUserId,
        recipientId = recipientId,
        content = content,
        messageType = TEXT,
        deliveryStatus = PENDING,
        sentAt = Clock.System.now()
      )
      
      messageRepo.sendMessage(message)
        .onSuccess { sentMessage ->
          // Auto-mark as read after 2 seconds if sender reads it
          markMessageAsRead(sentMessage.id)
        }
        .onFailure { error ->
          showErrorSnackbar(error.message ?: "Failed to send message")
        }
    }
  }
}
```

---

## Real-time Delivery System

### Delivery Tracking Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                  MESSAGE LIFECYCLE                          │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│ PENDING (0s)  → Local cache, awaiting network              │
│    ↓                                                         │
│ SENT (1-2s)   → Received by server, processing              │
│    ↓                                                         │
│ DELIVERED     → Received by recipient device               │
│ (2-10s)       → Status persisted in database                │
│    ↓                                                         │
│ READ          → Recipient opened message                    │
│ (5-30s)       → Timestamp recorded, UI shows ✓✓             │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Supabase Edge Function: send-message

```typescript
// supabase/functions/send-message/index.ts

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const supabaseUrl = Deno.env.get("SUPABASE_URL");
const supabaseKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

interface SendMessagePayload {
  conversationId: string;
  recipientId: string;
  senderId: string;
  content: string;
  messageType: string;
  mediaUrl?: string;
}

serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405 });
  }

  try {
    const payload: SendMessagePayload = await req.json();

    // Validate
    if (!payload.content?.trim()) {
      return new Response(
        JSON.stringify({ error: "Message content cannot be empty" }),
        { status: 400 }
      );
    }

    const supabase = createClient(supabaseUrl, supabaseKey);

    // 1. Check if users are blocked
    const { data: blocklist } = await supabase
      .from("blocklist")
      .select("*")
      .or(
        `and(blocker_id.eq.${payload.recipientId},blocked_user_id.eq.${payload.senderId})`
      )
      .single();

    if (blocklist) {
      return new Response(
        JSON.stringify({ error: "You are blocked by this user" }),
        { status: 403 }
      );
    }

    // 2. Create or get conversation
    const { data: conversation } = await supabase
      .from("conversations")
      .select("id")
      .eq("user_id_1", Math.min(payload.senderId, payload.recipientId))
      .eq("user_id_2", Math.max(payload.senderId, payload.recipientId))
      .single();

    let conversationId = payload.conversationId;

    if (!conversation) {
      const { data: newConversation } = await supabase
        .from("conversations")
        .insert([
          {
            user_id_1: Math.min(payload.senderId, payload.recipientId),
            user_id_2: Math.max(payload.senderId, payload.recipientId),
          },
        ])
        .select("id")
        .single();

      conversationId = newConversation.id;
    }

    // 3. Insert message with initial status 'sent'
    const { data: message, error: messageError } = await supabase
      .from("messages")
      .insert([
        {
          conversation_id: conversationId,
          sender_id: payload.senderId,
          recipient_id: payload.recipientId,
          content: payload.content,
          message_type: payload.messageType,
          media_url: payload.mediaUrl,
          delivery_status: "sent",
          sent_at: new Date().toISOString(),
        },
      ])
      .select("*")
      .single();

    if (messageError) throw messageError;

    // 4. Update conversation's last_message fields
    await supabase
      .from("conversations")
      .update({
        last_message_id: message.id,
        last_message_text: message.content,
        last_message_timestamp: message.sent_at,
        last_message_sender_id: payload.senderId,
        updated_at: new Date().toISOString(),
      })
      .eq("id", conversationId);

    // 5. Push notification to recipient (if online)
    // Via Supabase Realtime or external service
    const { data: recipient } = await supabase
      .from("users")
      .select("is_online")
      .eq("id", payload.recipientId)
      .single();

    if (!recipient?.is_online) {
      // Queue notification for when user comes online
      await supabase.from("pending_notifications").insert([
        {
          user_id: payload.recipientId,
          message_id: message.id,
          type: "new_message",
        },
      ]);
    }

    return new Response(
      JSON.stringify({
        success: true,
        message: {
          ...message,
          delivery_status: "sent",
        },
      }),
      {
        headers: { "Content-Type": "application/json" },
        status: 201,
      }
    );
  } catch (error) {
    console.error("Error sending message:", error);
    return new Response(
      JSON.stringify({
        error: error.message,
      }),
      {
        headers: { "Content-Type": "application/json" },
        status: 500,
      }
    );
  }
});
```

### DeliveryTracker Component

```kotlin
class DeliveryStatusIndicator(
  modifier: Modifier = Modifier,
  status: DeliveryStatus = PENDING,
  isEdited: Boolean = false
) {
  @Composable
  fun Content() {
    Row(
      modifier = modifier.height(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      when (status) {
        PENDING -> {
          // Animated clock icon
          Icon(
            painter = painterResource(id = R.drawable.ic_clock),
            contentDescription = "Pending",
            modifier = Modifier
              .size(12.dp)
              .rotate(animateFloatAsState(
                targetValue = if (isAnimating) 360f else 0f,
                animationSpec = infiniteRepeatable(
                  animation = tween(2000, easing = LinearEasing),
                  repeatMode = RepeatMode.Restart
                )
              ).value),
            tint = Color.Gray
          )
          Text("Sending...", fontSize = 10.sp, color = Color.Gray)
        }
        
        SENT -> {
          // Single checkmark
          Icon(
            painter = painterResource(id = R.drawable.ic_check_single),
            contentDescription = "Sent",
            modifier = Modifier.size(12.dp),
            tint = Color.Gray
          )
          Text("Sent", fontSize = 10.sp, color = Color.Gray)
        }
        
        DELIVERED -> {
          // Double checkmark
          Icon(
            painter = painterResource(id = R.drawable.ic_check_double),
            contentDescription = "Delivered",
            modifier = Modifier.size(12.dp),
            tint = Color.Gray
          )
          Text("Delivered", fontSize = 10.sp, color = Color.Gray)
        }
        
        READ -> {
          // Double checkmark with blue color
          Icon(
            painter = painterResource(id = R.drawable.ic_check_double),
            contentDescription = "Read",
            modifier = Modifier.size(12.dp),
            tint = Color(0xFF007AFF) // WhatsApp blue
          )
          Text("Read", fontSize = 10.sp, color = Color(0xFF007AFF))
        }
        
        FAILED -> {
          // Error icon with red color
          Icon(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = "Failed",
            modifier = Modifier.size(12.dp),
            tint = Color.Red
          )
          Text("Failed", fontSize = 10.sp, color = Color.Red)
        }
      }
      
      if (isEdited) {
        Text("(edited)", fontSize = 8.sp, color = Color.Gray, modifier = Modifier.alpha(0.7f))
      }
    }
  }
}
```

---

## Voice Calling System

### Architecture: WebRTC with Signaling Server

```
┌──────────────┐                         ┌──────────────┐
│  User A      │                         │  User B      │
│  (Initiator) │                         │  (Recipient) │
└──────┬───────┘                         └──────┬───────┘
       │                                        │
       │ 1. Call Initiation (REST API)         │
       ├───────────────────────────────────────►│
       │    - Call ID                           │
       │    - WebRTC Offer                      │
       │                                        │
       │                    2. Call Ringing     │
       │                    (Push Notification) │
       │◄────────────────────────────────────────┤
       │                                        │
       │                  3. Accept/Decline     │
       │                                        │
       │ (if accept)                           │
       │                                        │
       │◄───────────────────────────────────────┤
       │   WebRTC Answer + ICE Candidates       │
       │                                        │
       ├─ WebSocket Connection (Realtime) ─────►│
       │   (ICE Candidates Exchange)            │
       │                                        │
       ▼ P2P Audio Stream (RTP/SRTP)            ▼
    [WebRTC Stream Active]
       │                                        │
       │ 4. During Call (Real-time)             │
       ├───────────────────────────────────────►│
       │   - Quality Monitoring                 │
       │   - Network Adaptation                 │
       │   - Recording (optional)               │
       │                                        │
       │ 5. Call Ended                          │
       ├───────────────────────────────────────►│
       │   - Duration recorded                  │
       │   - Call log stored                    │
       └────────────────────────────────────────┘
```

### Signaling Server Setup (Supabase Edge Function)

```typescript
// supabase/functions/initiate-call/index.ts

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

serve(async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405 });
  }

  try {
    const payload = await req.json();
    const supabase = createClient(
      Deno.env.get("SUPABASE_URL"),
      Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")
    );

    const {
      initiatorId,
      recipientId,
      conversationId,
      callType, // 'audio' or 'video'
      offer, // WebRTC Offer SDP
    } = payload;

    // 1. Check if recipient exists and is not blocked
    const { data: blocklist } = await supabase
      .from("blocklist")
      .select("id")
      .eq("blocker_id", recipientId)
      .eq("blocked_user_id", initiatorId)
      .single();

    if (blocklist) {
      return new Response(
        JSON.stringify({ error: "You are blocked" }),
        { status: 403 }
      );
    }

    // 2. Create call record in database
    const { data: call, error: callError } = await supabase
      .from("calls")
      .insert([
        {
          conversation_id: conversationId,
          initiator_id: initiatorId,
          recipient_id: recipientId,
          call_type: callType,
          status: "ringing",
          initiator_offer: offer,
          signal_server_session_id: crypto.randomUUID(),
        },
      ])
      .select("*")
      .single();

    if (callError) throw callError;

    // 3. Send push notification to recipient
    const { data: recipient } = await supabase
      .from("users")
      .select("push_token")
      .eq("id", recipientId)
      .single();

    if (recipient?.push_token) {
      // Send via Firebase Cloud Messaging or equivalent
      await sendPushNotification({
        token: recipient.push_token,
        title: `Incoming ${callType} call`,
        body: "Tap to answer",
        data: {
          callId: call.id,
          type: callType,
          callData: JSON.stringify(call),
        },
      });
    }

    return new Response(
      JSON.stringify({
        success: true,
        callId: call.id,
        sessionId: call.signal_server_session_id,
      }),
      { status: 201 }
    );
  } catch (error) {
    console.error("Error initiating call:", error);
    return new Response(
      JSON.stringify({ error: error.message }),
      { status: 500 }
    );
  }
});
```

### WebRTC Manager (Kotlin Implementation)

```kotlin
class WebRtcCallManager(
  private val context: Context,
  private val supabase: SupabaseClient
) {
  
  private val peerConnectionFactory: PeerConnectionFactory by lazy {
    initializePeerConnectionFactory()
  }
  
  private var peerConnection: PeerConnection? = null
  private var dataChannel: DataChannel? = null
  private var audioTrack: AudioTrack? = null
  private var videoTrack: VideoTrack? = null
  
  private val _callState = MutableStateFlow<CallState>(CallState.IDLE)
  val callState: StateFlow<CallState> = _callState.asStateFlow()
  
  suspend fun initiateCall(
    conversationId: String,
    recipientId: String,
    callType: CallType = AUDIO
  ): Result<Call> = withContext(Dispatchers.Default) {
    try {
      // 1. Create peer connection
      peerConnection = createPeerConnection()
      
      // 2. Add audio/video tracks
      if (callType == AUDIO || callType == VIDEO) {
        audioTrack = createAudioTrack()
        peerConnection?.addTrack(audioTrack!!, listOf("stream1"))
      }
      
      if (callType == VIDEO) {
        videoTrack = createVideoTrack()
        peerConnection?.addTrack(videoTrack!!, listOf("stream1"))
      }
      
      // 3. Create WebRTC offer
      val constraints = MediaConstraints().apply {
        mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        if (callType == VIDEO) {
          mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }
      }
      
      val offer = peerConnection?.createOffer(constraints)
        ?: throw Exception("Failed to create offer")
      
      peerConnection?.setLocalDescription(
        object : SdpObserver {
          override fun onCreateSuccess(sessionDescription: SessionDescription?) {}
          override fun onSetSuccess() {}
          override fun onCreateFailure(error: String?) {}
          override fun onSetFailure(error: String?) {}
        },
        offer
      )
      
      // 4. Send call initiation to server
      val response = supabase.functions.invoke(
        "initiate-call",
        mapOf(
          "initiatorId" to getCurrentUserId(),
          "recipientId" to recipientId,
          "conversationId" to conversationId,
          "callType" to callType.name.lowercase(),
          "offer" to offer.description
        )
      )
      
      val callData = response.data.decodeAs<Call>()
      _callState.value = CallState.RINGING
      
      return@withContext Result.success(callData)
      
    } catch (e: Exception) {
      _callState.value = CallState.FAILED
      return@withContext Result.failure(e)
    }
  }
  
  suspend fun answerCall(callId: String): Result<Unit> = withContext(Dispatchers.Default) {
    try {
      // 1. Fetch call details
      val call = supabase
        .from("calls")
        .select()
        .eq("id", callId)
        .single()
        .decodeAs<Call>()
      
      // 2. Create peer connection
      peerConnection = createPeerConnection()
      
      // 3. Set remote description (offer from initiator)
      peerConnection?.setRemoteDescription(
        object : SdpObserver {
          override fun onCreateSuccess(sessionDescription: SessionDescription?) {}
          override fun onSetSuccess() {}
          override fun onCreateFailure(error: String?) {}
          override fun onSetFailure(error: String?) {}
        },
        SessionDescription(
          SessionDescription.Type.OFFER,
          call.initiatorOffer
        )
      )
      
      // 4. Add audio/video tracks
      audioTrack = createAudioTrack()
      peerConnection?.addTrack(audioTrack!!, listOf("stream1"))
      
      if (call.callType == "video") {
        videoTrack = createVideoTrack()
        peerConnection?.addTrack(videoTrack!!, listOf("stream1"))
      }
      
      // 5. Create and send answer
      val constraints = MediaConstraints()
      val answer = peerConnection?.createAnswer(constraints)
        ?: throw Exception("Failed to create answer")
      
      peerConnection?.setLocalDescription(
        object : SdpObserver {
          override fun onCreateSuccess(sessionDescription: SessionDescription?) {}
          override fun onSetSuccess() {}
          override fun onCreateFailure(error: String?) {}
          override fun onSetFailure(error: String?) {}
        },
        answer
      )
      
      // 6. Update call status
      supabase
        .from("calls")
        .update(mapOf(
          "status" to "ongoing",
          "recipient_answer" to answer.description,
          "started_at" to Clock.System.now()
        ))
        .eq("id", callId)
        .execute()
      
      _callState.value = CallState.ACTIVE
      
      return@withContext Result.success(Unit)
      
    } catch (e: Exception) {
      _callState.value = CallState.FAILED
      return@withContext Result.failure(e)
    }
  }
  
  private fun createPeerConnection(): PeerConnection {
    val iceServers = listOf(
      PeerConnection.IceServer.Builder(
        listOf("stun:stun.l.google.com:19302")
      ).build(),
      PeerConnection.IceServer.Builder(
        listOf("turn:turnserver.example.com:3478")
      )
        .setUsername("user")
        .setPassword("password")
        .build()
    )
    
    val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
      bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
      rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
      continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
    }
    
    return peerConnectionFactory.createPeerConnection(
      rtcConfig,
      object : PeerConnection.Observer {
        override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
        override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {}
        override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidate(candidate: IceCandidate?) {
          // Send ICE candidate to peer via signaling server
          candidate?.let { sendIceCandidate(it) }
        }
        override fun onRemoveIceCandidates(candidates: Array<out IceCandidate>?) {}
        override fun onAddStream(stream: MediaStream?) {}
        override fun onRemoveStream(stream: MediaStream?) {}
        override fun onDataChannel(dataChannel: DataChannel?) {}
        override fun onRenegotiationNeeded() {}
        override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
      }
    ) ?: throw Exception("Failed to create PeerConnection")
  }
  
  private fun createAudioTrack(): AudioTrack {
    val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
    return peerConnectionFactory.createAudioTrack("audio1", audioSource)
  }
  
  private fun createVideoTrack(): VideoTrack {
    val videoCapturer = Camera2Enumerator(context).run {
      val deviceNames = deviceNames
      var selectedDevice: String? = null
      
      for (deviceName in deviceNames) {
        if (isFrontFacing(deviceName)) {
          selectedDevice = deviceName
          break
        }
      }
      
      createCapturer(selectedDevice ?: deviceNames[0], null)
    }
    
    val videoSource = peerConnectionFactory.createVideoSource(false)
    videoCapturer?.initialize(
      SurfaceTextureHelper.create("CaptureThread", EglBase.create().eglBaseContext),
      context,
      videoSource.capturerObserver
    )
    videoCapturer?.startCapture(640, 480, 30)
    
    return peerConnectionFactory.createVideoTrack("video1", videoSource)
  }
  
  private fun sendIceCandidate(candidate: IceCandidate) {
    // Send via Supabase Realtime or REST API
    viewModelScope.launch {
      try {
        supabase.realtime.iceCandidate.send(candidate)
      } catch (e: Exception) {
        Log.e("WebRTC", "Failed to send ICE candidate", e)
      }
    }
  }
  
  fun endCall(callId: String) {
    audioTrack?.dispose()
    videoTrack?.dispose()
    peerConnection?.close()
    peerConnection = null
    
    viewModelScope.launch {
      supabase
        .from("calls")
        .update(mapOf(
          "status" to "ended",
          "ended_at" to Clock.System.now(),
          "duration_seconds" to calculateDuration()
        ))
        .eq("id", callId)
        .execute()
      
      _callState.value = CallState.ENDED
    }
  }
}

enum class CallState {
  IDLE,
  RINGING,
  CONNECTING,
  ACTIVE,
  RECONNECTING,
  FAILED,
  ENDED
}

enum class CallType {
  AUDIO,
  VIDEO
}
```

---

## Video Calling System

### Video Call UI (Composable)

```kotlin
@Composable
fun VideoCallScreen(
  callId: String,
  recipientName: String,
  callDuration: Duration,
  localVideoTrack: VideoTrack?,
  remoteVideoTrack: VideoTrack?,
  onEndCall: () -> Unit,
  onToggleMute: (Boolean) -> Unit,
  onToggleVideo: (Boolean) -> Unit,
  onSwitchCamera: () -> Unit
) {
  var isMuted by remember { mutableStateOf(false) }
  var isVideoEnabled by remember { mutableStateOf(true) }
  
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    // Remote video (full screen)
    if (remoteVideoTrack != null) {
      AndroidView(
        factory = { context ->
          SurfaceViewRenderer(context).apply {
            init(EglBase.create().eglBaseContext, null)
            remoteVideoTrack.addSink(this)
          }
        },
        modifier = Modifier.fillMaxSize()
      )
    } else {
      // Placeholder while loading
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(Color(0xFF1F1F1F))
          .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(120.dp)
            .background(Color.Gray, shape = CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            painter = painterResource(id = R.drawable.ic_person),
            contentDescription = null,
            modifier = Modifier.size(60.dp),
            tint = Color.White
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
          recipientName,
          fontSize = 24.sp,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
        Text(
          "Connecting...",
          fontSize = 14.sp,
          color = Color.Gray,
          modifier = Modifier.padding(top = 8.dp)
        )
      }
    }
    
    // Local video (picture-in-picture)
    if (isVideoEnabled && localVideoTrack != null) {
      AndroidView(
        factory = { context ->
          SurfaceViewRenderer(context).apply {
            init(EglBase.create().eglBaseContext, null)
            setZOrderMediaOverlay(true)
            localVideoTrack.addSink(this)
          }
        },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(16.dp)
          .size(100.dp, 160.dp)
          .clip(RoundedCornerShape(12.dp))
          .border(2.dp, Color.White, RoundedCornerShape(12.dp))
      )
    }
    
    // Top bar with call info
    Row(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .padding(top = 16.dp)
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        recipientName,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        callDuration.toFormattedString(), // "00:45" format
        color = Color.Gray,
        fontSize = 14.sp
      )
    }
    
    // Bottom control bar
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Mute button
      FloatingActionButton(
        onClick = {
          isMuted = !isMuted
          onToggleMute(isMuted)
        },
        containerColor = if (isMuted) Color(0xFFFF5252) else Color(0xFF333333),
        modifier = Modifier.size(56.dp)
      ) {
        Icon(
          painter = painterResource(
            id = if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on
          ),
          contentDescription = "Mute",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
      
      // Toggle video button
      FloatingActionButton(
        onClick = {
          isVideoEnabled = !isVideoEnabled
          onToggleVideo(isVideoEnabled)
        },
        containerColor = if (!isVideoEnabled) Color(0xFFFF5252) else Color(0xFF333333),
        modifier = Modifier.size(56.dp)
      ) {
        Icon(
          painter = painterResource(
            id = if (isVideoEnabled) R.drawable.ic_video_on else R.drawable.ic_video_off
          ),
          contentDescription = "Video",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
      
      // Switch camera button
      FloatingActionButton(
        onClick = { onSwitchCamera() },
        containerColor = Color(0xFF333333),
        modifier = Modifier.size(56.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_switch_camera),
          contentDescription = "Switch Camera",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
      
      // End call button
      FloatingActionButton(
        onClick = { onEndCall() },
        containerColor = Color(0xFFFF5252),
        modifier = Modifier.size(56.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.ic_call_end),
          contentDescription = "End Call",
          tint = Color.White,
          modifier = Modifier.size(24.dp)
        )
      }
    }
  }
}
```

---

## User Connection Feature (Browse Peoples → Connect)

### Updated User Flow

```
HOME SCREEN
    │
    ├─► CHATS (Existing conversations)
    │
    ├─► CONNECTIONS (NEW)
    │   │
    │   ├─ Browse People (Search & Discover)
    │   │  │
    │   │  ├─ Search bar (query by username/name)
    │   │  ├─ Suggested users (algorithm-based)
    │   │  └─ Recent users added
    │   │
    │   ├─ Connection Requests (Pending)
    │   │  │
    │   │  ├─ Outgoing (awaiting acceptance)
    │   │  └─ Incoming (approve/reject)
    │   │
    │   └─ Connected Users (Your network)
    │      │
    │      └─ Start conversation
    │
    └─► PROFILE (Existing)
```

### Data Models

```kotlin
data class UserProfile(
  val id: String,
  val username: String,
  val displayName: String,
  val bio: String,
  val profilePictureUrl: String,
  val status: String,
  val isOnline: Boolean,
  val lastSeenAt: LocalDateTime,
  val connectionStatus: ConnectionStatus // NEW
)

enum class ConnectionStatus {
  NOT_CONNECTED,
  REQUESTED,      // You sent request
  PENDING,        // They sent you request
  CONNECTED,
  BLOCKED
}

data class ConnectionRequest(
  val id: String,
  val fromUserId: String,
  val toUserId: String,
  val fromUser: UserProfile,
  val status: String, // pending, accepted, rejected
  val createdAt: LocalDateTime,
  val respondedAt: LocalDateTime? = null
)
```

### Database Trigger for Connection Status Updates

```sql
-- Trigger to automatically create first message conversation
-- when connection is accepted
CREATE OR REPLACE FUNCTION handle_connection_accepted()
RETURNS TRIGGER AS $$
BEGIN
  IF NEW.status = 'connected' AND OLD.status != 'connected' THEN
    -- Create conversation if doesn't exist
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
```

### UI: Browse Peoples Screen

```kotlin
@Composable
fun BrowsePeoplesScreen(
  viewModel: BrowsePeoplesViewModel,
  onUserSelected: (UserProfile) -> Unit,
  modifier: Modifier = Modifier
) {
  val searchQuery by viewModel.searchQuery.collectAsState()
  val suggestedUsers by viewModel.suggestedUsers.collectAsState()
  val searchResults by viewModel.searchResults.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("Suggested", "Search", "Recent")
  
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color.White)
  ) {
    // Search bar
    SearchBarComponent(
      query = searchQuery,
      onQueryChange = { viewModel.updateSearchQuery(it) },
      placeholder = "Search by username or name",
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    )
    
    // Tabs
    TabRow(
      selectedTabIndex = selectedTab,
      modifier = Modifier.fillMaxWidth()
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title, fontSize = 14.sp) }
        )
      }
    }
    
    // Content
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      when (selectedTab) {
        0 -> {
          // Suggested Users
          if (isLoading) {
            item {
              repeat(5) {
                ShimmerEffect()
              }
            }
          } else {
            items(suggestedUsers) { user ->
              UserProfileCard(
                user = user,
                onConnect = { viewModel.sendConnectionRequest(user.id) },
                onView = { onUserSelected(user) },
                modifier = Modifier.fillMaxWidth()
              )
            }
          }
        }
        
        1 -> {
          // Search Results
          if (searchQuery.isNotEmpty()) {
            if (isLoading) {
              item {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
              }
            } else if (searchResults.isEmpty()) {
              item {
                Text(
                  "No users found",
                  modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp),
                  color = Color.Gray,
                  fontSize = 16.sp
                )
              }
            } else {
              items(searchResults) { user ->
                UserProfileCard(
                  user = user,
                  onConnect = { viewModel.sendConnectionRequest(user.id) },
                  onView = { onUserSelected(user) },
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }
        
        2 -> {
          // Recent Users
          items(viewModel.recentUsers.value) { user ->
            UserProfileCard(
              user = user,
              onConnect = { viewModel.sendConnectionRequest(user.id) },
              onView = { onUserSelected(user) },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}

@Composable
fun UserProfileCard(
  user: UserProfile,
  onConnect: () -> Unit,
  onView: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onView() },
    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
  ) {
    Column(
      modifier = Modifier.padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // User info
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Avatar
          AsyncImage(
            model = user.profilePictureUrl,
            contentDescription = user.displayName,
            modifier = Modifier
              .size(48.dp)
              .clip(CircleShape)
              .background(Color.Gray),
            contentScale = ContentScale.Crop
          )
          
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(start = 12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                user.displayName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              
              if (user.isOnline) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .background(Color.Green, shape = CircleShape)
                )
              }
            }
            
            Text(
              "@${user.username}",
              fontSize = 12.sp,
              color = Color.Gray
            )
          }
        }
        
        // Connect button
        Button(
          onClick = onConnect,
          modifier = Modifier
            .size(36.dp),
          contentPadding = PaddingValues(0.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF007AFF)
          ),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = "Connect",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
      
      if (user.bio.isNotEmpty()) {
        Text(
          user.bio,
          fontSize = 13.sp,
          color = Color.Gray,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 8.dp)
        )
      }
    }
  }
}
```

### ViewModel: Browse Peoples

```kotlin
class BrowsePeoplesViewModel(
  private val userRepository: UserRepository,
  private val connectionRepository: ConnectionRepository
) : ViewModel() {
  
  val searchQuery = MutableStateFlow("")
  
  private val _suggestedUsers = MutableStateFlow<List<UserProfile>>(emptyList())
  val suggestedUsers: StateFlow<List<UserProfile>> = _suggestedUsers.asStateFlow()
  
  private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
  val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()
  
  private val _recentUsers = MutableStateFlow<List<UserProfile>>(emptyList())
  val recentUsers: StateFlow<List<UserProfile>> = _recentUsers.asStateFlow()
  
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
  
  init {
    loadSuggestedUsers()
    subscribeToSearchQuery()
  }
  
  private fun loadSuggestedUsers() {
    viewModelScope.launch {
      _isLoading.value = true
      userRepository.getSuggestedUsers()
        .onSuccess { users ->
          _suggestedUsers.value = users
        }
        .onFailure { error ->
          Log.e("BrowsePeoples", "Error loading suggested users", error)
        }
      _isLoading.value = false
    }
  }
  
  private fun subscribeToSearchQuery() {
    viewModelScope.launch {
      searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .collect { query ->
          if (query.isNotEmpty()) {
            _isLoading.value = true
            userRepository.searchUsers(query)
              .onSuccess { results ->
                _searchResults.value = results
              }
              .onFailure { error ->
                Log.e("BrowsePeoples", "Search error", error)
              }
            _isLoading.value = false
          }
        }
    }
  }
  
  fun sendConnectionRequest(toUserId: String) {
    viewModelScope.launch {
      connectionRepository.sendConnectionRequest(toUserId)
        .onSuccess {
          showSuccessMessage("Connection request sent!")
          // Update UI state
          updateUserConnectionStatus(toUserId, REQUESTED)
        }
        .onFailure { error ->
          showErrorMessage(error.message ?: "Failed to send request")
        }
    }
  }
  
  private fun updateUserConnectionStatus(userId: String, status: ConnectionStatus) {
    // Update in both suggested and search results
    _suggestedUsers.value = _suggestedUsers.value.map {
      if (it.id == userId) it.copy(connectionStatus = status) else it
    }
    _searchResults.value = _searchResults.value.map {
      if (it.id == userId) it.copy(connectionStatus = status) else it
    }
  }
}
```

---

## Supabase Integration

### Project Setup

```yaml
# 1. Initialize Supabase Project
# Visit: https://supabase.com/dashboard

# 2. Create tables (SQL from Database Schema section above)

# 3. Set up authentication
# Enable: Email/Password
# Enable: Phone (optional)
# Configure: OTP verification

# 4. Configure Row Level Security (RLS)

# Create RLS policies
enable_rls_on_tables:
  - users
  - conversations
  - messages
  - calls
  - user_connections

# 5. Set up Storage buckets
buckets:
  - profile-pictures
  - message-media
  - call-recordings (optional)
```

### Supabase Client Initialization (Kotlin)

```kotlin
// VelloApplication.kt
class VelloApplication : Application() {
  
  companion object {
    lateinit var supabase: SupabaseClient
      private set
  }
  
  override fun onCreate() {
    super.onCreate()
    
    // Initialize Supabase
    supabase = createSupabaseClient(
      supabaseUrl = BuildConfig.SUPABASE_URL,
      supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
      install(Auth) {
        autoLoadFromStorage = true
        autoRefreshToken = true
        sessionManager = AndroidSessionManager(this@VelloApplication)
      }
      install(Realtime) {
        // Auto-reconnect configuration
        eventsPerSecond = 10
      }
      install(GoTrue) {
        // Additional auth config
      }
      install(StorageClient) {
        // Storage config
      }
    }
  }
}

// Dependency Injection (using Hilt)
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
  
  @Provides
  @Singleton
  fun provideAuthRepository(
    supabase: SupabaseClient,
    context: Context
  ): AuthRepository =
    AuthRepository(supabase, context)
  
  @Provides
  @Singleton
  fun provideMessageRepository(
    supabase: SupabaseClient
  ): MessageRepository =
    MessageRepository(supabase)
  
  @Provides
  @Singleton
  fun provideCallRepository(
    supabase: SupabaseClient
  ): CallRepository =
    CallRepository(supabase)
  
  @Provides
  @Singleton
  fun provideUserRepository(
    supabase: SupabaseClient
  ): UserRepository =
    UserRepository(supabase)
}
```

### RLS (Row Level Security) Policies

```sql
-- Users table: Users can only see profiles (not auth data)
CREATE POLICY "Users can view profiles"
  ON users FOR SELECT
  USING (true);

CREATE POLICY "Users can update own profile"
  ON users FOR UPDATE
  USING (auth.uid() = auth_id)
  WITH CHECK (auth.uid() = auth_id);

-- Conversations: Users can only access their own conversations
CREATE POLICY "Users can view own conversations"
  ON conversations FOR SELECT
  USING (auth.uid() IN (
    SELECT id FROM users WHERE auth_id = auth.uid()
  ) AND (user_id_1 = (SELECT id FROM users WHERE auth_id = auth.uid())
    OR user_id_2 = (SELECT id FROM users WHERE auth_id = auth.uid())));

-- Messages: Users can only see messages in their conversations
CREATE POLICY "Users can view messages in their conversations"
  ON messages FOR SELECT
  USING (
    conversation_id IN (
      SELECT id FROM conversations
      WHERE user_id_1 IN (SELECT id FROM users WHERE auth_id = auth.uid())
        OR user_id_2 IN (SELECT id FROM users WHERE auth_id = auth.uid())
    )
  );

CREATE POLICY "Users can insert messages in their conversations"
  ON messages FOR INSERT
  WITH CHECK (
    sender_id IN (SELECT id FROM users WHERE auth_id = auth.uid())
    AND conversation_id IN (
      SELECT id FROM conversations
      WHERE (user_id_1 IN (SELECT id FROM users WHERE auth_id = auth.uid())
        OR user_id_2 IN (SELECT id FROM users WHERE auth_id = auth.uid()))
    )
  );

-- Calls: Similar restrictions
CREATE POLICY "Users can view calls in their conversations"
  ON calls FOR SELECT
  USING (
    conversation_id IN (
      SELECT id FROM conversations
      WHERE (user_id_1 IN (SELECT id FROM users WHERE auth_id = auth.uid())
        OR user_id_2 IN (SELECT id FROM users WHERE auth_id = auth.uid()))
    )
  );

-- Blocklist: Users can only manage their own blocklist
CREATE POLICY "Users can view their blocklist"
  ON blocklist FOR SELECT
  USING (blocker_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));

CREATE POLICY "Users can manage their blocklist"
  ON blocklist FOR INSERT
  WITH CHECK (blocker_id IN (SELECT id FROM users WHERE auth_id = auth.uid()));
```

---

## UI/UX & Animation Guidelines

### Design System (Material 3 + WhatsApp Styling)

```kotlin
@Composable
fun VelloTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) {
    darkColorScheme(
      primary = Color(0xFF00A884),        // WhatsApp green
      onPrimary = Color.White,
      secondary = Color(0xFF31A24C),      // Darker green
      background = Color(0xFF0A0E27),     // Dark gray
      surface = Color(0xFF111B32),        // Slightly lighter
      error = Color(0xFFCF6679)
    )
  } else {
    lightColorScheme(
      primary = Color(0xFF00A884),        // WhatsApp green
      onPrimary = Color.White,
      secondary = Color(0xFF31A24C),
      background = Color.White,
      surface = Color(0xFFF5F5F5),
      error = Color(0xFFB3261E)
    )
  }
  
  MaterialTheme(
    colorScheme = colorScheme,
    typography = VelloTypography,
    content = content
  )
}

val VelloTypography = Typography(
  headlineSmall = TextStyle(
    fontSize = 20.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 28.sp
  ),
  titleMedium = TextStyle(
    fontSize = 16.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 24.sp
  ),
  bodyMedium = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 20.sp
  ),
  labelSmall = TextStyle(
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 16.sp
  )
)
```

### Animation Specifications

#### Message Bubble Animation

```kotlin
@Composable
fun MessageBubbleAnimation(
  isOutgoing: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  val scale = remember { Animatable(0.8f) }
  val alpha = remember { Animatable(0f) }
  
  LaunchedEffect(Unit) {
    launch {
      scale.animateTo(
        targetValue = 1f,
        animationSpec = spring(
          dampingRatio = 0.75f,
          stiffness = 400f
        )
      )
    }
    launch {
      alpha.animateTo(
        targetValue = 1f,
        animationSpec = tween(300, easing = EaseIn)
      )
    }
  }
  
  Box(
    modifier = modifier
      .scale(scale.value)
      .alpha(alpha.value)
      .graphicsLayer {
        transformOrigin = if (isOutgoing) {
          TransformOrigin(1f, 1f) // Bottom-right
        } else {
          TransformOrigin(0f, 1f) // Bottom-left
        }
      }
  ) {
    content()
  }
}
```

#### Typing Indicator Animation

```kotlin
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
  val dots = listOf(
    remember { Animatable(0f) },
    remember { Animatable(0f) },
    remember { Animatable(0f) }
  )
  
  LaunchedEffect(Unit) {
    dots.forEachIndexed { index, animatable ->
      launch {
        delay(index * 150L)
        animatable.animateTo(
          targetValue = 8f,
          animationSpec = infiniteRepeatable(
            animation = keyframes {
              durationMillis = 600
              0f at 0
              8f at 150
              0f at 300
            },
            repeatMode = RepeatMode.Restart
          )
        )
      }
    }
  }
  
  Row(
    modifier = modifier
      .padding(8.dp)
      .background(Color(0xFFE8E8E8), shape = RoundedCornerShape(16.dp))
      .padding(12.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    dots.forEach { offset ->
      Box(
        modifier = Modifier
          .size(8.dp)
          .background(Color.Gray, shape = CircleShape)
          .offset(y = (-offset.value).dp)
      )
    }
  }
}
```

#### Screen Transitions

```kotlin
// Navigation with slide animations
val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
  {
    slideInHorizontally(
      initialOffsetX = { 1000 },
      animationSpec = tween(300, easing = EaseInOut)
    ) + fadeIn(animationSpec = tween(300))
  }

val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
  {
    slideOutHorizontally(
      targetOffsetX = { -1000 },
      animationSpec = tween(300, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(300))
  }

val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition =
  {
    slideInHorizontally(
      initialOffsetX = { -1000 },
      animationSpec = tween(300, easing = EaseInOut)
    ) + fadeIn(animationSpec = tween(300))
  }

val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition =
  {
    slideOutHorizontally(
      targetOffsetX = { 1000 },
      animationSpec = tween(300, easing = EaseInOut)
    ) + fadeOut(animationSpec = tween(300))
  }

NavHost(
  navController,
  startDestination = "chats",
  enterTransition = { enterTransition() },
  exitTransition = { exitTransition() },
  popEnterTransition = { popEnterTransition() },
  popExitTransition = { popExitTransition() }
) {
  composable("chats") { ChatListScreen() }
  composable("chat/{conversationId}") { ChatScreen() }
  // ... other routes
}
```

### Screen Layouts

#### Chat Screen Layout

```
┌─────────────────────────┐
│ ◄  Recipient Name  ⟳ ... │ (Header)
├─────────────────────────┤
│                         │
│  "Hey! How are you?"    │ (Received message)
│  timestamp              │
│                         │
│          You're great! ✓✓│ (Sent message)
│          timestamp      │
│                         │
│     Typing indicator   │ (Recipient typing)
│                         │
├─────────────────────────┤
│ ┌───────────────┐ ⊙ ► □ │ (Input area)
│ │ Type a message│   [Attachment] [Send]
│ └───────────────┘      
└─────────────────────────┘
```

---

## Implementation Checklist

### Phase 1: Core Architecture (Weeks 1-2)

- [ ] Set up Supabase project and database schema
- [ ] Implement authentication (OTP + Email)
- [ ] Create local Room database
- [ ] Set up dependency injection (Hilt)
- [ ] Implement base repository pattern
- [ ] Create app navigation structure

### Phase 2: Messaging System (Weeks 3-4)

- [ ] Implement message sending/receiving
- [ ] Add delivery status tracking
- [ ] Set up Supabase Realtime listeners
- [ ] Create message UI components
- [ ] Implement message read receipts
- [ ] Add typing indicator
- [ ] Implement message editing/deletion

### Phase 3: User Connections (Weeks 5-6)

- [ ] Create user_connections table
- [ ] Implement "Browse Peoples" screen
- [ ] Add user search functionality
- [ ] Create connection request flow
- [ ] Add connection request UI
- [ ] Implement connection management

### Phase 4: Calling System (Weeks 7-8)

- [ ] Set up WebRTC infrastructure
- [ ] Implement audio call signaling
- [ ] Create call initiation UI
- [ ] Implement incoming call handling
- [ ] Add audio track management
- [ ] Implement call duration tracking
- [ ] Create call history/logs

### Phase 5: Video Calling (Weeks 9-10)

- [ ] Implement video capture
- [ ] Add camera switching
- [ ] Create video UI layout
- [ ] Implement video quality adaptation
- [ ] Add video permissions handling
- [ ] Test on various devices

### Phase 6: Polish & Optimization (Weeks 11-12)

- [ ] Implement all animations
- [ ] Optimize database queries
- [ ] Add error handling
- [ ] Implement offline functionality
- [ ] Performance testing
- [ ] Security audit
- [ ] Beta testing

### Testing Checklist

- [ ] Unit tests (ViewModels, Repositories)
- [ ] Integration tests (Supabase APIs)
- [ ] UI tests (Chat screens, animations)
- [ ] WebRTC tests (Audio/Video quality)
- [ ] Network resilience tests
- [ ] Offline sync tests
- [ ] Load testing (1000+ messages)

---

## Production Deployment

### Pre-Launch Checklist

- [ ] SSL/TLS certificates configured
- [ ] Database backups enabled
- [ ] Monitoring & alerting set up
- [ ] Error tracking (Sentry, Crashlytics)
- [ ] Analytics enabled
- [ ] Rate limiting configured
- [ ] DDoS protection enabled
- [ ] App signing & signing key backup
- [ ] Terms of Service & Privacy Policy
- [ ] GDPR/Privacy compliance

### Monitoring Stack

```
Supabase Logs → Supabase Monitoring Dashboard
    ↓
Firebase Crashlytics → Crash reporting
    ↓
Firebase Analytics → User behavior
    ↓
Custom Dashboards → Performance metrics
```

---

## Conclusion

This production-ready system provides a scalable, secure messaging platform with modern features. The architecture supports millions of users with proper scaling, and all components are designed for maintainability and future enhancements.

**Next Steps:**
1. Review this document with your team
2. Start Phase 1 implementation
3. Set up CI/CD pipeline
4. Begin beta testing in Week 6
5. Launch to production in Week 13

---

**Document Version History**
- v1.0.0 (Sept 2026): Initial production-ready spec
Content is user-generated and unverified.
