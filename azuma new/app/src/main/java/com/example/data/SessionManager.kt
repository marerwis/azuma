package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore singleton
val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "azooma_session")

object SessionKeys {
    val USER_ID          = stringPreferencesKey("user_id")
    val FULL_NAME        = stringPreferencesKey("full_name")
    val EMAIL            = stringPreferencesKey("email")
    val PHONE            = stringPreferencesKey("phone")
    val ROLE             = stringPreferencesKey("role")
    val AVATAR_URL       = stringPreferencesKey("avatar_url")
    val FIREBASE_TOKEN   = stringPreferencesKey("firebase_token")
    val SUPABASE_TOKEN   = stringPreferencesKey("supabase_token")
}

class SessionManager(private val context: Context) {

    val sessionFlow: Flow<UserSession?> = context.sessionDataStore.data.map { prefs ->
        val id = prefs[SessionKeys.USER_ID] ?: return@map null
        UserSession(
            id            = id,
            email         = prefs[SessionKeys.EMAIL],
            fullName      = prefs[SessionKeys.FULL_NAME] ?: "",
            phone         = prefs[SessionKeys.PHONE],
            role          = prefs[SessionKeys.ROLE] ?: "customer",
            avatarUrl     = prefs[SessionKeys.AVATAR_URL],
            supabaseToken = prefs[SessionKeys.SUPABASE_TOKEN]
        )
    }

    val firebaseTokenFlow: Flow<String?> = context.sessionDataStore.data.map { prefs ->
        prefs[SessionKeys.FIREBASE_TOKEN]
    }

    suspend fun save(session: UserSession, firebaseToken: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[SessionKeys.USER_ID]        = session.id
            prefs[SessionKeys.FULL_NAME]      = session.fullName
            prefs[SessionKeys.EMAIL]          = session.email ?: ""
            prefs[SessionKeys.PHONE]          = session.phone ?: ""
            prefs[SessionKeys.ROLE]           = session.role
            prefs[SessionKeys.AVATAR_URL]     = session.avatarUrl ?: ""
            prefs[SessionKeys.FIREBASE_TOKEN] = firebaseToken
            prefs[SessionKeys.SUPABASE_TOKEN] = session.supabaseToken ?: ""
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.clear() }
    }
}
