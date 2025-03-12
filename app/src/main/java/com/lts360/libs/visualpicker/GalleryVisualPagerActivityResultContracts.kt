package com.lts360.libs.visualpicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import com.lts360.components.getParcelableArrayListExtraCompat


class GalleryVisualPagerActivityResultContracts {

    class PickSingleVisual : ActivityResultContract<Unit, Uri?>() {

        companion object {
            const val EXTRA_DATA = "data"
        }

        override fun createIntent(context: Context, input: Unit): Intent {

            return Intent(context, GalleyVisualsPagerActivity::class.java)
                .apply {
                    putExtra("is_single", true)
                }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {

            return if (resultCode == Activity.RESULT_OK) {
                Uri.parse(intent?.getStringExtra(EXTRA_DATA))
            } else {
                null
            }

        }
    }

    class PickMultipleVisuals(private val maxItems: Int = Integer.MAX_VALUE) :
        ActivityResultContract<Unit, List<Uri>>() {

        init {
            require(maxItems > 1) { "Max items must be higher than 1" }
        }

        companion object {
            const val EXTRA_DATA = "data"
        }

        override fun createIntent(context: Context, input: Unit): Intent {
            return Intent(context, GalleyVisualsPagerActivity::class.java)
                .apply {
                    putExtra("is_single", false)
                    putExtra("max_items", maxItems)
                }
        }

        override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
            return if (resultCode == Activity.RESULT_OK) {
                // Get the list of URIs from the intent (assuming they're passed as a Serializable or Parcelable)
                intent?.getParcelableArrayListExtraCompat<Uri>(EXTRA_DATA)?.take(maxItems)
                    ?: emptyList()
            } else {
                emptyList()  // Return an empty list if the result is not OK
            }
        }
    }

}

