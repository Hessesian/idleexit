package com.github.hessesian.idleexit.vim

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.ActionPlan
import com.intellij.openapi.editor.actionSystem.TypedActionHandlerEx
import com.maddyhome.idea.vim.VimTypedActionHandler
import com.maddyhome.idea.vim.command.OperatorArguments
import com.maddyhome.idea.vim.helper.editorMode
import com.maddyhome.idea.vim.helper.exitInsertMode
import com.maddyhome.idea.vim.helper.subMode
import com.maddyhome.idea.vim.helper.vimStateMachine
import javax.swing.Timer

class IdleExitActionHandler(private val vimTypedActionHandler: VimTypedActionHandler) : TypedActionHandlerEx {

  private var idleTimeout: Int = 600
  private var timer: Timer? = null
  override fun execute(editor: Editor, charTyped: Char, dataContext: DataContext) {
    vimTypedActionHandler.execute(editor, charTyped, dataContext)

    if (editor.isInsertMode) {
      if (timer == null) {
        timer = Timer(idleTimeout) {
          if (editor.isInsertMode) {
            thisLogger().warn("Executing insert mode")
            editor.exitInsertMode(
              dataContext, OperatorArguments(
                editor.vimStateMachine?.isOperatorPending ?: false,
                0, editor.editorMode, editor.subMode
              )
            )
          }
        }.apply { isRepeats = false }
      }
      try {
        timer?.restart()
      } catch (exc: Exception) {
        thisLogger().error(exc)
      }
    } else {
      timer?.stop()
      timer = null
      // continue with the default behavior of IdeaVim
    }
  }

  override fun beforeExecute(editor: Editor, char: Char, context: DataContext, plan: ActionPlan) {
    vimTypedActionHandler.beforeExecute(editor, char, context, plan)
  }
}