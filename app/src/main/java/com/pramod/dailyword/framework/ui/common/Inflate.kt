package com.pramod.dailyword.framework.ui.common

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

typealias SetContent<T> = (Activity) -> T