package com.example.data.supabase

import com.example.BuildConfig

object SupabaseConfig {
    val url: String
        get() {
            val u = try { BuildConfig.SUPABASE_URL } catch (_: Exception) { "" }
            val vU = try { BuildConfig.VITE_SUPABASE_URL } catch (_: Exception) { "" }
            return when {
                u.isNotBlank() && u != "https://your-project.supabase.co" -> u
                vU.isNotBlank() && vU != "https://your-project.supabase.co" -> vU
                else -> "https://your-project.supabase.co"
            }
        }

    val anonKey: String
        get() {
            val k = try { BuildConfig.SUPABASE_ANON_KEY } catch (_: Exception) { "" }
            val vK = try { BuildConfig.VITE_SUPABASE_ANON_KEY } catch (_: Exception) { "" }
            return when {
                k.isNotBlank() && k != "your-supabase-anon-key" -> k
                vK.isNotBlank() && vK != "your-supabase-anon-key" -> vK
                else -> "your-supabase-anon-key"
            }
        }

    val isConfigured: Boolean
        get() = url.startsWith("https://") &&
                !url.contains("your-project") &&
                anonKey.isNotBlank() &&
                anonKey != "your-supabase-anon-key"
}
