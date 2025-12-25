# Debug Checklist: Data Tidak Masuk ke Backend Database

## Quick Checks

### 1. Backend Server Status
- [ ] Backend server running di `http://localhost:3000`?
- [ ] Database MySQL running?
- [ ] No errors di backend console?

### 2. Network Configuration
- [ ] Emulator bisa akses `http://10.0.2.2:3000`?
- [ ] ApiClient BASE_URL correct?
- [ ] Internet permission di AndroidManifest?

### 3. Authentication
- [ ] User sudah login?
- [ ] Token tersimpan di SharedPreferences?
- [ ] Token valid (tidak expired)?

### 4. API Calls
- [ ] Check Logcat untuk API call logs
- [ ] Response code 200/201 atau error?
- [ ] Request body correct?

### 5. Repository Integration
- [ ] TagihanViewModel menggunakan repository?
- [ ] Create method memanggil repository.createTagihan()?

## Detailed Investigation Steps

Run these checks in order:

### Step 1: Verify Backend Server
```bash
# Check if server is running
curl http://localhost:3000/api/tagihan
# Should return 401 Unauthorized (expected without token)
```

### Step 2: Check Emulator Network
```bash
# From emulator terminal or adb shell
curl http://10.0.2.2:3000/api/tagihan
```

### Step 3: Check Logcat Filters
```
# In Android Studio Logcat, filter by:
- "TagihanRepository"
- "ApiClient"
- "TagihanViewModel"
- "HTTP"
```

### Step 4: Verify Token
```kotlin
// Add temporary log in LoginScreen after login
Log.d("DEBUG", "Token saved: ${tokenManager.getToken()}")
```

### Step 5: Check Create Flow
Look for these logs in order:
1. "Tagihan ditambahkan ke database" (local save)
2. "Tagihan saved locally with ID: X" (repository)
3. HTTP request log (OkHttp)
4. "Tagihan synced to backend with server ID: Y" (success)

## Common Issues & Solutions

### Issue 1: "No internet connection" in logs
**Cause**: NetworkUtils returns false
**Solution**: Check emulator network settings

### Issue 2: "403 Forbidden" or "401 Unauthorized"
**Cause**: Token missing or invalid
**Solution**: Logout and login again

### Issue 3: No HTTP logs at all
**Cause**: Repository not being used
**Solution**: Check if ViewModel is using repository methods

### Issue 4: "Connection refused"
**Cause**: Backend not running or wrong URL
**Solution**: Start backend, verify URL is `http://10.0.2.2:3000`

### Issue 5: Data saved locally but not synced
**Cause**: Offline or sync failed
**Solution**: Check network, call manualSync()
