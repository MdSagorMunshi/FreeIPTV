package com.ryanshelby.iptv.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ryanshelby.iptv.data.Channel
import com.ryanshelby.iptv.data.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val channels: List<Channel> = emptyList(),
    val filteredChannels: List<Channel> = emptyList(),
    val groups: List<String> = emptyList(),
    val selectedGroup: String = "All",
    val searchQuery: String = "",
    val currentIndex: Int = -1,
    val isLoading: Boolean = true,
    val favorites: Set<String> = emptySet(),
    val updateRequiredUrl: String? = null,
    val isCheckingForUpdate: Boolean = false,
    val toastMessage: String? = null,
    val worldChannels: List<Channel> = emptyList(),
    val filteredWorldChannels: List<Channel> = emptyList(),
    val worldGroups: List<String> = emptyList(),
    val selectedWorldGroup: String = "All",
    val worldSearchQuery: String = "",
    val currentWorldIndex: Int = -1,
    val isWorldLoading: Boolean = false,
    val isWorldLoaded: Boolean = false,
    val isViewingWorld: Boolean = false
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init { loadPlaylist() }

    private fun loadPlaylist() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val input = getApplication<Application>().assets.open("aynaott.m3u")
                val channels = M3UParser.parse(input)
                val groups = listOf("All") + channels.map { it.group }.filter { it.isNotBlank() }.distinct().sorted()
                _state.value = _state.value.copy(
                    channels = channels,
                    filteredChannels = channels,
                    groups = groups,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun selectChannel(index: Int, isWorld: Boolean = false) {
        if (isWorld) {
            _state.value = _state.value.copy(currentWorldIndex = index, isViewingWorld = true)
        } else {
            _state.value = _state.value.copy(currentIndex = index, isViewingWorld = false)
        }
    }

    fun nextChannel() {
        val list = if (_state.value.isViewingWorld) _state.value.filteredWorldChannels else _state.value.filteredChannels
        if (list.isEmpty()) return
        val current = if (_state.value.isViewingWorld) _state.value.currentWorldIndex else _state.value.currentIndex
        val next = if (current + 1 >= list.size) 0 else current + 1
        if (_state.value.isViewingWorld) {
            _state.value = _state.value.copy(currentWorldIndex = next)
        } else {
            _state.value = _state.value.copy(currentIndex = next)
        }
    }

    fun prevChannel() {
        val list = if (_state.value.isViewingWorld) _state.value.filteredWorldChannels else _state.value.filteredChannels
        if (list.isEmpty()) return
        val current = if (_state.value.isViewingWorld) _state.value.currentWorldIndex else _state.value.currentIndex
        val prev = if (current - 1 < 0) list.size - 1 else current - 1
        if (_state.value.isViewingWorld) {
            _state.value = _state.value.copy(currentWorldIndex = prev)
        } else {
            _state.value = _state.value.copy(currentIndex = prev)
        }
    }

    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        applyFilters()
    }

    fun selectGroup(group: String) {
        _state.value = _state.value.copy(selectedGroup = group)
        applyFilters()
    }

    fun toggleFavorite(channelUrl: String) {
        val favs = _state.value.favorites.toMutableSet()
        if (favs.contains(channelUrl)) favs.remove(channelUrl) else favs.add(channelUrl)
        _state.value = _state.value.copy(favorites = favs)
        applyFilters()
    }

    private fun applyFilters() {
        val s = _state.value
        var list = s.channels
        if (s.selectedGroup == "Favorites") {
            list = list.filter { s.favorites.contains(it.url) }
        } else if (s.selectedGroup != "All") {
            list = list.filter { it.group == s.selectedGroup }
        }
        if (s.searchQuery.isNotBlank()) {
            list = list.filter { it.name.contains(s.searchQuery, ignoreCase = true) }
        }
        
        var worldList = s.worldChannels
        if (s.selectedWorldGroup == "Favorites") {
            worldList = worldList.filter { s.favorites.contains(it.url) }
        } else if (s.selectedWorldGroup != "All") {
            worldList = worldList.filter { it.group == s.selectedWorldGroup }
        }
        if (s.worldSearchQuery.isNotBlank()) {
            worldList = worldList.filter { it.name.contains(s.worldSearchQuery, ignoreCase = true) }
        }
        
        _state.value = s.copy(filteredChannels = list, filteredWorldChannels = worldList)
    }

    fun getCurrentChannel(): Channel? {
        val s = _state.value
        if (s.isViewingWorld) {
            return if (s.currentWorldIndex in s.filteredWorldChannels.indices) s.filteredWorldChannels[s.currentWorldIndex] else null
        }
        return if (s.currentIndex in s.filteredChannels.indices) s.filteredChannels[s.currentIndex] else null
    }

    fun checkForUpdates(currentVersion: String, isManualCheck: Boolean = false) {
        if (_state.value.isCheckingForUpdate) return
        _state.value = _state.value.copy(isCheckingForUpdate = true)
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://api.github.com/repos/MdSagorMunshi/FreeIPTV/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val json = org.json.JSONObject(response)
                    val tagName = json.getString("tag_name").removePrefix("v")
                    val cleanCurrent = currentVersion.removePrefix("v")
                    
                    if (isNewerVersion(cleanCurrent, tagName)) {
                        // We found the APK asset directly
                        val assets = json.optJSONArray("assets")
                        var apkUrl = json.optString("html_url", "https://github.com/MdSagorMunshi/FreeIPTV/releases")
                        
                        if (assets != null) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.getJSONObject(i)
                                if (asset.getString("name").endsWith(".apk")) {
                                    apkUrl = asset.getString("browser_download_url")
                                    break
                                }
                            }
                        }
                        
                        _state.value = _state.value.copy(
                            updateRequiredUrl = apkUrl,
                            isCheckingForUpdate = false
                        )
                    } else {
                        val newToast = if (isManualCheck) "You are already using the latest version." else _state.value.toastMessage
                        _state.value = _state.value.copy(isCheckingForUpdate = false, toastMessage = newToast)
                    }
                } else {
                    _state.value = _state.value.copy(isCheckingForUpdate = false)
                }
            } catch (e: Exception) {
                val newToast = if (isManualCheck) "Failed to check for updates. Check connection." else _state.value.toastMessage
                _state.value = _state.value.copy(isCheckingForUpdate = false, toastMessage = newToast)
            }
        }
    }

    private fun isNewerVersion(current: String, remote: String): Boolean {
        val currParts = current.split(".").map { it.toIntOrNull() ?: 0 }
        val remParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val length = maxOf(currParts.size, remParts.size)
        for (i in 0 until length) {
            val c = currParts.getOrElse(i) { 0 }
            val r = remParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (c > r) return false
        }
        return false
    }

    fun clearToast() {
        _state.value = _state.value.copy(toastMessage = null)
    }

    fun searchWorld(query: String) {
        _state.value = _state.value.copy(worldSearchQuery = query)
        applyFilters()
    }

    fun selectWorldGroup(group: String) {
        _state.value = _state.value.copy(selectedWorldGroup = group)
        applyFilters()
    }

    fun setIsViewingWorld(isWorld: Boolean) {
        _state.value = _state.value.copy(isViewingWorld = isWorld)
    }

    fun loadWorldChannels() {
        if (_state.value.isWorldLoaded || _state.value.isWorldLoading) return
        _state.value = _state.value.copy(isWorldLoading = true)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val input = getApplication<Application>().assets.open("world.m3u")
                val channels = M3UParser.parse(input)
                val groups = listOf("All") + channels.map { it.group }.filter { it.isNotBlank() }.distinct().sorted()
                _state.value = _state.value.copy(
                    worldChannels = channels,
                    filteredWorldChannels = channels,
                    worldGroups = groups,
                    isWorldLoading = false,
                    isWorldLoaded = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isWorldLoading = false, toastMessage = "Error loading World TV list: ${e.message}")
            }
        }
    }
}
