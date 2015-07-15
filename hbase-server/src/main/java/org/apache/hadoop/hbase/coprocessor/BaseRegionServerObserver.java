begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|coprocessor
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
name|protobuf
operator|.
name|generated
operator|.
name|AdminProtos
operator|.
name|WALEntry
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
name|regionserver
operator|.
name|Region
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

begin_comment
comment|/**  * An abstract class that implements RegionServerObserver.  * By extending it, you can create your own region server observer without  * overriding all abstract methods of RegionServerObserver.  */
end_comment

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
name|BaseRegionServerObserver
implements|implements
name|RegionServerObserver
block|{
annotation|@
name|Override
specifier|public
name|void
name|preStopRegionServer
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|env
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preMerge
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|Region
name|regionA
parameter_list|,
name|Region
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postMerge
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|c
parameter_list|,
name|Region
name|regionA
parameter_list|,
name|Region
name|regionB
parameter_list|,
name|Region
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preMergeCommit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|Region
name|regionA
parameter_list|,
name|Region
name|regionB
parameter_list|,
name|List
argument_list|<
name|Mutation
argument_list|>
name|metaEntries
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postMergeCommit
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|Region
name|regionA
parameter_list|,
name|Region
name|regionB
parameter_list|,
name|Region
name|mergedRegion
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preRollBackMerge
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|Region
name|regionA
parameter_list|,
name|Region
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postRollBackMerge
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|Region
name|regionA
parameter_list|,
name|Region
name|regionB
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|preRollWALWriterRequest
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postRollWALWriterRequest
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|ReplicationEndpoint
name|postCreateReplicationEndPoint
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|ReplicationEndpoint
name|endpoint
parameter_list|)
block|{
return|return
name|endpoint
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preReplicateLogEntries
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{ }
annotation|@
name|Override
specifier|public
name|void
name|postReplicateLogEntries
parameter_list|(
name|ObserverContext
argument_list|<
name|RegionServerCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|WALEntry
argument_list|>
name|entries
parameter_list|,
name|CellScanner
name|cells
parameter_list|)
throws|throws
name|IOException
block|{ }
block|}
end_class

end_unit

