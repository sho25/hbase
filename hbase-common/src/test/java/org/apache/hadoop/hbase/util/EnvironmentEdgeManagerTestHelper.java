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

begin_comment
comment|/**  * Used by tests to inject an edge into the manager. The intent is to minimise  * the use of the injectEdge method giving it default permissions, but in  * testing we may need to use this functionality elsewhere.  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|EnvironmentEdgeManagerTestHelper
block|{
specifier|private
name|EnvironmentEdgeManagerTestHelper
parameter_list|()
block|{   }
specifier|public
specifier|static
name|void
name|reset
parameter_list|()
block|{
name|EnvironmentEdgeManager
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|injectEdge
parameter_list|(
name|EnvironmentEdge
name|edge
parameter_list|)
block|{
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|edge
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

