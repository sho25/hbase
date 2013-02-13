begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|server
operator|.
name|errorhandling
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|server
operator|.
name|errorhandling
operator|.
name|ExceptionCheckable
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|server
operator|.
name|errorhandling
operator|.
name|FaultInjector
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|server
operator|.
name|errorhandling
operator|.
name|impl
operator|.
name|delegate
operator|.
name|DelegatingExceptionDispatcher
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Pair
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|jasper
operator|.
name|compiler
operator|.
name|ErrorDispatcher
import|;
end_import

begin_comment
comment|/**  * {@link ErrorDispatcher} that delegates calls for all methods, but wraps exception checking to  * allow the fault injectors to have a chance to inject a fault into the running process  * @param<D> {@link ExceptionOrchestrator} to wrap for fault checking  * @param<T> type of generic error listener that should be notified  * @param<E> exception to be thrown on checks of {@link #failOnError()}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
class|class
name|InjectingExceptionDispatcher
parameter_list|<
name|D
extends|extends
name|ExceptionDispatcher
parameter_list|<
name|T
parameter_list|,
name|E
parameter_list|>
parameter_list|,
name|T
parameter_list|,
name|E
extends|extends
name|Exception
parameter_list|>
extends|extends
name|DelegatingExceptionDispatcher
argument_list|<
name|D
argument_list|,
name|T
argument_list|,
name|E
argument_list|>
block|{
specifier|private
specifier|final
name|List
argument_list|<
name|FaultInjector
argument_list|<
name|E
argument_list|>
argument_list|>
name|faults
decl_stmt|;
comment|/**    * Wrap an exception handler with one that will inject faults on calls to {@link #checkForError()}    * .    * @param delegate base exception handler to wrap    * @param faults injectors to run each time there is a check for an error    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|InjectingExceptionDispatcher
parameter_list|(
name|D
name|delegate
parameter_list|,
name|List
argument_list|<
name|FaultInjector
argument_list|<
name|?
argument_list|>
argument_list|>
name|faults
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
comment|// since we don't know the type of fault injector, we need to convert it.
comment|// this is only used in tests, so throwing a class-cast here isn't too bad.
name|this
operator|.
name|faults
operator|=
operator|new
name|ArrayList
argument_list|<
name|FaultInjector
argument_list|<
name|E
argument_list|>
argument_list|>
argument_list|(
name|faults
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|FaultInjector
argument_list|<
name|?
argument_list|>
name|fault
range|:
name|faults
control|)
block|{
name|this
operator|.
name|faults
operator|.
name|add
argument_list|(
operator|(
name|FaultInjector
argument_list|<
name|E
argument_list|>
operator|)
name|fault
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|failOnError
parameter_list|()
throws|throws
name|E
block|{
comment|// first fail if there is already an error
name|delegate
operator|.
name|failOnError
argument_list|()
expr_stmt|;
comment|// then check for an error via the update mechanism
if|if
condition|(
name|this
operator|.
name|checkForError
argument_list|()
condition|)
name|delegate
operator|.
name|failOnError
argument_list|()
expr_stmt|;
block|}
comment|/**    * Use the injectors to possibly inject an error into the delegate. Should call    * {@link ExceptionCheckable#checkForError()} or {@link ExceptionCheckable#failOnError()} after calling    * this method on return of<tt>true</tt>.    * @return<tt>true</tt> if an error found via injector or in the delegate,<tt>false</tt>    *         otherwise    */
annotation|@
name|Override
specifier|public
name|boolean
name|checkForError
parameter_list|()
block|{
comment|// if there are fault injectors, run them
if|if
condition|(
name|faults
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
comment|// get the caller of this method. Should be the direct calling class
name|StackTraceElement
index|[]
name|trace
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getStackTrace
argument_list|()
decl_stmt|;
for|for
control|(
name|FaultInjector
argument_list|<
name|E
argument_list|>
name|injector
range|:
name|faults
control|)
block|{
name|Pair
argument_list|<
name|E
argument_list|,
name|Object
index|[]
argument_list|>
name|info
init|=
name|injector
operator|.
name|injectFault
argument_list|(
name|trace
argument_list|)
decl_stmt|;
if|if
condition|(
name|info
operator|!=
literal|null
condition|)
block|{
name|delegate
operator|.
name|receiveError
argument_list|(
literal|"Injected fail"
argument_list|,
name|info
operator|.
name|getFirst
argument_list|()
argument_list|,
name|info
operator|.
name|getSecond
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|delegate
operator|.
name|checkForError
argument_list|()
return|;
block|}
block|}
end_class

end_unit

