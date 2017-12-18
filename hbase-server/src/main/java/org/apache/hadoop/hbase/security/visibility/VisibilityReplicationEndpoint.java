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
name|security
operator|.
name|visibility
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
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeoutException
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
name|ArrayBackedTag
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
name|Cell
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
name|PrivateCellUtil
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
name|Tag
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
name|TagType
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|WALEntryFilter
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
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
name|wal
operator|.
name|WALEdit
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VisibilityReplicationEndpoint
implements|implements
name|ReplicationEndpoint
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|VisibilityReplicationEndpoint
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReplicationEndpoint
name|delegator
decl_stmt|;
specifier|private
specifier|final
name|VisibilityLabelService
name|visibilityLabelsService
decl_stmt|;
specifier|public
name|VisibilityReplicationEndpoint
parameter_list|(
name|ReplicationEndpoint
name|endpoint
parameter_list|,
name|VisibilityLabelService
name|visibilityLabelsService
parameter_list|)
block|{
name|this
operator|.
name|delegator
operator|=
name|endpoint
expr_stmt|;
name|this
operator|.
name|visibilityLabelsService
operator|=
name|visibilityLabelsService
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|delegator
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|peerConfigUpdated
parameter_list|(
name|ReplicationPeerConfig
name|rpc
parameter_list|)
block|{
name|delegator
operator|.
name|peerConfigUpdated
argument_list|(
name|rpc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|replicate
parameter_list|(
name|ReplicateContext
name|replicateContext
parameter_list|)
block|{
if|if
condition|(
operator|!
name|delegator
operator|.
name|canReplicateToSameCluster
argument_list|()
condition|)
block|{
comment|// Only when the replication is inter cluster replication we need to
comment|// convert the visibility tags to
comment|// string based tags. But for intra cluster replication like region
comment|// replicas it is not needed.
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
init|=
name|replicateContext
operator|.
name|getEntries
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|visTags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|nonVisTags
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Entry
argument_list|>
name|newEntries
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|entries
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|Entry
name|entry
range|:
name|entries
control|)
block|{
name|WALEdit
name|newEdit
init|=
operator|new
name|WALEdit
argument_list|()
decl_stmt|;
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|cells
init|=
name|entry
operator|.
name|getEdit
argument_list|()
operator|.
name|getCells
argument_list|()
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|cells
control|)
block|{
if|if
condition|(
name|cell
operator|.
name|getTagsLength
argument_list|()
operator|>
literal|0
condition|)
block|{
name|visTags
operator|.
name|clear
argument_list|()
expr_stmt|;
name|nonVisTags
operator|.
name|clear
argument_list|()
expr_stmt|;
name|Byte
name|serializationFormat
init|=
name|VisibilityUtils
operator|.
name|extractAndPartitionTags
argument_list|(
name|cell
argument_list|,
name|visTags
argument_list|,
name|nonVisTags
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|visTags
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
try|try
block|{
name|byte
index|[]
name|modifiedVisExpression
init|=
name|visibilityLabelsService
operator|.
name|encodeVisibilityForReplication
argument_list|(
name|visTags
argument_list|,
name|serializationFormat
argument_list|)
decl_stmt|;
if|if
condition|(
name|modifiedVisExpression
operator|!=
literal|null
condition|)
block|{
name|nonVisTags
operator|.
name|add
argument_list|(
operator|new
name|ArrayBackedTag
argument_list|(
name|TagType
operator|.
name|STRING_VIS_TAG_TYPE
argument_list|,
name|modifiedVisExpression
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|ioe
parameter_list|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Exception while reading the visibility labels from the cell. The replication "
operator|+
literal|"would happen as per the existing format and not as "
operator|+
literal|"string type for the cell "
operator|+
name|cell
operator|+
literal|"."
argument_list|,
name|ioe
argument_list|)
expr_stmt|;
comment|// just return the old entries as it is without applying the string type change
name|newEdit
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// Recreate the cell with the new tags and the existing tags
name|Cell
name|newCell
init|=
name|PrivateCellUtil
operator|.
name|createCell
argument_list|(
name|cell
argument_list|,
name|nonVisTags
argument_list|)
decl_stmt|;
name|newEdit
operator|.
name|add
argument_list|(
name|newCell
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newEdit
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|newEdit
operator|.
name|add
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
block|}
name|newEntries
operator|.
name|add
argument_list|(
operator|new
name|Entry
argument_list|(
operator|(
name|entry
operator|.
name|getKey
argument_list|()
operator|)
argument_list|,
name|newEdit
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|replicateContext
operator|.
name|setEntries
argument_list|(
name|newEntries
argument_list|)
expr_stmt|;
return|return
name|delegator
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|delegator
operator|.
name|replicate
argument_list|(
name|replicateContext
argument_list|)
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|UUID
name|getPeerUUID
parameter_list|()
block|{
return|return
name|delegator
operator|.
name|getPeerUUID
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|canReplicateToSameCluster
parameter_list|()
block|{
return|return
name|delegator
operator|.
name|canReplicateToSameCluster
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|WALEntryFilter
name|getWALEntryfilter
parameter_list|()
block|{
return|return
name|delegator
operator|.
name|getWALEntryfilter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isRunning
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegator
operator|.
name|isRunning
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStarting
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegator
operator|.
name|isStarting
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|this
operator|.
name|delegator
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|awaitRunning
parameter_list|()
block|{
name|this
operator|.
name|delegator
operator|.
name|awaitRunning
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|awaitRunning
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|TimeoutException
block|{
name|this
operator|.
name|delegator
operator|.
name|awaitRunning
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|this
operator|.
name|delegator
operator|.
name|stop
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|awaitTerminated
parameter_list|()
block|{
name|this
operator|.
name|delegator
operator|.
name|awaitTerminated
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|awaitTerminated
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
throws|throws
name|TimeoutException
block|{
name|this
operator|.
name|delegator
operator|.
name|awaitTerminated
argument_list|(
name|timeout
argument_list|,
name|unit
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Throwable
name|failureCause
parameter_list|()
block|{
return|return
name|this
operator|.
name|delegator
operator|.
name|failureCause
argument_list|()
return|;
block|}
block|}
end_class

end_unit

