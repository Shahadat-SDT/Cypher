package com.github.sdt.cypher.ui.fragments.calls

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.github.sdt.cypher.R
import com.github.sdt.cypher.data.model.DataDelete
import com.github.sdt.cypher.data.preference.DataSharePreference.statePersmissionPhotoShow
import com.github.sdt.cypher.ui.adapters.photoadapter.PhotoRecyclerAdapter
import com.github.sdt.cypher.ui.animation.OvershootInRightAnimator
import com.github.sdt.cypher.ui.fragments.base.BaseFragment
import com.github.sdt.cypher.ui.fragments.photo.InterfaceInteractorPhoto
import com.github.sdt.cypher.ui.fragments.photo.InterfaceViewPhoto
import com.github.sdt.cypher.ui.fragments.photo.PhotoFragment
import com.github.sdt.cypher.ui.widget.CustomRecyclerView
import com.github.sdt.cypher.ui.widget.toolbar.CustomToolbar
import com.github.sdt.cypher.utils.ConstFun
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.database.DataSnapshot
import com.pawegio.kandroid.e
import kotterknife.bindView
import javax.inject.Inject

class FragmentCallhistory : BaseFragment(R.layout.fragment_callhistory), InterfaceViewPhoto {

    companion object {
        const val TAG = "FragmentCallhistory"
    }

    private var dataList: MutableList<DataDelete> = mutableListOf()
    private val main: CoordinatorLayout by bindView(R.id.main_view)
    private val list: CustomRecyclerView by bindView(R.id.list)
    private val content: ConstraintLayout by bindView(R.id.content)
    private val appBar: AppBarLayout by bindView(R.id.app_bar)
    private val toolbar: CustomToolbar by bindView(R.id.toolbar)
    private var recyclerAdapter: PhotoRecyclerAdapter? = null

    @Inject
    lateinit var layoutM: LinearLayoutManager

    @Inject
    lateinit var interactor: InterfaceInteractorPhoto<InterfaceViewPhoto>


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setToolbar(
            toolbar,
            true,
            R.string.search_photos,
            R.id.nav_clear_photo,
            R.string.search_photos_date
        )
        ConstFun.contentGlobalLayout(content, appBar)
        list.setAppBar(appBar)
        if (getComponent() != null) {
            getComponent()!!.inject(this)
            interactor.onAttach(this)

        }
    }

    @SuppressLint("ClickableViewAccessibility")

    override fun setValueState(dataSnapshot: DataSnapshot) {
        toolbar.enableStatePermission = true
        try {
            if (dataSnapshot.exists()) toolbar.statePermission = dataSnapshot.value as Boolean
            else toolbar.statePermission = false
        } catch (t: Throwable) {
            e(PhotoFragment.TAG, t.message.toString())
        }
    }

    override fun setValuePermission(dataSnapshot: DataSnapshot) {
        try {
            if (dataSnapshot.exists()) {
                if (dataSnapshot.value as Boolean) {
                    activity!!.statePersmissionPhotoShow = true
                } else {
                    toolbar.showProgress = false
                    if (activity!!.statePersmissionPhotoShow)
                        showDialog(
                            SweetAlertDialog.ERROR_TYPE,
                            R.string.ops,
                            getString(R.string.message_dialog_permission_photo_disable),
                            android.R.string.ok
                        ) {
                            setConfirmClickListener {
                                context.statePersmissionPhotoShow = false
                                hideDialog()
                            }
                            show()
                        }
                }
            }
        } catch (t: Throwable) {
            e(PhotoFragment.TAG, t.message.toString())
        }
    }

    override fun setRecyclerAdapter(recyclerAdapter: PhotoRecyclerAdapter) {
        this.recyclerAdapter = recyclerAdapter
        layoutM.stackFromEnd = true
        layoutM.reverseLayout = true
        list.apply {
            itemAnimator = OvershootInRightAnimator()
            itemAnimator?.addDuration = 600
            itemAnimator?.removeDuration = 600
            layoutManager = layoutM
            adapter = recyclerAdapter
            recycledViewPool.clear()

        }

    }

    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            CustomToolbar.BUTTON_BACK -> interactor.setSearchQuery("")
            CustomToolbar.BUTTON_ACTION_DELETE -> interactor.onDeleteData(dataList)
            CustomToolbar.BUTTON_STATE -> showSnackbar(
                if (toolbar.statePermission) R.string.enable_photo else R.string.disable_photo,
                main
            )
            CustomToolbar.BUTTON_CHILD_USER -> changeChild(PhotoFragment.TAG)
            else -> super.onButtonClicked(buttonCode)
        }
    }

}