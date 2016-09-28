/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package demo.server;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.common.client.api.IsElement;
import org.livespark.flow.api.Tuple2;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.AppFlowDescriptor;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.StepDescriptor;
import org.livespark.flow.api.descriptor.display.DisplayerDescriptor;
import org.livespark.flow.api.descriptor.display.UIComponentDescriptor;
import org.livespark.flow.api.descriptor.display.UIStepDescriptor;
import org.livespark.flow.api.descriptor.function.FeedbackDescriptor;
import org.livespark.flow.api.descriptor.function.TransformationDescriptor;

import demo.client.AppFlowDescriptorInfo;
import demo.client.DescriptorService;

import org.livespark.flow.api.descriptor.type.TypeFactory;
import org.livespark.flow.api.descriptor.type.Type.ParameterizedType;

@Service
@ApplicationScoped
public class DescriptorServiceImpl implements DescriptorService {

    @Inject
    private DescriptorFactory descriptorFactory;

    @Inject
    private TypeFactory typeFactory;

    @Override
    public AppFlowDescriptorInfo getAppFlowDescriptors() {
        final StepDescriptor confirmStep = descriptorFactory.createStepDescriptor( "Confirm",
                                                                             typeFactory.simpleType( Unit.class ),
                                                                             typeFactory.simpleType( Boolean.class ) );
        final FeedbackDescriptor whileFalse = descriptorFactory.createFeedbackDescriptor( "WhileFalse",
                                                                                   typeFactory.simpleType( Object.class ),
                                                                                   typeFactory.simpleType( Boolean.class ) );
        final AppFlowDescriptor confirmLoop = descriptorFactory
            .createAppFlowDescriptor( confirmStep )
            .loop( whileFalse );

        final StepDescriptor customConfirmStep = descriptorFactory.createStepDescriptor( "Confirm",
                                                                                         typeFactory.simpleType( String.class ),
                                                                                         typeFactory.simpleType( Boolean.class ) );
        final DisplayerDescriptor displayer = descriptorFactory.createDisplayerDescriptor( "DefaultCDIDisplayer",
                                                                                           typeFactory.simpleType( IsElement.class ) );
        final UIComponentDescriptor singleTextInput = descriptorFactory.createUIComponentDescriptor( "SingleTextInput",
                                                                                                     typeFactory.simpleType( String.class ),
                                                                                                     typeFactory.simpleType( String.class ),
                                                                                                     typeFactory.simpleType( IsElement.class ) );


        final UIStepDescriptor singleTextInputStep = descriptorFactory.createUIStepDescriptor( displayer,
                                                                                               UIStepDescriptor.Action.SHOW,
                                                                                               singleTextInput );
        final UIStepDescriptor singleTextInputHideStep = descriptorFactory.createUIStepDescriptor( displayer,
                                                                                               UIStepDescriptor.Action.HIDE,
                                                                                               singleTextInput );

        final TransformationDescriptor emptyString = descriptorFactory.createTransformationDescriptor( "EmptyString",
                                                                                           typeFactory.simpleType( Object.class ),
                                                                                           typeFactory.simpleType( String.class ) );

        final AppFlowDescriptor inputLoopFlow = descriptorFactory
                .createAppFlowDescriptor( emptyString )
                .andThen( descriptorFactory
                              .createAppFlowDescriptor( singleTextInputStep )
                              .andThen( customConfirmStep )
                              .loop( whileFalse ) )
                .andThen( singleTextInputHideStep );

        final ParameterizedType stringTuple = typeFactory.parameterizedType( typeFactory.genericType( Tuple2.class,
                                                                                                      "V1",
                                                                                                      "V2" ),
                                                                             typeFactory.simpleType( String.class ),
                                                                             typeFactory.simpleType( String.class ) );
        final StepDescriptor loginService = descriptorFactory.createStepDescriptor( "LoginService",
                                                                                              stringTuple,
                                                                                              typeFactory.simpleType( Boolean.class ) );
        final UIComponentDescriptor loginComponent = descriptorFactory.createUIComponentDescriptor( "LoginComponent",
                                                                                                    typeFactory.simpleType( Unit.class ),
                                                                                                    stringTuple,
                                                                                                    typeFactory.simpleType( IsElement.class ) );
        final UIStepDescriptor showLogin = descriptorFactory.createUIStepDescriptor( displayer, UIStepDescriptor.Action.SHOW, loginComponent );
        final UIStepDescriptor hideLogin = descriptorFactory.createUIStepDescriptor( displayer, UIStepDescriptor.Action.HIDE, loginComponent );

        final AppFlowDescriptor loginFlow = descriptorFactory.createAppFlowDescriptor( showLogin ).andThen( loginService ).loop( whileFalse ).andThen( hideLogin );

        final Map<String, AppFlowDescriptor> map = new HashMap<>();
        map.put( "TestConfirmLoop", confirmLoop );
        map.put( "InputLoop", inputLoopFlow );
        map.put( "LoginFlow", loginFlow );

        return new AppFlowDescriptorInfo( map );
    }

}
