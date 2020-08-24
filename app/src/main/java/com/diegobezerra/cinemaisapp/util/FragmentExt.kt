package com.diegobezerra.cinemaisapp.util

import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar

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