Anda mengalami error `500 Internal Server Error` dengan pesan:
`Your account is not verified. Please complete KYC process in your profile. (java.lang.IllegalStateException)`

Ini menunjukkan bahwa meskipun Anda sudah mengisi data profil di aplikasi, backend menganggap akun Anda belum terverifikasi (KYC). Namun, di kode aplikasi Android yang saya periksa (`User.kt`), validasi profil hanya mengecek kelengkapan field (`name`, `nik`, `address`, dll) dan tidak ada field status `isVerified` atau `kycStatus`.

Kemungkinan besar:
1.  **Backend Logic:** Backend memiliki logika verifikasi tambahan yang tidak tercermin di frontend saat ini. Backend mungkin mengharapkan user melakukan upload KTP atau proses verifikasi lain yang belum ada fiturnya di aplikasi Android.
2.  **Missing Field:** Mungkin ada field tertentu yang wajib di backend tapi belum dikirim oleh Android saat update profile.

Karena saya hanya memiliki akses ke kode Android, solusi terbaik saat ini adalah memastikan semua data yang diminta backend dikirim dengan benar.

Namun, untuk mengatasi error `500` yang Anda alami saat ini dan melanjutkan perbaikan sebelumnya, saya merevisi rencana sebagai berikut:

### 1. Perbaikan "Simpan Profile" (Sudah Teridentifikasi)
*   Menambahkan `delay(2000)` agar pesan sukses terbaca. (Ini penting agar user yakin datanya tersimpan).

### 2. Perbaikan Flow Pinjaman (Simulation -> Apply)
*   Tetap melanjutkan perbaikan navigasi agar `amount` dan `tenor` otomatis terisi dari simulasi.

### 3. Penanganan Error 500 & Clean Code
*   **Analisis Error 500:** Error ini berasal dari Backend. Aplikasi Android sudah mengirim data dengan benar (terlihat dari log request body yang lengkap).
*   **Action:** Saya akan tetap menghapus library `Room` yang tidak terpakai untuk clean code.

**Rencana Eksekusi Revisi:**
1.  **Terapkan Delay di Profile:** Agar Anda bisa memastikan profil tersimpan dengan nyaman.
2.  **Perbaiki Navigasi Simulasi:** Agar UX lebih mulus.
3.  **Hapus Room:** Cleanup project.

*Catatan: Terkait error KYC, jika setelah profil tersimpan dengan benar (setelah fix delay) error masih muncul, berarti memang ada proses di sisi backend yang perlu dicek manual atau backend perlu mengupdate status user menjadi verified secara manual/otomatis setelah update profile.*

Apakah Anda setuju untuk melanjutkan dengan perbaikan di sisi Android ini (Delay Profile, Navigasi Simulasi, Hapus Room)?