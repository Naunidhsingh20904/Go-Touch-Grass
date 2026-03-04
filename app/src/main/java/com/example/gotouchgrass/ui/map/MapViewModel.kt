package com.example.gotouchgrass.ui.map

import androidx.lifecycle.ViewModel
import com.example.gotouchgrass.domain.ApproxPresence
import com.example.gotouchgrass.domain.FakeData

class MapViewModel() : ViewModel() {
    fun getTotalXp(): Int {
        return FakeData.users.firstOrNull { it.id == "user_you" }?.xpTotal ?: 0
    }

    fun getNearbyUsers(): List<ApproxPresence> {
        return FakeData.approxPresence
    }
}