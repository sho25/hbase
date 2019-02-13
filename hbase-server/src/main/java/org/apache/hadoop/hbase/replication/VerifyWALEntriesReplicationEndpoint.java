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
name|replication
package|;
end_package

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
name|CellUtil
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

begin_comment
comment|/**  * A dummy {@link ReplicationEndpoint} that replicates nothing.  *<p/>  * Mainly used by ITBLL to check whether all the entries in WAL files are fine, since for normal  * case, we will only read the WAL files when there are region servers crash and we need to split  * the log, but for replication we will read all the entries and pass them to the  * {@link ReplicationEndpoint}, so setting up a replication peer can help finding out whether there  * are broken entries in WAL files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|VerifyWALEntriesReplicationEndpoint
extends|extends
name|BaseReplicationEndpoint
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|canReplicateToSameCluster
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|UUID
name|getPeerUUID
parameter_list|()
block|{
return|return
name|ctx
operator|.
name|getClusterId
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
literal|null
return|;
block|}
specifier|private
name|void
name|checkCell
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
comment|// check whether all the fields are fine
name|CellUtil
operator|.
name|cloneRow
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|CellUtil
operator|.
name|cloneFamily
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|CellUtil
operator|.
name|cloneQualifier
argument_list|(
name|cell
argument_list|)
expr_stmt|;
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|cell
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
name|replicateContext
operator|.
name|entries
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|WAL
operator|.
name|Entry
operator|::
name|getEdit
argument_list|)
operator|.
name|flatMap
argument_list|(
name|e
lambda|->
name|e
operator|.
name|getCells
argument_list|()
operator|.
name|stream
argument_list|()
argument_list|)
operator|.
name|forEach
argument_list|(
name|this
operator|::
name|checkCell
argument_list|)
expr_stmt|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|startAsync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|()
block|{
name|stopAsync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStart
parameter_list|()
block|{
name|notifyStarted
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doStop
parameter_list|()
block|{
name|notifyStopped
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
