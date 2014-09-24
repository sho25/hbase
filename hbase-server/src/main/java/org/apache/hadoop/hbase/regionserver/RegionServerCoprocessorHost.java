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
name|hbase
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
name|HBaseInterfaceAudience
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
name|MetaMutationAnnotation
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
name|client
operator|.
name|Mutation
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
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
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
name|super
argument_list|(
name|rsServices
argument_list|)
expr_stmt|;
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
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|preStopRegionServer
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postEnvCall
parameter_list|(
name|RegionServerEnvironment
name|env
parameter_list|)
block|{
comment|// invoke coprocessor stop method
name|shutdown
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|preMerge
parameter_list|(
specifier|final
name|HRegion
name|regionA
parameter_list|,
specifier|final
name|HRegion
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|preMerge
argument_list|(
name|ctx
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
name|void
name|postMerge
parameter_list|(
specifier|final
name|HRegion
name|regionA
parameter_list|,
specifier|final
name|HRegion
name|regionB
parameter_list|,
specifier|final
name|HRegion
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|postMerge
argument_list|(
name|ctx
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|,
name|mergedRegion
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|preMergeCommit
parameter_list|(
specifier|final
name|HRegion
name|regionA
parameter_list|,
specifier|final
name|HRegion
name|regionB
parameter_list|,
specifier|final
annotation|@
name|MetaMutationAnnotation
name|List
argument_list|<
name|Mutation
argument_list|>
name|metaEntries
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|preMergeCommit
argument_list|(
name|ctx
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|,
name|metaEntries
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
name|void
name|postMergeCommit
parameter_list|(
specifier|final
name|HRegion
name|regionA
parameter_list|,
specifier|final
name|HRegion
name|regionB
parameter_list|,
specifier|final
name|HRegion
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|postMergeCommit
argument_list|(
name|ctx
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|,
name|mergedRegion
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|preRollBackMerge
parameter_list|(
specifier|final
name|HRegion
name|regionA
parameter_list|,
specifier|final
name|HRegion
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|preRollBackMerge
argument_list|(
name|ctx
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postRollBackMerge
parameter_list|(
specifier|final
name|HRegion
name|regionA
parameter_list|,
specifier|final
name|HRegion
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|postRollBackMerge
argument_list|(
name|ctx
argument_list|,
name|regionA
argument_list|,
name|regionB
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|preRollWALWriterRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|preRollWALWriterRequest
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postRollWALWriterRequest
parameter_list|()
throws|throws
name|IOException
block|{
name|execOperation
argument_list|(
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessorOperation
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
name|oserver
operator|.
name|postRollWALWriterRequest
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|abstract
class|class
name|CoprocessorOperation
extends|extends
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
block|{
specifier|public
name|CoprocessorOperation
parameter_list|()
block|{     }
specifier|public
specifier|abstract
name|void
name|call
parameter_list|(
name|RegionServerObserver
name|oserver
parameter_list|,
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
function_decl|;
specifier|public
name|void
name|postEnvCall
parameter_list|(
name|RegionServerEnvironment
name|env
parameter_list|)
block|{     }
block|}
specifier|private
name|boolean
name|execOperation
parameter_list|(
specifier|final
name|CoprocessorOperation
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|ctx
operator|==
literal|null
condition|)
return|return
literal|false
return|;
name|boolean
name|bypass
init|=
literal|false
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
operator|.
name|prepare
argument_list|(
name|env
argument_list|)
expr_stmt|;
name|Thread
name|currentThread
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
decl_stmt|;
name|ClassLoader
name|cl
init|=
name|currentThread
operator|.
name|getContextClassLoader
argument_list|()
decl_stmt|;
try|try
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|env
operator|.
name|getClassLoader
argument_list|()
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|call
argument_list|(
operator|(
name|RegionServerObserver
operator|)
name|env
operator|.
name|getInstance
argument_list|()
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|handleCoprocessorThrowable
argument_list|(
name|env
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|currentThread
operator|.
name|setContextClassLoader
argument_list|(
name|cl
argument_list|)
expr_stmt|;
block|}
name|bypass
operator||=
name|ctx
operator|.
name|shouldBypass
argument_list|()
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
name|ctx
operator|.
name|postEnvCall
argument_list|(
name|env
argument_list|)
expr_stmt|;
block|}
return|return
name|bypass
return|;
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

