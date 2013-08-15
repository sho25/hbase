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
name|chaos
operator|.
name|policies
package|;
end_package

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
name|hbase
operator|.
name|IntegrationTestingUtility
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
name|chaos
operator|.
name|actions
operator|.
name|Action
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
name|StoppableImplementation
import|;
end_import

begin_comment
comment|/**  * A policy to introduce chaos to the cluster  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|Policy
extends|extends
name|StoppableImplementation
implements|implements
name|Runnable
block|{
specifier|protected
specifier|static
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|Policy
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|PolicyContext
name|context
decl_stmt|;
specifier|public
name|void
name|init
parameter_list|(
name|PolicyContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|this
operator|.
name|context
operator|=
name|context
expr_stmt|;
block|}
comment|/**    * A context for a Policy    */
specifier|public
specifier|static
class|class
name|PolicyContext
extends|extends
name|Action
operator|.
name|ActionContext
block|{
specifier|public
name|PolicyContext
parameter_list|(
name|IntegrationTestingUtility
name|util
parameter_list|)
block|{
name|super
argument_list|(
name|util
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

