package me.alexpetrakov.morty.common.presentation.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.CoroutineScope

val Fragment.viewLifecycle: Lifecycle
    get() = viewLifecycleOwner.lifecycle

val Fragment.viewLifecycleScope: CoroutineScope
    get() = viewLifecycle.coroutineScope