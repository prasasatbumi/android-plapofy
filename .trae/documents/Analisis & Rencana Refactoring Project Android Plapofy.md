Terima kasih telah meminta pengecekan spesifik pada logika Firebase.

### 1. Analisis Logika Firebase (Existing)
Saya telah memeriksa `LoginScreen.kt`, `AuthViewModel.kt`, `AuthRepositoryImpl.kt`, dan `MyFirebaseMessagingService.kt`.

**Status: SUDAH TERIMPLEMENTASI dengan Baik ✅**
*   **Token Retrieval:** Di `LoginScreen`, aplikasi sudah mengambil FCM Token via `FirebaseMessaging.getInstance().token`.
*   **Token Sending:** Token dikirim ke backend saat fungsi `login` dipanggil (`viewModel.login(..., fcmToken)`).
*   **Notification Handling:** `MyFirebaseMessagingService` sudah siap menerima pesan dan menampilkan notifikasi.

### 2. Gap / Kekurangan yang Ditemukan
Meskipun logika dasar sudah ada, ada satu hal **KRUSIAL** yang belum tertangani untuk Android 13 (Tiramisu) ke atas:

*   **Runtime Permission:** Di `AndroidManifest.xml` sudah ada `POST_NOTIFICATIONS`, tetapi **kode untuk meminta izin tersebut ke user (popup permission) belum ada**.
    *   *Dampak:* Pada HP Android 13+, notifikasi tidak akan muncul sama sekali karena user belum mengizinkan.

*   **Hardcoded Channel Name:** Di `MyFirebaseMessagingService`, nama channel masih "Channel human readable title". Sebaiknya diganti agar terlihat profesional di pengaturan notifikasi user.

### 3. Rencana Implementasi (Final)

Saya akan menggabungkan rekomendasi Clean Code sebelumnya dengan perbaikan Firebase ini.

#### Tahap 1: Cleanup & Foundation
1.  **Hapus Room** (Unused dependency).
2.  **Extract Strings** (Hardcoded text di UI & Service).
3.  **Fix Channel Name** di `MyFirebaseMessagingService`.

#### Tahap 2: Firebase Improvement
1.  **Implementasi Permission Request:** Menambahkan logic di `HomeScreen` atau `LoginScreen` untuk meminta izin notifikasi (Android 13+).

#### Tahap 3: Architecture Refactoring
1.  Membuat **UseCases** (`ValidateLoanUseCase`, `SubmitLoanUseCase`).
2.  Refactor `LoanViewModel` menggunakan UseCases.

Apakah Anda setuju untuk memulai dengan **Tahap 1 (Cleanup & Fix Channel Name)** lalu lanjut ke **Permission Request**?