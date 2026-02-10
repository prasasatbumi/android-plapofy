I will modify `LoanRepositoryImpl.kt` to handle `retrofit2.HttpException` in the `submitLoan` function.

**Problem:**
When the backend returns a 500 error (e.g., "Account not verified"), Retrofit throws an `HttpException`. The current code catches generic `Exception` but does not parse the error body, so the UI receives the generic "HTTP 500" message instead of the specific KYC instruction.

**Plan:**
1.  In `LoanRepositoryImpl.kt`, import `retrofit2.HttpException` and `com.google.gson.Gson`.
2.  Modify the `submitLoan` method's `try-catch` block.
3.  Add a specific catch block for `HttpException`.
4.  Inside that block:
    *   Read `e.response()?.errorBody()?.string()`.
    *   Parse the JSON string to extract the `message` field using `JSONObject` or Gson.
    *   Return `Result.failure` with the parsed message (e.g., "Your account is not verified...").
5.  This will ensure the `LoanViewModel` receives the correct error string, triggering the `KycRequiredView` I implemented earlier.

**Code Change:**
```kotlin
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorMessage = try {
                // Parse error body to get message
                // Assuming format: {"success":false, "message":"...", ...}
                org.json.JSONObject(errorBody).getString("message")
            } catch (jsonException: Exception) {
                e.message() ?: "Terjadi kesalahan server"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: Exception) {
            Result.failure(e)
        }
```
This change is safe and directly addresses the user's issue.
