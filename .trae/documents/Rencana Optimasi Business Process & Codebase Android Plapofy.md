Berikut adalah analisis komprehensif dan rencana iterasi untuk menyelaraskan flow bisnis antara Android, Backend, dan kebutuhan User.

# Analisis Kesenjangan (Gap Analysis)

Berdasarkan pengecekan codebase Android dan Backend, berikut adalah temuan utama:

### 1. Customer Journey (Android)
| Fitur | Requirement User | Kondisi Existing (Android) | Status |
| :--- | :--- | :--- | :--- |
| **Pre-Login** | Splash, Info Produk, **Promo**, **Banner OJK** | Splash & Info Produk ada. **Promo & Banner OJK tidak ada**. | ⚠️ Partial |
| **Profile** | KTP & **No. Rekening** | Hanya ada NIK (KTP). **No. Rekening belum ada**. | ⚠️ Partial |
| **Loan Input** | Tenor **Dropdown (Backend)** | Input Text Manual (Bebas ketik). | ❌ Mismatch |
| **Loan Input** | Pilih **Cabang (Backend)** | **Tidak ada fitur pilih cabang**. | ❌ Critical |
| **Status** | History (Reviewed, Reject, Disbursed) | Mapping status terbatas (`LoanStatus` perlu update). | ⚠️ Partial |

### 2. Backend & RBAC Readiness
| Fitur | Requirement | Kondisi Existing (Backend) | Status |
| :--- | :--- | :--- | :--- |
| **RBAC Roles** | Marketing, Branch Manager, BO | **Ada** di database seeds (`MARKETING`, `BRANCH_MANAGER`, `BACK_OFFICE`). | ✅ Ready |
| **Branch Data** | Master Data Cabang | **Ada** (`BranchController`, `Branch` entity). | ✅ Ready |
| **Flow Approval** | Review -> Approval -> Disburse | **Ada** (`LoanStatus` supports `REVIEWED`, `APPROVED`, `DISBURSED`). | ✅ Ready |
| **Customer Data** | Bank Account Fields | **Ada** di tabel `customers` (`bank_name`, `bank_account_number`). | ✅ Ready |

---

# Rencana Iterasi (Implementation Plan)

Saya mengusulkan rencana perbaikan berikut untuk memenuhi requirement Anda:

## Phase 1: Android UI & Data Model Update (Prioritas Utama)
Fokus pada kelengkapan data agar pengajuan pinjaman bisa diproses oleh backend.

1.  **Update User Profile:**
    *   Menambahkan field `bankName` dan `bankAccountNumber` di `User` model dan halaman `ProfileScreen`.
    *   Memastikan data ini dikirim saat update profil.
2.  **Enhance Loan Application (`ApplyLoanScreen`):**
    *   **Branch Selection:** Menambahkan API Call `GET /branches` dan membuat Dropdown Pilihan Cabang.
    *   **Tenor Dropdown:** Mengubah input tenor menjadi Dropdown yang datanya diambil dari `Plafond.interests` (sesuai data backend).
3.  **Home & Info Update:**
    *   Menambahkan **Banner OJK** (Static Image) dan **Promo Section** di `HomeScreen`.

## Phase 2: State Management & History
Fokus pada transparansi status ke user.

1.  **Update LoanStatus Enum:**
    *   Sinkronisasi enum Android dengan Backend (`REVIEWED`, `APPROVED`, `DISBURSED`, `REJECTED`).
2.  **History Screen:**
    *   Memastikan status "Sedang Direview" (oleh Marketing/BM) terlihat jelas, bukan sekadar "Pending".

## Phase 3: Backend Integration (Jika diperlukan)
*   Memastikan endpoint `/branches` dapat diakses publik/authenticated oleh aplikasi Android.

---

Apakah Anda setuju untuk memulai dengan **Phase 1** (Melengkapi Profile & Form Pengajuan Pinjaman) agar sesuai dengan logika Backend?
