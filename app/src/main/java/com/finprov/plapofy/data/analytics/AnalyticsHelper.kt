package com.finprov.plapofy.data.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Analytics helper for tracking user events and screen views.
 * Uses Firebase Analytics under the hood.
 */
object AnalyticsHelper {
    
    private val analytics: FirebaseAnalytics by lazy { Firebase.analytics }
    
    // Screen names
    object Screens {
        const val SPLASH = "splash_screen"
        const val HOME = "home_screen"
        const val LOGIN = "login_screen"
        const val REGISTER = "register_screen"
        const val PROFILE = "profile_screen"
        const val SIMULATION = "simulation_screen"
        const val APPLY_LOAN = "apply_loan_screen"
        const val MY_LOANS = "my_loans_screen"
        const val LOAN_DETAIL = "loan_detail_screen"
        const val KYC = "kyc_screen"
        const val CHANGE_PASSWORD = "change_password_screen"
    }
    
    // Event names
    object Events {
        const val LOGIN = "login"
        const val SIGN_UP = "sign_up"
        const val LOAN_SIMULATE = "loan_simulate"
        const val LOAN_SUBMIT = "loan_submit"
        const val LOAN_VIEW_DETAIL = "loan_view_detail"
        const val PROFILE_UPDATE = "profile_update"
        const val KYC_SUBMIT = "kyc_submit"
        const val PASSWORD_CHANGE = "password_change"
    }
    
    // Params
    object Params {
        const val METHOD = "method"
        const val PLAFOND_ID = "plafond_id"
        const val PLAFOND_NAME = "plafond_name"
        const val AMOUNT = "amount"
        const val TENOR = "tenor"
        const val LOAN_ID = "loan_id"
        const val SUCCESS = "success"
    }
    
    /**
     * Log a screen view event
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
    
    /**
     * Log user login event
     */
    fun logLogin(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }
    
    /**
     * Log user sign up event
     */
    fun logSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }
    
    /**
     * Log loan simulation event
     */
    fun logLoanSimulate(plafondId: Long, amount: Double, tenor: Int) {
        val bundle = Bundle().apply {
            putLong(Params.PLAFOND_ID, plafondId)
            putDouble(Params.AMOUNT, amount)
            putInt(Params.TENOR, tenor)
        }
        analytics.logEvent(Events.LOAN_SIMULATE, bundle)
    }
    
    /**
     * Log loan submission event
     */
    fun logLoanSubmit(plafondId: Long, plafondName: String?, amount: Double, tenor: Int, success: Boolean) {
        val bundle = Bundle().apply {
            putLong(Params.PLAFOND_ID, plafondId)
            plafondName?.let { putString(Params.PLAFOND_NAME, it) }
            putDouble(Params.AMOUNT, amount)
            putInt(Params.TENOR, tenor)
            putBoolean(Params.SUCCESS, success)
        }
        analytics.logEvent(Events.LOAN_SUBMIT, bundle)
    }
    
    /**
     * Log loan detail view
     */
    fun logLoanViewDetail(loanId: Long) {
        val bundle = Bundle().apply {
            putLong(Params.LOAN_ID, loanId)
        }
        analytics.logEvent(Events.LOAN_VIEW_DETAIL, bundle)
    }
    
    /**
     * Log profile update
     */
    fun logProfileUpdate(success: Boolean) {
        val bundle = Bundle().apply {
            putBoolean(Params.SUCCESS, success)
        }
        analytics.logEvent(Events.PROFILE_UPDATE, bundle)
    }
    
    /**
     * Log KYC submission
     */
    fun logKycSubmit(success: Boolean) {
        val bundle = Bundle().apply {
            putBoolean(Params.SUCCESS, success)
        }
        analytics.logEvent(Events.KYC_SUBMIT, bundle)
    }
    
    /**
     * Log password change
     */
    fun logPasswordChange(success: Boolean) {
        val bundle = Bundle().apply {
            putBoolean(Params.SUCCESS, success)
        }
        analytics.logEvent(Events.PASSWORD_CHANGE, bundle)
    }
    
    /**
     * Set user ID for better tracking
     */
    fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }
    
    /**
     * Set user property
     */
    fun setUserProperty(name: String, value: String?) {
        analytics.setUserProperty(name, value)
    }
}
