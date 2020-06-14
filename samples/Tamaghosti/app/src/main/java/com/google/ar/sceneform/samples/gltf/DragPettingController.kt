package com.google.ar.sceneform.samples.gltf

import android.util.Log
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.BaseTransformationController
import com.google.ar.sceneform.ux.DragGesture
import com.google.ar.sceneform.ux.DragGestureRecognizer

class DragPettingController(private var dragon: Dragon, transformableNode: BaseTransformableNode, gestureRecognizer: DragGestureRecognizer) :
        BaseTransformationController<DragGesture>(transformableNode, gestureRecognizer) {

    private var animationStart: Boolean = false;


    public override fun canStartTransformation(gesture: DragGesture): Boolean {
        return transformableNode.isSelected
    }


    public override fun onContinueTransformation(gesture: DragGesture) {
        if (!animationStart) {
            dragon.updateAnimation(dragon.getPet_index);
            animationStart = true
            Log.d("Petting", "Petting started")
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