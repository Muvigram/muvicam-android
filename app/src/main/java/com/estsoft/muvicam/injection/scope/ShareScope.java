package com.estsoft.muvicam.injection.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by estsoft on 2017-02-03.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface ShareScope {
}
