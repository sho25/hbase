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
name|commons
operator|.
name|lang
operator|.
name|ClassUtils
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
name|CellScanner
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
name|MetricsCoprocessor
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
name|SingletonCoprocessorService
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
name|ipc
operator|.
name|RpcServer
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
name|metrics
operator|.
name|MetricRegistry
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
name|replication
operator|.
name|ReplicationEndpoint
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
name|security
operator|.
name|User
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
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
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionServerCoprocessorHost
operator|.
name|class
argument_list|)
decl_stmt|;
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
comment|// Log the state of coprocessor loading here; should appear only once or
comment|// twice in the daemon log, depending on HBase version, because there is
comment|// only one RegionServerCoprocessorHost instance in the RS process
name|boolean
name|coprocessorsEnabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
name|DEFAULT_COPROCESSORS_ENABLED
argument_list|)
decl_stmt|;
name|boolean
name|tableCoprocessorsEnabled
init|=
name|conf
operator|.
name|getBoolean
argument_list|(
name|USER_COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
name|DEFAULT_USER_COPROCESSORS_ENABLED
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"System coprocessor loading is "
operator|+
operator|(
name|coprocessorsEnabled
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Table coprocessor loading is "
operator|+
operator|(
operator|(
name|coprocessorsEnabled
operator|&&
name|tableCoprocessorsEnabled
operator|)
condition|?
literal|"enabled"
else|:
literal|"disabled"
operator|)
argument_list|)
expr_stmt|;
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
comment|// While stopping the region server all coprocessors method should be executed first then the
comment|// coprocessor should be cleaned up.
name|execShutdown
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
parameter_list|,
specifier|final
name|User
name|user
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
argument_list|(
name|user
argument_list|)
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
parameter_list|,
specifier|final
name|User
name|user
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
argument_list|(
name|user
argument_list|)
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
parameter_list|,
specifier|final
name|User
name|user
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
argument_list|(
name|user
argument_list|)
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
parameter_list|,
specifier|final
name|User
name|user
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
argument_list|(
name|user
argument_list|)
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
parameter_list|,
specifier|final
name|User
name|user
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
argument_list|(
name|user
argument_list|)
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
parameter_list|,
specifier|final
name|User
name|user
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
argument_list|(
name|user
argument_list|)
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
specifier|public
name|void
name|preReplicateLogEntries
parameter_list|(
specifier|final
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
specifier|final
name|CellScanner
name|cells
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
name|preReplicateLogEntries
argument_list|(
name|ctx
argument_list|,
name|entries
argument_list|,
name|cells
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|postReplicateLogEntries
parameter_list|(
specifier|final
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
specifier|final
name|CellScanner
name|cells
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
name|postReplicateLogEntries
argument_list|(
name|ctx
argument_list|,
name|entries
argument_list|,
name|cells
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ReplicationEndpoint
name|postCreateReplicationEndPoint
parameter_list|(
specifier|final
name|ReplicationEndpoint
name|endpoint
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|execOperationWithResult
argument_list|(
name|endpoint
argument_list|,
name|coprocessors
operator|.
name|isEmpty
argument_list|()
condition|?
literal|null
else|:
operator|new
name|CoprocessOperationWithResult
argument_list|<
name|ReplicationEndpoint
argument_list|>
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
name|setResult
argument_list|(
name|oserver
operator|.
name|postCreateReplicationEndPoint
argument_list|(
name|ctx
argument_list|,
name|getResult
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
return|;
block|}
specifier|public
name|void
name|preClearCompactionQueues
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
name|preClearCompactionQueues
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
name|postClearCompactionQueues
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
name|postClearCompactionQueues
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
parameter_list|<
name|T
parameter_list|>
name|T
name|execOperationWithResult
parameter_list|(
specifier|final
name|T
name|defaultValue
parameter_list|,
specifier|final
name|CoprocessOperationWithResult
argument_list|<
name|T
argument_list|>
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
name|defaultValue
return|;
name|ctx
operator|.
name|setResult
argument_list|(
name|defaultValue
argument_list|)
expr_stmt|;
name|execOperation
argument_list|(
name|ctx
argument_list|)
expr_stmt|;
return|return
name|ctx
operator|.
name|getResult
argument_list|()
return|;
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
block|{
name|this
argument_list|(
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|CoprocessorOperation
parameter_list|(
name|User
name|user
parameter_list|)
block|{
name|super
argument_list|(
name|user
argument_list|)
expr_stmt|;
block|}
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
specifier|static
specifier|abstract
class|class
name|CoprocessOperationWithResult
parameter_list|<
name|T
parameter_list|>
extends|extends
name|CoprocessorOperation
block|{
specifier|private
name|T
name|result
init|=
literal|null
decl_stmt|;
specifier|public
name|void
name|setResult
parameter_list|(
specifier|final
name|T
name|result
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
specifier|public
name|T
name|getResult
parameter_list|()
block|{
return|return
name|this
operator|.
name|result
return|;
block|}
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
name|List
argument_list|<
name|RegionServerEnvironment
argument_list|>
name|envs
init|=
name|coprocessors
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|envs
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|RegionServerEnvironment
name|env
init|=
name|envs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
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
comment|/**    * RegionServer coprocessor classes can be configured in any order, based on that priority is set    * and chained in a sorted order. For preStop(), coprocessor methods are invoked in call() and    * environment is shutdown in postEnvCall().<br>    * Need to execute all coprocessor methods first then postEnvCall(), otherwise some coprocessors    * may remain shutdown if any exception occurs during next coprocessor execution which prevent    * RegionServer stop. (Refer:    *<a href="https://issues.apache.org/jira/browse/HBASE-16663">HBASE-16663</a>    * @param ctx CoprocessorOperation    * @return true if bypaas coprocessor execution, false if not.    * @throws IOException    */
specifier|private
name|boolean
name|execShutdown
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
name|List
argument_list|<
name|RegionServerEnvironment
argument_list|>
name|envs
init|=
name|coprocessors
operator|.
name|get
argument_list|()
decl_stmt|;
name|int
name|envsSize
init|=
name|envs
operator|.
name|size
argument_list|()
decl_stmt|;
comment|// Iterate the coprocessors and execute CoprocessorOperation's call()
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|envsSize
condition|;
name|i
operator|++
control|)
block|{
name|RegionServerEnvironment
name|env
init|=
name|envs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
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
block|}
comment|// Iterate the coprocessors and execute CoprocessorOperation's postEnvCall()
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|envsSize
condition|;
name|i
operator|++
control|)
block|{
name|RegionServerEnvironment
name|env
init|=
name|envs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
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
specifier|final
name|RegionServerServices
name|regionServerServices
decl_stmt|;
specifier|private
specifier|final
name|MetricRegistry
name|metricRegistry
decl_stmt|;
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"BC_UNCONFIRMED_CAST"
argument_list|,
name|justification
operator|=
literal|"Intentional; FB has trouble detecting isAssignableFrom"
argument_list|)
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
for|for
control|(
name|Object
name|itf
range|:
name|ClassUtils
operator|.
name|getAllInterfaces
argument_list|(
name|implClass
argument_list|)
control|)
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
operator|(
name|Class
argument_list|<
name|?
argument_list|>
operator|)
name|itf
decl_stmt|;
if|if
condition|(
name|SingletonCoprocessorService
operator|.
name|class
operator|.
name|isAssignableFrom
argument_list|(
name|c
argument_list|)
condition|)
block|{
comment|// FindBugs: BC_UNCONFIRMED_CAST
name|this
operator|.
name|regionServerServices
operator|.
name|registerService
argument_list|(
operator|(
operator|(
name|SingletonCoprocessorService
operator|)
name|impl
operator|)
operator|.
name|getService
argument_list|()
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|this
operator|.
name|metricRegistry
operator|=
name|MetricsCoprocessor
operator|.
name|createRegistryForRSCoprocessor
argument_list|(
name|implClass
operator|.
name|getName
argument_list|()
argument_list|)
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
annotation|@
name|Override
specifier|public
name|MetricRegistry
name|getMetricRegistryForRegionServer
parameter_list|()
block|{
return|return
name|metricRegistry
return|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|shutdown
parameter_list|()
block|{
name|super
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|MetricsCoprocessor
operator|.
name|removeRegistry
argument_list|(
name|metricRegistry
argument_list|)
expr_stmt|;
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
annotation|@
name|Override
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

