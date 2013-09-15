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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Manages a singleton instance of the environment edge. This class shall  * implement static versions of the interface {@link EnvironmentEdge}, then  * defer to the delegate on invocation.  */
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
comment|/**    * Defers to the delegate and calls the    * {@link EnvironmentEdge#currentTimeMillis()} method.    *    * @return current time in millis according to the delegate.    */
specifier|public
specifier|static
name|long
name|currentTimeMillis
parameter_list|()
block|{
return|return
name|getDelegate
argument_list|()
operator|.
name|currentTimeMillis
argument_list|()
return|;
block|}
block|}
end_class

end_unit

