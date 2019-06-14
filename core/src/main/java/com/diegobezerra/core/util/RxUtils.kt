package com.diegobezerra.core.util

import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class RxUtils {

    companion object {

        fun <T : Any> getSingle(s: Single<T>): Single<T> =
            s.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    }

}
