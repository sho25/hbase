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
name|wal
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|OptionalLong
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|CopyOnWriteArrayList
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
name|atomic
operator|.
name|AtomicBoolean
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
name|client
operator|.
name|RegionInfo
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
name|regionserver
operator|.
name|wal
operator|.
name|WALCoprocessorHost
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
name|FSUtils
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

begin_comment
comment|// imports for things that haven't moved from regionserver.wal yet.
end_comment

begin_comment
comment|/**  * No-op implementation of {@link WALProvider} used when the WAL is disabled.  *  * Should only be used when severe data loss is acceptable.  *  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|DisabledWALProvider
implements|implements
name|WALProvider
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
name|DisabledWALProvider
operator|.
name|class
argument_list|)
decl_stmt|;
name|WAL
name|disabled
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|WALFactory
name|factory
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|String
name|providerId
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
literal|null
operator|!=
name|disabled
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"WALProvider.init should only be called once."
argument_list|)
throw|;
block|}
if|if
condition|(
literal|null
operator|==
name|providerId
condition|)
block|{
name|providerId
operator|=
literal|"defaultDisabled"
expr_stmt|;
block|}
name|disabled
operator|=
operator|new
name|DisabledWAL
argument_list|(
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getWALRootDir
argument_list|(
name|conf
argument_list|)
argument_list|,
name|providerId
argument_list|)
argument_list|,
name|conf
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|WAL
argument_list|>
name|getWALs
parameter_list|()
block|{
name|List
argument_list|<
name|WAL
argument_list|>
name|wals
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|wals
operator|.
name|add
argument_list|(
name|disabled
argument_list|)
expr_stmt|;
return|return
name|wals
return|;
block|}
annotation|@
name|Override
specifier|public
name|WAL
name|getWAL
parameter_list|(
name|RegionInfo
name|region
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|disabled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|IOException
block|{
name|disabled
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
block|{
name|disabled
operator|.
name|shutdown
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|DisabledWAL
implements|implements
name|WAL
block|{
specifier|protected
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
init|=
operator|new
name|CopyOnWriteArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
name|Path
name|path
decl_stmt|;
specifier|protected
specifier|final
name|WALCoprocessorHost
name|coprocessorHost
decl_stmt|;
specifier|protected
specifier|final
name|AtomicBoolean
name|closed
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|public
name|DisabledWAL
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|,
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|List
argument_list|<
name|WALActionsListener
argument_list|>
name|listeners
parameter_list|)
block|{
name|this
operator|.
name|coprocessorHost
operator|=
operator|new
name|WALCoprocessorHost
argument_list|(
name|this
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|path
operator|=
name|path
expr_stmt|;
if|if
condition|(
literal|null
operator|!=
name|listeners
condition|)
block|{
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|listeners
control|)
block|{
name|registerWALActionsListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|registerWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
block|{
name|listeners
operator|.
name|add
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|unregisterWALActionsListener
parameter_list|(
specifier|final
name|WALActionsListener
name|listener
parameter_list|)
block|{
return|return
name|listeners
operator|.
name|remove
argument_list|(
name|listener
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|rollWriter
parameter_list|()
block|{
if|if
condition|(
operator|!
name|listeners
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|listeners
control|)
block|{
name|listener
operator|.
name|logRollRequested
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|preLogRoll
argument_list|(
name|path
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring exception from listener."
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|listeners
control|)
block|{
try|try
block|{
name|listener
operator|.
name|postLogRoll
argument_list|(
name|path
argument_list|,
name|path
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Ignoring exception from listener."
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
index|[]
name|rollWriter
parameter_list|(
name|boolean
name|force
parameter_list|)
block|{
return|return
name|rollWriter
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
if|if
condition|(
name|closed
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|listeners
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|this
operator|.
name|listeners
control|)
block|{
name|listener
operator|.
name|logCloseRequested
argument_list|()
expr_stmt|;
block|}
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
name|shutdown
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|append
parameter_list|(
name|RegionInfo
name|info
parameter_list|,
name|WALKeyImpl
name|key
parameter_list|,
name|WALEdit
name|edits
parameter_list|,
name|boolean
name|inMemstore
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|listeners
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
specifier|final
name|long
name|start
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|long
name|len
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Cell
name|cell
range|:
name|edits
operator|.
name|getCells
argument_list|()
control|)
block|{
name|len
operator|+=
name|PrivateCellUtil
operator|.
name|estimatedSerializedSizeOf
argument_list|(
name|cell
argument_list|)
expr_stmt|;
block|}
specifier|final
name|long
name|elapsed
init|=
operator|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|start
operator|)
operator|/
literal|1000000L
decl_stmt|;
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|this
operator|.
name|listeners
control|)
block|{
name|listener
operator|.
name|postAppend
argument_list|(
name|len
argument_list|,
name|elapsed
argument_list|,
name|key
argument_list|,
name|edits
argument_list|)
expr_stmt|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateStore
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|,
name|Long
name|sequenceid
parameter_list|,
name|boolean
name|onlyIfGreater
parameter_list|)
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|listeners
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
for|for
control|(
name|WALActionsListener
name|listener
range|:
name|this
operator|.
name|listeners
control|)
block|{
name|listener
operator|.
name|postSync
argument_list|(
literal|0L
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|sync
parameter_list|(
name|long
name|txid
parameter_list|)
block|{
name|sync
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|startCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Long
argument_list|>
name|flushedFamilyNamesToSeq
parameter_list|)
block|{
return|return
name|startCacheFlush
argument_list|(
name|encodedRegionName
argument_list|,
name|flushedFamilyNamesToSeq
operator|.
name|keySet
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|startCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|flushedFamilyNames
parameter_list|)
block|{
if|if
condition|(
name|closed
operator|.
name|get
argument_list|()
condition|)
return|return
literal|null
return|;
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|completeCacheFlush
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|void
name|abortCacheFlush
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{     }
annotation|@
name|Override
specifier|public
name|WALCoprocessorHost
name|getCoprocessorHost
parameter_list|()
block|{
return|return
name|coprocessorHost
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEarliestMemStoreSeqNum
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|)
block|{
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getEarliestMemStoreSeqNum
parameter_list|(
name|byte
index|[]
name|encodedRegionName
parameter_list|,
name|byte
index|[]
name|familyName
parameter_list|)
block|{
return|return
name|HConstants
operator|.
name|NO_SEQNUM
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"WAL disabled."
return|;
block|}
annotation|@
name|Override
specifier|public
name|OptionalLong
name|getLogFileSizeIfBeingWritten
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
name|OptionalLong
operator|.
name|empty
argument_list|()
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNumLogFiles
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getLogFileSize
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addWALActionsListener
parameter_list|(
name|WALActionsListener
name|listener
parameter_list|)
block|{
name|disabled
operator|.
name|registerWALActionsListener
argument_list|(
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

