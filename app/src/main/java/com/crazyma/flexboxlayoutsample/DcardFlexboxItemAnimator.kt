package com.crazyma.flexboxlayoutsample

import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.OvershootInterpolator

class DcardFlexboxItemAnimator: FlexboxItemAnimator() {

    override fun animateAddImpl(holder: RecyclerView.ViewHolder, fadeInIndex: Int) {
        val view = holder.itemView
        val delay = fadeInIndex * 80L
        addAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.alpha(1f).scaleX(1f).scaleY(1f).setInterpolator(OvershootInterpolator())
            .setDuration(addDuration).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchAddStarting(holder)
                }

                override fun onAnimationCancel(view: View) {
                    ViewCompat.animate(view).apply {
                        startDelay = 0
                        interpolator = null
                    }

                    view.alpha = 1f
                }

                override fun onAnimationEnd(view: View) {
                    ViewCompat.animate(view).apply {
                        startDelay = 0
                        interpolator = null
                    }
                    animation.setListener(null)
                    dispatchAddFinished(holder)
                    addAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            })
            .setStartDelay(delay)
            .start()

    }

    override fun animateRemoveImpl(holder: RecyclerView.ViewHolder) {
        val view = holder.itemView
        val animation = ViewCompat.animate(view)
        animation.setDuration(removeDuration)
            .alpha(0f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchRemoveStarting(holder)
                }

                override fun onAnimationEnd(view: View) {
                    animation.setListener(null)
                    view.alpha = 1f
                    dispatchRemoveFinished(holder)
                    removeAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }).start()
        removeAnimations.add(holder)
    }

    override fun animateMoveImpl(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0f)
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0f)
        }

        moveAnimations.add(holder)
        val animation = ViewCompat.animate(view)
        animation.setDuration(moveDuration).setListener(object : VpaListenerAdapter() {
            override fun onAnimationStart(view: View) {
                dispatchMoveStarting(holder)
            }

            override fun onAnimationCancel(view: View) {
                if (deltaX != 0) {
                    view.translationX = 0f
                }
                if (deltaY != 0) {
                    view.translationY = 0f
                }
            }

            override fun onAnimationEnd(view: View) {
                animation.setListener(null)
                dispatchMoveFinished(holder)
                moveAnimations.remove(holder)
                dispatchFinishedWhenDone()
            }
        }).start()
    }

    override fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            changeAnimations.add(changeInfo.oldHolder!!)
            val oldViewAnim = ViewCompat.animate(view).setDuration(
                changeDuration
            )
            oldViewAnim.translationX((changeInfo.toX - changeInfo.fromX).toFloat())
            oldViewAnim.translationY((changeInfo.toY - changeInfo.fromY).toFloat())
            oldViewAnim.alpha(0f).setListener(object : VpaListenerAdapter() {
                override fun onAnimationStart(view: View) {
                    dispatchChangeStarting(changeInfo.oldHolder, true)
                }

                override fun onAnimationEnd(view: View) {
                    oldViewAnim.setListener(null)
                    view.alpha = 1f
                    view.translationX = 0f
                    view.translationY = 0f
                    dispatchChangeFinished(changeInfo.oldHolder, true)
                    changeAnimations.remove(changeInfo.oldHolder!!)
                    dispatchFinishedWhenDone()
                }
            }).start()
        }
        if (newView != null) {
            changeAnimations.add(changeInfo.newHolder!!)
            val newViewAnimation = ViewCompat.animate(newView)
            newViewAnimation.translationX(0f).translationY(0f).setDuration(changeDuration).alpha(1f)
                .setListener(object : VpaListenerAdapter() {
                    override fun onAnimationStart(view: View) {
                        dispatchChangeStarting(changeInfo.newHolder, false)
                    }

                    override fun onAnimationEnd(view: View) {
                        newViewAnimation.setListener(null)
                        newView.alpha = 1f
                        newView.translationX = 0f
                        newView.translationY = 0f
                        dispatchChangeFinished(changeInfo.newHolder, false)
                        changeAnimations.remove(changeInfo.newHolder!!)
                        dispatchFinishedWhenDone()
                    }
                }).start()
        }
    }

    private open class VpaListenerAdapter : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {}
        override fun onAnimationEnd(view: View) {}
        override fun onAnimationCancel(view: View) {}
    }
}