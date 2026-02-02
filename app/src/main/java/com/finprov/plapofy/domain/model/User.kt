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
    val selfieImagePath: String? = null
) {
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
                !bankAccountNumber.isNullOrBlank()
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
        return missing
    }
}
