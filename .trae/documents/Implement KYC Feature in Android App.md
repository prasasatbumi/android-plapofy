Based on the comprehensive analysis of both the Android (client) and Backend (server) codebase, here is the explanation and the plan to resolve the "Account not verified" issue.

### Root Cause Analysis
The error **"Your account is not verified. Please complete KYC process in your profile."** is triggered by the Backend server, specifically in `LoanServiceImpl.java`.

1.  **Backend Validation Logic (`LoanServiceImpl.java`):**
    *   When a loan is submitted (`submitLoan`), the system retrieves the `Customer` profile associated with the current user.
    *   It checks `customer.getKycStatus()`.
    *   **Condition:** If `kycStatus` is NOT `VERIFIED`, it throws the exception: `IllegalStateException("Your account is not verified...")`.
    *   **Current State:** Your user likely has `kycStatus` as `UNVERIFIED` or `PENDING` in the database.

2.  **KYC Process Gap:**
    *   The `Customer` entity has fields for KYC images (`ktpImagePath`, `selfieImagePath`, etc.) and a `kycStatus` enum.
    *   There is an endpoint `POST /api/customers/kyc` to submit these documents.
    *   There is an endpoint `PATCH /api/customers/{id}/verify-kyc` for Back Office to approve them.
    *   **Missing Link:** The Android app currently **DOES NOT** have a screen or logic to upload these KYC documents (`ktpImage`, `selfieImage`). It only updates basic profile info (Name, NIK, etc.) via `ProfileRepository`.

### Plan to Resolve

To fix this end-to-end, we need to implement the missing KYC flow in the Android app.

#### Phase 1: Android KYC Implementation (New Feature)
1.  **Create KYC Screen:**
    *   Build a new UI `KycScreen` that allows users to upload/take photos of:
        *   KTP (Wajib)
        *   Selfie (Wajib)
        *   NPWP (Opsional)
    *   Use `ActivityResultContracts.TakePicture` or file picker.
2.  **Implement KYC Repository & API:**
    *   Add `submitKyc` method in `ProfileRepository`.
    *   Define `MultipartBody.Part` parameters for image upload in `ProfileApi`.
    *   Connect to `POST /api/customers/kyc`.
3.  **Update Profile Logic:**
    *   In `ProfileScreen` or `HomeScreen`, show a "Verifikasi Akun" button if status is not verified.
    *   Handle the specific error in `LoanViewModel` to redirect users to `KycScreen` if they try to apply for a loan without verification.

#### Phase 2: Backend Verification (Manual Workaround for Testing)
Since the `verify-kyc` endpoint requires a Back Office role, for immediate testing purposes, you can manually update the database or use the API if you have a Back Office account.
*   **SQL Workaround:** `UPDATE customers SET kyc_status = 'VERIFIED' WHERE user_id = [YOUR_USER_ID];`

### Immediate Action Plan (Code Changes)

I will proceed with **Phase 1** to build the KYC feature in Android so users can legitimately verify their accounts.

1.  **Create `KycScreen.kt`**: UI for image capture/upload.
2.  **Update `ProfileApi.kt`**: Add `submitKyc` endpoint (Multipart).
3.  **Update `ProfileRepository`**: Handle image file conversion and API call.
4.  **Integrate Navigation**: Add route to KYC screen from Profile/Home.
