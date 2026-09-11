package br.com.assistpro

import android.app.Application

class AssistProApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val anterior = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, erro ->
            LogApp.e("Falha nao tratada em ${thread.name}", erro)
            anterior?.uncaughtException(thread, erro)
        }
        LogApp.i("App iniciado (v${BuildConfig.VERSION_NAME})")
    }
}
