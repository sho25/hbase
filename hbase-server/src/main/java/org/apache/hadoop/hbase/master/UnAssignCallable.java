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
operator|.
name|master
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|Callable
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
name|hbase
operator|.
name|HRegionInfo
import|;
end_import

begin_comment
comment|/**  * A callable object that invokes the corresponding action that needs to be  * taken for unassignment of a region in transition. Implementing as future  * callable we are able to act on the timeout asynchronously.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|UnAssignCallable
implements|implements
name|Callable
argument_list|<
name|Object
argument_list|>
block|{
specifier|private
name|AssignmentManager
name|assignmentManager
decl_stmt|;
specifier|private
name|HRegionInfo
name|hri
decl_stmt|;
specifier|public
name|UnAssignCallable
parameter_list|(
name|AssignmentManager
name|assignmentManager
parameter_list|,
name|HRegionInfo
name|hri
parameter_list|)
block|{
name|this
operator|.
name|assignmentManager
operator|=
name|assignmentManager
expr_stmt|;
name|this
operator|.
name|hri
operator|=
name|hri
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|call
parameter_list|()
throws|throws
name|Exception
block|{
name|assignmentManager
operator|.
name|unassign
argument_list|(
name|hri
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

