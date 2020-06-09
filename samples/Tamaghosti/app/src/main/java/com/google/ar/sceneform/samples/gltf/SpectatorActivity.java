package com.google.ar.sceneform.samples.gltf;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.lang.ref.WeakReference;

public class SpectatorActivity extends AppCompatActivity {

    private ArFragment arFragment;

    private Renderable renderable;

    private Dragon dragon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectator);

        FirebaseManager firebaseManager = new FirebaseManager();

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.spectator_fragment);
        

        WeakReference<SpectatorActivity> weakActivity = new WeakReference<>(this);


        ModelRenderable.builder()
                .setSource(
                        this, R.raw.dino)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(
                        modelRenderable -> {
                            SpectatorActivity activity = weakActivity.get();
                            if (activity != null) {
                                activity.renderable = modelRenderable;
                            }
                        })
                .exceptionally(
                        throwable -> {

                            // showToast("while loading an error occurred.");

                            return null;
                        });

        Button resolve = findViewById(R.id.resolve);

        resolve.setOnClickListener(view -> {

            String anchorId = firebaseManager.getAnchorFromDatabase();

            if(anchorId == null) {

                Toast.makeText(getApplicationContext(), "Please wait a second!", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(getApplicationContext(), anchorId, Toast.LENGTH_SHORT).show();
                Anchor resolvedAnchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(anchorId);

                AnchorNode anchorNode = new AnchorNode(resolvedAnchor);
                anchorNode.setParent(arFragment.getArSceneView().getScene());

                // Place Resolved Anchor
                createDragonNode(anchorNode);

            }

        });

    }

    private void createDragonNode(AnchorNode anchorNode) {
        // Transformable makes it possible to scale and drag the model
        dragon = new Dragon(arFragment);

        // Deactivate Rotation and Translation
        dragon.getTranslationController().setEnabled(false);
        dragon.getRotationController().setEnabled(false);
        //     model.getScaleController().setEnabled(false);

        dragon.setParent(anchorNode);
        dragon.setRenderable(renderable);
        dragon.select();

        dragon.setDragonAnimations();

    }


}
