package com.lts360.libs.ui

import android.content.Context
import android.widget.Toast


fun LongToast(context:Context, message:String){
    Toast.makeText(context, message, Toast.LENGTH_LONG)
        .show()
}


fun ShortToast(context: Context, message:String){

    Toast.makeText(context, message, Toast.LENGTH_SHORT)
        .show()
}

