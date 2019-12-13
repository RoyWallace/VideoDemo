package com.mrhuang.demo.chapter3

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.mrhuang.demo.BaseActivity
import com.mrhuang.demo.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader

class VideoClipActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_clip)
        GlobalScope.launch {
            val command = arrayOf("getevent -l")
            val result = execCommand(command, false, true)
            Log.i("etong", result.successMsg)
            Log.i("etong", result.errorMsg)
        }
    }

    fun showWindow(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val para = WindowManager.LayoutParams()
        para.height = -1
        para.width = -1
        para.format = 1
        para.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        para.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        val mView = LayoutInflater.from(context).inflate(R.layout.activity_video_clip, null)
        wm.addView(mView, para)
    }

    /**
     * 运行结果
     *
     *  * [CommandResult.result] means result of command, 0 means normal,
     * else means error, same to excute in linux shell
     *  * [CommandResult.successMsg] means success message of command
     * result
     *  * [CommandResult.errorMsg] means error message of command result
     *
     *
     * @author [Trinea](http://www.trinea.cn)
     * 2013-5-16
     */
    class CommandResult {
        /**
         * 运行结果
         */
        var result: Int
        /**
         * 运行成功结果
         */
        var successMsg: String? = null
        /**
         * 运行失败结果
         */
        var errorMsg: String? = null

        constructor(result: Int) {
            this.result = result
        }

        constructor(result: Int, successMsg: String?, errorMsg: String?) {
            this.result = result
            this.successMsg = successMsg
            this.errorMsg = errorMsg
        }
    }

    companion object {
        const val COMMAND_SU = "su"
        const val COMMAND_SH = "sh"
        const val COMMAND_EXIT = "exit\n"
        const val COMMAND_LINE_END = "\n"
        fun execCommand(commands: Array<String>?, isRoot: Boolean,
                        isNeedResultMsg: Boolean): CommandResult {
            var result = -1
            if (commands == null || commands.size == 0) {
                return CommandResult(result, null, null)
            }
            var process: Process? = null
            var successResult: BufferedReader? = null
            var errorResult: BufferedReader? = null
            var successMsg: StringBuilder? = null
            var errorMsg: StringBuilder? = null
            var os: DataOutputStream? = null
            try {
                process = Runtime.getRuntime().exec(
                        if (isRoot) COMMAND_SU else COMMAND_SH)
                os = DataOutputStream(process.outputStream)
                for (command in commands) {
                    if (command == null) {
                        continue
                    }
                    // donnot use os.writeBytes(commmand), avoid chinese charset
// error
                    os.write(command.toByteArray())
                    os.writeBytes(COMMAND_LINE_END)
                    os.flush()
                }
                os.writeBytes(COMMAND_EXIT)
                os.flush()
                result = process.waitFor()
                // get command result
                if (isNeedResultMsg) {
                    successMsg = StringBuilder()
                    errorMsg = StringBuilder()
                    successResult = BufferedReader(InputStreamReader(
                            process.inputStream))
                    errorResult = BufferedReader(InputStreamReader(
                            process.errorStream))
                    var s: String?
                    while (successResult.readLine().also { s = it } != null) {
                        successMsg.append(s)
                    }
                    while (errorResult.readLine().also { s = it } != null) {
                        errorMsg.append(s)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    os?.close()
                    successResult?.close()
                    errorResult?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                process?.destroy()
            }
            return CommandResult(result, successMsg?.toString(), errorMsg?.toString())
        }
    }
}