package com.estsoft.muvigram.injection.qualifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * A pair of qualifiers, whose pair is {@link ApplicationContext}, to specify where
 * the {@link android.content.Context} belongs.
 * <p>
 * Created by jaylim on 12/12/2016.
 */

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ActivityContext {
}
