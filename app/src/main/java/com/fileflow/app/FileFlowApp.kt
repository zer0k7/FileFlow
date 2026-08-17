package com.fileflow.app

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class FileFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
    }
}
