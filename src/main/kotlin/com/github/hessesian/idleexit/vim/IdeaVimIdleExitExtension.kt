package com.github.hessesian.idleexit.vim

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.intellij.util.ui.EDT
import com.maddyhome.idea.vim.VimTypedActionHandler
import com.maddyhome.idea.vim.api.injector
import com.maddyhome.idea.vim.extension.VimExtension
import java.util.Timer
import java.util.TimerTask

class IdeaVimIdleExitExtension : VimExtension {
  override fun getName(): String {
    return "idle-exit"
  }

  private var timerId: Long = -1
  private var idleTimeout: Int = 600
  override fun init() {
    val typedAction = TypedAction.getInstance()
    val timer = Timer()
    timer.schedule(object : TimerTask() {
      override fun run() {
        // assign to local val for the type check and smart casting to work properly
        val rawTypedActionHandler = typedAction.rawHandler
        if (rawTypedActionHandler is VimTypedActionHandler) {
          typedAction.setupRawHandler { editor, charTyped, dataContext ->
            if (editor.isInsertMode) {
              restartTimer() {
                if (editor.isInsertMode) {
                  EDT.getEventDispatchThread().run {
                    injector.application.runReadAction {
                      EditorActionManager.getInstance().getActionHandler(IdeActions.ACTION_EDITOR_ESCAPE).execute(editor, dataContext)
                    }
                  }
                }
              }
            }
            rawTypedActionHandler.execute(editor, charTyped, dataContext)
            timer.cancel()
          }
        }
      }
    }, 0L, 500L)
    // Get the idle timeout from the configuration
  }

  override fun dispose() {
    super.dispose()
  }

  private fun restartTimer(runnable: Runnable) {
    stopTimer()
    timerId = System.currentTimeMillis() + idleTimeout

    Thread {
      while (System.currentTimeMillis() < timerId) {
        try {
          Thread.sleep(50)
        } catch (e: InterruptedException) {
          // Handle thread interruption if needed
          e.printStackTrace()
        }
      }

      // Exit insert mode to normal mode after idleTimeout
      runnable.run()
    }.start()
  }

  private fun stopTimer() {
    timerId = -1
  }

  private fun exitInsertMode() {
    // Code to exit insert mode and switch to normal mode
    // Implement your own logic here
  }
}