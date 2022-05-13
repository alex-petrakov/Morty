package me.alexpetrakov.morty.common.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.alexpetrakov.morty.common.domain.ResourceProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : ResourceProvider {

    private val resources = context.resources

    override fun getString(id: Int): String = resources.getString(id)
}