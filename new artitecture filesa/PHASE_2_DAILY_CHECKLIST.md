# Phase 2: Day-by-Day Implementation Checklist
## Messaging Core System (Weeks 3-4)

**Goal:** Get users sending and receiving messages in real-time with delivery status ✅

---

## 🚀 WEEK 3: Core Messaging Build

### Day 1-2: Project Setup & Dependencies ⚙️

**Morning (Day 1):**

- [ ] Pull latest code from main branch
- [ ] Create feature branch: `git checkout -b feature/phase2-messaging`
- [ ] Update `build.gradle.kts` with Phase 2 dependencies (see PHASE_2_MESSAGING_CORE.md)

```bash
git checkout -b feature/phase2-messaging
# Then add the dependencies section from the doc
```

- [ ] Sync Gradle: `Tools → Android → Sync Now`
- [ ] Verify no build errors

**Afternoon (Day 1):**

- [ ] Create directory structure:
  ```
  app/src/main/kotlin/com/mr10/vello/
  ├── ui/
  │   ├── screens/
  │   ├── components/
  │   └── viewmodel/
  ```

- [ ] Create base classes/interfaces:
  - [ ] `DeliveryStatus.kt` (enum)
  - [ ] `MessageType.kt` (enum)
  - [ ] `Message.kt` (data class)

**Day 2: Complete Database Setup**

- [ ] In Supabase dashboard, run these SQL migrations:

```sql
-- Add typing status table
CREATE TABLE IF NOT EXISTS typing_status (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  conversation_id UUID NOT NULL REFERENCES conversations(id),
  user_id UUID NOT NULL REFERENCES users(id),
  is_typing BOOLEAN DEFAULT FALSE,
  updated_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(conversation_id, user_id)
);

-- Add indexes
CREATE INDEX idx_typing_status_conversation ON typing_status(conversation_id);

-- Enable Realtime
ALTER PUBLICATION supabase_realtime ADD TABLE IF NOT EXISTS typing_status;
```

- [ ] Verify all message-related RLS policies are enabled
- [ ] Test connection: Run Supabase query in dashboard

---

### Day 3: ViewModel Implementation 📱

**Goal:** Have working ConversationViewModel

- [ ] Create `ConversationViewModel.kt` (from PHASE_2_MESSAGING_CORE.md)
  - Copy entire class
  - Update package name
  - Verify no compilation errors

- [ ] Create factory:
```kotlin
// app/src/main/kotlin/com/mr10/vello/ui/viewmodel/ConversationViewModelFactory.kt

@Suppress("UNCHECKED_CAST")
class ConversationViewModelFactory(
  private val messageRepo: MessageRepository,
  private val conversationRepo: ConversationRepository,
  private val userRepo: UserRepository
) : ViewModelProvider.Factory {
  
  override fun <T : ViewModel> create(
    modelClass: Class<T>,
    extras: CreationExtras
  ): T {
    val conversationId = extras.createDefaultHandle()
      .get<String>("conversationId") ?: ""
    
    return ConversationViewModel(
      messageRepo,
      conversationRepo,
      userRepo,
      conversationId,
      extras.createDefaultHandle()
    ) as T
  }
}
```

- [ ] Test in Android Studio: 
  - Open Project view
  - Navigate to ConversationViewModel.kt
  - No red squiggles = success ✅

---

### Day 4: UI Components - Message Bubble 🎨

**Goal:** Message bubbles display correctly

- [ ] Create `MessageBubble.kt` (from PHASE_2_MESSAGING_CORE.md)
  - Add `ic_clock.xml`, `ic_check_single.xml`, `ic_check_double.xml` drawable files

**Drawable files needed:**

```xml
<!-- res/drawable/ic_clock.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:fillColor="@android:color/darker_gray"
      android:pathData="M12,2C6.48,2 2,6.48 2,12s4.48,10 10,10 10,-4.48 10,-10S17.52,2 12,2zM12,20c-4.41,0 -8,-3.59 -8,-8s3.59,-8 8,-8 8,3.59 8,8 -3.59,8 -8,8zM12.5,7H11v6l5.25,3.15 0.75,-1.23 -4.5,-2.67z"/>
</vector>

<!-- res/drawable/ic_check_single.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:fillColor="@android:color/darker_gray"
      android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41z"/>
</vector>

<!-- res/drawable/ic_check_double.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:fillColor="@android:color/darker_gray"
      android:pathData="M7.79,12.29L9.2,13.7l5.79,-5.79L13.78,6.5zM3.41,16.38l1.41,1.41L9.21,13.4l-1.41,-1.41zM16.78,6.5L13.37,9.91l1.41,1.41L18.19,7.91z"/>
</vector>
```

- [ ] Create `MessageBubbleAnimation.kt` function
- [ ] Preview in Android Studio Compose Preview
- [ ] Test with sample message data

---

### Day 5: UI Components - Chat Input & Header 🎤

**Goal:** Input field and header are functional

- [ ] Create `ChatInputField.kt` (from PHASE_2_MESSAGING_CORE.md)
  - Add drawable: `ic_attachment.xml`, `ic_send.xml`, `ic_mic.xml`
  - Preview in Compose Preview

- [ ] Create `ChatHeader.kt` (from PHASE_2_MESSAGING_CORE.md)
  - Add drawables: `ic_back.xml`, `ic_call.xml`, `ic_video.xml`, `ic_more.xml`
  - Preview with sample data

- [ ] Create animations folder:
  ```
  res/anim/
  ├── slide_in_right.xml
  ├── slide_out_left.xml
  └── fade_in.xml
  ```

- [ ] Test: Build project → no errors ✅

---

### Day 6-7: Chat Screen Assembly 📱

**Goal:** Full chat screen working with dummy data

**Day 6:**

- [ ] Create `ChatScreen.kt` (from PHASE_2_MESSAGING_CORE.md)
- [ ] Create dialogs:
  - `DeleteMessageDialog.kt`
  - `EditMessageDialog.kt`

```kotlin
// DeleteMessageDialog.kt
@Composable
fun DeleteMessageDialog(
  onDelete: () -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Delete message?") },
    text = { Text("This cannot be undone.") },
    confirmButton = {
      Button(
        onClick = onDelete,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
      ) {
        Text("Delete", color = Color.White)
      }
    },
    dismissButton = {
      Button(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

// EditMessageDialog.kt
@Composable
fun EditMessageDialog(
  message: Message,
  onSave: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var editedText by remember { mutableStateOf(message.content) }
  
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit message") },
    text = {
      TextField(
        value = editedText,
        onValueChange = { editedText = it },
        modifier = Modifier.fillMaxWidth()
      )
    },
    confirmButton = {
      Button(onClick = { onSave(editedText) }) {
        Text("Save")
      }
    },
    dismissButton = {
      Button(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
```

- [ ] Add navigation entry to `NavGraph.kt`:
```kotlin
composable(
  route = "chat/{conversationId}",
  arguments = listOf(
    navArgument("conversationId") { type = NavType.StringType }
  )
) { backStackEntry ->
  val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
  ChatScreen(
    conversationId = conversationId,
    onBackClick = { navController.popBackStack() },
    onCallClick = { /* TODO */ },
    onVideoClick = { /* TODO */ }
  )
}
```

**Day 7:**

- [ ] Test ChatScreen with preview data:
```kotlin
@Preview
@Composable
fun ChatScreenPreview() {
  ChatScreen(
    conversationId = "conv-1",
    onBackClick = {},
    onCallClick = {},
    onVideoClick = {}
  )
}
```

- [ ] Build and run on emulator
- [ ] Navigate to chat screen
- [ ] Verify layout, no crashes
- [ ] Commit: `git add . && git commit -m "feat: chat screen UI complete"`

---

## 🎯 WEEK 4: Real-time & Features

### Day 8-9: Message Repository Implementation 🔄

**Goal:** Messages send/receive with real-time updates

**Day 8: Enhance MessageRepository**

- [ ] Update `MessageRepository.kt` with these methods from PHASE_2_MESSAGING_CORE.md:
  - `sendMessage()`
  - `getConversationMessages()` 
  - `subscribeToMessageStatus()`
  - `markAsRead()`
  - `deleteMessage()`
  - `editMessage()`

- [ ] Create unit tests:
```kotlin
// app/src/test/kotlin/com/mr10/vello/data/repository/MessageRepositoryTest.kt

class MessageRepositoryTest {
  
  private lateinit var messageRepo: MessageRepository
  private val mockSupabase = mockk<SupabaseClient>()
  
  @Before
  fun setup() {
    messageRepo = MessageRepository(mockSupabase)
  }
  
  @Test
  fun `sendMessage calls supabase function`() = runTest {
    val message = Message(
      id = "1",
      conversationId = "conv-1",
      senderId = "user-1",
      recipientId = "user-2",
      content = "Test",
      messageType = MessageType.TEXT,
      deliveryStatus = DeliveryStatus.PENDING,
      sentAt = Clock.System.now()
    )
    
    coEvery { 
      mockSupabase.functions.invoke(any(), any()) 
    } returns mockk {
      every { data } returns """{"id":"1","delivery_status":"sent"}"""
    }
    
    val result = messageRepo.sendMessage(message)
    
    assertTrue(result.isSuccess)
    coVerify { mockSupabase.functions.invoke("send-message", any()) }
  }
}
```

- [ ] Run tests: `./gradlew test` ✅

**Day 9: ConversationRepository & Utils**

- [ ] Create `ConversationRepository.kt`:
```kotlin
class ConversationRepository(private val supabase: SupabaseClient) {
  
  suspend fun getConversation(conversationId: String): Conversation {
    return supabase
      .from("conversations")
      .select()
      .eq("id", conversationId)
      .single()
      .decodeAs<Conversation>()
  }
  
  suspend fun setUserTyping(
    conversationId: String,
    userId: String,
    isTyping: Boolean
  ) {
    supabase
      .from("typing_status")
      .upsert(mapOf(
        "conversation_id" to conversationId,
        "user_id" to userId,
        "is_typing" to isTyping,
        "updated_at" to Clock.System.now()
      ))
      .execute()
  }
  
  fun subscribeToTypingStatus(conversationId: String): Flow<List<String>> = flow {
    supabase.realtime
      .subscribeAs<TypingStatus>(
        channel = "typing_$conversationId",
        event = "*"
      ) { typingUsers ->
        emit(typingUsers.map { it.userId })
      }
      .collect()
  }
}
```

- [ ] Create `DateTimeFormatter.kt` utility:
```kotlin
fun LocalDateTime.toFormattedTime(): String {
  val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
  return when {
    this.date == now.date -> this.time.toString().substring(0, 5) // "14:30"
    this.date.year == now.date.year -> {
      val monthDay = this.date.toString().substring(5) // "12-25"
      "$monthDay ${this.time.toString().substring(0, 5)}"
    }
    else -> this.date.toString() // "2024-12-25"
  }
}

fun LocalDateTime.toFormattedLastSeen(): String {
  val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
  val duration = now.minus(this)
  return when {
    duration.inWholeMinutes < 1 -> "Just now"
    duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m ago"
    duration.inWholeHours < 24 -> "${duration.inWholeHours}h ago"
    duration.inWholeDays == 1L -> "Yesterday"
    else -> this.date.toString()
  }
}
```

---

### Day 10: Typing Indicator & Delivery Status 💬

**Goal:** See when someone is typing and message delivery status

- [ ] Create `TypingIndicator.kt` composable (from PHASE_2_MESSAGING_CORE.md):
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

- [ ] Integrate into ChatScreen
- [ ] Test: Type in one device, see typing in another ✅

- [ ] Create delivery status indicator tests:
```kotlin
@Preview
@Composable
fun DeliveryStatusPreview() {
  Column(modifier = Modifier.padding(16.dp)) {
    MessageBubble(
      message = Message(..., deliveryStatus = PENDING),
      isOutgoing = true
    )
    MessageBubble(
      message = Message(..., deliveryStatus = SENT),
      isOutgoing = true
    )
    MessageBubble(
      message = Message(..., deliveryStatus = DELIVERED),
      isOutgoing = true
    )
    MessageBubble(
      message = Message(..., deliveryStatus = READ),
      isOutgoing = true
    )
  }
}
```

---

### Day 11: Offline Support with WorkManager 🔁

**Goal:** Messages queue offline and send when back online

- [ ] Add WorkManager dependency
- [ ] Create `MessageRetryWorker.kt` (from PHASE_2_MESSAGING_CORE.md)
- [ ] Create `SyncWorker.kt`:

```kotlin
class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
  
  override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
    return@withContext try {
      // Sync pending messages
      Log.d("SyncWorker", "Starting sync...")
      Result.success()
    } catch (e: Exception) {
      Log.e("SyncWorker", "Sync failed", e)
      if (runAttemptCount < 5) Result.retry() else Result.failure()
    }
  }
}
```

- [ ] Schedule periodic sync in `Application.onCreate()`:
```kotlin
// In VelloApplication.kt
PeriodicWorkRequestBuilder<SyncWorker>(
  Duration.ofMinutes(15)
).build().let { workRequest ->
  WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "sync_messages",
    ExistingPeriodicWorkPolicy.KEEP,
    workRequest
  )
}
```

- [ ] Test offline scenario:
  1. Send message
  2. Enable Airplane Mode
  3. Send another message
  4. Verify queued locally
  5. Disable Airplane Mode
  6. Verify messages send automatically ✅

---

### Day 12: Integration Testing & Bug Fixes 🧪

**Goal:** Everything works together

- [ ] Run full instrumentation tests:
```bash
./gradlew connectedAndroidTest
```

- [ ] Test scenarios:
  - [ ] Send message → appears immediately
  - [ ] Receive message → updates in real-time
  - [ ] Delivery status updates correctly
  - [ ] Read receipts work
  - [ ] Edit message
  - [ ] Delete message
  - [ ] Offline sending

- [ ] Fix any issues that come up
- [ ] Check logcat for warnings/errors
- [ ] Verify no crashes

---

### Day 13-14: Polish & Documentation 📝

**Day 13:**

- [ ] Add error handling throughout
- [ ] Create error strings in `res/values/strings.xml`:
```xml
<string name="error_send_message">Failed to send message</string>
<string name="error_load_messages">Failed to load messages</string>
<string name="error_network">No internet connection</string>
<string name="error_unknown">Something went wrong</string>
```

- [ ] Test error scenarios:
  - [ ] Network failure → shows error banner
  - [ ] Can retry
  - [ ] Error clears after retry

- [ ] Add logging:
```kotlin
Log.d("ChatScreen", "Screen created for conversation: $conversationId")
Log.d("ChatScreen", "Loaded ${messages.size} messages")
Log.e("ChatScreen", "Failed to send message", exception)
```

**Day 14:**

- [ ] Create UI documentation
- [ ] Record screen recording of features working
- [ ] Create commit with final changes:
```bash
git add .
git commit -m "feat(phase2): complete messaging core system

- Message sending/receiving in real-time
- Delivery status tracking (pending→sent→delivered→read)
- Typing indicators
- Message edit/delete
- Offline message queuing
- Read receipts
- Error handling and retry logic"
```

- [ ] Create PR with description
- [ ] Get code review from team (if applicable)
- [ ] Merge to main branch

---

## ✅ Phase 2 Completion Checklist

### Functionality
- [x] Send text messages
- [x] Receive messages in real-time
- [x] Delivery status shows correctly
- [x] Read receipts work
- [x] Typing indicator
- [x] Edit messages
- [x] Delete messages
- [x] Offline message queue

### Code Quality
- [x] Unit tests passing
- [x] UI tests passing
- [x] No crashes
- [x] No memory leaks
- [x] Proper error handling
- [x] Code documented

### Performance
- [x] Messages load in < 500ms
- [x] Smooth scrolling (60fps)
- [x] No ANR warnings
- [x] Battery efficient

### UI/UX
- [x] Modern, clean design
- [x] Smooth animations
- [x] Responsive to input
- [x] Clear error messages
- [x] Accessibility ready

---

## 🎯 Success Metrics

By end of Day 14:

| Metric | Target | Status |
|--------|--------|--------|
| Message Latency | < 500ms | ✅ |
| Delivery Success Rate | 99.9% | ✅ |
| App Crashes | 0 | ✅ |
| Test Coverage | > 80% | ✅ |
| UI Frame Rate | 60fps | ✅ |

---

## 🚀 What's Next (Week 5-6)

Once Phase 2 is complete:
- Browse Peoples screen
- User search functionality
- Send connection requests
- Accept/reject connections
- Connected users list

---

**Good luck! You've got this! 💪**

**Need help?** 
- Stuck on a task? → Check PHASE_2_MESSAGING_CORE.md for full code
- Build error? → Check Gradle sync is complete
- Test failing? → Run with `--info` flag for details

**Track progress:**
```bash
# Check current branch
git branch

# See commits
git log --oneline

# Push to remote
git push origin feature/phase2-messaging
```

**Celebrate when done!** 🎉
