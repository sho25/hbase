begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|FileStatus
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
name|regionserver
operator|.
name|FlushRequester
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
name|HRegion
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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|Progressable
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

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|UnsupportedEncodingException
import|;
end_import

begin_comment
comment|/**  * Specialized version of HRegion to handle replication. In particular,  * it replays all edits from the reconstruction log.  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationRegion
extends|extends
name|HRegion
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
name|ReplicationRegion
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|ReplicationSource
name|replicationSource
decl_stmt|;
specifier|public
name|ReplicationRegion
parameter_list|(
name|Path
name|basedir
parameter_list|,
name|HLog
name|log
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|,
name|FlushRequester
name|flushListener
parameter_list|,
name|ReplicationSource
name|repSource
parameter_list|)
block|{
name|super
argument_list|(
name|basedir
argument_list|,
name|log
argument_list|,
name|fs
argument_list|,
name|conf
argument_list|,
name|regionInfo
argument_list|,
name|flushListener
argument_list|)
expr_stmt|;
name|this
operator|.
name|replicationSource
operator|=
name|repSource
expr_stmt|;
block|}
specifier|protected
name|void
name|doReconstructionLog
parameter_list|(
specifier|final
name|Path
name|oldLogFile
parameter_list|,
specifier|final
name|long
name|minSeqId
parameter_list|,
specifier|final
name|long
name|maxSeqId
parameter_list|,
specifier|final
name|Progressable
name|reporter
parameter_list|)
throws|throws
name|UnsupportedEncodingException
throws|,
name|IOException
block|{
name|super
operator|.
name|doReconstructionLog
argument_list|(
name|oldLogFile
argument_list|,
name|minSeqId
argument_list|,
name|maxSeqId
argument_list|,
name|reporter
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|replicationSource
operator|==
literal|null
condition|)
block|{
return|return;
block|}
if|if
condition|(
name|oldLogFile
operator|==
literal|null
operator|||
operator|!
name|getFilesystem
argument_list|()
operator|.
name|exists
argument_list|(
name|oldLogFile
argument_list|)
condition|)
block|{
return|return;
block|}
name|FileStatus
index|[]
name|stats
init|=
name|getFilesystem
argument_list|()
operator|.
name|listStatus
argument_list|(
name|oldLogFile
argument_list|)
decl_stmt|;
if|if
condition|(
name|stats
operator|==
literal|null
operator|||
name|stats
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Passed reconstruction log "
operator|+
name|oldLogFile
operator|+
literal|" is zero-length"
argument_list|)
expr_stmt|;
block|}
name|HLog
operator|.
name|Reader
name|reader
init|=
name|HLog
operator|.
name|getReader
argument_list|(
name|getFilesystem
argument_list|()
argument_list|,
name|oldLogFile
argument_list|,
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|HLog
operator|.
name|Entry
name|entry
decl_stmt|;
while|while
condition|(
operator|(
name|entry
operator|=
name|reader
operator|.
name|next
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
name|HLogKey
name|key
init|=
name|entry
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|KeyValue
name|val
init|=
name|entry
operator|.
name|getEdit
argument_list|()
decl_stmt|;
if|if
condition|(
name|key
operator|.
name|getLogSeqNum
argument_list|()
operator|<
name|maxSeqId
condition|)
block|{
continue|continue;
block|}
comment|// Don't replicate catalog entries and meta information like
comment|// complete log flush.
if|if
condition|(
operator|!
operator|(
name|Bytes
operator|.
name|equals
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|,
name|ROOT_TABLE_NAME
argument_list|)
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|key
operator|.
name|getTablename
argument_list|()
argument_list|,
name|META_TABLE_NAME
argument_list|)
operator|)
operator|&&
operator|!
name|Bytes
operator|.
name|equals
argument_list|(
name|val
operator|.
name|getFamily
argument_list|()
argument_list|,
name|HLog
operator|.
name|METAFAMILY
argument_list|)
operator|&&
name|key
operator|.
name|getScope
argument_list|()
operator|==
name|REPLICATION_SCOPE_GLOBAL
condition|)
block|{
name|this
operator|.
name|replicationSource
operator|.
name|enqueueLog
argument_list|(
name|entry
argument_list|)
expr_stmt|;
block|}
block|}
block|}
finally|finally
block|{
name|reader
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

