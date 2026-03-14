package com.example.readermode

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Application-level service that persists the Reader Mode enabled/disabled state.
 */
@Service(Service.Level.APP)
@State(name = "ReaderModeService", storages = [Storage("readerMode.xml")])
class ReaderModeService : PersistentStateComponent<ReaderModeService.State> {

    class State {
        @JvmField
        var enabled: Boolean = false
    }

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    var isEnabled: Boolean
        get() = myState.enabled
        set(value) {
            myState.enabled = value
        }

    companion object {
        @JvmStatic
        fun getInstance(): ReaderModeService =
            ApplicationManager.getApplication().getService(ReaderModeService::class.java)
    }
}

