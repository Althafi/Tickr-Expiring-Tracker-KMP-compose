package com.project.tickr.di

import com.project.tickr.data.di.dataModule
import com.project.tickr.domain.di.domainModule
import com.project.tickr.presentation.di.presentationModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(appDeclaration: KoinApplication.() -> Unit = {}) = startKoin {
    appDeclaration()
    modules(dataModule, domainModule, presentationModule)
}
