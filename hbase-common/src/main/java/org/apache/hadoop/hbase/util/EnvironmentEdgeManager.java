begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/**  * Manages a singleton instance of the environment edge. This class shall  * implement static versions of the interface {@link EnvironmentEdge}, then  * defer to the delegate on invocation.  *<br>  *<b>Original Motivation:</b>  * The main purpose of the Environment Edge Manager was to have better control  * over the tests so that they behave the same when run in any system.  * (Refer:<a href="https://issues.apache.org/jira/browse/HBASE-2578">HBASE-2578</a> - The issue  * which added the {@link org.apache.hadoop.hbase.util.EnvironmentEdgeManager}).  * The idea is to have a central place where time can be assigned in HBase. That makes  * it easier to inject different implementations of time. The default environment edge is the Java  * Current Time in millis. The environment edge manager class is designed to be able  * to plug in a new implementation of time by simply injecting an implementation  * of {@link org.apache.hadoop.hbase.util.EnvironmentEdge} interface to  * {@link org.apache.hadoop.hbase.util.EnvironmentEdgeManager}<p><b>Problems with Environment Edge:</b><br>  1. One of the major problems is the side effects of injecting an Environment Edge into     Environment Edge Manager.<br>     For example, A test could inject an edge to fast forward time in order to avoid thread     sleep to save time, but it could trigger a premature waking up of another thread waiting     on a condition dependent on time lapse, which could potentially affect the normal     working of the system leading to failure of tests.<br>  2. Every test should ensure it is setting the Environment Edge it needs for the test to     perform in an expected way. Because another test which might have run before the current test     could have injected its own custom Environment Edge which may not be applicable to this     test. This is still solvable but the problem is that the tests can run in parallel     leading to different combinations of environment edges being injected causing unexpected     results.<br>  3. Another important issue with respect to injecting time through Environment Edge is that     the milliseconds unit of time is ingrained throughout the codebase in the form of hardcoded     sleep time or timeouts that any change of time unit or making it fast or slow can potentially     trigger unexpected failures due to timeout or unintended flow of execution.<br></p>  Because of the above issues, only {@link org.apache.hadoop.hbase.util.DefaultEnvironmentEdge}  is being used, whose implementation of time returns the {@link System#currentTimeMillis()}. It  is advised not to inject any other {@link org.apache.hadoop.hbase.util.EnvironmentEdge}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|EnvironmentEdgeManager
block|{
specifier|private
specifier|static
specifier|volatile
name|EnvironmentEdge
name|delegate
init|=
operator|new
name|DefaultEnvironmentEdge
argument_list|()
decl_stmt|;
specifier|private
name|EnvironmentEdgeManager
parameter_list|()
block|{    }
comment|/**    * Retrieves the singleton instance of the {@link EnvironmentEdge} that is    * being managed.    *    * @return the edge.    */
specifier|public
specifier|static
name|EnvironmentEdge
name|getDelegate
parameter_list|()
block|{
return|return
name|delegate
return|;
block|}
comment|/**    * Resets the managed instance to the default instance: {@link    * DefaultEnvironmentEdge}.    */
specifier|public
specifier|static
name|void
name|reset
parameter_list|()
block|{
name|injectEdge
argument_list|(
operator|new
name|DefaultEnvironmentEdge
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Injects the given edge such that it becomes the managed entity. If null is    * passed to this method, the default type is assigned to the delegate.    *    * @param edge the new edge.    */
specifier|public
specifier|static
name|void
name|injectEdge
parameter_list|(
name|EnvironmentEdge
name|edge
parameter_list|)
block|{
if|if
condition|(
name|edge
operator|==
literal|null
condition|)
block|{
name|reset
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|delegate
operator|=
name|edge
expr_stmt|;
block|}
block|}
comment|/**    * Defers to the delegate and calls the    * {@link EnvironmentEdge#currentTime()} method.    *    * @return current time in millis according to the delegate.    */
specifier|public
specifier|static
name|long
name|currentTime
parameter_list|()
block|{
return|return
name|getDelegate
argument_list|()
operator|.
name|currentTime
argument_list|()
return|;
block|}
block|}
end_class

end_unit

