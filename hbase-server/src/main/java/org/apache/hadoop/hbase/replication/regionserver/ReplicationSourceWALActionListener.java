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
name|fs
operator|.
name|Path
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
name|regionserver
operator|.
name|wal
operator|.
name|WALActionsListener
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
name|ReplicationUtils
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
name|hadoop
operator|.
name|hbase
operator|.
name|wal
operator|.
name|WALKey
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
name|WALKeyImpl
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
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * Used to receive new wals.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ReplicationSourceWALActionListener
implements|implements
name|WALActionsListener
block|{
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSourceManager
name|manager
decl_stmt|;
specifier|public
name|ReplicationSourceWALActionListener
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ReplicationSourceManager
name|manager
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|manager
operator|=
name|manager
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|preLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{
name|manager
operator|.
name|preLogRoll
argument_list|(
name|newPath
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postLogRoll
parameter_list|(
name|Path
name|oldPath
parameter_list|,
name|Path
name|newPath
parameter_list|)
throws|throws
name|IOException
block|{
name|manager
operator|.
name|postLogRoll
argument_list|(
name|newPath
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|visitLogEntryBeforeWrite
parameter_list|(
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|)
throws|throws
name|IOException
block|{
name|scopeWALEdits
argument_list|(
name|logKey
argument_list|,
name|logEdit
argument_list|,
name|conf
argument_list|)
expr_stmt|;
block|}
comment|/**    * Utility method used to set the correct scopes on each log key. Doesn't set a scope on keys from    * compaction WAL edits and if the scope is local.    * @param logKey Key that may get scoped according to its edits    * @param logEdit Edits used to lookup the scopes    */
annotation|@
name|VisibleForTesting
specifier|static
name|void
name|scopeWALEdits
parameter_list|(
name|WALKey
name|logKey
parameter_list|,
name|WALEdit
name|logEdit
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
comment|// For bulk load replication we need meta family to know the file we want to replicate.
if|if
condition|(
name|ReplicationUtils
operator|.
name|isReplicationForBulkLoadDataEnabled
argument_list|(
name|conf
argument_list|)
condition|)
block|{
return|return;
block|}
name|WALKeyImpl
name|keyImpl
init|=
operator|(
name|WALKeyImpl
operator|)
name|logKey
decl_stmt|;
comment|// For serial replication we need to count all the sequence ids even for markers, so here we
comment|// always need to retain the replication scopes to let the replication wal reader to know that
comment|// we need serial replication. The ScopeWALEntryFilter will help filtering out the cell for
comment|// WALEdit.METAFAMILY.
if|if
condition|(
name|keyImpl
operator|.
name|hasSerialReplicationScope
argument_list|()
condition|)
block|{
return|return;
block|}
comment|// For replay, or if all the cells are markers, do not need to store replication scope.
if|if
condition|(
name|logEdit
operator|.
name|isReplay
argument_list|()
operator|||
name|logEdit
operator|.
name|getCells
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|c
lambda|->
name|CellUtil
operator|.
name|matchingFamily
argument_list|(
name|c
argument_list|,
name|WALEdit
operator|.
name|METAFAMILY
argument_list|)
argument_list|)
condition|)
block|{
name|keyImpl
operator|.
name|clearReplicationScope
argument_list|()
block|;     }
block|}
block|}
end_class

end_unit

