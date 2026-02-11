package com.finprov.plapofy.domain.model

data class User(
    val id: Long,
    val username: String,
    val email: String?,
    val name: String?,
    val phoneNumber: String?,
    val nik: String?,
    val npwp: String?,
    val address: String?,
    val occupation: String?,
    val monthlyIncome: Double?,
    val bankName: String?,
    val bankAccountNumber: String?,
    val isActive: Boolean = true,
    val kycStatus: String? = null,
    val ktpImagePath: String? = null,
    val selfieImagePath: String? = null,
    val isGoogleUser: Boolean = false
) {
    // Helper to get full image URL
    fun getKtpUrl(): String? = sanitizeUrl(ktpImagePath)
    fun getSelfieUrl(): String? = sanitizeUrl(selfieImagePath)

    private fun sanitizeUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http")) return path
        
        val cleanPath = path.replace("\\", "/")
        val fileName = if (cleanPath.startsWith("/")) cleanPath.substring(1) else cleanPath
        
        // BASE_URL from BuildConfig
        return com.finprov.plapofy.BuildConfig.BASE_URL + "files/" + fileName
    }

    // Check if profile is complete for loan application
    fun isProfileComplete(): Boolean {
        return !name.isNullOrBlank() &&
                !email.isNullOrBlank() &&
                !phoneNumber.isNullOrBlank() &&
                !nik.isNullOrBlank() &&
                !address.isNullOrBlank() &&
                !occupation.isNullOrBlank() &&
                monthlyIncome != null && monthlyIncome > 0 &&
                !bankName.isNullOrBlank() &&
                !bankAccountNumber.isNullOrBlank() &&
                // KYC Check
                (kycStatus == "VERIFIED" || (kycStatus == "SUBMITTED" && !ktpImagePath.isNullOrBlank() && !selfieImagePath.isNullOrBlank()))
    }

    // Get list of missing fields
    fun getMissingFields(): List<String> {
        val missing = mutableListOf<String>()
        if (name.isNullOrBlank()) missing.add("Nama Lengkap")
        if (email.isNullOrBlank()) missing.add("Email")
        if (phoneNumber.isNullOrBlank()) missing.add("Nomor Telepon")
        if (nik.isNullOrBlank()) missing.add("NIK")
        if (address.isNullOrBlank()) missing.add("Alamat")
        if (occupation.isNullOrBlank()) missing.add("Pekerjaan")
        if (monthlyIncome == null || monthlyIncome <= 0) missing.add("Penghasilan Bulanan")
        if (bankName.isNullOrBlank()) missing.add("Nama Bank")
        if (bankAccountNumber.isNullOrBlank()) missing.add("Nomor Rekening")
        
        // KYC Checks
        if (kycStatus != "VERIFIED") {
             if (ktpImagePath.isNullOrBlank()) missing.add("Foto KTP")
             if (selfieImagePath.isNullOrBlank()) missing.add("Selfie dengan KTP")
             // If submitted but waiting, maybe we don't block? 
             // But for now, let's assume if it's not VERIFIED and images are missing, it's incomplete.
        }
        
        return missing
    }
}
