package com.v2x.thing.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.common.stdlib.system.StatusBarUtils
import com.gyf.immersionbar.ImmersionBar
import com.kaopiz.kprogresshud.KProgressHUD
import com.orhanobut.logger.Logger
import com.v2x.thing.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

/**
 * Created by xzw on 2018/3/14.
 */
open class BaseActivity : AppCompatActivity(), CoroutineScope {
    protected val context: Context = this
    val TAG = this.javaClass.simpleName
    private var stackSize = 0
    private var exitCtrl = ExitController()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d("$TAG onCreate")
//        fitDp()
//        ImmersionBar.with(this).init()
        //        fitDp(this);
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = currentFragment ?: return@addOnBackStackChangedListener
            val newStackSize = supportFragmentManager.backStackEntryCount
            fragment.onStackTop(newStackSize < stackSize)
            stackSize = newStackSize
        }
    }

    override fun onRestart() {
        super.onRestart()
        Logger.d("$TAG onRestart")
    }

    protected open fun shouldTryExitAgain(): Boolean {
        return false
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        //        setStatusBar();
    }

    protected fun setStatusBar() {
//        StatusBarUtils.setColor(this, getResources().getColor(R.color.colorPrimary), 0);
    }

    protected fun setStatusBar(color: Int) {
        StatusBarUtils.setColor(this, color, 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        fitDp()
    }

    override val coroutineContext: CoroutineContext
        get() = MainScope().coroutineContext

    inner class ExitController {
        private val TIME_GAP = 2500
        private var lastBackEventTime: Long = 0
        fun requestExit(): Boolean {
            val currentTime = System.currentTimeMillis()
            if (lastBackEventTime == 0L || currentTime <= lastBackEventTime || currentTime -
                lastBackEventTime >= TIME_GAP
            ) {
                lastBackEventTime = currentTime
                Toast.makeText(applicationContext, "再按一次退出程序", Toast.LENGTH_SHORT).show()
                return false
            }
            return try {
                true
            } finally {
                lastBackEventTime = 0
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    public override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause")
    }

    public override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
    }

    public override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }

    public override fun onDestroy() {
        super.onDestroy()
        this.cancel(null)
        Log.d(TAG, "onDestroy")
    }

    fun pushFragment(
        fragment: BaseFragment,
        animFrom: AnimDirection = AnimDirection.RIGHT_TO_LEFT
    ) {
        val tag = fragment.javaClass.name
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val count = fm.backStackEntryCount
        if (count >= 1) {
            if (animFrom == AnimDirection.LEFT_TO_RIGHT) {
                ft.setCustomAnimations(
                    R.anim.activity_open_enter_left_to_right,
                    R.anim.activity_open_exit_left_to_right,
                    R.anim.activity_close_enter_left_to_right,
                    R.anim.activity_close_exit_left_to_right
                )
            } else {
                ft.setCustomAnimations(
                    R.anim.activity_open_enter_right_to_left,
                    R.anim.activity_open_exit_right_to_left,
                    R.anim.activity_close_enter_left_to_right,
                    R.anim.activity_close_exit_left_to_right
                )
            }
        }
        ft.replace(R.id.rl_fragment_content, fragment, tag)
        ft.addToBackStack(tag)
        ft.commit()
    }

    protected fun showFragment(fragment: BaseFragment?) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.setCustomAnimations(
            R.anim.activity_open_enter_left_to_right,
            R.anim.activity_open_exit_left_to_right,
            R.anim.activity_close_enter_left_to_right,
            R.anim.activity_close_exit_left_to_right
        )
        ft.show(fragment!!).commit()
    }

    protected fun hideFragment(fragment: BaseFragment?) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.setCustomAnimations(
            R.anim.activity_open_enter_left_to_right,
            R.anim.activity_open_exit_left_to_right,
            R.anim.activity_close_enter_left_to_right,
            R.anim.activity_close_exit_left_to_right
        )
        ft.hide(fragment!!).commit()
    }

    fun addFragment(
        fragment: BaseFragment,
        addToBackStack: Boolean = true,
        animFrom: AnimDirection = AnimDirection.LEFT_TO_RIGHT
    ) {
        val tag = fragment.javaClass.name
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val count = fm.backStackEntryCount
        if (count >= 1) {
            if (animFrom == AnimDirection.LEFT_TO_RIGHT) {
                ft.setCustomAnimations(
                    R.anim.activity_open_enter_left_to_right,
                    R.anim.activity_open_exit_left_to_right,
                    R.anim.activity_close_enter_left_to_right,
                    R.anim.activity_close_exit_left_to_right
                )
            } else {
                ft.setCustomAnimations(
                    R.anim.activity_open_enter_right_to_left,
                    R.anim.activity_open_exit_right_to_left,
                    R.anim.activity_close_enter_left_to_right,
                    R.anim.activity_close_exit_left_to_right
                )
            }
        }
        var isFirstAdded = false
        var f = fm.findFragmentByTag(tag)
        if (f == null) {
            f = fragment
            ft.add(R.id.rl_fragment_content, fragment, tag)
            isFirstAdded = true
        }
        ft.show(f)
        val list = fm.fragments
        if (list != null) {
            for (i in list.indices) {
                if (list[i] != null && list[i] !== f) {
                    ft.hide(list[i]!!)
                }
            }
        }
        if (addToBackStack && isFirstAdded) {
            ft.addToBackStack(tag)
        }
        ft.commit()
    }

    fun popFragment(): Boolean {
        val fm = supportFragmentManager
        val entryCount = fm.backStackEntryCount
        val ft = fm.beginTransaction()
        var popSucceed = true
        if (entryCount <= 1) {
            onFragmentEmpty()
        } else {
            fm.popBackStack()
            ft.commit()
        }
        return popSucceed
    }

    fun onFragmentEmpty() {
        finish()
    }

    fun clearFragmentStack() {
        val fm = supportFragmentManager
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    val currentFragment: BaseFragment?
        get() {
            val fm = supportFragmentManager
            val f = fm.fragments.find { (it is BaseFragment) && !it.isHidden }
            return f as BaseFragment?
        }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK && shouldTryExitAgain()) {
            if (!handleHomeBack()) {
                if (exitCtrl.requestExit()) {
                    exit()
                }
            }
            true
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBack()
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val currentFragment = currentFragment
        return if (currentFragment != null && currentFragment.handleDispatchKeyEvent(event)) {
            true
        } else super.dispatchKeyEvent(
            event
        )
    }

    private fun handleHomeBack(): Boolean {
        val view = this.currentFocus
        if (view != null) {
            val inputManager =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
        return false
    }

    protected open fun handleBack() {
        val view = this.currentFocus
        if (view != null) {
            val inputManager =
                this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
        val currentFragment = currentFragment
        try {
            if (currentFragment != null) {
                if (!currentFragment.handleBack()) {
                    if (supportFragmentManager.backStackEntryCount > 1) {
                        popFragment()
                    }
                }
            } else {
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected open fun exit() {
        finish()
    }

    protected fun overrideQuiteTransition() {
        overridePendingTransition(0, 0)
    }

    override fun finish() {
        super.finish()
        //        overrideQuiteTransition();
    }

    fun setWindowAlpha(transparent: Boolean) {
        val window = window
        val wl = window.attributes
        if (transparent) {
            wl.alpha = 0.5f
        } else {
            wl.alpha = 1.0f
        }
        window.attributes = wl
    }

    fun hideSoftKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    fun forceSoftKeyBoardVisible(show: Boolean) {
        window.setSoftInputMode(if (show) WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE else WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
    }

    fun showSoftInput(view: View?) {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
            view,
            InputMethodManager.SHOW_IMPLICIT
        )
    }

    private var progress: KProgressHUD? = null
    fun showProgress(text: String = "", cancellable: Boolean = false) {
        if (progress == null) {
            progress = KProgressHUD.create(this)
        }
        progress?.run {
            setStyle(KProgressHUD.Style.SPIN_INDETERMINATE).setCancellable(cancellable)
            if (!text.isNullOrBlank()) {
                setLabel(text)
            }
            show()
        }
    }

    fun hideProgress() {
        progress?.dismiss()
    }

}

enum class AnimDirection {
    LEFT_TO_RIGHT, RIGHT_TO_LEFT
}
