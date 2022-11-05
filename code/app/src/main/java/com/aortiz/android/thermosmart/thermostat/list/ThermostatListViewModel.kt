package com.aortiz.android.thermosmart.thermostat.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aortiz.android.thermosmart.domain.Thermostat
import com.aortiz.android.thermosmart.notifications.ThermosmartFirebaseMessagingService.Companion.refreshToken
import com.aortiz.android.thermosmart.repository.ThermostatRepository
import com.aortiz.android.thermosmart.utils.ERROR
import com.aortiz.android.thermosmart.utils.OperationResult
import kotlinx.coroutines.launch
import timber.log.Timber

class ThermostatListViewModel(app: Application, private val repository: ThermostatRepository) :
    AndroidViewModel(app) {

    enum class UnfollowState {
        IDLE, UNFOLLOWED, ERROR
    }

    enum class FollowState {
        IDLE, FOLLOWED, ERROR
    }

    private val _unfollowState = MutableLiveData(UnfollowState.IDLE)
    val unfollowState: LiveData<UnfollowState>
        get() = _unfollowState

    private val _followState = MutableLiveData(FollowState.IDLE)
    val followState: LiveData<FollowState>
        get() = _followState

    private val _errorCode = MutableLiveData<ERROR?>(null)
    val errorCode: LiveData<ERROR?>
        get() = _errorCode

    var thermostatList: LiveData<List<Thermostat>> = repository.getUserThermostatListLiveData()

    init {
        viewModelScope.launch {
            repository.load()
            refreshToken {
                when (it) {
                    is OperationResult.Success -> repository.updateDeviceToken(it.data)
                    else -> {}
                }
            }
        }
    }

    fun unfollowThermostat(id: String) {
        Timber.d("unfollowThermostat")
        if (unfollowState.value == UnfollowState.IDLE) {
            repository.unfollowThermostat(id) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("unfollowThermostat:reply: success ${result.data}")
                        _unfollowState.value = UnfollowState.UNFOLLOWED
                    }
                    is OperationResult.Error -> {
                        Timber.d("unfollowThermostat:reply: error ${result.exception}")
                        _unfollowState.value = UnfollowState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun followThermostat(id: String) {
        Timber.d("followThermostat $id")
        if (followState.value == FollowState.IDLE) {
            repository.followThermostat(id) { result ->
                when (result) {
                    is OperationResult.Success -> {
                        Timber.d("followThermostat:reply: success ${result.data}")
                        _followState.value = FollowState.FOLLOWED
                    }
                    is OperationResult.Error -> {
                        Timber.d("followThermostat:reply: error ${result.exception}")
                        _followState.value = FollowState.ERROR
                        _errorCode.value = result.code
                    }
                }
            }
        }
    }

    fun clearUnfollowState() {
        _unfollowState.value = UnfollowState.IDLE
    }

    fun clearFollowState() {
        _followState.value = FollowState.IDLE
    }

    fun clearError() {
        _errorCode.value = null
    }

}