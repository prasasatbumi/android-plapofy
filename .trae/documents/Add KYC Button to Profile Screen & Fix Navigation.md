Based on the user's feedback and my analysis, here are the issues and the plan to fix them:

**Issues Identified:**

1.  **Missing KYC Button in Profile Screen:**
    *   I confirmed by reading `ProfileScreen.kt` that there is indeed NO button to navigate to the KYC screen.
    *   **Fix:** Add a "Verifikasi Akun (KYC)" button in the `ProfileScreen`, preferably near the top or bottom of the form.

2.  **KYC Trigger Not Working in Apply Loan:**
    *   The user says they don't see the trigger. This could be because the error message parsing is still failing or the UI state update isn't triggering the recomposition correctly.
    *   I've already updated `LoanRepositoryImpl` to parse the error message.
    *   The issue might be that the error message from the backend contains extra characters or formatting I didn't account for, OR the `ApplyLoanScreen` logic `state.error!!.contains("not verified", ignoreCase = true)` is too strict or the message is slightly different.
    *   **Wait:** The user input showed the error message as: `"Your account is not verified. Please complete KYC process in your profile. (java.lang.IllegalStateException)"`.
    *   My parsing logic gets this string. My check is `contains("not verified")`. This *should* work.
    *   **Hypothesis:** Maybe the `ApplyLoanScreen` UI priority logic is hiding it?
        *   Code: `state.error != null && state.plafond == null` -> ErrorView (Full screen)
        *   Code: `state.error != null && state.error!!.contains(...)` -> KycRequiredView (Full screen)
        *   If `state.plafond` is NOT null (which it isn't, because we are on the form), and `state.error` is NOT null...
        *   Wait, the `ApplyLoanScreen` uses `when` block.
        *   If `state.isLoading` is true -> Loading.
        *   If `state.error != null && state.plafond == null` -> ErrorView.
        *   If `state.profileCheck?.isComplete == false` -> ProfileIncompleteView.
        *   If `state.plafond != null` -> Show Form.
        *   **CRITICAL BUG:** The check for KYC error is INSIDE the `when` block but placed *before* `state.plafond != null`.
        *   Let's re-read `ApplyLoanScreen.kt`:
            ```kotlin
            when {
                state.isLoading -> { ... }
                state.error != null && state.plafond == null -> { ... }
                state.error != null && state.error!!.contains("not verified", ignoreCase = true) -> { ... } // This is correct order
                state.profileCheck?.isComplete == false -> { ... }
                state.plafond != null -> { ... }
            }
            ```
        *   This looks correct logically. If `error` is present and contains "not verified", it should show `KycRequiredView`.
        *   However, maybe the error message is NOT being parsed correctly in `LoanRepositoryImpl` because of the `try-catch` block around `JSONObject`. The server response shown in the log is standard JSON.
        *   **Action:** I will add logging or a fallback mechanism. And most importantly, I will ensure `ProfileScreen` has the button so the user has a *manual* way to verify.

**Plan:**

1.  **Update `ProfileScreen.kt`:**
    *   Add a callback `onKycClick: () -> Unit` to `ProfileScreen`.
    *   Add a Button "Verifikasi Akun (KYC)" inside the `ProfileForm`.
    *   Update `NavGraph` to pass the navigation action.

2.  **Verify/Enhance `LoanRepositoryImpl` Parsing:**
    *   The user's log shows `Content-Type: application/json`.
    *   The error body is: `{"success":false,"message":"Your account is not verified...","data":null,"timestamp":"..."}`.
    *   My code: `org.json.JSONObject(errorBody).getString("message")`. This is correct.
    *   I will double check imports and ensure `org.json` is available (it is standard in Android).

3.  **Refine `ApplyLoanScreen`:**
    *   I will verify the `when` block order again to be absolutely sure.

**Focus:** The most critical missing piece is the **Manual KYC Button** in the Profile screen, as requested by the user ("saya tidak menemukan tombol untuk kyc di profile").

I will proceed with adding the button to the Profile Screen first.
