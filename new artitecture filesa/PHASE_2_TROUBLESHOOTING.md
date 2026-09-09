# Phase 2: Troubleshooting Guide
## Common Issues & Quick Fixes

---

## 🔴 Build & Compilation Errors

### Error: "Unresolved reference: MessageRepository"

**Cause:** Repository not created or wrong import

**Fix:**
```kotlin
// Make sure file exists:
// app/src/main/kotlin/com/mr10/vello/data/repository/MessageRepository.kt

// If it doesn't exist, create it:
class MessageRepository(private val supabase: SupabaseClient) {
  // ... implementation
}
```

### Error: "Cannot find symbol: SupabaseClient"

**Cause:** Missing Supabase dependency

**Fix:**
```gradle
// In app/build.gradle.kts, add:
dependencies {
  implementation("io.github.supabase:postgrest-kt:1.4.0")
  implementation("io.github.supabase:realtime-kt:1.4.0")
}
```

Then: `Sync Now` in Android Studio

### Error: "Type mismatch: StateFlow vs Flow"

**Cause:** Mixing StateFlow and Flow

**Fix:**
```kotlin
// ❌ Wrong
private val _messages = MutableStateFlow<List<Message>>(emptyList())
val messages: Flow<List<Message>> = _messages // Error!

// ✅ Correct
private val _messages = MutableStateFlow<List<Message>>(emptyList())
val messages: StateFlow<List<Message>> = _messages.asStateFlow()
```

### Error: "Unresolved reference: @Composable"

**Cause:** Missing Compose import

**Fix:**
```kotlin
// Add import
import androidx.compose.runtime.Composable

// Or add to build.gradle.kts
dependencies {
  implementation("androidx.compose.runtime:runtime:1.5.0")
}
```

### Error: "Could not find method install()"

**Cause:** Supabase client initialization syntax wrong

**Fix:**
```kotlin
// ✅ Correct syntax
supabase = createSupabaseClient(url, key) {
  install(Auth)
  install(Realtime)
  install(StorageClient)
}
```

---

## 🟡 Runtime Errors

### Crash: "java.lang.NullPointerException: Attempt to invoke virtual method on null object"

**Cause:** Accessing null recipient info or messages

**Fix:**
```kotlin
// ❌ Wrong
val recipientName = recipientInfo.displayName // Can crash!

// ✅ Correct
val recipientName = recipientInfo?.displayName ?: "Loading..."
```

Or in ViewModel:
```kotlin
// Safe navigation
recipientInfo.value?.let { recipient ->
  // Safe to use recipient here
}
```

### Crash: "NetworkOnMainThreadException"

**Cause:** Making network call on main thread

**Fix:**
```kotlin
// ❌ Wrong
fun sendMessage(content: String) {
  messageRepo.sendMessage(message) // Runs on Main!
}

// ✅ Correct
fun sendMessage(content: String) {
  viewModelScope.launch(Dispatchers.IO) {
    messageRepo.sendMessage(message) // Runs on IO thread
  }
}
```

### Crash: "IllegalStateException: Job was cancelled"

**Cause:** Coroutine cancelled before completion

**Fix:**
```kotlin
// Use structured concurrency
viewModelScope.launch {
  try {
    val result = messageRepo.sendMessage(message)
  } catch (e: CancellationException) {
    throw e // Re-throw to not swallow cancellation
  } catch (e: Exception) {
    _error.value = "Failed to send"
  }
}
```

### Crash: "DeadObjectException" or "ServiceNotConnectedException"

**Cause:** Supabase not initialized or connection lost

**Fix:**
```kotlin
// In Application.onCreate()
supabase = createSupabaseClient(
  supabaseUrl = BuildConfig.SUPABASE_URL,
  supabaseKey = BuildConfig.SUPABASE_ANON_KEY
) {
  install(Auth)
  install(Realtime)
}

// Use lazy initialization
class ChatViewModel : ViewModel() {
  private val supabase by lazy { 
    VelloApplication.supabase 
  }
}
```

---

## 🟠 Message Sending Issues

### Messages Not Sending

**Symptoms:** 
- Message shows "pending" forever
- No error message
- No network error

**Debug steps:**
```kotlin
// 1. Check logcat
adb logcat | grep MessageRepo

// 2. Add logging to repository
suspend fun sendMessage(message: Message): Result<Message> {
  Log.d("MessageRepo", "Sending message: ${message.id}")
  
  return try {
    val response = supabase.functions.invoke("send-message", mapOf(...))
    Log.d("MessageRepo", "Response: ${response.data}")
    Result.success(...)
  } catch (e: Exception) {
    Log.e("MessageRepo", "Error sending", e)
    Result.failure(e)
  }
}

// 3. Check Supabase logs
// → Go to supabase.com → Functions → send-message → Logs
```

**Common causes:**
- [ ] Function not deployed: `supabase functions deploy send-message`
- [ ] User not authenticated: Check `Auth.currentUser`
- [ ] Conversation doesn't exist: Check conversations table in DB
- [ ] RLS policy blocking: Check row level security policies

### Messages Sent But Not Delivered

**Check:**
```kotlin
// 1. Verify message in database
// Supabase → SQL Editor → 
SELECT * FROM messages ORDER BY created_at DESC LIMIT 1;

// 2. Check delivery_status field
// Should be "sent" or "delivered", not "pending"

// 3. Check recipient online status
SELECT is_online FROM users WHERE id = 'recipient-id';
```

**If recipient is offline:**
- Expected: Message stays "sent" until recipient comes online
- Expected: Status changes to "delivered" when they open app

---

## 🟠 Real-time Subscription Issues

### Typing Indicator Not Working

**Debug:**
```kotlin
// 1. Check Realtime is enabled
// Supabase Dashboard → Replication → Enable typing_status table

// 2. Verify subscription
viewModelScope.launch {
  conversationRepo.subscribeToTypingStatus(conversationId)
    .collect { typingUsers ->
      Log.d("Typing", "Users typing: $typingUsers")
      _recipientIsTyping.value = typingUsers.contains(recipientId)
    }
}

// 3. Check database
// Supabase → Inspect → typing_status table → see records?
```

**Fix:**
```sql
-- Enable Realtime for typing_status
ALTER PUBLICATION supabase_realtime ADD TABLE typing_status;

-- Verify it's enabled
SELECT * FROM pg_publication_tables 
WHERE pubname = 'supabase_realtime';
```

### Messages Not Updating in Real-time

**Debug:**
```kotlin
// 1. Check WebSocket connection
supabase.realtime.connect() // Call manually if needed

// 2. Verify Realtime enabled in Supabase
// Dashboard → Replication → messages table should be checked

// 3. Add logging
fun subscribeToMessages(conversationId: String): Flow<List<Message>> = flow {
  Log.d("Realtime", "Subscribing to messages: $conversationId")
  
  supabase.realtime
    .messages
    .on(INSERT) { payload ->
      Log.d("Realtime", "New message: ${payload.data}")
    }
    .subscribe()
}
```

**Fix:**
```sql
-- Enable Realtime for messages table
ALTER PUBLICATION supabase_realtime ADD TABLE messages;
ALTER PUBLICATION supabase_realtime ADD TABLE message_read_receipts;
```

---

## 🟠 UI/Composition Issues

### List Not Scrolling to Bottom

**Cause:** LazyColumn not updating

**Fix:**
```kotlin
// Use proper state management
val listState = rememberLazyListState()

LaunchedEffect(messages.size) {
  if (messages.isNotEmpty()) {
    listState.animateScrollToItem(messages.size - 1)
  }
}

LazyColumn(state = listState) {
  items(messages) { message ->
    MessageBubble(message)
  }
}
```

### Animations Not Playing

**Cause:** Not using Animatable or wrong scope

**Fix:**
```kotlin
// ✅ Correct
@Composable
fun MessageBubbleAnimation(content: @Composable () -> Unit) {
  val scale = remember { Animatable(0.8f) }
  
  LaunchedEffect(Unit) {
    scale.animateTo(1f, animationSpec = spring(...))
  }
  
  Box(modifier = Modifier.scale(scale.value)) {
    content()
  }
}

// ❌ Wrong (won't animate)
@Composable
fun MessageBubbleAnimation(content: @Composable () -> Unit) {
  val scale = mutableStateOf(0.8f)
  Box(modifier = Modifier.scale(scale.value)) {
    content()
  }
}
```

### Input Field Not Clearing After Send

**Fix:**
```kotlin
// In ViewModel
fun sendMessage(content: String) {
  viewModelScope.launch {
    messageRepo.sendMessage(message)
      .onSuccess {
        _messageInput.value = "" // Clear!
      }
  }
}

// In Composable
TextField(
  value = messageInput,
  onValueChange = { viewModel.updateMessageInput(it) }
)
```

### Message Bubbles Overlapping

**Cause:** Wrong padding/margin

**Fix:**
```kotlin
// Ensure proper spacing
LazyColumn(
  verticalArrangement = Arrangement.spacedBy(8.dp),
  contentPadding = PaddingValues(vertical = 12.dp)
) {
  items(messages) { message ->
    MessageBubbleAnimation(...) {
      MessageBubble(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 4.dp)
      )
    }
  }
}
```

---

## 🟠 Database & Backend Issues

### "Table 'messages' does not exist"

**Fix:**
```sql
-- Create all tables from PRODUCTION_FIX.md
-- Or run this test query
SELECT * FROM messages LIMIT 1;

-- If error: Run the SQL migration in Supabase
```

### "Permission denied for schema 'public'"

**Cause:** RLS policies not set up

**Fix:**
```sql
-- Check RLS is enabled
SELECT relrowsecurity FROM pg_class WHERE relname='messages';
-- Should return 't' (true)

-- If not, enable it
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- Check policies exist
SELECT * FROM pg_policies WHERE tablename='messages';
```

### "Foreign key constraint violation"

**Cause:** Trying to insert message with invalid conversation_id

**Fix:**
```kotlin
// Verify conversation exists before sending
suspend fun sendMessage(message: Message) {
  // 1. Verify conversation exists
  val conversation = conversationRepo.getConversation(message.conversationId)
  if (conversation == null) {
    // Create conversation
    conversationRepo.createConversation(
      userId1 = message.senderId,
      userId2 = message.recipientId
    )
  }
  
  // 2. Now send message
  messageRepo.sendMessage(message)
}
```

---

## 🔵 Performance Issues

### App Freezing When Sending Message

**Cause:** Blocking main thread

**Fix:**
```kotlin
// ❌ Wrong
fun sendMessage(content: String) {
  val message = messageRepo.sendMessage(msg) // Blocking!
}

// ✅ Correct
fun sendMessage(content: String) {
  viewModelScope.launch(Dispatchers.IO) {
    messageRepo.sendMessage(msg) // Non-blocking
  }
}
```

### Slow Message Loading

**Cause:** Loading too many messages at once

**Fix:**
```kotlin
// Pagination
fun getConversationMessages(
  conversationId: String,
  limit: Int = 50,    // Only load 50 at a time
  offset: Int = 0
): Flow<List<Message>>

// In ViewModel
private var offset = 0

fun loadMoreMessages() {
  viewModelScope.launch {
    offset += 50
    messageRepo.getConversationMessages(
      conversationId,
      limit = 50,
      offset = offset
    ).collect { messages ->
      _messages.value = _messages.value + messages
    }
  }
}
```

### High Memory Usage

**Cause:** Keeping all messages in memory

**Fix:**
```kotlin
// Use pagination and don't keep too many
val MAX_MESSAGES_IN_MEMORY = 200

// Periodically clear old messages
if (_messages.value.size > MAX_MESSAGES_IN_MEMORY) {
  _messages.value = _messages.value.takeLast(MAX_MESSAGES_IN_MEMORY)
}
```

---

## 🟢 Testing Issues

### Tests Failing with "Timeout"

**Cause:** Coroutine not completing

**Fix:**
```kotlin
@Test
fun testSendMessage() = runTest {
  // Wrap in runTest and use advanceUntilIdle()
  
  coEvery { messageRepo.sendMessage(any()) } returns Result.success(message)
  
  viewModel.sendMessage("Test")
  
  advanceUntilIdle() // Wait for coroutines
  
  assertTrue(viewModel.messages.value.isNotEmpty())
}
```

### Mock Not Working

**Cause:** Mock not set up for suspend function

**Fix:**
```kotlin
// ✅ Correct way to mock suspend function
private val messageRepo = mockk<MessageRepository>()

coEvery { messageRepo.sendMessage(any()) } returns Result.success(message)

// Then in test:
coVerify { messageRepo.sendMessage(any()) }
```

---

## 📱 Emulator-Specific Issues

### Emulator Slow

**Fix:**
```bash
# Use Android 12+ image (better performance)
# Use x86_64 architecture (faster than arm64)
# Enable GPU acceleration in AVD settings
# Use at least 4GB RAM allocated
```

### Network Not Working in Emulator

**Fix:**
```bash
# Restart emulator
# Check DNS: adb shell ping google.com

# Use 10.0.2.2 instead of localhost if needed:
val supabaseUrl = if (BuildConfig.DEBUG) {
  "http://10.0.2.2:54321" // For emulator
} else {
  "https://your-supabase-url.com"
}
```

---

## 🚨 Critical Issues to Watch

### User Cannot Send Messages
1. Check auth: Is user logged in?
2. Check network: Is internet working?
3. Check function: Is send-message deployed?
4. Check conversation: Does it exist?
5. Check RLS: Can user insert messages?

### Messages Disappear After Reopen
1. Check Room database: Are messages saved locally?
2. Check Supabase: Are messages in cloud?
3. Check subscriptions: Is Realtime working?

### Delivery Status Not Updating
1. Check message_read_receipts table: Do records exist?
2. Check Realtime: Is table subscribed?
3. Check Edge Function: Is it triggering update?

---

## 💡 Debug Shortcuts

```bash
# View Logcat in real-time
adb logcat | grep -i "ChatViewModel\|MessageRepo\|Realtime"

# Check app crashes
adb logcat | grep "FATAL"

# Clear app data (fresh start)
adb shell pm clear com.mr10.vello

# View database on device (SQL)
adb shell sqlite3 /data/data/com.mr10.vello/databases/vello.db

# Monitor network requests
adb shell setprop log.tag.OkHttp DEBUG
```

---

## ✅ Testing Checklist Before Launch

- [ ] Send message → appears immediately
- [ ] Receive message → real-time update
- [ ] Offline message → queues and sends on reconnect
- [ ] Delivery status → pending → sent → delivered → read
- [ ] Typing indicator → shows and hides
- [ ] Edit message → updates for both users
- [ ] Delete message → shows as deleted
- [ ] No crashes in logcat
- [ ] No ANR warnings
- [ ] Memory doesn't leak
- [ ] Battery usage reasonable

---

**Still stuck?** 
1. Check the full PHASE_2_MESSAGING_CORE.md file
2. Search Android docs
3. Ask in Kotlin Slack
4. Check Supabase docs

**You've got this!** 💪
