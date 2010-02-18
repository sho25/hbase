begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|wal
operator|.
name|replication
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
name|FileSystem
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
name|HRegionInfo
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
name|KeyValue
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
name|HConstants
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
name|replication
operator|.
name|ReplicationSource
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
name|HLog
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
name|HLogKey
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
name|LogRollListener
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * HLog specialized in replication. It replicates every entry from every  * user table at the moment.  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationHLog
extends|extends
name|HLog
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ReplicationHLog
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ReplicationSource
name|replicationSource
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|isReplicator
decl_stmt|;
comment|/**    * New constructor used for replication    * @param fs filesystem to use    * @param dir directory to store the wal    * @param conf conf ot use    * @param listener log listener to pass to super class    * @param replicationSource where to put the entries    * @throws IOException    */
specifier|public
name|ReplicationHLog
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|,
specifier|final
name|Path
name|oldLogDir
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|LogRollListener
name|listener
parameter_list|,
name|ReplicationSource
name|replicationSource
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|,
name|oldLogDir
argument_list|,
name|conf
argument_list|,
name|listener
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationSource
operator|=
name|replicationSource
expr_stmt|;
name|this
operator|.
name|isReplicator
operator|=
name|this
operator|.
name|replicationSource
operator|!=
literal|null
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|doWrite
parameter_list|(
name|HRegionInfo
name|info
parameter_list|,
name|HLogKey
name|logKey
parameter_list|,
name|KeyValue
name|logEdit
parameter_list|,
name|long
name|now
parameter_list|)
throws|throws
name|IOException
block|{
name|logKey
operator|.
name|setScope
argument_list|(
name|info
operator|.
name|getTableDesc
argument_list|()
operator|.
name|getFamily
argument_list|(
name|logEdit
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|.
name|getScope
argument_list|()
argument_list|)
expr_stmt|;
name|super
operator|.
name|doWrite
argument_list|(
name|info
argument_list|,
name|logKey
argument_list|,
name|logEdit
argument_list|,
name|now
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|isReplicator
operator|&&
operator|!
operator|(
name|info
operator|.
name|isMetaRegion
argument_list|()
operator|||
name|info
operator|.
name|isRootRegion
argument_list|()
operator|)
operator|&&
name|logKey
operator|.
name|getScope
argument_list|()
operator|==
name|HConstants
operator|.
name|REPLICATION_SCOPE_GLOBAL
condition|)
block|{
name|this
operator|.
name|replicationSource
operator|.
name|enqueueLog
argument_list|(
operator|new
name|Entry
argument_list|(
name|logKey
argument_list|,
name|logEdit
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|ReplicationSource
name|getReplicationSource
parameter_list|()
block|{
return|return
name|this
operator|.
name|replicationSource
return|;
block|}
block|}
end_class

end_unit

