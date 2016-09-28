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


package demo.client.local.builtin;

import static org.jboss.errai.common.client.dom.DOMUtil.removeFromParent;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.livespark.flow.api.Displayer;
import org.livespark.flow.api.Step;
import org.livespark.flow.api.Tuple2;
import org.livespark.flow.api.UIComponent;
import org.livespark.flow.api.Unit;
import org.livespark.flow.api.descriptor.DescriptorFactory;
import org.livespark.flow.api.descriptor.conversion.DescriptorRegistry;
import org.livespark.flow.api.descriptor.type.TypeFactory;
import org.livespark.flow.api.descriptor.type.Type.ParameterizedType;

import com.google.gwt.user.client.Window;

import demo.client.LoginService;

@EntryPoint
public class BuiltinFlowPartRegistrar {

    @Inject
    private DescriptorRegistry descriptorRegistry;

    @Inject
    private DescriptorFactory descriptorFactory;

    @Inject
    private TypeFactory typeFactory;

    @Inject
    private Event<IsElement> displayerEvent;

    @Inject
    private ManagedInstance<SingleTextInputComponent> singleInputComponentProvider;

    @Inject
    private ManagedInstance<LoginComponent> loginComponentProvider;

    @Inject
    private Caller<LoginService> loginService;

    @PostConstruct
    public void registerFlowParts() {
        descriptorRegistry.addStep( descriptorFactory.createStepDescriptor( "Confirm",
                                                                            typeFactory.simpleType( Unit.class ),
                                                                            typeFactory.simpleType( Boolean.class ) ),
                                    () -> new Step<Unit, Boolean>() {

                                        @Override
                                        public void execute( final Unit input,
                                                             final Consumer<Boolean> callback ) {
                                            callback.accept( Window.confirm( "Confirm?" ) );
                                        }

                                        @Override
                                        public String getName() {
                                            return "TestConfirm";
                                        }
                                    } );
        descriptorRegistry.addFeedback( descriptorFactory.createFeedbackDescriptor( "WhileTrue",
                                                                                    typeFactory.simpleType( Object.class ),
                                                                                    typeFactory.simpleType( Boolean.class ) ),
                                        () -> ( final Object input,
                                                final Boolean output ) -> (output ? Optional.of( input ) : Optional.empty() ) );
        descriptorRegistry.addFeedback( descriptorFactory.createFeedbackDescriptor( "WhileFalse",
                                                                                    typeFactory.simpleType( Object.class ),
                                                                                    typeFactory.simpleType( Boolean.class ) ),
                                        () -> ( final Object input,
                                                final Boolean output ) -> (output ? Optional.empty() : Optional.of( input ) ) );
        descriptorRegistry.addDisplayer( descriptorFactory.createDisplayerDescriptor( "DefaultCDIDisplayer",
                                                                                      typeFactory.simpleType( IsElement.class ) ),
                                         () -> new Displayer<IsElement>() {

                                            @Override
                                            public void show( final UIComponent<?, ?, IsElement> uiComponent ) {
                                                displayerEvent.fire( uiComponent.asComponent() );
                                            }

                                            @Override
                                            public void hide( final UIComponent<?, ?, IsElement> uiComponent ) {
                                                removeFromParent( uiComponent.asComponent().getElement() );
                                            }
                                        } );
        descriptorRegistry.addUIComponent( descriptorFactory.createUIComponentDescriptor( "SingleTextInput",
                                                                                          typeFactory.simpleType( String.class ),
                                                                                          typeFactory.simpleType( String.class ),
                                                                                          typeFactory.simpleType( IsElement.class ) ),
                                           () -> new UIComponent<String, String, IsElement>() {

                                               private final SingleTextInputComponent component = singleInputComponentProvider.get();

                                            @Override
                                            public void start( final String input,
                                                               final Consumer<String> callback ) {
                                                component.setInputValue( input );
                                                component.setSubmitCallback( callback );
                                            }

                                            @Override
                                            public void onHide() {
                                                component.setSubmitCallback( null );
                                                singleInputComponentProvider.destroy( component );
                                            }

                                            @Override
                                            public IsElement asComponent() {
                                                return component;
                                            }

                                            @Override
                                            public String getName() {
                                                return SingleTextInputComponent.class.getSimpleName();
                                            }
                                        } );
        descriptorRegistry.addStep( descriptorFactory.createStepDescriptor( "Confirm",
                                                                            typeFactory.simpleType( String.class ),
                                                                            typeFactory.simpleType( Boolean.class ) ),
                                    () -> new Step<String, Boolean>() {

                                        @Override
                                        public void execute( final String input,
                                                             final Consumer<Boolean> callback ) {
                                            callback.accept( Window.confirm( input ) );
                                        }

                                        @Override
                                        public String getName() {
                                            return "Confirm";
                                        }
                                    } );
        /*
         * This is a workaround. Currently all flows from descriptors must have Unit input,
         * but eventually we will want to be able to include @Portable values as inputs to flows.
         */
        descriptorRegistry.addTransformation( descriptorFactory.createTransformationDescriptor( "EmptyString",
                                                                                                typeFactory.simpleType( Object.class ),
                                                                                                typeFactory.simpleType( String.class ) ),
                                              () -> (final Object o) -> "" );
        descriptorRegistry.addTransformation( descriptorFactory.createTransformationDescriptor( "ToUnit",
                                                                                                typeFactory.simpleType( Object.class ),
                                                                                                typeFactory.simpleType( Unit.class ) ),
                                              () -> (final Object o) -> Unit.INSTANCE );
        final ParameterizedType stringTuple = typeFactory.parameterizedType( typeFactory.genericType( Tuple2.class,
                                                                                                      "V1",
                                                                                                      "V2" ),
                                                                             typeFactory.simpleType( String.class ),
                                                                             typeFactory.simpleType( String.class ) );
        descriptorRegistry.addUIComponent( descriptorFactory.createUIComponentDescriptor( "LoginComponent",
                                                                                          typeFactory.simpleType( Unit.class ),
                                                                                          stringTuple,
                                                                                          typeFactory.simpleType( IsElement.class ) ),
                                           () -> new UIComponent<Unit, Tuple2<String, String>, IsElement>() {

                                               private final LoginComponent component = loginComponentProvider.get();

                                            @Override
                                            public void start( final Unit input,
                                                               final Consumer<Tuple2<String, String>> callback ) {
                                                   component.setCallback( ( username,
                                                                            password ) -> callback.accept( new Tuple2<>( username, password ) ) );
                                               }

                                            @Override
                                            public void onHide() {
                                                component.setCallback( null );
                                                loginComponentProvider.destroy( component );
                                            }

                                            @Override
                                            public IsElement asComponent() {
                                                return component;
                                            }

                                            @Override
                                            public String getName() {
                                                return LoginComponent.class.getSimpleName();
                                            }
                                        } );
        descriptorRegistry.addStep( descriptorFactory.createStepDescriptor( "LoginService",
                                                                            stringTuple,
                                                                            typeFactory.simpleType( Boolean.class ) ),
                                    () -> new Step<Tuple2<String, String>, Boolean>() {

                                        @Override
                                        public void execute( final Tuple2<String, String> input,
                                                             final Consumer<Boolean> callback ) {
                                            loginService.call( ( final Boolean b ) -> callback.accept( b ) ).login( input.getOne(),
                                                                                                                    input.getTwo() );
                                        }

                                        @Override
                                        public String getName() {
                                            return "LoginService";
                                        }
                                    } );
    }

}
