
# VELLO System Architecture - Visual Guide

## 🏗️ Complete System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          VELLO MESSAGING PLATFORM                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                    ANDROID CLIENT LAYER                            │   │
│  │                    (Jetpack Compose UI)                            │   │
│  ├────────────────────────────────────────────────────────────────────┤   │
│  │                                                                    │   │
│  │  ┌─────────────┐  ┌──────────────┐  ┌──────────────┐             │   │
│  │  │ Chat Screen │  │ Call Screen  │  │Connections  │             │   │
│  │  │   (Msgs)    │  │  (Audio/Video)│  │   Screen    │             │   │
│  │  └──────┬──────┘  └──────┬───────┘  └──────┬───────┘             │   │
│  │         │                 │                 │                     │   │
│  │  ┌──────▼─────────────────▼─────────────────▼──────┐              │   │
│  │  │         ViewModel Layer (State Management)      │              │   │
│  │  │  ├─ ChatViewModel                               │              │   │
│  │  │  ├─ CallViewModel                               │              │   │
│  │  │  ├─ ConnectionViewModel                         │              │   │
│  │  │  └─ AuthViewModel                               │              │   │
│  │  └──────────────────┬───────────────────────────────┘              │   │
│  │                     │                                              │   │
│  │  ┌──────────────────▼────────────────────────────┐                │   │
│  │  │       Repository Layer (Abstraction)         │                │   │
│  │  │  ├─ MessageRepository                        │                │   │
│  │  │  ├─ CallRepository                           │                │   │
│  │  │  ├─ ConnectionRepository                     │                │   │
│  │  │  ├─ UserRepository                           │                │   │
│  │  │  └─ AuthRepository                           │                │   │
│  │  └──────────────────┬────────────────────────────┘                │   │
│  │                     │                                              │   │
│  └─────────────────────┼──────────────────────────────────────────────┘   │
│                        │                                                   │
│        ┌───────────────┼──────────────────────┬──────────────┐             │
│        │               │                      │              │             │
│   ┌────▼────┐  ┌──────▼────────┐  ┌─────────▼───┐  ┌───────▼─────┐      │
│   │ Local   │  │  Supabase    │  │   WebRTC   │  │  Firebase   │      │
│   │ Room DB │  │  REST API    │  │   Engine   │  │  FCM Push   │      │
│   │         │  │              │  │            │  │   Notif     │      │
│   └─────────┘  │ ┌──────────┐  │  │ ┌────────┐│  └─────────────┘      │
│                │ │Functions │  │  │ │Signaling││                       │
│                │ │          │  │  │ │Server  ││                       │
│                │ └──────────┘  │  │ └────────┘│                       │
│                └────────────────┘  └───────────┘                       │
│                      │                                                  │
├──────────────────────┼──────────────────────────────────────────────────┤
│                      │        CLOUD SERVICES LAYER                     │
│  ┌───────────────────▼──────────────────────────────────────┐          │
│  │                  SUPABASE (Backend)                      │          │
│  ├──────────────────────────────────────────────────────────┤          │
│  │                                                          │          │
│  │  ┌──────────────┐  ┌───────────────┐  ┌─────────────┐  │          │
│  │  │ PostgreSQL   │  │ Auth (GoTrue) │  │  Storage    │  │          │
│  │  │ Database     │  │  + JWT + OTP  │  │  (S3-like)  │  │          │
│  │  │              │  │               │  │             │  │          │
│  │  │ Tables:      │  │ ├─ Register   │  │ ├─ Profile  │  │          │
│  │  │ ├─ users     │  │ ├─ Login      │  │ │  pics     │  │          │
│  │  │ ├─ messages  │  │ ├─ OTP        │  │ └─ Media    │  │          │
│  │  │ ├─ calls     │  │ └─ Session    │  │             │  │          │
│  │  │ ├─ convo     │  │   mgmt        │  │  Bucket:    │  │          │
│  │  │ ├─ connections│ │               │  │  vello-    │  │          │
│  │  │ ├─ receipts  │  │               │  │  storage    │  │          │
│  │  │ └─ blocklist │  │               │  │             │  │          │
│  │  └────────────┬─┘  └───────────────┘  └─────────────┘  │          │
│  │               │                                          │          │
│  │  ┌────────────▼────────────────────────────────────┐   │          │
│  │  │       Realtime Subscriptions (WebSocket)       │   │          │
│  │  │  ├─ Message stream                              │   │          │
│  │  │  ├─ Delivery status updates                     │   │          │
│  │  │  ├─ User online/offline status                  │   │          │
│  │  │  ├─ Call signaling (WebRTC offer/answer)        │   │          │
│  │  │  └─ Typing indicator                            │   │          │
│  │  └─────────────────────────────────────────────────┘   │          │
│  │                                                          │          │
│  │  ┌──────────────────────────────────────────────────┐   │          │
│  │  │    Edge Functions (Server-side Logic)           │   │          │
│  │  │  ├─ send-message (validation + storage)         │   │          │
│  │  │  ├─ initiate-call (signaling)                   │   │          │
│  │  │  ├─ check-user (auth verification)              │   │          │
│  │  │  ├─ search-users (full-text search)             │   │          │
│  │  │  ├─ send-connection-request                     │   │          │
│  │  │  └─ update-user-status                          │   │          │
│  │  └──────────────────────────────────────────────────┘   │          │
│  │                                                          │          │
│  └──────────────────────────────────────────────────────────┘          │
│                                                                         │
│  ┌──────────────────────────────────────────────────────────┐          │
│  │     WebRTC Infrastructure (Via Signaling Server)        │          │
│  ├──────────────────────────────────────────────────────────┤          │
│  │                                                          │          │
│  │  P2P Connection:  User A ◄──────────► User B           │          │
│  │  ├─ ICE Servers (STUN/TURN)                            │          │
│  │  ├─ Offer/Answer exchange                             │          │
│  │  ├─ ICE Candidates exchange                           │          │
│  │  ├─ Media Streams (Audio + Video)                      │          │
│  │  └─ Data Channel (optional)                            │          │
│  │                                                          │          │
│  │  STUN: stun:stun.l.google.com:19302                    │          │
│  │  TURN: your-turn-server.com (optional)                 │          │
│  │                                                          │          │
│  └──────────────────────────────────────────────────────────┘          │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 📊 Data Flow Diagrams

### Message Sending Flow

```
USER TYPES MESSAGE
        │
        ▼
┌──────────────────┐
│ Create Message   │
│ in local Room DB │
│ Status: PENDING  │
└────────┬─────────┘
         │
         ▼
┌──────────────────────────────┐
│ Upload via Supabase Function │
│ send-message()               │
└────────┬─────────────────────┘
         │
    ┌────┴────┐
    │          │
   YES        NO
    │          │
    ▼          ▼
┌───────┐  ┌──────────────┐
│ Update│  │ Mark as FAILED
│Status │  │ Queue retry w/
│ SENT  │  │ exponential
└───┬───┘  │ backoff
    │      └──────┬───────┐
    │             │       │
    │      ┌──────▼──────┐│
    │      │ WorkManager ││
    │      │ retry task  ││
    │      └─────────────┘│
    │                     │
    │      Retry at:
    │      15s, 30s, 60s, 2m
    │
    ▼
┌────────────────────────┐
│ Subscribe to Message   │
│ status via Realtime    │
│ (WebSocket)            │
└────────┬───────────────┘
         │
    ┌────┴─────────────────┐
    │                      │
    ▼                      ▼
┌─────────────┐    ┌──────────────┐
│ Recipient   │    │ Recipient    │
│ ONLINE      │    │ OFFLINE      │
└────┬────────┘    └────┬─────────┘
     │                  │
     ▼                  ▼
┌──────────────┐  ┌─────────────────────┐
│ Push notif   │  │ Store in queue,     │
│ via Firebase │  │ try again when they │
│ FCM          │  │ come online         │
└────┬─────────┘  └──────────┬──────────┘
     │                       │
     ▼                       ▼
┌──────────────────────────────────┐
│ User B sees message              │
│ Status: DELIVERED                │
│ Send via Realtime                │
└───────────┬──────────────────────┘
            │
            ▼
┌───────────────────────────┐
│ User B opens message      │
│ Create read receipt       │
│ Status: READ              │
│ Record timestamp          │
└───────────┬───────────────┘
            │
            ▼
┌──────────────────────────────────┐
│ User A receives read notification│
│ Display double checkmark (blue)  │
└──────────────────────────────────┘
```

### Call Initiation Flow

```
USER A INITIATES CALL
        │
        ▼
┌──────────────────────────────┐
│ Create PeerConnection        │
│ Add audio track (mic)        │
│ Add video track (optional)   │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Create WebRTC Offer (SDP)    │
│ Set as Local Description     │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Call Supabase Function:      │
│ initiate-call()              │
│ Send: Offer + User IDs       │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Create Call record in DB     │
│ Status: RINGING              │
│ Generate unique call ID      │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Send Push Notification to B  │
│ via Firebase FCM             │
│ Include call ID + caller ID  │
└────────┬─────────────────────┘
         │
         ├─────────────────────────────┐
         │                             │
      USER B                        USER B
      DECLINES                      ACCEPTS
         │                             │
         ▼                             ▼
    ┌─────────────┐           ┌──────────────────┐
    │ Send decline│           │ Create Peer Conn │
    │ via Realtime│           │ Add audio track  │
    │ Update Call │           │ Add video track  │
    │ status:     │           └────────┬─────────┘
    │ DECLINED    │                    │
    └─────────────┘                    ▼
         │              ┌──────────────────────────┐
         │              │ Set Remote Description  │
         │              │ (A's Offer)             │
         │              └────────┬─────────────────┘
         │                       │
         │                       ▼
         │              ┌──────────────────────────┐
         │              │ Create WebRTC Answer     │
         │              │ Set as Local Description│
         │              └────────┬─────────────────┘
         │                       │
         │                       ▼
         │              ┌──────────────────────────┐
         │              │ Send Answer to A via     │
         │              │ Supabase Function        │
         │              │ Update Call Status:      │
         │              │ ONGOING                  │
         │              └────────┬─────────────────┘
         │                       │
         │                       ▼
         │              ┌──────────────────────────┐
         │              │ ICE Candidates Exchange  │
         │              │ via Realtime WebSocket   │
         │              │ Both peers collect IPs   │
         │              │ & network info           │
         │              └────────┬─────────────────┘
         │                       │
         │                       ▼
         │              ┌──────────────────────────┐
         │              │ P2P Connection Active    │
         │              │ Audio/Video streaming    │
         │              │ Both directions work     │
         │              └────────┬─────────────────┘
         │                       │
         │                       ▼
         │              ┌──────────────────────────┐
         │              │ Call Ends (Either user) │
         │              │ Update status: ENDED    │
         │              │ Record duration         │
         │              │ Save to call logs       │
         │              └──────────────────────────┘
         │
         ▼
    ┌────────────────┐
    │ Notify A:      │
    │ Call declined  │
    └────────────────┘
```

---

## 🎨 UI Layout Specifications

### Screen 1: Chat List (Home Tab)

```
┌─────────────────────────────────┐
│ ☰ VELLO          ⭕ 🔍          │ Header
├─────────────────────────────────┤
│  💬 CHATS  👥 CONNECTIONS  ⚙️  │ Tabs
├─────────────────────────────────┤
│                                 │
│  ┌───────────────────────────┐  │
│  │ 🖼️ John Doe         | NEW │  │
│  │ Hey! How are you?         │  │
│  │ Yesterday · 3 msgs        │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 🖼️ Sarah Smith  | READ    │  │
│  │ That sounds great!         │  │
│  │ 2 hours ago               │  │
│  └───────────────────────────┘  │
│                                 │
│  ┌───────────────────────────┐  │
│  │ 🖼️ Alex Johnson           │  │
│  │ See you tomorrow ✓✓       │  │
│  │ Yesterday                 │  │
│  └───────────────────────────┘  │
│                                 │
├─────────────────────────────────┤
│ + New Chat          🎤 📞 📹    │ FAB
└─────────────────────────────────┘

Color Scheme:
- Primary: #00A884 (WhatsApp Green)
- Unread badge: #25D366 (Lighter Green)
- Timestamp: #999999 (Gray)
- Divider: #E0E0E0 (Light Gray)
```

### Screen 2: Chat Detail

```
┌─────────────────────────────────┐
│ ◄ John Doe        ⭕ 🔍  📞 📹  │ Header
├─────────────────────────────────┤
│ Online · Last seen 2 mins ago   │ Status
├─────────────────────────────────┤
│                                 │ Messages
│                 ✓ Hey there!    │ List
│                 Yesterday       │
│                                 │
│ That's awesome! ✓✓              │
│ Yesterday                       │
│                                 │
│                 How are you?    │
│                                 │
│ I'm great! ✓✓                   │
│                                 │
│  ↖️  Typing indicator...        │ (animated)
│                                 │
├─────────────────────────────────┤
│ ┌──────────────────────────────┐│ Input
│ │ Type a message             ⊙││
│ └──────────────────────────────┘│
│         📎         🎤  ➤         │ Actions
└─────────────────────────────────┘

Message Bubble Colors:
- Sent (Outgoing): #DCF8C6 (Light Green)
- Received: #E8E8EA (Light Gray)
- Text: #000000 / #FFFFFF
```

### Screen 3: Connections / Browse Peoples

```
┌─────────────────────────────────┐
│ ☰ VELLO                         │
├─────────────────────────────────┤
│  BROWSE | REQUESTS | CONNECTED  │ Tabs
├─────────────────────────────────┤
│  🔍 Search by username          │
├─────────────────────────────────┤
│ SUGGESTED                        │
│                                 │
│  ┌────────────────────────────┐ │
│  │ 🖼️ Alex Johnson           │ │
│  │ @alexjohn                  │ │
│  │ Full Stack Dev 💻          │ │
│  │           [CONNECT]        │ │
│  └────────────────────────────┘ │
│                                 │
│  ┌────────────────────────────┐ │
│  │ 🖼️ Maria Garcia   ⭕       │ │
│  │ @mariadev                  │ │
│  │ UI/UX Designer             │ │
│  │           [CONNECT]        │ │
│  └────────────────────────────┘ │
│                                 │
│  ┌────────────────────────────┐ │
│  │ 🖼️ Chris Brown            │ │
│  │ @chrisbrown                │ │
│  │ Mobile Engineer            │ │
│  │           [CONNECT]        │ │
│  └────────────────────────────┘ │
│                                 │
└─────────────────────────────────┘

Connection Status Buttons:
- CONNECT (Not connected): #007AFF (Blue)
- PENDING (Request sent): #999999 (Gray, disabled)
- CONNECTED: ✓ (Checkmark)
```

### Screen 4: Incoming Call

```
┌─────────────────────────────────┐
│           (Black background)    │
│                                 │
│           🖼️ Avatar             │
│           (120x120)             │
│                                 │
│       John Doe                  │
│       (24sp, bold)              │
│                                 │
│       Incoming audio call...    │
│       (14sp, gray)              │
│                                 │
│                                 │
│                                 │
│                                 │
│        ⭕         ✓             │ Action buttons
│      DECLINE    ANSWER          │ (56dp FAB)
│        🔴        🟢             │
│                                 │
└─────────────────────────────────┘

Colors:
- Background: #000000 (Black)
- Decline: #FF5252 (Red)
- Answer: #00C853 (Green)
```

### Screen 5: Active Call

```
┌─────────────────────────────────┐
│                                 │
│    Remote Video (Full Screen)  │
│    ┌──────────────────────────┐│
│    │                          ││
│    │  [Video Feed from B]    ││
│    │                          ││
│    │                          ││
│    └──────────────────────────┘│
│                                 │
│  John Doe     00:45             │ Header (centered)
│                                 │
│                                 │
│     ┌─────────────────┐         │
│     │ Local Video PiP │         │ PiP (top-right)
│     │  [100x160dp]    │         │ with border
│     └─────────────────┘         │
│                                 │
│                                 │
│  ⭕    ⭕    ⭕    🔴            │ Controls (bottom)
│ Mute  Video Camera  End         │
│        Toggle Switch            │
│                                 │
└─────────────────────────────────┘

Colors (Call Screen):
- Background: #000000
- Controls: #333333 (Dark gray)
- Muted indicator: #FF5252 (Red)
- End button: #FF5252 (Red)
- Button hover: Brighter shade
```

---

## 🔄 State Management Flow

```
┌────────────────────────────────────────────────────────────┐
│              STATE MANAGEMENT ARCHITECTURE                │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  UI Layer                                                │
│  ┌──────────┐                                            │
│  │ Composable                                             │
│  │ (ChatScreen)                                           │
│  └────────┬─────────────────────────────────────┐        │
│           │                                     │        │
│           ▼                                     ▼        │
│  ┌──────────────────────┐          ┌──────────────────┐ │
│  │  collectAsState()   │          │ OnEvent Handler  │ │
│  │  from StateFlow     │          │ (onClick, etc)   │ │
│  └──────────┬──────────┘          └────────┬─────────┘ │
│             │                              │           │
└─────────────┼──────────────────────────────┼───────────┘
              │                              │
         ┌────▼──────────────────────────────▼──────┐
         │        ViewModel Layer (Logic)           │
         │  ┌────────────────────────────────────┐  │
         │  │ ChatViewModel                      │  │
         │  │                                    │  │
         │  │ Private MutableStateFlow:          │  │
         │  │ - _messages                        │  │
         │  │ - _recipientInfo                   │  │
         │  │ - _isLoading                       │  │
         │  │ - _error                           │  │
         │  │                                    │  │
         │  │ Public StateFlow (read-only):      │  │
         │  │ - messages.asStateFlow()           │  │
         │  │ - recipientInfo.asStateFlow()      │  │
         │  │                                    │  │
         │  │ Methods:                           │  │
         │  │ - sendMessage(text)                │  │
         │  │ - markAsRead(messageId)            │  │
         │  │ - deleteMessage(messageId)         │  │
         │  │ - retryFailedMessage(messageId)    │  │
         │  │                                    │  │
         │  │ Init Block:                        │  │
         │  │ - subscribeToMessages()            │  │
         │  │ - subscribeToStatusUpdates()       │  │
         │  │ - subscribeToRecipientStatus()     │  │
         │  └──────────────┬───────────────────┘  │
         │                 │                      │
         └─────────────────┼──────────────────────┘
                           │
              ┌────────────▼──────────────┐
              │  Repository Layer        │
              │  ┌────────────────────┐  │
              │  │ MessageRepository  │  │
              │  │                    │  │
              │  │ Methods:           │  │
              │  │ - sendMessage()    │  │
              │  │ - getMessages()    │  │
              │  │ - markAsRead()     │  │
              │  │ - subscribeToMsgs()│  │
              │  │                    │  │
              │  └────────┬───────────┘  │
              └───────────┼──────────────┘
                          │
              ┌───────────▼─────────────┐
              │  Local + Remote Data    │
              │  ┌──────────────────┐   │
              │  │ Room Database    │   │
              │  │ (Local Cache)    │   │
              │  └──────────────────┘   │
              │  ┌──────────────────┐   │
              │  │ Supabase         │   │
              │  │ (Cloud Source)   │   │
              │  └──────────────────┘   │
              │  ┌──────────────────┐   │
              │  │ Realtime Stream  │   │
              │  │ (WebSocket)      │   │
              │  └──────────────────┘   │
              └────────────────────────┘
```

---

## 🗂️ File Structure Breakdown

```
VELLO-testing/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── kotlin/com/mr10/vello/
│           │   ├── VelloApplication.kt          (Supabase init)
│           │   │
│           │   ├── data/
│           │   │   ├── local/
│           │   │   │   ├── db/
│           │   │   │   │   ├── AppDatabase.kt   (Room setup)
│           │   │   │   │   └── AppDatabaseMigrations.kt
│           │   │   │   ├── dao/
│           │   │   │   │   ├── MessageDao.kt
│           │   │   │   │   ├── ConversationDao.kt
│           │   │   │   │   ├── CallDao.kt
│           │   │   │   │   ├── UserDao.kt
│           │   │   │   │   └── ConnectionDao.kt
│           │   │   │   └── entities/
│           │   │   │       ├── MessageEntity.kt
│           │   │   │       ├── ConversationEntity.kt
│           │   │   │       ├── CallEntity.kt
│           │   │   │       ├── UserEntity.kt
│           │   │   │       └── ConnectionEntity.kt
│           │   │   │
│           │   │   ├── remote/
│           │   │   │   ├── api/
│           │   │   │   │   ├── SupabaseClient.kt
│           │   │   │   │   ├── EdgeFunctionClient.kt
│           │   │   │   │   └── AuthApiService.kt
│           │   │   │   └── dto/
│           │   │   │       ├── MessageDto.kt
│           │   │   │       ├── CallDto.kt
│           │   │   │       └── UserDto.kt
│           │   │   │
│           │   │   └── repository/
│           │   │       ├── MessageRepository.kt   (Core)
│           │   │       ├── ConversationRepository.kt
│           │   │       ├── CallRepository.kt
│           │   │       ├── UserRepository.kt
│           │   │       ├── ConnectionRepository.kt
│           │   │       └── AuthRepository.kt
│           │   │
│           │   ├── domain/
│           │   │   ├── model/
│           │   │   │   ├── Message.kt
│           │   │   │   ├── Conversation.kt
│           │   │   │   ├── Call.kt
│           │   │   │   ├── User.kt
│           │   │   │   ├── Connection.kt
│           │   │   │   └── enums/
│           │   │   │       ├── DeliveryStatus.kt
│           │   │   │       ├── CallStatus.kt
│           │   │   │       ├── ConnectionStatus.kt
│           │   │   │       └── MessageType.kt
│           │   │   │
│           │   │   └── usecase/
│           │   │       ├── SendMessageUseCase.kt
│           │   │       ├── GetMessagesUseCase.kt
│           │   │       ├── InitiateCallUseCase.kt
│           │   │       └── SearchUsersUseCase.kt
│           │   │
│           │   ├── ui/
│           │   │   ├── screens/
│           │   │   │   ├── ChatScreen.kt        (Messages)
│           │   │   │   ├── ChatListScreen.kt    (Home)
│           │   │   │   ├── CallScreen.kt        (Active call)
│           │   │   │   ├── IncomingCallScreen.kt
│           │   │   │   ├── VideoCallScreen.kt   (Video call)
│           │   │   │   ├── ConnectionsScreen.kt (New feature)
│           │   │   │   ├── BrowsePeoplesScreen.kt
│           │   │   │   ├── ConnectionRequestsScreen.kt
│           │   │   │   ├── AuthScreen.kt
│           │   │   │   ├── ProfileScreen.kt
│           │   │   │   └── SplashScreen.kt
│           │   │   │
│           │   │   ├── components/
│           │   │   │   ├── MessageBubble.kt
│           │   │   │   ├── ChatHeader.kt
│           │   │   │   ├── ChatInputField.kt
│           │   │   │   ├── DeliveryStatusIndicator.kt
│           │   │   │   ├── TypingIndicator.kt
│           │   │   │   ├── UserProfileCard.kt
│           │   │   │   ├── ConnectionRequestCard.kt
│           │   │   │   └── CallControls.kt
│           │   │   │
│           │   │   ├── viewmodel/
│           │   │   │   ├── ChatViewModel.kt
│           │   │   │   ├── ChatListViewModel.kt
│           │   │   │   ├── CallViewModel.kt
│           │   │   │   ├── ConnectionsViewModel.kt
│           │   │   │   ├── BrowsePeoplesViewModel.kt
│           │   │   │   ├── AuthViewModel.kt
│           │   │   │   └── ProfileViewModel.kt
│           │   │   │
│           │   │   ├── theme/
│           │   │   │   ├── Theme.kt
│           │   │   │   ├── Color.kt
│           │   │   │   ├── Type.kt
│           │   │   │   └── Dimen.kt
│           │   │   │
│           │   │   └── navigation/
│           │   │       ├── NavGraph.kt
│           │   │       ├── NavEvent.kt
│           │   │       └── Routes.kt
│           │   │
│           │   ├── rtc/
│           │   │   ├── WebRtcCallManager.kt    (Core)
│           │   │   ├── PeerConnectionFactory.kt
│           │   │   ├── IceServer.kt
│           │   │   ├── observer/
│           │   │   │   ├── PeerConnectionObserver.kt
│           │   │   │   ├── SdpObserver.kt
│           │   │   │   ├── DataChannelObserver.kt
│           │   │   │   └── AudioVideoObserver.kt
│           │   │   └── util/
│           │   │       ├── AudioTrackFactory.kt
│           │   │       ├── VideoTrackFactory.kt
│           │   │       └── CameraUtils.kt
│           │   │
│           │   ├── service/
│           │   │   ├── ChatService.kt
│           │   │   ├── CallService.kt
│           │   │   ├── RealtimeService.kt     (WebSocket)
│           │   │   ├── NotificationService.kt (FCM)
│           │   │   └── SyncService.kt        (Offline sync)
│           │   │
│           │   ├── worker/
│           │   │   ├── MessageRetryWorker.kt
│           │   │   ├── SyncWorker.kt
│           │   │   └── CallStatusWorker.kt
│           │   │
│           │   ├── util/
│           │   │   ├── DateTimeFormatter.kt
│           │   │   ├── FileUtils.kt
│           │   │   ├── PermissionHelper.kt
│           │   │   ├── ConnectivityHelper.kt
│           │   │   └── Constants.kt
│           │   │
│           │   └── di/
│           │       ├── RepositoryModule.kt
│           │       ├── DatabaseModule.kt
│           │       ├── SupabaseModule.kt
│           │       ├── RtcModule.kt
│           │       └── ServiceModule.kt
│           │
│           └── AndroidManifest.xml
│
├── supabase/
│   └── functions/
│       ├── send-message/
│       │   └── index.ts            (Message sending logic)
│       ├── initiate-call/
│       │   └── index.ts            (Call signaling)
│       ├── search-users/
│       │   └── index.ts            (User search)
│       └── check-user/
│           └── index.ts            (Auth verification)
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## ✅ Implementation Verification Checklist

### Database Layer ✓
- [ ] All tables created in Supabase
- [ ] All indexes created
- [ ] RLS policies applied
- [ ] Storage buckets created
- [ ] Test data inserted (for dev)

### Data Layer ✓
- [ ] Room database configured
- [ ] DAOs implemented
- [ ] Entities mapped correctly
- [ ] Repositories created
- [ ] Dependency injection set up

### Service Layer ✓
- [ ] Supabase client initialized
- [ ] Authentication working
- [ ] Realtime subscriptions working
- [ ] Edge functions deployed
- [ ] Error handling implemented

### UI/UX ✓
- [ ] All screens built
- [ ] Navigation configured
- [ ] Animations implemented
- [ ] Colors/theme applied
- [ ] Responsive layouts

### Features ✓
- [ ] Message sending
- [ ] Message receiving
- [ ] Delivery tracking
- [ ] Read receipts
- [ ] Voice calling
- [ ] Video calling
- [ ] User connections
- [ ] Search functionality

### Testing ✓
- [ ] Unit tests written
- [ ] Integration tests created
- [ ] UI tests implemented
- [ ] Manual testing done
- [ ] Performance tested

---

## 🎯 Success Metrics

By week 12, your app should have:

| Metric | Target | How to Measure |
|--------|--------|----------------|
| Message Latency | < 500ms | Timestamp delta |
| Call Connection | < 2s | From initiate to audio |
| Offline Sync | 100% reliability | Test airplane mode |
| Crash Rate | < 0.1% | Crashlytics dashboard |
| User Retention D1 | > 40% | Analytics |
| Message Delivery | 99.9% | Database audit |

---

## 🚀 Launch Readiness Checklist

- [ ] All 12 weeks completed
- [ ] Beta testing with 100+ users
- [ ] App signed and optimized
- [ ] Play Store listing created
- [ ] Terms of Service published
- [ ] Privacy Policy in place
- [ ] Support system in place
- [ ] Monitoring/analytics active
- [ ] Crash reporting configured
- [ ] Rate limiting tested
- [ ] Load testing passed (1000+ concurrent)
- [ ] Security audit completed

You're ready to launch when ALL of these are ✓

---

Good luck! This is ambitious but achievable. 🚀
Content is user-generated and unverified.
