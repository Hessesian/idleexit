package com.github.hessesian.idleexit.vim

import com.intellij.openapi.editor.actionSystem.TypedAction
import com.maddyhome.idea.vim.VimTypedActionHandler
import com.maddyhome.idea.vim.extension.VimExtension
import java.util.Timer
import java.util.TimerTask

class IdeaVimIdleExitExtension : VimExtension {
  override fun getName(): String {
    return "idle-exit"
  }

  override fun init() {

    val typedAction = TypedAction.getInstance()
    val initTimer = Timer()
    initTimer.schedule(object : TimerTask() {
      override fun run() {
        // assign to local val for the type check and smart casting to work properly
        val rawTypedActionHandler = typedAction.rawHandler
        if (rawTypedActionHandler is VimTypedActionHandler) {
          typedAction.setupRawHandler(IdleExitActionHandler(rawTypedActionHandler))
          initTimer.cancel()
        }
      }
    }, 0L, 500L)
    // Get the idle timeout from the configuration
  }
}