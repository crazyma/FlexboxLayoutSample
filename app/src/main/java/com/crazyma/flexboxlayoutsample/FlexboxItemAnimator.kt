package com.crazyma.flexboxlayoutsample

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.view.View
import android.view.animation.OvershootInterpolator

//  Reference: https://android.googlesource.com/platform/frameworks/support/+/c110be5/v7/recyclerview/src/android/support/v7/widget/DefaultItemAnimator.java
abstract class FlexboxItemAnimator : SimpleItemAnimator() {

    private val pendingRemovals = ArrayList<RecyclerView.ViewHolder>()
    private val pendingAdditions = ArrayList<RecyclerView.ViewHolder>()
    private val pendingMoves = ArrayList<MoveInfo>()
    private val pendingChanges = ArrayList<ChangeInfo>()
    protected var addAnimations: ArrayList<RecyclerView.ViewHolder> = ArrayList()
    protected var moveAnimations: ArrayList<RecyclerView.ViewHolder> = ArrayList()
    protected var removeAnimations: ArrayList<RecyclerView.ViewHolder> = ArrayList()
    protected var changeAnimations: ArrayList<RecyclerView.ViewHolder> = ArrayList()
    private var additionsList: ArrayList<ArrayList<RecyclerView.ViewHolder>> = ArrayList()
    private var movesList: ArrayList<ArrayList<MoveInfo>> = ArrayList()
    private var changesList: ArrayList<ArrayList<ChangeInfo>> = ArrayList()

    abstract fun animateAddImpl(holder: RecyclerView.ViewHolder, fadeInIndex: Int)

    abstract fun animateRemoveImpl(holder: RecyclerView.ViewHolder)

    abstract fun animateMoveImpl(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int)

    abstract fun animateChangeImpl(changeInfo: ChangeInfo)

    override fun runPendingAnimations() {
        val removalsPending = !pendingRemovals.isEmpty()
        val movesPending = !pendingMoves.isEmpty()
        val changesPending = !pendingChanges.isEmpty()
        val additionsPending = !pendingAdditions.isEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }

        // First, remove stuff
        for (holder in pendingRemovals) {
            //  TODO: animate remove implement
            animateRemoveImpl(holder)
        }
        pendingRemovals.clear()

        // Next, move stuff
        if (movesPending) {
            val moves = ArrayList<MoveInfo>()
            moves.addAll(pendingMoves)
            movesList.add(moves)
            pendingMoves.clear()
            val mover = Runnable {
                for (moveInfo in moves) {
                    //  TODO: animate move implement
                    animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY)
                }
                moves.clear()
                movesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, removeDuration)
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList<ChangeInfo>()
            changes.addAll(pendingChanges)
            changesList.add(changes)
            pendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    //  TODO: animate change implement
                    animateChangeImpl(change)
                }
                changes.clear()
                changesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(holder!!.itemView, changer, removeDuration)
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList<RecyclerView.ViewHolder>()
            additions.addAll(pendingAdditions)
            additions.sortBy { it.adapterPosition }
            additionsList.add(additions)
            pendingAdditions.clear()
            val adder = Runnable {
                for ((index, holder) in additions.withIndex()) {
                    //  TODO: animate add implement
                    animateAddImpl(holder, index)
                }
                additions.clear()
                additionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = if (removalsPending) removeDuration else 0
                val moveDuration = if (movesPending) moveDuration else 0
                val changeDuration = if (changesPending) changeDuration else 0
                val totalDelay = removeDuration + Math.max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        this.resetAnimation(holder)
        holder.itemView.alpha = 0.0f
        holder.itemView.scaleX = 0.5f
        holder.itemView.scaleY = 0.5f
        pendingAdditions.add(holder)
        return true
    }

    override fun animateMove(holder: RecyclerView.ViewHolder, _fromX: Int, _fromY: Int, toX: Int, toY: Int): Boolean {
        val view = holder.itemView
        var fromX = _fromX
        var fromY = _fromY
        fromX += holder.itemView.translationX.toInt()
        fromY += holder.itemView.translationY.toInt()
        this.resetAnimation(holder)
        val deltaX = toX - fromX
        val deltaY = toY - fromY

        return if (deltaX == 0 && deltaY == 0) {
            this.dispatchMoveFinished(holder)
            false
        } else {
            if (deltaX != 0) {
                view.translationX = (-deltaX).toFloat()
            }

            if (deltaY != 0) {
                view.translationY = (-deltaY).toFloat()
            }

            pendingMoves.add(MoveInfo(holder, fromX, fromY, toX, toY))
            true
        }
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder,
        fromX: Int, fromY: Int, toX: Int, toY: Int
    ): Boolean {
        return if (oldHolder == newHolder) {
            this.animateMove(oldHolder, fromX, fromY, toX, toY)
        } else {
            val prevTranslationX = oldHolder.itemView.translationX
            val prevTranslationY = oldHolder.itemView.translationY
            val prevAlpha = oldHolder.itemView.alpha
            this.resetAnimation(oldHolder)
            val deltaX = ((toX - fromX).toFloat() - prevTranslationX).toInt()
            val deltaY = ((toY - fromY).toFloat() - prevTranslationY).toInt()
            oldHolder.itemView.translationX = prevTranslationX
            oldHolder.itemView.translationY = prevTranslationY
            oldHolder.itemView.alpha = prevAlpha

            this.resetAnimation(newHolder)
            newHolder.itemView.translationX = (-deltaX).toFloat()
            newHolder.itemView.translationY = (-deltaY).toFloat()
            newHolder.itemView.alpha = 0.0f


            this.pendingChanges.add(ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY))

            true
        }
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        this.resetAnimation(holder)
        this.pendingRemovals.add(holder)
        return true
    }

    override fun isRunning(): Boolean {
        return !pendingAdditions.isEmpty() || !pendingChanges.isEmpty() || !pendingMoves.isEmpty() ||
                !pendingRemovals.isEmpty() || !moveAnimations.isEmpty() || !removeAnimations.isEmpty() ||
                !addAnimations.isEmpty() || !changeAnimations.isEmpty() || !movesList.isEmpty() ||
                !additionsList.isEmpty() || !this.changesList.isEmpty()
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel()

        for (i in pendingMoves.size - 1 downTo 0) {
            val moveInfo = pendingMoves[i]
            if (moveInfo.holder == item) {
                view.translationX = 0.0f
                view.translationY = 0.0f
                dispatchMoveFinished(item)
                pendingMoves.removeAt(i)
            }
        }

        endChangeAnimation(pendingChanges, item)
        if (pendingRemovals.remove(item)) {
            view.alpha = 1f
            dispatchRemoveFinished(item)
        }
        if (pendingAdditions.remove(item)) {
            view.alpha = 1f
            dispatchAddFinished(item)
        }
        for (i in changesList.size - 1 downTo 0) {
            val changes = changesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                changesList.remove(changes)
            }
        }
        for (i in movesList.size - 1 downTo 0) {
            val moves = movesList[i]
            for (j in moves.size - 1 downTo 0) {
                val moveInfo = moves.get(j)
                if (moveInfo.holder === item) {
                    view.translationY = 0f
                    view.translationX = 0f
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        movesList.remove(moves)
                    }
                    break
                }
            }
        }
        for (i in additionsList.size - 1 downTo 0) {
            val additions = additionsList[i]
            if (additions.remove(item)) {
                view.alpha = 1f
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    additionsList.remove(additions)
                }
            }
        }
        // animations should be ended by the cancel above.
        if (removeAnimations.remove(item)) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mRemoveAnimations list")
        }
        if (addAnimations.remove(item)) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mAddAnimations list")
        }
        if (changeAnimations.remove(item)) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mChangeAnimations list")
        }
        if (moveAnimations.remove(item)) {
            throw IllegalStateException("after animation is cancelled, item should not be in " + "mMoveAnimations list")
        }
        dispatchFinishedWhenDone()
    }

    override fun endAnimations() {
        var count = pendingMoves.size
        for (i in count - 1 downTo 0) {
            val item = pendingMoves.get(i)
            val view = item.holder.itemView
            view.translationY = 0f
            view.translationX = 0f
            dispatchMoveFinished(item.holder)
            pendingMoves.removeAt(i)
        }
        count = pendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = pendingRemovals[i]
            dispatchRemoveFinished(item)
            pendingRemovals.removeAt(i)
        }
        count = pendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = pendingAdditions[i]
            val view = item.itemView
            view.alpha = 1f
            dispatchAddFinished(item)
            pendingAdditions.removeAt(i)
        }
        count = pendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(pendingChanges[i])
        }
        pendingChanges.clear()
        if (!isRunning) {
            return
        }
        var listCount = movesList.size
        for (i in listCount - 1 downTo 0) {
            val moves = movesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo = moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                view.translationY = 0f
                view.translationX = 0f
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    movesList.remove(moves)
                }
            }
        }
        listCount = additionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = additionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions.get(j)
                val view = item.itemView
                view.alpha = 1f
                dispatchAddFinished(item)
                additions.removeAt(j)
                if (additions.isEmpty()) {
                    additionsList.remove(additions)
                }
            }
        }
        listCount = changesList.size
        for (i in listCount - 1 downTo 0) {
            val changes = changesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    changesList.remove(changes)
                }
            }
        }
        cancelAll(removeAnimations)
        cancelAll(moveAnimations)
        cancelAll(addAnimations)
        cancelAll(changeAnimations)
        dispatchAnimationsFinished()
    }

    private fun cancelAll(viewHolders: List<RecyclerView.ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            ViewCompat.animate(viewHolders[i].itemView).cancel()
        }
    }

    protected fun dispatchFinishedWhenDone() {
        if (!this.isRunning) {
            this.dispatchAnimationsFinished()
        }
    }

    private fun resetAnimation(holder: RecyclerView.ViewHolder) {
        endAnimation(holder)
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: RecyclerView.ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (this.endChangeAnimationIfNecessary(
                    changeInfo,
                    item
                ) && changeInfo.oldHolder == null && changeInfo.newHolder == null
            ) {
                infoList.remove(changeInfo)
            }
        }

    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            this.endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder!!)
        }

        if (changeInfo.newHolder != null) {
            this.endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder!!)
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo, item: RecyclerView.ViewHolder): Boolean {
        var oldItem = false
        if (changeInfo.newHolder === item) {
            changeInfo.newHolder = null
        } else {
            if (changeInfo.oldHolder !== item) {
                return false
            }

            changeInfo.oldHolder = null
            oldItem = true
        }

        item.itemView.alpha = 1.0f
        item.itemView.translationX = 0.0f
        item.itemView.translationY = 0.0f
        dispatchChangeFinished(item, oldItem)
        return true
    }

    private class MoveInfo internal constructor(
        var holder: RecyclerView.ViewHolder,
        var fromX: Int,
        var fromY: Int,
        var toX: Int,
        var toY: Int
    )

    class ChangeInfo private constructor(
        var oldHolder: RecyclerView.ViewHolder?,
        var newHolder: RecyclerView.ViewHolder?
    ) {
        var fromX: Int = 0
        var fromY: Int = 0
        var toX: Int = 0
        var toY: Int = 0

        internal constructor(
            oldHolder: RecyclerView.ViewHolder,
            newHolder: RecyclerView.ViewHolder,
            fromX: Int,
            fromY: Int,
            toX: Int,
            toY: Int
        ) : this(oldHolder, newHolder) {
            this.fromX = fromX
            this.fromY = fromY
            this.toX = toX
            this.toY = toY
        }

        override fun toString(): String {
            return "ChangeInfo{oldHolder=" + this.oldHolder + ", newHolder=" + this.newHolder + ", fromX=" + this.fromX + ", fromY=" + this.fromY + ", toX=" + this.toX + ", toY=" + this.toY + '}'.toString()
        }
    }

//    private open class VpaListenerAdapter : ViewPropertyAnimatorListener {
//        override fun onAnimationStart(view: View) {}
//        override fun onAnimationEnd(view: View) {}
//        override fun onAnimationCancel(view: View) {}
//    }

}