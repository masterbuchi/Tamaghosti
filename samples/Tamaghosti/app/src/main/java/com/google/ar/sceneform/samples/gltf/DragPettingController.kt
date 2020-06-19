package com.google.ar.sceneform.samples.gltf

import android.util.Log
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.BaseTransformationController
import com.google.ar.sceneform.ux.DragGesture
import com.google.ar.sceneform.ux.DragGestureRecognizer
import java.util.concurrent.TimeUnit

class DragPettingController(private var dragon: Dragon, transformableNode: BaseTransformableNode, gestureRecognizer: DragGestureRecognizer,private var control: Control) :
        BaseTransformationController<DragGesture>(transformableNode, gestureRecognizer) {

    private var animationStart: Boolean = false

    private var startTimeOfCurrentAnimation: Long = 0


    public override fun canStartTransformation(gesture: DragGesture): Boolean {
        return transformableNode.isSelected
    }


    public override fun onContinueTransformation(gesture: DragGesture) {

        if (dragon.getPettingAllowed()) {

            if (!animationStart) {
                dragon.updateAnimation(dragon.getPet_index)
                animationStart = true
                startTimeOfCurrentAnimation = System.nanoTime()
            }

            if ((System.nanoTime() - startTimeOfCurrentAnimation) / TimeUnit.SECONDS.toNanos(1).toDouble() > 1) {
                startTimeOfCurrentAnimation = System.nanoTime();
                control.setNeed("social",10)
                control.setNeed("hunger",-5)
                control.showPlus(500)
                control.updateRestrictions()
            }
        }

    }

    public override fun onEndTransformation(gesture: DragGesture) {

        if (animationStart) {
            dragon.updateAnimation(dragon.idle_index)
            animationStart = false

            Log.d("Petting", "Petting finished")
        }
    }




}