package com.v2x.thing.base

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

/**
 * Created by xzw on 2018/3/14.
 */
open class BaseFragment : Fragment() {
    protected val TAG = javaClass.simpleName
    fun onStackTop(isBack: Boolean) {}
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    val name: String
        get() = this.javaClass.name + this.hashCode()

    override fun onPause() {
        super.onPause()
        hideSoftKeyboard()
    }

    override fun onResume() {
        super.onResume()
    }

    protected fun hideSoftKeyboard() {
        val currentRoot = view ?: return
        activity?.apply {
            val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            mgr.hideSoftInputFromWindow(currentRoot.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    protected fun forceSoftKeyboardVisible(show: Boolean) {
        (activity as BaseActivity?)?.forceSoftKeyBoardVisible(show)
    }

    open fun handleBack(): Boolean {
        return (activity as BaseActivity?)?.popFragment() ?: false
    }

    fun pushFragment(fragment: BaseFragment?, animFrom: AnimDirection = AnimDirection.RIGHT_TO_LEFT) {
        (activity as BaseActivity?)?.pushFragment(fragment!!, animFrom)
    }

    fun addFragment(
        fragment: BaseFragment?,
        addToBackStack: Boolean = true,
        animFrom: AnimDirection = AnimDirection.LEFT_TO_RIGHT
    ) {
        (activity as BaseActivity?)?.addFragment(fragment!!, addToBackStack, animFrom)
    }

    fun onFragmentEmpty() {
        (activity as BaseActivity?)?.onFragmentEmpty()
    }

    protected fun clearFragmentStack() {
        (activity as BaseActivity?)?.clearFragmentStack()
    }

    protected fun finish() {
        activity?.finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun handleDispatchKeyEvent(event: KeyEvent?): Boolean {
        return false
    }

    fun showProgress(text: String = "", cancellable: Boolean = false) {
        (activity as BaseActivity?)?.showProgress(text, cancellable)
    }

    fun hideProgress() {
        (activity as BaseActivity?)?.hideProgress()
    }

    open fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return false
    }
}