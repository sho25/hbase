begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_comment
comment|/**  * The InjectionHandler is an object provided to a class,  * which can perform custom actions for JUnit testing.  * JUnit test can implement custom version of the handler.  * For example, let's say we want to supervise FSImage object:  *  *<code>  * // JUnit test code  * class MyInjectionHandler extends InjectionHandler {  *   protected void _processEvent(InjectionEvent event,  *       Object... args) {  *     if (event == InjectionEvent.MY_EVENT) {  *       LOG.info("Handling my event for fsImage: "  *         + args[0].toString());  *     }  *   }  * }  *  * public void testMyEvent() {  *   InjectionHandler ih = new MyInjectionHandler();  *   InjectionHandler.set(ih);  *   ...  *  *   InjectionHandler.clear();  * }  *  * // supervised code example  *  * class FSImage {  *  *   private doSomething() {  *     ...  *     if (condition1&& InjectionHandler.trueCondition(MY_EVENT1) {  *       ...  *     }  *     if (condition2 || condition3  *       || InjectionHandler.falseCondition(MY_EVENT1) {  *       ...  *     }  *     ...  *     InjectionHandler.processEvent(MY_EVENT2, this)  *     ...  *     try {  *       read();  *       InjectionHandler.processEventIO(MY_EVENT3, this, object);  *       // might throw an exception when testing  *     catch (IOEXception) {  *       LOG.info("Exception")  *     }  *     ...  *   }  *   ...  * }  *</code>  *  * Each unit test should use a unique event type.  * The types can be defined by adding them to  * InjectionEvent class.  *  * methods:  *  * // simulate actions  * void processEvent()  * // simulate exceptions  * void processEventIO() throws IOException  *  * // simulate conditions  * boolean trueCondition()  * boolean falseCondition()  *  * The class implementing InjectionHandler must  * override respective protected methods  * _processEvent()  * _processEventIO()  * _trueCondition()  * _falseCondition()  */
end_comment

begin_class
specifier|public
class|class
name|InjectionHandler
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|InjectionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// the only handler to which everyone reports
specifier|private
specifier|static
name|InjectionHandler
name|handler
init|=
operator|new
name|InjectionHandler
argument_list|()
decl_stmt|;
comment|// can not be instantiated outside, unless a testcase extends it
specifier|protected
name|InjectionHandler
parameter_list|()
block|{}
comment|// METHODS FOR PRODUCTION CODE
specifier|protected
name|void
name|_processEvent
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
comment|// by default do nothing
block|}
specifier|protected
name|void
name|_processEventIO
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
throws|throws
name|IOException
block|{
comment|// by default do nothing
block|}
specifier|protected
name|boolean
name|_trueCondition
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
literal|true
return|;
comment|// neutral in conjunction
block|}
specifier|protected
name|boolean
name|_falseCondition
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
literal|false
return|;
comment|// neutral in alternative
block|}
comment|////////////////////////////////////////////////////////////
comment|/**    * Set to the empty/production implementation.    */
specifier|public
specifier|static
name|void
name|clear
parameter_list|()
block|{
name|handler
operator|=
operator|new
name|InjectionHandler
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set custom implementation of the handler.    */
specifier|public
specifier|static
name|void
name|set
parameter_list|(
name|InjectionHandler
name|custom
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"WARNING: SETTING INJECTION HANDLER"
operator|+
literal|" - THIS SHOULD NOT BE USED IN PRODUCTION !!!"
argument_list|)
expr_stmt|;
name|handler
operator|=
name|custom
expr_stmt|;
block|}
comment|/*   * Static methods for reporting to the handler   */
specifier|public
specifier|static
name|void
name|processEvent
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
name|handler
operator|.
name|_processEvent
argument_list|(
name|event
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|processEventIO
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
throws|throws
name|IOException
block|{
name|handler
operator|.
name|_processEventIO
argument_list|(
name|event
argument_list|,
name|args
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|boolean
name|trueCondition
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
name|handler
operator|.
name|_trueCondition
argument_list|(
name|event
argument_list|,
name|args
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|boolean
name|falseCondition
parameter_list|(
name|InjectionEvent
name|event
parameter_list|,
name|Object
modifier|...
name|args
parameter_list|)
block|{
return|return
name|handler
operator|.
name|_falseCondition
argument_list|(
name|event
argument_list|,
name|args
argument_list|)
return|;
block|}
block|}
end_class

end_unit

