package com.progreen.recycleapp.ui.common

import androidx.fragment.app.Fragment
import com.progreen.recycleapp.RecycleApplication
import com.progreen.recycleapp.data.repository.AuthRepository
import com.progreen.recycleapp.data.repository.RecyclingRepository

object Injection {
    fun authRepository(fragment: Fragment): AuthRepository {
        val app = fragment.requireActivity().application as RecycleApplication
        return AuthRepository(app.prefsManager)
    }

    fun recyclingRepository(fragment: Fragment): RecyclingRepository {
        val app = fragment.requireActivity().application as RecycleApplication
        return RecyclingRepository(app.prefsManager)
    }

    fun authRepository(activity: androidx.appcompat.app.AppCompatActivity): AuthRepository {
        val app = activity.application as RecycleApplication
        return AuthRepository(app.prefsManager)
    }

    fun recyclingRepository(activity: androidx.appcompat.app.AppCompatActivity): RecyclingRepository {
        val app = activity.application as RecycleApplication
        return RecyclingRepository(app.prefsManager)
    }
}
