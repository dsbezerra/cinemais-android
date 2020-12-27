package com.diegobezerra.cinemaisapp.util

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import com.google.android.material.appbar.MaterialToolbar
import kotlin.reflect.KClass

fun Fragment.setupToolbarAsActionBar(
    rootView: View, @IdRes toolbarId: Int,
    action: ActionBar.() -> Unit
): MaterialToolbar {
    val toolbar = rootView.findViewById<MaterialToolbar>(toolbarId)
    (requireActivity() as AppCompatActivity).run {
        setSupportActionBar(toolbar)
        supportActionBar?.run {
            action()
        }
    }
    return toolbar
}

fun FragmentTransaction.switchToAdded(
    tag: String,
    fragments: List<Fragment>
): Fragment? {
    var target: Fragment? = null

    for (f in fragments) {
        if (f.tag == tag) {
            if (f.isHidden) {
                target = f
                show(f)
            }
        } else {
            if (f.isAdded) {
                hide(f)
            }
        }
    }

    return target
}

fun <T : AppCompatActivity> Fragment.safeRequireActivity(
    clazz: KClass<out T>,
    invoke: (activity: T) -> Unit
) {
    try {
        invoke(clazz.javaObjectType.cast(requireActivity())!!)
    } catch (e: Exception) {
        // No-op
    }
}

@MainThread
inline fun <reified VM : ViewModel> Fragment.parentFragmentViewModels(
    noinline factoryProducer: (() -> Factory)? = null
) = createViewModelLazy(VM::class, { requireParentFragment().viewModelStore },
    factoryProducer ?: { requireParentFragment().defaultViewModelProviderFactory })