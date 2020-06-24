package com.google.ar.sceneform.samples.gltf

import android.util.Log
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.BaseTransformationController
import com.google.ar.sceneform.ux.DragGesture
import com.google.ar.sceneform.ux.DragGestureRecognizer
import java.util.concurrent.TimeUnit

/**
 * Based on a Template for a DragController but changed a lot
 * https://stackoverflow.com/questions/56384267/rotate-node-with-gestures-or-touch-event-sceneform-ar-core/56579866#56579866
 * It was only available in Kotlin, but I liked to work with Kotlin and would have loved to do a lot more in Kotlin
 */
class DragPettingController(private var dragon: Dragon, transformableNode: BaseTransformableNode, gestureRecognizer: DragGestureRecognizer,private var control: Control) :
        BaseTransformationController<DragGesture>(transformableNode, gestureRecognizer) {

    private var animationStart: Boolean = false
    private var startTimeOfCurrentAnimation: Long = 0


    // This function returns a boolean, to show, that the dragon is selected. If true, then a drag-transformation starts
    public override fun canStartTransformation(gesture: DragGesture): Boolean {
        return transformableNode.isSelected
    }


    // While the Transformation of the dragging is running
    public override fun onContinueTransformation(gesture: DragGesture) {

        // Only when nothing else is running
        if (dragon.getPettingAllowed() && !dragon.moving) {

            // Start the Animation only ones
            if (!animationStart) {
                dragon.updateAnimation(dragon.getPet_index)
                animationStart = true
                startTimeOfCurrentAnimation = System.nanoTime()
                control.dragon.firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.PET)
            }

            // Every Second add some needs
            if ((System.nanoTime() - startTimeOfCurrentAnimation) / TimeUnit.SECONDS.toNanos(1).toDouble() > 1) {
                startTimeOfCurrentAnimation = System.nanoTime()
                control.setNeed("social",10)
                control.setNeed("hunger",-5)
                control.showPlus(500)
                control.updateRestrictions()
            }
        }

    }
    // Stop the animation and set the Dragon to Idle
    public override fun onEndTransformation(gesture: DragGesture) {

        if (animationStart) {
            dragon.updateAnimation(dragon.idle_index)
            animationStart = false
            control.dragon.firebaseManager.uploadAnimationState(FirebaseManager.AnimationState.IDLE)
        }
    }




}