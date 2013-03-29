/**
 * Copyright (C) 2010-2013 eBusiness Information, Excilys Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.androidannotations.helper;

import com.sun.codemodel.*;
import org.androidannotations.processing.EBeanHolder;
import org.androidannotations.processing.OnSeekBarChangeListenerHolder;
import org.androidannotations.rclass.IRClass;
import org.androidannotations.helper.IdAnnotationHelper;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import static com.sun.codemodel.JExpr.cast;

public class OnSeekBarChangeListenerHelper extends IdAnnotationHelper {

    private final APTCodeModelHelper codeModelHelper;
    private final IdAnnotationHelper helper;
    private ThirdPartyLibHelper holoEverywhereHelper;

    public OnSeekBarChangeListenerHelper(//
                                         ProcessingEnvironment processingEnv, //
                                         String annotationName, //
                                         IRClass rClass, //
                                         APTCodeModelHelper codeModelHelper) {

        super(processingEnv, annotationName, rClass);

        this.codeModelHelper = codeModelHelper;
        helper = new IdAnnotationHelper(processingEnv, getTarget(), rClass);
        holoEverywhereHelper = new ThirdPartyLibHelper(helper);

    }

    public OnSeekBarChangeListenerHolder getOrCreateListener(JCodeModel codeModel, EBeanHolder holder, JFieldRef idRef) {

        String idRefString = codeModelHelper.getIdStringFromIdFieldRef(idRef);
        OnSeekBarChangeListenerHolder onSeekBarChangeListenerHolder = holder.onSeekBarChangeListeners.get(idRefString);

        if (onSeekBarChangeListenerHolder == null) {
            JClass seekBarClass;
            JDefinedClass onSeekbarChangeListenerClass;
            TypeMirror viewParameterType;
            if (holoEverywhereHelper.usesHoloEverywhere(holder))
            {
                seekBarClass = holder.classes().HOLO_EVERYWHERE_SEEKBAR;
                viewParameterType = typeElementFromQualifiedName(CanonicalNameConstants.HOLO_EVERYWHERE_SEEKBAR).asType();
                onSeekbarChangeListenerClass = codeModel.anonymousClass(holder.classes().HOLO_EVERYWHERE_ON_SEEKBAR_CHANGE_LISTENER);
            }
            else
            {
                seekBarClass = holder.classes().SEEKBAR;
                viewParameterType = typeElementFromQualifiedName(CanonicalNameConstants.SEEKBAR).asType();
                onSeekbarChangeListenerClass = codeModel.anonymousClass(holder.classes().ON_SEEKBAR_CHANGE_LISTENER);
            }



            JMethod onStartTrackingTouchMethod = onSeekbarChangeListenerClass.method(JMod.PUBLIC, codeModel.VOID, "onStartTrackingTouch");
            onStartTrackingTouchMethod.param(seekBarClass, "seekBar");
            onStartTrackingTouchMethod.annotate(Override.class);

            JMethod onProgressChangedMethod = onSeekbarChangeListenerClass.method(JMod.PUBLIC, codeModel.VOID, "onProgressChanged");
            onProgressChangedMethod.param(seekBarClass, "seekBar");
            onProgressChangedMethod.param(codeModel.INT, "progress");
            onProgressChangedMethod.param(codeModel.BOOLEAN, "fromUser");
            onProgressChangedMethod.annotate(Override.class);

            JMethod onStopTrackingTouchMethod = onSeekbarChangeListenerClass.method(JMod.PUBLIC, codeModel.VOID, "onStopTrackingTouch");
            onStopTrackingTouchMethod.param(seekBarClass, "seekBar");
            onStopTrackingTouchMethod.annotate(Override.class);

            JBlock block = holder.onViewChanged().body().block();

            String viewParameterTypeString = viewParameterType.toString();
            JClass viewClass = holder.refClass(viewParameterTypeString);

            JExpression findViewById = cast(viewClass, holder.onViewChanged().findViewById(idRef));

            JVar viewVariable = block.decl(JMod.FINAL, viewClass, "view", findViewById);
            block._if(viewVariable.ne(JExpr._null()))._then().invoke(viewVariable, "setOnSeekBarChangeListener").arg(JExpr._new(onSeekbarChangeListenerClass));

            onSeekBarChangeListenerHolder = new OnSeekBarChangeListenerHolder(//
                    onStartTrackingTouchMethod, //
                    onProgressChangedMethod, //
                    onStopTrackingTouchMethod, //
                    viewVariable);

            holder.onSeekBarChangeListeners.put(idRefString, onSeekBarChangeListenerHolder);
        }

        return onSeekBarChangeListenerHolder;
    }
}
