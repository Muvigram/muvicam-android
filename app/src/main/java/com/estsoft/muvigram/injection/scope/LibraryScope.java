package com.estsoft.muvigram.injection.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by jaylim on 10/01/2017.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface LibraryScope {
}
