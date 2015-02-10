begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|text
operator|.
name|MessageFormat
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_comment
comment|/**  * A class that provides a standard waitFor pattern  * See details at https://issues.apache.org/jira/browse/HBASE-7384  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|Waiter
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
name|Waiter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * System property name whose value is a scale factor to increase time out values dynamically used    * in {@link #sleep(Configuration, long)}, {@link #waitFor(Configuration, long, Predicate)},    * {@link #waitFor(Configuration, long, long, Predicate)}, and    * {@link #waitFor(Configuration, long, long, boolean, Predicate)} method    *<p/>    * The actual time out value will equal to hbase.test.wait.for.ratio * passed-in timeout    */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_TEST_WAIT_FOR_RATIO
init|=
literal|"hbase.test.wait.for.ratio"
decl_stmt|;
specifier|private
specifier|static
name|float
name|HBASE_WAIT_FOR_RATIO_DEFAULT
init|=
literal|1
decl_stmt|;
specifier|private
specifier|static
name|float
name|waitForRatio
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|Waiter
parameter_list|()
block|{   }
comment|/**    * Returns the 'wait for ratio' used in the {@link #sleep(Configuration, long)},    * {@link #waitFor(Configuration, long, Predicate)},    * {@link #waitFor(Configuration, long, long, Predicate)} and    * {@link #waitFor(Configuration, long, long, boolean, Predicate)} methods of the class    *<p/>    * This is useful to dynamically adjust max time out values when same test cases run in different    * test machine settings without recompiling& re-deploying code.    *<p/>    * The value is obtained from the Java System property or configuration setting    *<code>hbase.test.wait.for.ratio</code> which defaults to<code>1</code>.    * @param conf the configuration    * @return the 'wait for ratio' for the current test run.    */
specifier|public
specifier|static
name|float
name|getWaitForRatio
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
if|if
condition|(
name|waitForRatio
operator|<
literal|0
condition|)
block|{
comment|// System property takes precedence over configuration setting
if|if
condition|(
name|System
operator|.
name|getProperty
argument_list|(
name|HBASE_TEST_WAIT_FOR_RATIO
argument_list|)
operator|!=
literal|null
condition|)
block|{
name|waitForRatio
operator|=
name|Float
operator|.
name|parseFloat
argument_list|(
name|System
operator|.
name|getProperty
argument_list|(
name|HBASE_TEST_WAIT_FOR_RATIO
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|waitForRatio
operator|=
name|conf
operator|.
name|getFloat
argument_list|(
name|HBASE_TEST_WAIT_FOR_RATIO
argument_list|,
name|HBASE_WAIT_FOR_RATIO_DEFAULT
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|waitForRatio
return|;
block|}
comment|/**    * A predicate 'closure' used by the {@link Waiter#waitFor(Configuration, long, Predicate)} and    * {@link Waiter#waitFor(Configuration, long, Predicate)} and    * {@link Waiter#waitFor(Configuration, long, long, boolean, Predicate) methods.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|Predicate
parameter_list|<
name|E
extends|extends
name|Exception
parameter_list|>
block|{
comment|/**      * Perform a predicate evaluation.      * @return the boolean result of the evaluation.      * @throws Exception thrown if the predicate evaluation could not evaluate.      */
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|E
function_decl|;
block|}
comment|/**    * A mixin interface, can be used with {@link Waiter} to explain failed state.    */
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ExplainingPredicate
parameter_list|<
name|E
extends|extends
name|Exception
parameter_list|>
extends|extends
name|Predicate
argument_list|<
name|E
argument_list|>
block|{
comment|/**      * Perform a predicate evaluation.      *      * @return explanation of failed state      */
name|String
name|explainFailure
parameter_list|()
throws|throws
name|E
function_decl|;
block|}
comment|/**    * Makes the current thread sleep for the duration equal to the specified time in milliseconds    * multiplied by the {@link #getWaitForRatio(Configuration)}.    * @param conf the configuration    * @param time the number of milliseconds to sleep.    */
specifier|public
specifier|static
name|void
name|sleep
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|long
name|time
parameter_list|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
call|(
name|long
call|)
argument_list|(
name|getWaitForRatio
argument_list|(
name|conf
argument_list|)
operator|*
name|time
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Sleep interrupted, {0}"
argument_list|,
name|ex
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Waits up to the duration equal to the specified timeout multiplied by the    * {@link #getWaitForRatio(Configuration)} for the given {@link Predicate} to become    *<code>true</code>, failing the test if the timeout is reached and the Predicate is still    *<code>false</code>.    *<p/>    * @param conf the configuration    * @param timeout the timeout in milliseconds to wait for the predicate.    * @param predicate the predicate to evaluate.    * @return the effective wait, in milli-seconds until the predicate becomes<code>true</code> or    *         wait is interrupted otherwise<code>-1</code> when times out    */
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|Exception
parameter_list|>
name|long
name|waitFor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|long
name|timeout
parameter_list|,
name|Predicate
argument_list|<
name|E
argument_list|>
name|predicate
parameter_list|)
throws|throws
name|E
block|{
return|return
name|waitFor
argument_list|(
name|conf
argument_list|,
name|timeout
argument_list|,
literal|100
argument_list|,
literal|true
argument_list|,
name|predicate
argument_list|)
return|;
block|}
comment|/**    * Waits up to the duration equal to the specified timeout multiplied by the    * {@link #getWaitForRatio(Configuration)} for the given {@link Predicate} to become    *<code>true</code>, failing the test if the timeout is reached and the Predicate is still    *<code>false</code>.    *<p/>    * @param conf the configuration    * @param timeout the max timeout in milliseconds to wait for the predicate.    * @param interval the interval in milliseconds to evaluate predicate.    * @param predicate the predicate to evaluate.    * @return the effective wait, in milli-seconds until the predicate becomes<code>true</code> or    *         wait is interrupted otherwise<code>-1</code> when times out    */
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|Exception
parameter_list|>
name|long
name|waitFor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|long
name|timeout
parameter_list|,
name|long
name|interval
parameter_list|,
name|Predicate
argument_list|<
name|E
argument_list|>
name|predicate
parameter_list|)
throws|throws
name|E
block|{
return|return
name|waitFor
argument_list|(
name|conf
argument_list|,
name|timeout
argument_list|,
name|interval
argument_list|,
literal|true
argument_list|,
name|predicate
argument_list|)
return|;
block|}
comment|/**    * Waits up to the duration equal to the specified timeout multiplied by the    * {@link #getWaitForRatio(Configuration)} for the given {@link Predicate} to become    *<code>true</code>, failing the test if the timeout is reached, the Predicate is still    *<code>false</code> and failIfTimeout is set as<code>true</code>.    *<p/>    * @param conf the configuration    * @param timeout the timeout in milliseconds to wait for the predicate.    * @param interval the interval in milliseconds to evaluate predicate.    * @param failIfTimeout indicates if should fail current test case when times out.    * @param predicate the predicate to evaluate.    * @return the effective wait, in milli-seconds until the predicate becomes<code>true</code> or    *         wait is interrupted otherwise<code>-1</code> when times out    */
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|Exception
parameter_list|>
name|long
name|waitFor
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|long
name|timeout
parameter_list|,
name|long
name|interval
parameter_list|,
name|boolean
name|failIfTimeout
parameter_list|,
name|Predicate
argument_list|<
name|E
argument_list|>
name|predicate
parameter_list|)
throws|throws
name|E
block|{
name|long
name|started
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|long
name|adjustedTimeout
init|=
call|(
name|long
call|)
argument_list|(
name|getWaitForRatio
argument_list|(
name|conf
argument_list|)
operator|*
name|timeout
argument_list|)
decl_stmt|;
name|long
name|mustEnd
init|=
name|started
operator|+
name|adjustedTimeout
decl_stmt|;
name|long
name|remainderWait
init|=
literal|0
decl_stmt|;
name|long
name|sleepInterval
init|=
literal|0
decl_stmt|;
name|Boolean
name|eval
init|=
literal|false
decl_stmt|;
name|Boolean
name|interrupted
init|=
literal|false
decl_stmt|;
try|try
block|{
name|LOG
operator|.
name|info
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Waiting up to [{0}] milli-secs(wait.for.ratio=[{1}])"
argument_list|,
name|adjustedTimeout
argument_list|,
name|getWaitForRatio
argument_list|(
name|conf
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
operator|(
name|eval
operator|=
name|predicate
operator|.
name|evaluate
argument_list|()
operator|)
operator|&&
operator|(
name|remainderWait
operator|=
name|mustEnd
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|)
operator|>
literal|0
condition|)
block|{
try|try
block|{
comment|// handle tail case when remainder wait is less than one interval
name|sleepInterval
operator|=
operator|(
name|remainderWait
operator|>
name|interval
operator|)
condition|?
name|interval
else|:
name|remainderWait
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepInterval
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|eval
operator|=
name|predicate
operator|.
name|evaluate
argument_list|()
expr_stmt|;
name|interrupted
operator|=
literal|true
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
operator|!
name|eval
condition|)
block|{
if|if
condition|(
name|interrupted
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Waiting interrupted after [{0}] msec"
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|started
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|failIfTimeout
condition|)
block|{
name|String
name|msg
init|=
name|getExplanation
argument_list|(
name|predicate
argument_list|)
decl_stmt|;
name|fail
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Waiting timed out after [{0}] msec"
argument_list|,
name|adjustedTimeout
argument_list|)
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|String
name|msg
init|=
name|getExplanation
argument_list|(
name|predicate
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
name|MessageFormat
operator|.
name|format
argument_list|(
literal|"Waiting timed out after [{0}] msec"
argument_list|,
name|adjustedTimeout
argument_list|)
operator|+
name|msg
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|(
name|eval
operator|||
name|interrupted
operator|)
condition|?
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|started
operator|)
else|:
operator|-
literal|1
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|ex
argument_list|)
throw|;
block|}
block|}
specifier|public
specifier|static
name|String
name|getExplanation
parameter_list|(
name|Predicate
name|explain
parameter_list|)
block|{
if|if
condition|(
name|explain
operator|instanceof
name|ExplainingPredicate
condition|)
block|{
try|try
block|{
return|return
literal|" "
operator|+
operator|(
operator|(
name|ExplainingPredicate
operator|)
name|explain
operator|)
operator|.
name|explainFailure
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Failed to get explanation, "
argument_list|,
name|e
argument_list|)
expr_stmt|;
return|return
name|e
operator|.
name|getMessage
argument_list|()
return|;
block|}
block|}
else|else
block|{
return|return
literal|""
return|;
block|}
block|}
block|}
end_class

end_unit

