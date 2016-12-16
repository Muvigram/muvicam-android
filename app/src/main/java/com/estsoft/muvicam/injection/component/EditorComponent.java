package com.estsoft.muvicam.injection.component;

import dagger.Component;
import com.estsoft.muvicam.injection.module.EditorModule;
import com.estsoft.muvicam.injection.scope.EditorScope;

/**
 * Created by jaylim on 12/12/2016.
 */

@EditorScope
@Component(dependencies = ActivityComponent.class, modules = EditorModule.class)
public interface EditorComponent {

}
