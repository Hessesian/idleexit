package com.github.hessesian.idleexit.vim

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.actionSystem.TypedAction
import com.maddyhome.idea.vim.VimTypedActionHandler
import com.maddyhome.idea.vim.extension.VimExtension
import com.maddyhome.idea.vim.group.visual.VimVisualTimer
import com.maddyhome.idea.vim.helper.editorMode
import com.maddyhome.idea.vim.helper.popAllModes
import java.util.Timer
import java.util.TimerTask
import javax.swing.Timer as STimer

class IdeaVimIdleExitExtension : VimExtension {
  override fun getName(): String {
    return "idle-exit"
  }

  private var idleTimeout: Int = 600
  private val logger = Logger.getInstance(this::class.java)
  override fun init() {

    val typedAction = TypedAction.getInstance()
    val initTimer = Timer()
    initTimer.schedule(object : TimerTask() {
      override fun run() {
        // assign to local val for the type check and smart casting to work properly
        val rawTypedActionHandler = typedAction.rawHandler
        if (rawTypedActionHandler is VimTypedActionHandler) {
          var timer: STimer? = null
          typedAction.setupRawHandler { editor, charTyped, dataContext ->
            if (editor.isInsertMode) {
              if (timer == null) {
                timer = STimer(idleTimeout) {
                    if (editor.isInsertMode) {
                      VimVisualTimer.singleTask(editor.editorMode) {
                        editor.popAllModes()
                      }
                  }
                }
              }
              try {
                timer?.restart()
              } catch (exc: Exception) {
                logger.error(exc)
              }
            }
            rawTypedActionHandler.execute(editor, charTyped, dataContext)
          }
        }
      }
    }, 0L, 500L)
    // Get the idle timeout from the configuration
  }

  override fun dispose() {
    super.dispose()
  }

}