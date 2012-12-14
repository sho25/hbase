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
name|regionserver
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
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|ObserverContext
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
name|coprocessor
operator|.
name|RegionServerCoprocessorEnvironment
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
name|coprocessor
operator|.
name|RegionServerObserver
import|;
end_import

begin_class
specifier|public
class|class
name|RegionServerCoprocessorHost
extends|extends
name|CoprocessorHost
argument_list|<
name|RegionServerCoprocessorHost
operator|.
name|RegionServerEnvironment
argument_list|>
block|{
specifier|private
name|RegionServerServices
name|rsServices
decl_stmt|;
specifier|public
name|RegionServerCoprocessorHost
parameter_list|(
name|RegionServerServices
name|rsServices
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|rsServices
operator|=
name|rsServices
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
comment|// load system default cp's from configuration.
name|loadSystemCoprocessors
argument_list|(
name|conf
argument_list|,
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerEnvironment
name|createEnvironment
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
name|Coprocessor
name|instance
parameter_list|,
name|int
name|priority
parameter_list|,
name|int
name|sequence
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|RegionServerEnvironment
argument_list|(
name|implClass
argument_list|,
name|instance
argument_list|,
name|priority
argument_list|,
name|sequence
argument_list|,
name|conf
argument_list|,
name|this
operator|.
name|rsServices
argument_list|)
return|;
block|}
specifier|public
name|void
name|preStop
parameter_list|(
name|String
name|message
parameter_list|)
throws|throws
name|IOException
block|{
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
init|=
literal|null
decl_stmt|;
for|for
control|(
name|RegionServerEnvironment
name|env
range|:
name|coprocessors
control|)
block|{
if|if
condition|(
name|env
operator|.
name|getInstance
argument_list|()
operator|instanceof
name|RegionServerObserver
condition|)
block|{
name|ctx
operator|=
name|ObserverContext
operator|.
name|createAndPrepare
argument_list|(
name|env
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
operator|(
operator|(
name|RegionServerObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
operator|)
operator|.
name|preStopRegionServer
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
if|if
condition|(
name|ctx
operator|.
name|shouldComplete
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
comment|/**    * Coprocessor environment extension providing access to region server    * related services.    */
specifier|static
class|class
name|RegionServerEnvironment
extends|extends
name|CoprocessorHost
operator|.
name|Environment
implements|implements
name|RegionServerCoprocessorEnvironment
block|{
specifier|private
name|RegionServerServices
name|regionServerServices
decl_stmt|;
specifier|public
name|RegionServerEnvironment
parameter_list|(
specifier|final
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|,
specifier|final
name|Coprocessor
name|impl
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
specifier|final
name|int
name|seq
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|RegionServerServices
name|services
parameter_list|)
block|{
name|super
argument_list|(
name|impl
argument_list|,
name|priority
argument_list|,
name|seq
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|regionServerServices
operator|=
name|services
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerServices
name|getRegionServerServices
parameter_list|()
block|{
return|return
name|regionServerServices
return|;
block|}
block|}
comment|/**    * Environment priority comparator. Coprocessors are chained in sorted    * order.    */
specifier|static
class|class
name|EnvironmentPriorityComparator
implements|implements
name|Comparator
argument_list|<
name|CoprocessorEnvironment
argument_list|>
block|{
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|CoprocessorEnvironment
name|env1
parameter_list|,
specifier|final
name|CoprocessorEnvironment
name|env2
parameter_list|)
block|{
if|if
condition|(
name|env1
operator|.
name|getPriority
argument_list|()
operator|<
name|env2
operator|.
name|getPriority
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|env1
operator|.
name|getPriority
argument_list|()
operator|>
name|env2
operator|.
name|getPriority
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
if|if
condition|(
name|env1
operator|.
name|getLoadSequence
argument_list|()
operator|<
name|env2
operator|.
name|getLoadSequence
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
elseif|else
if|if
condition|(
name|env1
operator|.
name|getLoadSequence
argument_list|()
operator|>
name|env2
operator|.
name|getLoadSequence
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
return|return
literal|0
return|;
block|}
block|}
block|}
end_class

end_unit

