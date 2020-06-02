package com.google.ar.sceneform.samples.gltf

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.BaseTransformationController
import com.google.ar.sceneform.ux.DragGesture
import com.google.ar.sceneform.ux.DragGestureRecognizer

class DragPettingController(private var dragon: Dragon, transformableNode: BaseTransformableNode, gestureRecognizer: DragGestureRecognizer) :
        BaseTransformationController<DragGesture>(transformableNode, gestureRecognizer) {

    private var animationStart: Boolean = false;


    public override fun canStartTransformation(gesture: DragGesture): Boolean {
        if (!animationStart) {
            dragon.updateAnimation(dragon.getPet_index);
            animationStart = true
        }

        return transformableNode.isSelected
    }

    public override fun onContinueTransformation(gesture: DragGesture) {


       /* var localRotation = transformableNode.localRotation

        val rotationAmountX = gesture.delta.x * rotationRateDegrees
        val rotationDeltaX = Quaternion(Vector3.up(), rotationAmountX)
        localRotation = Quaternion.multiply(localRotation, rotationDeltaX)

        transformableNode.localRotation = localRotation*/
    }

    public override fun onEndTransformation(gesture: DragGesture) {
      dragon.updateAnimation(dragon.idle_index)
        animationStart = false
    }


}